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
import net.minecraft.world.level.Level;

public class CraftSpellTrigger extends SimpleCriterionTrigger<CraftSpellTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ISpellDefinition spell) { this.trigger(player, instance -> instance.test(spell, player.level())); }
    public record Instance(Optional<ContextAwarePredicate> player, @Nullable CraftSpellPredicate predicate) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            CraftSpellPredicate.CODEC.optionalFieldOf("spell").forGetter(i -> Optional.ofNullable(i.predicate))
        ).apply(inst, (p, sp) -> new Instance(p, sp.orElse(null))));
        public boolean test(ISpellDefinition spell, Level world) {
            if (this.predicate != null) return this.predicate.test(spell, world);
            return true;
        }
    }
}
