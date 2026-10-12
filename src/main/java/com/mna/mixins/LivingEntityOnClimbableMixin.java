 
package com.mna.mixins;

import com.mna.effects.EffectInit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public class LivingEntityOnClimbableMixin {
    @Inject(at={@At(value="RETURN")}, method={"onClimbable"}, cancellable=true)
    public void mna$onClimbable(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)this;
        if (self.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SPIDER_CLIMBING.get())) && self.horizontalCollision) {
            cir.setReturnValue(true);
        }
    }
}

