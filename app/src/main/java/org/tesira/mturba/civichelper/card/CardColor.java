package org.tesira.mturba.civichelper.card;

public enum CardColor {
    RED("Civic"),
    GREEN("Science"),
    BLUE("Arts"),
    YELLOW("Religion"),
    ORANGE("Crafts");

    private String name;

    CardColor(String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }
}
