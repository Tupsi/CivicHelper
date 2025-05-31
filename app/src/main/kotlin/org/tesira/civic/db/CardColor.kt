package org.tesira.civic.db

/**
 * Each [Card] is of one color with the exception of Mysticism, Written Record, Theocracy,
 * Literacy, Philosophy, Mathematics and Wonders of the World which belong to two [CardColor].
 */
enum class CardColor(val colorName: String) {
    BLUE("Arts"),
    GREEN("Science"),
    ORANGE("Crafts"),
    RED("Civic"),
    YELLOW("Religion")
}
