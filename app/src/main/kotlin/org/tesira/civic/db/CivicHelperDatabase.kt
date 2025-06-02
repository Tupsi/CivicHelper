package org.tesira.civic.db

import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.concurrent.Volatile

@Database(
    entities = [Card::class, Purchase::class, Effect::class, SpecialAbility::class, Immunity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CivicHelperDatabase : RoomDatabase() {
    abstract fun civicDao(): CivicHelperDao

    companion object {
        @Volatile
        private var INSTANCE: CivicHelperDatabase? = null

        private const val NUMBER_OF_THREADS = 4
        private const val NUMBER_OF_FAMILIES = 17
        private const val FILENAME = "advances.xml"

        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(
            NUMBER_OF_THREADS
        )

        @JvmStatic
        fun getDatabase(context: Context): CivicHelperDatabase? {
//        Log.v("DATABASE", "in getDatabase");
            val appContext = context.applicationContext
            if (INSTANCE == null) {
                synchronized(CivicHelperDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = databaseBuilder(
                            context.applicationContext,
                            CivicHelperDatabase::class.java, "civic_helper.db"
                        )
                            .addCallback(createRoomDatabaseCallback(appContext))
                            .addMigrations(MIGRATION_2_3)
                            // .fallbackToDestructiveMigration()
                            // .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE
        }
        private fun createRoomDatabaseCallback(appContext: Context): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    databaseWriteExecutor.execute {
                        INSTANCE?.let {
                            importCivicsFromXML(appContext)
                        } ?: Log.e("CivicHelperDatabase", "INSTANCE is null in onCreate callback.")
                    }
                }
            }
        }

        /**
         * Imports all civilization advances from file into the database to be used in the RecyclerView.
         * Calculates and adds the special family bonus.
         */
        @JvmStatic
        @Synchronized
        fun importCivicsFromXML(context: Context) {
            val dao = INSTANCE?.civicDao()
            if (dao == null) {
                Log.e("CivicHelperDatabase", "INSTANCE is null in importCivicsFromXML.")
                return
            }
            try {
                context.assets.open(FILENAME).use { inputStream ->
                    val dbFactory = DocumentBuilderFactory.newInstance()
                    val dBuilder = dbFactory.newDocumentBuilder()
                    val doc = dBuilder.parse(inputStream) // Parse direkt den inputStream
                    doc.documentElement.normalize()
                    val nList = doc.getElementsByTagName("advance")
                    for (i in 0..<nList.length) {
                        val node = nList.item(i)
                        // Log.d("DATABASE", "importCivicsFromXML: " + node.getNodeName());
                        if (node.nodeType == Node.ELEMENT_NODE) {
                            val color = arrayOfNulls<CardColor>(2)
                            val credits = IntArray(5)
                            for (j in 0..4) {
                                credits[j] = 0
                            }
                            val element2 = node as Element
                            val name = element2.getElementsByTagName("name").item(0).textContent
                            val family =
                                element2.getElementsByTagName("family").item(0).textContent.toInt()
                            val vp = element2.getElementsByTagName("vp").item(0).textContent.toInt()
                            val price =
                                element2.getElementsByTagName("price").item(0).textContent.toInt()
                            for (x in 0..<element2.getElementsByTagName("group").length) {
                                color[x] = CardColor.valueOf(
                                    element2.getElementsByTagName("group").item(x).textContent
                                )
                            }
                            for (x in 0..<element2.getElementsByTagName("credit").length) {
                                val cardColor = CardColor.valueOf(
                                    element2.getElementsByTagName("credit")
                                        .item(x).attributes.item(0).textContent
                                )
                                val discount =
                                    element2.getElementsByTagName("credit")
                                        .item(x).textContent.toInt()
                                when (cardColor) {
                                    CardColor.BLUE -> credits[0] = discount
                                    CardColor.GREEN -> credits[1] = discount
                                    CardColor.ORANGE -> credits[2] = discount
                                    CardColor.RED -> credits[3] = discount
                                    CardColor.YELLOW -> credits[4] = discount
                                }
                            }
                            for (x in 0..<element2.getElementsByTagName("effect").length) {
                                val effect = element2.getElementsByTagName("effect")
                                    .item(x).attributes.item(0).textContent
                                val value =
                                    element2.getElementsByTagName("effect")
                                        .item(x).textContent.toInt()
                                val newEffect = Effect(advance = name, name = effect, value = value)
                                dao.insertEffect(newEffect)
                            }
                            val civic = Card(
                                name, family, vp, price,
                                color[0], color[1], credits[0],
                                credits[1], credits[2], credits[3], credits[4], null,
                                0, false, price, 0, false
                            )

                            dao.insert(civic)
                            for (x in 0..<element2.getElementsByTagName("special").length) {
                                val abilityText =
                                    element2.getElementsByTagName("special").item(x).textContent
                                val ability = SpecialAbility(name, abilityText)
                                dao.insertSpecialAbility(ability)
                            }
                            for (x in 0..<element2.getElementsByTagName("immunity").length) {
                                val immunityText =
                                    element2.getElementsByTagName("immunity").item(x).textContent
                                val immunity = Immunity(name, immunityText)
                                dao.insertImmunity(immunity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // now we need to inject the family bonus 10/20
            var cards: List<Card>
            for (i in 1..NUMBER_OF_FAMILIES) {
                cards = dao.getAdvancesByFamily(i)
                dao.updateBonusCard(cards[0].name, cards[1].name)
                dao.updateBonus(cards[0].name, 10)
                dao.updateBonusCard(cards[1].name, cards[2].name)
                dao.updateBonus(cards[1].name, 20)
            }
        }


        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!isColumnExists(db, "cards", "buyingPrice")) {
                    db.execSQL("ALTER TABLE cards ADD COLUMN buyingPrice INTEGER NOT NULL DEFAULT 0")
                }
                if (!isColumnExists(db, "cards", "hasHeart")) {
                    db.execSQL("ALTER TABLE cards ADD COLUMN hasHeart INTEGER NOT NULL DEFAULT 0")
                }
            }

            /**
             * Hilfsmethode, um zu prüfen, ob eine Spalte in einer Tabelle existiert.
             * @param db Die SupportSQLiteDatabase-Instanz.
             * @param tableName Der Name der Tabelle.
             * @param columnName Der Name der zu prüfenden Spalte.
             * @return true, wenn die Spalte existiert, sonst false.
             */
            private fun isColumnExists(
                db: SupportSQLiteDatabase,
                tableName: String,
                columnName: String
            ): Boolean {
                var cursor: Cursor? = null
                try {
                    // PRAGMA table_info gibt Informationen über die Spalten einer Tabelle zurück.
                    // Wir verwenden LIKE, um sicherzustellen, dass wir bei Groß-/Kleinschreibungsproblemen
                    // (obwohl SQLite meist case-insensitive für Bezeichner ist) keine Probleme bekommen.
                    // Besser ist es, exakte Namen zu verwenden, wenn möglich.
                    cursor = db.query("PRAGMA table_info($tableName)")
                    val nameColumnIndex = cursor.getColumnIndex("name")
                    if (nameColumnIndex >= 0) {
                        while (cursor.moveToNext()) {
                            if (columnName.equals(
                                    cursor.getString(nameColumnIndex),
                                    ignoreCase = true
                                )
                            ) {
                                return true // Spalte gefunden
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Logge den Fehler oder handle ihn, falls nötig
                    Log.e("MigrationUtil", "Error checking if column exists", e)
                } finally {
                    cursor?.close()
                }
                return false // Spalte nicht gefunden
            }
        }
    }
}
