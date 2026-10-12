 
package com.mna.effects.beneficial;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.entity.MAPFX;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.interfaces.IDoubleTapEndEarly;
import com.mna.effects.interfaces.IInputDisable;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.effects.particles.EffectWithCustomClientParticles;
import com.mna.factions.Factions;
import com.mna.tools.EntityUtil;
import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EffectMistForm
extends EffectWithCustomClientParticles
implements IInputDisable,
IDoubleTapEndEarly,
INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectMistForm() {
        super(MobEffectCategory.BENEFICIAL, 0, MAPFX.Flag.MIST_FORM, MAPFX.Flag.CANCEL_RENDER);
    }

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
            if (pLivingEntity instanceof Player) {
                Player p = (Player)pLivingEntity;
                IPlayerProgression pr = (IPlayerProgression)pLivingEntity.getData(PlayerProgressionProvider.PROGRESSION.get());
                if (pr != null && pr.getAlliedFaction() == Factions.UNDEAD && pLivingEntity.getPersistentData().getBoolean("bone_armor_set_bonus")) {
                    p.getPersistentData().putBoolean("mist_form_speed_remove", true);
                    p.getAbilities().setFlyingSpeed(p.getAbilities().getFlyingSpeed() + 0.025f);
                    p.onUpdateAbilities();
                }
            }
    
    }

    public void addAttributeModifiers(AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pAttributeMap, pAmplifier);
    }

    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
            Player p;        if (pLivingEntity instanceof Player && (p = (Player)pLivingEntity).getPersistentData().getBoolean("mist_form_speed_remove")) {
                p.getPersistentData().remove("mist_form_speed_remove");
                p.getAbilities().setFlyingSpeed(p.getAbilities().getFlyingSpeed() - 0.025f);
                p.onUpdateAbilities();
            }
    
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        super.removeAttributeModifiers(pAttributeMap);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn instanceof Player) {
            Player player = (Player)entityLivingBaseIn;
            MobEffectInstance activeEffect = player.getEffect(com.mna.api.tools.MAEffects.holder(this));
            if (activeEffect.getDuration() <= 5 && !activeEffect.isInfiniteDuration()) {
                ManaAndArtifice.instance.proxy.setFlightEnabled(player, false);
                player.noPhysics = false;
                EntityUtil.removeInvisibility(entityLivingBaseIn);
            } else {
                ManaAndArtifice.instance.proxy.setFlightEnabled(player, true);
                player.getAbilities().flying = true;
                boolean shouldNoClip = false;
                BlockHitResult brtr = player.level().clip(new ClipContext(player.getEyePosition(1.0f), player.getEyePosition(1.0f).add(player.getDeltaMovement().scale(10.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
                if (player.horizontalCollision || player.verticalCollision) {
                    if (brtr.getType() == HitResult.Type.BLOCK) {
                        shouldNoClip = !player.level().getBlockState(brtr.getBlockPos()).canOcclude();
                } else {
                    boolean bl = shouldNoClip = !player.level().getBlockState(player.blockPosition()).canOcclude();
                }
                if (shouldNoClip) {
                    Direction offset = player.getMotionDirection();
                    Vec3i offsetVec = offset.getNormal();
                    for (int i = 0; i < 1; ++i) {
                        shouldNoClip &= !player.level().getBlockState(brtr.getBlockPos().offset(offsetVec.relative(offset, i))).canOcclude();
                    }
                }
                player.noPhysics = shouldNoClip;
            }
            entityLivingBaseIn.setInvisible(true);
        }
        }
        return true;
    }

    @Override
    public EnumSet<IInputDisable.InputMask> getDisabledFlags() {
        return EnumSet.of(IInputDisable.InputMask.LEFT_CLICK, IInputDisable.InputMask.RIGHT_CLICK);
    }

    @Override
    public void onRemoved(LivingEntity living, MobEffectInstance effect) {
        living.noPhysics = false;
        living.setInvisible(false);
        if (living instanceof Player) {
            Player player = (Player)living;
            player.getAbilities().mayfly = player.isCreative() || player.isSpectator();
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }
}

