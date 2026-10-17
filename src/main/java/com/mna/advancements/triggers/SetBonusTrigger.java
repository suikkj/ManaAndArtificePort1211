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

public class SetBonusTrigger extends SimpleCriterionTrigger<SetBonusTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ResourceLocation setID) { this.trigger(player, instance -> instance.test(setID)); }
    public record Instance(Optional<ContextAwarePredicate> player, Optional<ResourceLocation> setID) implements SimpleCriterionTrigger.SimpleInstance {
        // [PORT 1.21.1] optionalFieldOf com default null -> NPE no RecordCodecBuilder (conditions sem "set"). Optional evita.
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("set").forGetter(Instance::setID)
        ).apply(inst, Instance::new));
        public boolean test(ResourceLocation setID) {
            if (setID == null) return false;
            return this.setID.isEmpty() || this.setID.get().equals(setID);
        }
    }
}
