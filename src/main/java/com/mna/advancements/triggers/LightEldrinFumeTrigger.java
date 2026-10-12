package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import com.mna.api.affinity.Affinity;
import java.util.List;

public class LightEldrinFumeTrigger extends SimpleCriterionTrigger<LightEldrinFumeTrigger.Instance> {
    private static final Codec<Affinity> AFFINITY_CODEC = Codec.STRING.xmap(Affinity::valueOf, Affinity::name);
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, Affinity type) { this.trigger(player, instance -> instance.matches(type)); }
    public record Instance(Optional<ContextAwarePredicate> player, List<Affinity> affinities) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            LightEldrinFumeTrigger.AFFINITY_CODEC.listOf().optionalFieldOf("affinities", java.util.List.of()).forGetter(Instance::affinities)
        ).apply(inst, Instance::new));
        public boolean matches(Affinity type) {
            if (this.affinities != null && this.affinities.size() > 0) return this.affinities.contains(type);
            return true;
        }
    }
}
