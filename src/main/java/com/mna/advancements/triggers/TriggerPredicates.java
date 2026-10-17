package com.mna.advancements.triggers;

import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.component.DataComponentPredicate;

/**
 * [PORT 1.21.1] O 1.21.1 removeu a constante ItemPredicate.ANY (no 1.20.1,
 * ItemPredicate.fromJson(null) devolvia ANY). Varias advancements do MNA omitem
 * "item" nas conditions; com fieldOf("item") obrigatorio elas falhavam o parse.
 * Este e o equivalente do ANY para usar como default de optionalFieldOf.
 */
public final class TriggerPredicates {
    public static final ItemPredicate ANY = new ItemPredicate(Optional.empty(), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Map.of());
    private TriggerPredicates() {
    }
}
