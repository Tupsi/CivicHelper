package org.tesira.mturba.civichelper.card;

/**
 * Each {@link Advance} is of one color with the exception of Mysticism, Written Record, Theocracy,
 * Literacy, Philosophy, Mathematics and Wonders of the World which belong to two {@link CardColor}.
 */
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
