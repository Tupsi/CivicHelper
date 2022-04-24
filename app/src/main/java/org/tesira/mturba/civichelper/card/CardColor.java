package org.tesira.mturba.civichelper.card;

/**
 * Each {@link Advance} is of one color with the exception of Mysticism, Written Record, Theocracy,
 * Literacy, Philosophy, Mathematics and Wonders of the World which belong to two {@link CardColor}.
 */
public enum CardColor {
    BLUE("Arts"),
    GREEN("Science"),
    ORANGE("Crafts"),
    RED("Civic"),
    YELLOW("Religion");

    private String name;

    CardColor(String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }
}
