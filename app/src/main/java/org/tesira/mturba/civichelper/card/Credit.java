package org.tesira.mturba.civichelper.card;

/**
 * Helper class fpr {@link Advance}. Each Advance gives credits towards buying other Advances.
 * Each {@link Credit} belongs to a {@link CardColor} representing the five different groups
 * and a value stating the bonus for buying another {@link Advance} from that {@link CardColor}.
 */
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
