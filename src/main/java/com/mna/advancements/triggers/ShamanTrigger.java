package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import com.mna.items.ItemInit;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Item;

public class ShamanTrigger extends SimpleCriterionTrigger<ShamanTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player) { this.trigger(player, instance -> instance.test(player)); }
    public record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
        ).apply(inst, Instance::new));
        public boolean test(ServerPlayer player) {
            return player.getStats().getValue(Stats.ITEM_USED.get((Item) ItemInit.FALL_CHARM.get())) > 0
                && player.getStats().getValue(Stats.ITEM_USED.get((Item) ItemInit.BED_CHARM.get())) > 0
                && player.getStats().getValue(Stats.ITEM_USED.get((Item) ItemInit.BURN_CHARM.get())) > 0
                && player.getStats().getValue(Stats.ITEM_USED.get((Item) ItemInit.DROWN_CHARM.get())) > 0;
        }
    }
}
