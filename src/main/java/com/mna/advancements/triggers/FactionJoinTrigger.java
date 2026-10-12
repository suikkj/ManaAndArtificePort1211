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

public class FactionJoinTrigger extends SimpleCriterionTrigger<FactionJoinTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, ResourceLocation faction) { this.trigger(player, instance -> instance.test(faction)); }
    public record Instance(Optional<ContextAwarePredicate> player, @Nullable ResourceLocation faction) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("faction", (ResourceLocation)null).forGetter(Instance::faction)
        ).apply(inst, Instance::new));
        public boolean test(ResourceLocation factionID) {
            if (factionID == null) return false;
            if (this.faction != null) return this.faction.equals(factionID);
            return true;
        }
    }
}
