package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class StudyDeskRoteTrigger extends SimpleCriterionTrigger<StudyDeskRoteTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ResourceLocation part, float progress) { this.trigger(player, instance -> instance.matches(part, progress)); }
    public record Instance(Optional<ContextAwarePredicate> player, List<ResourceLocation> parts, float percent) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.listOf().optionalFieldOf("parts", new ArrayList<>()).forGetter(Instance::parts),
            Codec.FLOAT.optionalFieldOf("progress", 0.0f).forGetter(Instance::percent)
        ).apply(inst, Instance::new));
        public boolean matches(ResourceLocation part, float progress) {
            if (progress < this.percent) return false;
            if (this.parts != null && this.parts.size() > 0) return this.parts.contains(part);
            return true;
        }
    }
}
