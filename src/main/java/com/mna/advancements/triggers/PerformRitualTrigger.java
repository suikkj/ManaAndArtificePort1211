package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class PerformRitualTrigger extends SimpleCriterionTrigger<PerformRitualTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ResourceLocation ritualId, int tier) { this.trigger(player, instance -> instance.matches(ritualId, tier)); }
    public record Instance(Optional<ContextAwarePredicate> player, List<ResourceLocation> ritualIds, int tier) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.listOf().optionalFieldOf("ritualIDs", java.util.List.of()).forGetter(Instance::ritualIds),
            Codec.INT.optionalFieldOf("tier", 0).forGetter(Instance::tier)
        ).apply(inst, Instance::new));
        public boolean matches(ResourceLocation patternId, int tier) {
            if (tier < this.tier) return false;
            if (this.ritualIds != null && this.ritualIds.size() > 0) return this.ritualIds.contains(patternId);
            return true;
        }
    }
}
