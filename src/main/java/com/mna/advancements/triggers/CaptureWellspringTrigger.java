package com.mna.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import com.mna.api.affinity.Affinity;
import javax.annotation.Nullable;

public class CaptureWellspringTrigger extends SimpleCriterionTrigger<CaptureWellspringTrigger.Instance> {
    private static final Codec<Affinity> AFFINITY_CODEC = Codec.STRING.xmap(Affinity::valueOf, Affinity::name);
    @Override public Codec<Instance> codec() { return Instance.CODEC; }
    public void trigger(ServerPlayer player, Affinity type) { this.trigger(player, instance -> instance.matches(type)); }
    public record Instance(Optional<ContextAwarePredicate> player, Optional<Affinity> type) implements SimpleCriterionTrigger.SimpleInstance {
        // [PORT 1.21.1] optionalFieldOf("type", (Affinity)null) -> DataResult.success(null) -> NPE no
        // RecordCodecBuilder quando conditions vem sem "type" (3 advancements quebravam). Optional evita.
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            CaptureWellspringTrigger.AFFINITY_CODEC.optionalFieldOf("type").forGetter(Instance::type)
        ).apply(inst, Instance::new));
        public boolean matches(Affinity type) {
            return this.type.isEmpty() || type == this.type.get();
        }
    }
}
