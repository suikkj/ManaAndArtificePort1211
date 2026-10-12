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

public class CompleteMultiblockTrigger extends SimpleCriterionTrigger<CompleteMultiblockTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ResourceLocation structureID) { this.trigger(player, instance -> instance.matches(structureID)); }
    public record Instance(Optional<ContextAwarePredicate> player, List<ResourceLocation> structureIDs) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.listOf().optionalFieldOf("multiblockIDs", new ArrayList<>()).forGetter(Instance::structureIDs)
        ).apply(inst, Instance::new));
        public boolean matches(ResourceLocation structureID) {
            if (this.structureIDs != null && this.structureIDs.size() > 0) return this.structureIDs.contains(structureID);
            return true;
        }
    }
}
