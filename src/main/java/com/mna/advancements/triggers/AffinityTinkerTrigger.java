package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import com.mna.advancements.predicates.CraftSpellPredicate;
import com.mna.api.affinity.Affinity;
import com.mna.api.spells.base.ISpellDefinition;
import javax.annotation.Nullable;
import net.minecraft.world.level.Level;

public class AffinityTinkerTrigger extends SimpleCriterionTrigger<AffinityTinkerTrigger.Instance> {
    private static final Codec<Affinity> AFFINITY_CODEC = Codec.STRING.xmap(Affinity::valueOf, Affinity::name);
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ISpellDefinition spell, Affinity from, Affinity to) { this.trigger(player, instance -> instance.matches(player.level(), spell, from, to)); }
    public record Instance(Optional<ContextAwarePredicate> player, @Nullable CraftSpellPredicate predicate, Affinity from, Affinity to) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            CraftSpellPredicate.CODEC.optionalFieldOf("spell").forGetter(i -> Optional.ofNullable(i.predicate)),
            AffinityTinkerTrigger.AFFINITY_CODEC.optionalFieldOf("from", Affinity.UNKNOWN).forGetter(Instance::from),
            AffinityTinkerTrigger.AFFINITY_CODEC.optionalFieldOf("to", Affinity.UNKNOWN).forGetter(Instance::to)
        ).apply(inst, (p, sp, frm, to) -> new Instance(p, sp.orElse(null), frm, to)));
        public boolean matches(Level world, ISpellDefinition spell, Affinity from, Affinity to) {
            if (this.from != Affinity.UNKNOWN && this.from != from) return false;
            if (this.to != Affinity.UNKNOWN && this.to != to) return false;
            if (this.predicate != null) return this.predicate.test(spell, world);
            return true;
        }
    }
}
