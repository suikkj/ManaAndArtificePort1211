package com.mna.advancements.triggers;

import com.mna.advancements.predicates.SummonConstructPredicate;
import com.mna.entities.constructs.animated.Construct;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class SummonConstructTrigger extends SimpleCriterionTrigger<SummonConstructTrigger.Instance> {
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, Construct construct) { this.trigger(player, instance -> instance.test(construct)); }
    public record Instance(Optional<ContextAwarePredicate> player, @Nullable SummonConstructPredicate predicate) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            SummonConstructPredicate.CODEC.optionalFieldOf("construction").forGetter(i -> Optional.ofNullable(i.predicate))
        ).apply(inst, (p, cp) -> new Instance(p, cp.orElse(null))));
        public boolean test(Construct construct) {
            if (this.predicate != null) return this.predicate.test(construct);
            return true;
        }
    }
}
