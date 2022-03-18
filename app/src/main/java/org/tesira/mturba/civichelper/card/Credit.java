package org.tesira.mturba.civichelper.card;

public class Credit {
    private CardColor group;
    private int value;

    public Credit(CardColor group, int value) {
        this.group = group;
        this.value = value;
    }

    public CardColor getGroup() {
        return group;
    }

    public int getValue() {
        return value;
    }
}
