package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import com.mna.advancements.predicates.CraftSpellPredicate;
import com.mna.api.spells.base.ISpellDefinition;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TranscribeSpellTrigger extends SimpleCriterionTrigger<TranscribeSpellTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ISpellDefinition spell, ItemStack destination) { this.trigger(player, instance -> instance.test(spell, player.level(), destination)); }
    public record Instance(Optional<ContextAwarePredicate> player, @Nullable CraftSpellPredicate spellPredicate, @Nullable ItemPredicate itemPredicate) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            CraftSpellPredicate.CODEC.optionalFieldOf("spell").forGetter(i -> Optional.ofNullable(i.spellPredicate)),
            ItemPredicate.CODEC.optionalFieldOf("item").forGetter(i -> Optional.ofNullable(i.itemPredicate))
        ).apply(inst, (p, sp, it) -> new Instance(p, sp.orElse(null), it.orElse(null))));
        public boolean test(ISpellDefinition spell, Level world, ItemStack destinationStack) {
            boolean success = true;
            if (this.spellPredicate != null) success &= this.spellPredicate.test(spell, world);
            if (this.itemPredicate != null) success &= this.itemPredicate.test(destinationStack);
            return success;
        }
    }
}
