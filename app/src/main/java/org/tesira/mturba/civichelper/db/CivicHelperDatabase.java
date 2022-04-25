package org.tesira.mturba.civichelper.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.tesira.mturba.civichelper.card.CardColor;
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

//@Database(entities = {Card.class, PurchasedAdvance.class}, version = 1, exportSchema = false)
@Database(entities = {Card.class}, version = 1, exportSchema = false)
public abstract class CivicHelperDatabase extends RoomDatabase {

    public abstract CardDao civicDao();
//    public abstract PurchasedAdvanceDao purchaseDao();

    private static volatile CivicHelperDatabase INSTANCE;
    private static volatile Context ASSET_CONTEXT;
    private static final int NUMBER_OF_THREADS = 4;
    private static final int NUMBER_OF_FAMILIES = 17;
    private static final String FILENAME = "advances.xml";

    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static CivicHelperDatabase getDatabase(final Context context) {
        Log.v("DB", "getDataBase");
        if (INSTANCE == null) {
            Log.v("DB", "getDataBase INSTANCE == null");
            synchronized (CivicHelperDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CivicHelperDatabase.class, "civic.db").addCallback(sRoomDatabaseCallback).build();
                    ASSET_CONTEXT = context;
                }
            }
        }
        return INSTANCE;
    }

    public static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                CardDao dao = INSTANCE.civicDao();
                dao.deleteAll();
                importCivicsFromXML();
                Log.v("DB", "creating");
            });
        }
    };


    /**
     * Imports all civilization advances from file into the database to be used in the RecyclerView.
     * Calculates and adds the special family bonus.
     * */
    private static void importCivicsFromXML() {
        CardDao dao = INSTANCE.civicDao();
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
                    Log.v("DB", "color 1 :" + color[0] + " -  color2 :" + color[1]);
                    for (int x=0; x<element2.getElementsByTagName("credit").getLength(); x++) {
                        CardColor cardColor = CardColor.valueOf(element2.getElementsByTagName("credit").item(x).getAttributes().item(0).getTextContent());
                        Log.v("DB", name + " : CardColor :" + cardColor);
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
//                    for (int x=0; x<element2.getElementsByTagName("effect").getLength(); x++) {
//                        String name = element2.getElementsByTagName("effect").item(x).getAttributes().item(0).getTextContent();
//                        int value = parseInt(element2.getElementsByTagName("effect").item(x).getTextContent());
//                        adv.addEffect(name, value);
//                    }
//                    advances.getValue().add(adv);
                    Card civic = new Card(name, family, vp, price, color[0], color[1], credits[0], credits[1], credits[2], credits[3], credits[4], null, 0, false);
                    dao.insert(civic);
                }
            }
//            Advance.addFamilyBonus(advances.getValue());
//            greenCardsAnatomy.setValue(Advance.getGreenCards(advances.getValue(), purchasedAdvances.getValue()));
//            setCurrentPrice();
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
}
