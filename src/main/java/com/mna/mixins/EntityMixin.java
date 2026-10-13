 
package com.mna.mixins;

import com.mna.effects.EffectInit;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public abstract class EntityMixin {
    @Shadow
    public Level level;

    @Inject(at={@At(value="HEAD")}, method={"playSound"}, cancellable=true)
    public void mna$playSound(SoundEvent pSound, float pVolume, float pPitch, CallbackInfo cbi) {
        Entity entity = (Entity)(Object)this;
        if (entity instanceof LivingEntity) {
            float pitchChange;
            MobEffectInstance efct = ((LivingEntity)entity).getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.REDUCE.get()));
            boolean playAndCancel = false;
            if (efct != null) {
                pitchChange = 0.1f * (float)efct.getAmplifier();
                pPitch += pitchChange;
                playAndCancel = true;
            }
            if ((efct = ((LivingEntity)entity).getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()))) != null) {
                pitchChange = 0.05f * (float)efct.getAmplifier();
                pPitch -= pitchChange;
                playAndCancel = true;
            }
            if (playAndCancel) {
                if (!this.isSilent()) {
                    this.level.playSound((Player)null, this.getX(), this.getY(), this.getZ(), pSound, this.getSoundSource(), pVolume, pPitch);
                }
                cbi.cancel();
            }
        }
    }

    @Shadow
    public abstract EntityDimensions getDimensions(Pose var1);

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract boolean isSilent();

    @Shadow
    public abstract SoundSource getSoundSource();
}

