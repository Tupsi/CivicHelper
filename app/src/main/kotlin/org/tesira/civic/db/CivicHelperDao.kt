package org.tesira.civic.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.tesira.civic.Calamity

@Dao
interface CivicHelperDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertCard(civilizationAdvance: Card)

    @Query("DELETE FROM cards WHERE name = :name")
    fun deleteCard(name: String)

    @Query("DELETE FROM cards")
    fun deleteAllCards()

    @Query("DELETE FROM effects")
    fun deleteAllEffects()

    @Query("DELETE FROM immunity")
    fun deleteAllImmunities()

    @Query("DELETE FROM specials")
    fun deleteAllSpecials()

    @Query("SELECT * FROM cards ORDER BY name ASC")
    fun getAdvancesByName(): List<Card>

    @Query("SELECT * FROM cards WHERE family = :familyNumber ORDER BY vp ASC")
    fun getAdvancesByFamily(familyNumber: Int): List<Card>

    @Query("UPDATE cards SET bonusCard = :newBonusCard WHERE name = :name")
    fun updateBonusCard(name: String, newBonusCard: String)

    @Query("UPDATE cards SET bonus = :newBonus WHERE name = :name")
    fun updateBonus(name: String, newBonus: Int)

    @Query("SELECT * FROM cards WHERE name = :name ")
    fun getAdvanceByNameToCard(name: String): Card?

    @Query(
        "SELECT cards.* FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL ORDER BY " +
                "CASE WHEN :sortingOrder = 'name' THEN cards.name END ASC," +
                "CASE WHEN :sortingOrder = 'family' THEN cards.family END ASC," +
                "CASE WHEN :sortingOrder = 'color' THEN cards.group1 END," +
                "CASE WHEN :sortingOrder = 'color' THEN cards.currentPrice END ASC," +
                "CASE WHEN :sortingOrder = 'vp' THEN cards.vp END," +
                "CASE WHEN :sortingOrder = 'vp' THEN cards.currentPrice END ASC," +
                "CASE WHEN :sortingOrder = 'currentPrice'THEN cards.currentPrice END," +
                "CASE WHEN :sortingOrder = 'currentPrice'THEN cards.name END ASC," +
                "CASE WHEN :sortingOrder = 'heart' THEN cards.hasHeart END DESC, " +
                "CASE WHEN :sortingOrder = 'heart'THEN cards.currentPrice END," +
                "CASE WHEN :sortingOrder = 'heart' THEN cards.name END ASC"
    )
    fun getAllAdvancesNotBoughtLiveData(sortingOrder: String): LiveData<List<Card>>

    @Query("SELECT cards.* FROM cards LEFT JOIN purchases ON cards.name = purchases.name WHERE purchases.name NOT NULL ORDER BY name ASC")
    fun getInventoryAsCardLiveData(): LiveData<List<Card>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPurchase(purchase: Purchase)

    @Query("DELETE FROM purchases")
    fun deleteAllPurchases()

    @Query("SELECT cards.* FROM cards LEFT JOIN purchases ON cards.name = purchases.name WHERE purchases.name NOT NULL AND cards.vp IN (1,3)")
    fun getPurchasesForFamilyBonus(): List<Card>

    @Query("UPDATE cards SET currentPrice = :current WHERE name = :name")
    fun updateCurrentPrice(name: String, current: Int)

    @Query("Update cards SET currentPrice = price")
    fun resetCurrentPrice()

    @Query("SELECT cards.name FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL AND price < 100 AND (group1 = 'GREEN' OR group2 = 'GREEN') ORDER BY cards.name ASC")
    fun getAnatomyCards(): List<String>

    @Insert
    fun insertEffect(effect: Effect)

    @Query("SELECT * FROM effects WHERE name = :name AND advance = :advance ORDER BY advance ASC")
    fun getEffect(advance: String?, name: String?): List<Effect>

    @Query("SELECT  effects.name AS calamity, SUM(effects.value) AS bonus FROM effects LEFT JOIN purchases on effects.advance = purchases.name WHERE purchases.name IS NOT NULL AND calamity NOT LIKE 'Credits%' AND calamity NOT LIKE 'FreeScience' GROUP BY effects.name ORDER BY calamity ASC")
    fun getCalamityBonus(): List<Calamity>

    @Query("SELECT  effects.name AS calamity, SUM(effects.value) AS bonus FROM effects LEFT JOIN purchases on effects.advance = purchases.name WHERE purchases.name IS NOT NULL AND calamity NOT LIKE 'Credits%' AND calamity NOT LIKE 'FreeScience' GROUP BY effects.name ORDER BY calamity ASC")
    fun getCalamityBonusLiveData(): LiveData<List<Calamity>>

    @Insert
    fun insertSpecialAbility(ability: SpecialAbility)

    @Query("SELECT specials.ability from specials LEFT JOIN purchases on specials.advance = purchases.name WHERE purchases.name IS NOT NULL")
    fun getSpecialAbilitiesLiveData(): LiveData<List<String>>

    @Insert
    fun insertImmunity(immunity: Immunity)

    @Query("SELECT immunity.immunity FROM immunity LEFT JOIN purchases on immunity.advance = purchases.name WHERE purchases.name IS NOT NULL")
    fun getImmunitiesLiveData(): LiveData<List<String>>

    @Query("SELECT SUM(cards.vp) AS sum FROM cards LEFT JOIN purchases ON cards.name = purchases.name WHERE purchases.name NOT NULL")
    fun cardsVp(): LiveData<Int>

    @Query("UPDATE cards SET hasHeart = 0")
    fun resetAllHearts()

    @Query("UPDATE cards SET hasHeart = 1 WHERE name IN (:cardNames)")
    fun setHeartsForCards(cardNames: List<String>)

    @Query(
        """
    UPDATE cards 
    SET creditsBlue   = creditsBlue   + :blue,
        creditsGreen  = creditsGreen  + :green,
        creditsOrange = creditsOrange + :orange,
        creditsRed    = creditsRed    + :red,
        creditsYellow = creditsYellow + :yellow
    WHERE name = :cardName
"""
    )
    fun addCreditsToCard(cardName: String, blue: Int, green: Int, orange: Int, red: Int, yellow: Int)

    @Transaction
    @Query("SELECT * FROM cards WHERE name = :cardName")
    fun getCardWithDetailsByName(cardName: String): LiveData<CardWithDetails?>

    @Transaction
    @Query("SELECT * FROM cards")
    fun getAllCardsWithDetailsUnsorted(): LiveData<List<CardWithDetails>>

    @Transaction
    @Query("SELECT cards.* FROM cards LEFT JOIN purchases ON cards.name = purchases.name WHERE purchases.name NOT NULL")
    fun getAllPurchasedCardsWithDetailsUnsorted(): LiveData<List<CardWithDetails>>

    @Transaction
    @Query("SELECT cards.* FROM cards LEFT JOIN purchases on cards.name = purchases.name WHERE purchases.name IS NULL")
    fun getAllPurchasableCardsWithDetailsUnsorted(): LiveData<List<CardWithDetails>>
}
