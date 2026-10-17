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

public class ManaweaveAltarCraftTrigger extends SimpleCriterionTrigger<ManaweaveAltarCraftTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ItemStack pItem, boolean factionMatch) { this.trigger(player, instance -> instance.matches(pItem, factionMatch)); }
    public record Instance(Optional<ContextAwarePredicate> player, ItemPredicate item, boolean matchFaction) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ItemPredicate.CODEC.optionalFieldOf("item", TriggerPredicates.ANY).forGetter(Instance::item),
            Codec.BOOL.optionalFieldOf("mustMatchFaction", false).forGetter(Instance::matchFaction)
        ).apply(inst, Instance::new));
        public boolean matches(ItemStack pItem, boolean factionMatch) { return this.item.test(pItem) && (!this.matchFaction || factionMatch); }
    }
}
