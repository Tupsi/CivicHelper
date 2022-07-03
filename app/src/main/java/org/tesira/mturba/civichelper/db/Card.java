package org.tesira.mturba.civichelper.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * This describes a civilization advance also referred to as civic card.
 */
@Entity(tableName = "cards", indices = {@Index("name")})
public class Card {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name ="name")
    private String mName;

    // advances are divided in three columns and 17 rows and give a bonus for buying the
    // next in line in the same row
    @ColumnInfo(name="family")
    private int mFamily;                 // give bonus to next family member in same row

    @ColumnInfo(name="vp")
    private int mVp;                     // victory points (1,3,6)

    @ColumnInfo(name="price")
    private int mPrice;

    @ColumnInfo(name="group1")
    private CardColor mGroup1;

    @ColumnInfo(name="group2")
    private CardColor mGroup2;

    @ColumnInfo(name="creditsBlue")
    private int mCreditsBlue;

    @ColumnInfo(name="creditsGreen")
    private int mCreditsGreen;

    @ColumnInfo(name="creditsOrange")
    private int mCreditsOrange;

    @ColumnInfo(name="creditsRed")
    private int mCreditsRed;

    @ColumnInfo(name="creditsYellow")
    private int mCreditsYellow;

    @ColumnInfo(name="bonusCard")
    private String mBonusCard;

    @ColumnInfo(name="bonus")
    private int mBonus;

    @ColumnInfo(name="isBuyable")
    private boolean mIsBuyable;

    @ColumnInfo(name="currentPrice")
    private int mCurrentPrice;

    public Card(@NonNull String mName, int mFamily, int mVp, int mPrice,
                @NonNull CardColor mGroup1, CardColor mGroup2, int mCreditsBlue,
                int mCreditsGreen, int mCreditsOrange,
                int mCreditsRed, int mCreditsYellow, String mBonusCard, int mBonus, boolean mIsBuyable, int mCurrentPrice) {
        this.mName = mName;
        this.mFamily = mFamily;
        this.mVp = mVp;
        this.mPrice = mPrice;
        this.mGroup1 = mGroup1;
        this.mGroup2 = mGroup2;
        this.mCreditsBlue = mCreditsBlue;
        this.mCreditsGreen = mCreditsGreen;
        this.mCreditsOrange = mCreditsOrange;
        this.mCreditsRed = mCreditsRed;
        this.mCreditsYellow = mCreditsYellow;
        this.mBonusCard = mBonusCard;
        this.mBonus = mBonus;
        this.mIsBuyable = mIsBuyable;
        this.mCurrentPrice = mCurrentPrice;
    }

    public String getName(){return this.mName;}

    public int getFamily() {
        return mFamily;
    }

    public int getVp() {
        return mVp;
    }

    public int getPrice() {
        return mPrice;
    }

    public String getPriceAsString() {
        return Integer.toString(mPrice);
    }

    @NonNull
    public CardColor getGroup1() {
        return mGroup1;
    }

    public CardColor getGroup2() {
        return mGroup2;
    }

    public int getCreditsBlue() {
        return mCreditsBlue;
    }

    public int getCreditsGreen() {
        return mCreditsGreen;
    }

    public int getCreditsOrange() {
        return mCreditsOrange;
    }

    public int getCreditsRed() {
        return mCreditsRed;
    }

    public int getCreditsYellow() {
        return mCreditsYellow;
    }

    public String getBonusCard() { return mBonusCard; }

    public int getBonus() { return mBonus; }

    public boolean getIsBuyable() { return mIsBuyable; }
    public void setIsBuyable(boolean isBuyable) { mIsBuyable = isBuyable; }

    public int getCurrentPrice() { return mCurrentPrice; }
}