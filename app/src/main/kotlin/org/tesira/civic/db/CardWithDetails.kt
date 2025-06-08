package org.tesira.civic.db

import androidx.room.Embedded
import androidx.room.Relation

data class CardWithDetails(
    @Embedded // Nimmt alle Felder der Card-Klasse auf
    val card: Card,

    @Relation(
        parentColumn = "name", // Primärschlüssel in Card
        entityColumn = "advance" // Fremdschlüssel in Effect, der auf Card.name zeigt
    )
    val effects: List<Effect>,

    @Relation(
        parentColumn = "name",
        entityColumn = "advance"
    )
    val immunities: List<Immunity>,

    @Relation(
        parentColumn = "name",
        entityColumn = "advance"
    )
    val specialAbilities: List<SpecialAbility>
)