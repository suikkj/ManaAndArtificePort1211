package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public class DrawManaweavingPatternTrigger extends SimpleCriterionTrigger<DrawManaweavingPatternTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ResourceLocation patternId) { this.trigger(player, instance -> instance.matches(patternId)); }
    public record Instance(Optional<ContextAwarePredicate> player, Optional<ResourceLocation> patternId) implements SimpleCriterionTrigger.SimpleInstance {
        // [PORT 1.21.1] optionalFieldOf com default null -> NPE no RecordCodecBuilder (conditions sem "patternId"). Optional evita.
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("patternId").forGetter(Instance::patternId)
        ).apply(inst, Instance::new));
        public boolean matches(ResourceLocation patternId) {
            return this.patternId.isEmpty() || this.patternId.get().equals(patternId);
        }
    }
}
