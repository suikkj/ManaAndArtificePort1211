 
package com.mna.effects.neutral;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.interfaces.IInputDisable;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.network.ServerMessageDispatcher;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class EffectLift
extends MobEffect
implements IInputDisable,
INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public EffectLift() {
        super(MobEffectCategory.NEUTRAL, 0);
    }

    public List<ItemStack> getCurativeItems() {
        return new ArrayList<ItemStack>();
    }

    public void onEffectAdded(LivingEntity entityLivingBaseIn, int amplifier) {
            entityLivingBaseIn.setNoGravity(true);
            if (entityLivingBaseIn instanceof Player) {
                IPlayerMagic magic = MNACap.get((IPlayerMagic)((Player)entityLivingBaseIn), PlayerMagicProvider.MAGIC).orElse(null);
                Vec3 vec = entityLivingBaseIn.position();
                magic.setLiftPosition(vec);
                magic.setDidAllowFlying(((Player)entityLivingBaseIn).getAbilities().mayfly);
                ManaAndArtifice.instance.proxy.setFlightEnabled((Player)entityLivingBaseIn, true);
                if (!entityLivingBaseIn.level().isClientSide()) {
                    ServerMessageDispatcher.sendSetLiftPosition(vec.x, vec.y, vec.z, (ServerPlayer)entityLivingBaseIn);
                }
            }
    
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            entityLivingBaseIn.setNoGravity(false);
            if (entityLivingBaseIn instanceof Player) {
                IPlayerMagic magic = MNACap.get((IPlayerMagic)((Player)entityLivingBaseIn), PlayerMagicProvider.MAGIC).orElse(null);
                ManaAndArtifice.instance.proxy.setFlightEnabled((Player)entityLivingBaseIn, magic.didAllowFlying());
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        Vec3 forcePos = entityLivingBaseIn.position();
        entityLivingBaseIn.setDeltaMovement(0.0, 0.0, 0.0);
        entityLivingBaseIn.fallDistance = 0.0f;
        entityLivingBaseIn.hasImpulse = true;
        if (amplifier == 1) {
            float liftSpeed = 0.0175f;
            if (entityLivingBaseIn.getPersistentData().contains("lift_speed")) {
                liftSpeed = entityLivingBaseIn.getPersistentData().getFloat("lift_speed");

        return true;
    }
            if (entityLivingBaseIn instanceof Player) {
                IPlayerMagic magic = MNACap.get((IPlayerMagic)((Player)entityLivingBaseIn), PlayerMagicProvider.MAGIC).orElse(null);
                forcePos = magic.getLiftPosition();
                if (forcePos == null) {
                    forcePos = entityLivingBaseIn.position();
                }
                forcePos = forcePos.add(0.0, (double)liftSpeed, 0.0);
                magic.setLiftPosition(forcePos);
                ManaAndArtifice.instance.proxy.setFlightEnabled((Player)entityLivingBaseIn, true);
            } else {
                forcePos = forcePos.add(0.0, (double)liftSpeed, 0.0);
            }
        }
        entityLivingBaseIn.setPos(forcePos.x, forcePos.y, forcePos.z);
    }

    @Override
    public EnumSet<IInputDisable.InputMask> getDisabledFlags() {
        return EnumSet.of(IInputDisable.InputMask.LEFT_CLICK, IInputDisable.InputMask.RIGHT_CLICK, IInputDisable.InputMask.MOVEMENT);
    }
}

