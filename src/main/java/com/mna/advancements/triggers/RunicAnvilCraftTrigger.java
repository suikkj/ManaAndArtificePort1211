package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RunicAnvilCraftTrigger extends SimpleCriterionTrigger<RunicAnvilCraftTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ItemStack pItem) { this.trigger(player, instance -> instance.matches(pItem)); }
    public record Instance(Optional<ContextAwarePredicate> player, ItemPredicate item) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ItemPredicate.CODEC.optionalFieldOf("item", TriggerPredicates.ANY).forGetter(Instance::item)
        ).apply(inst, Instance::new));
        public boolean matches(ItemStack pItem){ return this.item.test(pItem); }
    }
}
