package com.mna.advancements.triggers;

import com.mna.api.tools.RLoc;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class TierTrigger extends SimpleCriterionTrigger<TierTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, int tier) {
        this.trigger(player, instance -> instance.test(tier));
    }

    public record Instance(Optional<ContextAwarePredicate> player, int tier) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            Codec.INT.fieldOf("tier").forGetter(Instance::tier)
        ).apply(inst, Instance::new));

        public boolean test(int tier) {
            return tier >= this.tier;
        }
    }
}
