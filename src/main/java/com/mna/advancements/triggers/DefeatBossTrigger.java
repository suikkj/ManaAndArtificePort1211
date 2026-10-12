package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class DefeatBossTrigger extends SimpleCriterionTrigger<DefeatBossTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ResourceLocation entityID, int numDefeated) { this.trigger(player, instance -> instance.test(entityID, numDefeated)); }
    public record Instance(Optional<ContextAwarePredicate> player, ResourceLocation entityID, int count) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.fieldOf("entityType").forGetter(Instance::entityID),
            Codec.INT.optionalFieldOf("count", 0).forGetter(Instance::count)
        ).apply(inst, Instance::new));
        public boolean test(ResourceLocation entityID, int numDefeated) {
            return entityID.equals(this.entityID) && numDefeated >= this.count;
        }
    }
}
