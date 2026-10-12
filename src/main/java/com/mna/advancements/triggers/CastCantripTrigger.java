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

public class CastCantripTrigger extends SimpleCriterionTrigger<CastCantripTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ResourceLocation cantripId, int cantripTier) { this.trigger(player, instance -> instance.matches(cantripId, cantripTier)); }
    public record Instance(Optional<ContextAwarePredicate> player, List<ResourceLocation> cantripIDs, int tier) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.listOf().optionalFieldOf("cantripIDs", java.util.List.of()).forGetter(Instance::cantripIDs),
            Codec.INT.optionalFieldOf("tier", 0).forGetter(Instance::tier)
        ).apply(inst, Instance::new));
        public boolean matches(ResourceLocation cantripId, int cantripTier) {
            if (cantripTier < this.tier) return false;
            if (this.cantripIDs != null && this.cantripIDs.size() > 0) return this.cantripIDs.contains(cantripId);
            return true;
        }
    }
}
