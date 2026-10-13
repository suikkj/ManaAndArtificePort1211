package com.mna.mixins;

import com.mna.effects.EffectInit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * [PORT 1.21.1] Entity.canEnterPose(Pose) foi removido no 1.21.1; o equivalente
 * agora e Player.canPlayerFitWithinBlocksAndEntitiesWhen(Pose) (chamado com
 * Pose.SWIMMING em updatePlayerPose, mesma semantica do antigo canEnterPose).
 * O efeito REDUCE (encolher) so importa para players, entao o handler vive aqui.
 */
@Mixin(value={Player.class})
public abstract class PlayerPoseMixin {
    @Inject(at={@At(value="RETURN")}, method={"canPlayerFitWithinBlocksAndEntitiesWhen"}, cancellable=true)
    public void mna$canPlayerFit(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player)(Object)this;
        MobEffectInstance shrinkEffect = player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.REDUCE.get()));
        if (shrinkEffect != null) {
            float scale = 1.0f - 0.1f * (float)(shrinkEffect.getAmplifier() + 1);
            EntityDimensions entitysize = player.getDimensions(pose).scale(scale);
            float f = entitysize.width() / 2.0f;
            Vec3 vector3d = new Vec3(player.getX() - (double)f, player.getY(), player.getZ() - (double)f);
            Vec3 vector3d1 = new Vec3(player.getX() + (double)f, player.getY() + (double)entitysize.height(), player.getZ() + (double)f);
            AABB box = new AABB(vector3d, vector3d1);
            cir.setReturnValue(player.level().noCollision((Entity)player, box.deflate(1.0E-7)));
        }
    }
}
