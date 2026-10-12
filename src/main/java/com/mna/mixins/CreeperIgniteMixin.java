 
package com.mna.mixins;

import com.mna.effects.EffectInit;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Creeper.class})
public class CreeperIgniteMixin {
    @Accessor(value="DATA_IS_IGNITED")
    public static EntityDataAccessor<Boolean> getIgnitedData() {
        throw new AssertionError();
    }

    @Accessor(value="DATA_SWELL_DIR")
    public static EntityDataAccessor<Integer> getSwellDir() {
        throw new AssertionError();
    }

    @ModifyVariable(method={"setSwellDir(I)V"}, at=@At(value="HEAD"), ordinal=0)
    private int mna$modifyParamSetSwellDir(int dir) {
        Creeper self = (Creeper)this;
        if (self.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()))) {
            return -1;
        }
        return dir;
    }

    @Inject(at={@At(value="HEAD")}, method={"ignite"}, cancellable=true)
    public void mna$ignite(CallbackInfo cir) {
        Creeper self = (Creeper)this;
        if (self.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()))) {
            self.getEntityData().set(CreeperIgniteMixin.getSwellDir(), -1);
            cir.cancel();
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"tick"})
    public void mna$tick(CallbackInfo cir) {
        Creeper self = (Creeper)this;
        if (self.isIgnited() && self.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()))) {
            self.getEntityData().set(CreeperIgniteMixin.getIgnitedData(), false);
            self.getEntityData().set(CreeperIgniteMixin.getSwellDir(), -1);
        }
    }
}

