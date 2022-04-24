package org.tesira.mturba.civichelper.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.tesira.mturba.civichelper.card.CardColor;
import org.tesira.mturba.civichelper.card.Credit;

import java.util.HashMap;
import java.util.List;

@Entity(tableName = "card_table", indices = {@Index("name")})
public class CivilizationAdvance {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name ="name")
    private String mName;

    // advances are divided in three columns and 17 rows and give a bonus for buying the
    // next in line in the same row
    @NonNull
    @ColumnInfo(name="family")
    private int mFamily;                 // give bonus to next family member in same row

    @NonNull
    @ColumnInfo(name="vp")
    private int mVp;                     // victory points (1,3,6)

    @NonNull
    @ColumnInfo(name="price")
    private int mPrice;

//    @NonNull
//    @ColumnInfo(name="currentPrice")
//    private int mCurrentPrice;

    @NonNull
    @ColumnInfo(name="group1")
    private CardColor mGroup1;

    @ColumnInfo(name="group2")
    private CardColor mGroup2;

    @NonNull
    @ColumnInfo(name="creditsBlue")
    private int mCreditsBlue;

    @NonNull
    @ColumnInfo(name="creditsGreen")
    private int mCreditsGreen;

    @NonNull
    @ColumnInfo(name="creditsOrange")
    private int mCreditsOrange;

    @NonNull
    @ColumnInfo(name="creditsRed")
    private int mCreditsRed;

    @NonNull
    @ColumnInfo(name="creditsYellow")
    private int mCreditsYellow;


    // optional
    // effects hold special bonuses during gameplay, to be displayed at in summation
    // on  startscreen
//    private HashMap<String, Integer> mEffects;
//    private int mFamilyBonus;
//    private String mFamilyName;

//    public CivilizationAdvance(@NonNull String name) {
//        this.mName = name;
//    }

    public CivilizationAdvance(@NonNull String mName, int mFamily, int mVp, int mPrice,
                               @NonNull CardColor mGroup1, CardColor mGroup2, int mCreditsBlue,
                               int mCreditsGreen, int mCreditsOrange,
                               int mCreditsRed, int mCreditsYellow) {
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
}