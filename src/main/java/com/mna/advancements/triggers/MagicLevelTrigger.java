package com.mna.advancements.triggers;

import com.mna.api.tools.RLoc;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class MagicLevelTrigger extends SimpleCriterionTrigger<MagicLevelTrigger.Instance> {
    private static final String ID = "magic_level";

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, int magicLevel) {
        this.trigger(player, instance -> instance.matches(magicLevel));
    }

    public record Instance(Optional<ContextAwarePredicate> player, int minLevel) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            Codec.INT.fieldOf("minLevel").forGetter(Instance::minLevel)
        ).apply(inst, Instance::new));

        public boolean matches(int level) {
            return level >= this.minLevel;
        }
    }
}
