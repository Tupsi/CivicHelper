package org.tesira.civic.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@Database(entities = {Card.class, Purchase.class, Effect.class, SpecialAbility.class, Immunity.class}, version = 3, exportSchema = true)
@TypeConverters({Converters.class})
public abstract class CivicHelperDatabase extends RoomDatabase {

    public abstract CivicHelperDao civicDao();

    private static volatile CivicHelperDatabase INSTANCE;
    private static volatile Context ASSET_CONTEXT;
    private static final int NUMBER_OF_THREADS = 4;
    private static final int NUMBER_OF_FAMILIES = 17;
    private static final String FILENAME = "advances.xml";

    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static CivicHelperDatabase getDatabase(final Context context) {
//        Log.v("DATABASE", "in getDatabase");
        if (INSTANCE == null) {
            synchronized (CivicHelperDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CivicHelperDatabase.class, "civic_helper.db")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_2_3)
//                            .fallbackToDestructiveMigration()
//                            .allowMainThreadQueries()
                            .build();
                    ASSET_CONTEXT = context.getApplicationContext();
                }
            }
        }
        return INSTANCE;
    }

    public static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
//            Log.v("DATABASE", "in callback onCreate");
            // Populate the database in the background.
            databaseWriteExecutor.execute(CivicHelperDatabase::importCivicsFromXML);
        }

        /**
         * Called when the database has been opened.
         *
         * @param db The database.
         */
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };


    /**
     * Imports all civilization advances from file into the database to be used in the RecyclerView.
     * Calculates and adds the special family bonus.
     * */
    public static synchronized void importCivicsFromXML() {
        CivicHelperDao dao = INSTANCE.civicDao();
        try {
            InputStream is = ASSET_CONTEXT.getAssets().open(FILENAME);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            Element element = doc.getDocumentElement();
            element.normalize();
            NodeList nList = doc.getElementsByTagName("advance");
            for (int i=0; i<nList.getLength(); i++) {
                Node node = nList.item(i);
//                Log.d("DATABASE", "importCivicsFromXML: " + node.getNodeName());
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    CardColor[] color = new CardColor[2];
                    int[] credits = new int[5];
                    for (int j=0; j<5; j++) {
                        credits[j] = 0;
                    }
                    Element element2 = (Element) node;
                    String name = element2.getElementsByTagName("name").item(0).getTextContent();
                    int family = Integer.parseInt(element2.getElementsByTagName("family").item(0).getTextContent());
                    int vp = Integer.parseInt(element2.getElementsByTagName("vp").item(0).getTextContent());
                    int price = Integer.parseInt(element2.getElementsByTagName("price").item(0).getTextContent());
                    for (int x=0; x<element2.getElementsByTagName("group").getLength();x++) {
                        color[x] = CardColor.valueOf(element2.getElementsByTagName("group").item(x).getTextContent());
                    }
                    for (int x=0; x<element2.getElementsByTagName("credit").getLength(); x++) {
                        CardColor cardColor = CardColor.valueOf(element2.getElementsByTagName("credit").item(x).getAttributes().item(0).getTextContent());
                        int discount = Integer.parseInt(element2.getElementsByTagName("credit").item(x).getTextContent());
                        switch(cardColor) {
                            case BLUE:
                                credits[0] = discount;
                                break;
                            case GREEN:
                                credits[1] = discount;
                                break;
                            case ORANGE:
                                credits[2] = discount;
                                break;
                            case RED:
                                credits[3] = discount;
                                break;
                            case YELLOW:
                                credits[4] = discount;
                                break;
                        }
                    }
                    for (int x=0; x<element2.getElementsByTagName("effect").getLength(); x++) {
                        String effect = element2.getElementsByTagName("effect").item(x).getAttributes().item(0).getTextContent();
                        int value = Integer.parseInt(element2.getElementsByTagName("effect").item(x).getTextContent());
                        Effect newEffect = new Effect(name, effect, value);
                        dao.insertEffect(newEffect);
                    }
                    Card civic = new Card(name, family, vp, price, color[0], color[1], credits[0],
                            credits[1], credits[2], credits[3], credits[4], null,
                            0, false, price,0,false);

                    dao.insert(civic);
                    for (int x=0; x<element2.getElementsByTagName("special").getLength();x++) {
                        String abilityText = element2.getElementsByTagName("special").item(x).getTextContent();
                        SpecialAbility ability = new SpecialAbility(name, abilityText);
                        dao.insertSpecialAbility(ability);
                    }
                    for (int x=0; x<element2.getElementsByTagName("immunity").getLength();x++) {
                        String immunityText = element2.getElementsByTagName("immunity").item(x).getTextContent();
                        Immunity immunity = new Immunity(name, immunityText);
                        dao.insertImmunity(immunity);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // now we need to inject the family bonus 10/20
        List<Card> cards;
        for (int i=1; i<= NUMBER_OF_FAMILIES; i++) {
            cards = dao.getAdvancesByFamily(i);
            dao.updateBonusCard(cards.get(0).getName(), cards.get(1).getName());
            dao.updateBonus(cards.get(0).getName(),10);
            dao.updateBonusCard(cards.get(1).getName(), cards.get(2).getName());
            dao.updateBonus(cards.get(1).getName(),20);
        }
    }


    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            if (!isColumnExists(database, "cards", "buyingPrice")) {
                database.execSQL("ALTER TABLE cards ADD COLUMN buyingPrice INTEGER NOT NULL DEFAULT 0");
            }
            if (!isColumnExists(database, "cards", "hasHeart")) {
                database.execSQL("ALTER TABLE cards ADD COLUMN hasHeart INTEGER NOT NULL DEFAULT 0");
            }
        }

        /**
         * Hilfsmethode, um zu prüfen, ob eine Spalte in einer Tabelle existiert.
         * @param db Die SupportSQLiteDatabase-Instanz.
         * @param tableName Der Name der Tabelle.
         * @param columnName Der Name der zu prüfenden Spalte.
         * @return true, wenn die Spalte existiert, sonst false.
         */
        private boolean isColumnExists(@NonNull SupportSQLiteDatabase db, @NonNull String tableName, @NonNull String columnName) {
            Cursor cursor = null;
            try {
                // PRAGMA table_info gibt Informationen über die Spalten einer Tabelle zurück.
                // Wir verwenden LIKE, um sicherzustellen, dass wir bei Groß-/Kleinschreibungsproblemen
                // (obwohl SQLite meist case-insensitive für Bezeichner ist) keine Probleme bekommen.
                // Besser ist es, exakte Namen zu verwenden, wenn möglich.
                cursor = db.query("PRAGMA table_info(" + tableName + ")");
                int nameColumnIndex = cursor.getColumnIndex("name");
                if (nameColumnIndex >= 0) {
                    while (cursor.moveToNext()) {
                        if (columnName.equalsIgnoreCase(cursor.getString(nameColumnIndex))) {
                            return true; // Spalte gefunden
                        }
                    }
                }
            } catch (Exception e) {
                // Logge den Fehler oder handle ihn, falls nötig
                Log.e("MigrationUtil", "Error checking if column exists", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return false; // Spalte nicht gefunden
        }
    };
}
