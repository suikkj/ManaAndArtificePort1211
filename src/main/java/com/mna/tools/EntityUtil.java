 
package com.mna.tools;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.EffectInit;
import com.mna.effects.beneficial.EffectCamouflage;
import com.mna.entities.sorcery.EntityDecoy;
import com.mna.items.ItemInit;
import com.mna.items.artifice.FactionSpecificSpellModifierRing;
import com.mna.network.ServerMessageDispatcher;
import com.mna.spells.components.ComponentTrueInvisibility;
import com.mna.tools.SummonUtils;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.apache.commons.lang3.mutable.MutableFloat;
import com.mna.api.tools.MNACap;

public class EntityUtil {
    public static boolean doElytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks, float manaCost, float maxSpeed) {
        IPlayerMagic magic;
        if (!(entity instanceof Player)) {
            return false;
        }
        if (maxSpeed <= 0.0f) {
            return true;
        }
        if (flightTicks % 100 == 0 && stack.getItem() instanceof IFactionSpecific) {
            ((IFactionSpecific)stack.getItem()).usedByPlayer((Player)entity);
        }
        if ((magic = MNACap.get((IPlayerMagic)((Player)entity), PlayerMagicProvider.MAGIC).orElse(null)) == null || !magic.getCastingResource().hasEnoughAbsolute(entity, manaCost)) {
            return false;
        }
        Vec3 look = entity.getLookAngle();
        Vec3 motion = null;
        if (!entity.isShiftKeyDown()) {
            magic.getCastingResource().consume(entity, manaCost);
            motion = entity.getDeltaMovement();
            float maxLength = maxSpeed;
            lookScale = 0.06;
            scaled_look = look.scale(lookScale);
            motion = motion.add(scaled_look);
            if (motion.length() > (double)maxLength) {
                motion = motion.scale((double)maxLength / motion.length());
            }
            entity.setDeltaMovement(motion);
        } else {
            magic.getCastingResource().consume(entity, manaCost * 0.75f);
            motion = entity.getDeltaMovement();
            float minLength = 0.1f;
            lookScale = -0.01;
            scaled_look = look.scale(lookScale);
            motion = motion.add(scaled_look);
            if (motion.length() < (double)minLength) {
                motion = motion.scale((double)minLength / motion.length());
            }
            entity.setDeltaMovement(motion);
        }
        if (entity.level().isClientSide()) {
            Vec3 pos = entity.position().add(look.scale(3.0));
            for (int i = 0; i < 5; ++i) {
                entity.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10), pos.x - 0.5 + Math.random(), pos.y - 0.5 + Math.random(), pos.z - 0.5 + Math.random(), -look.x, -look.y, -look.z);
            }
        }
        return true;
    }

    public static void SetLiftSpeed(Player p, float speed) {
        p.getPersistentData().putFloat("lift_speed", 0.055f);
        if (!p.level().isClientSide() && p instanceof ServerPlayer) {
            ServerMessageDispatcher.sendSetLiftSpeedMessage((ServerPlayer)p, speed);
        }
    }

    public static List<LivingEntity> getEntitiesWithinCone(Level world, Vec3 origin, Vec3 forward, float distance, float minAngle, float maxAngle, Predicate<LivingEntity> selectionPredicate) {
        List entities = world.getEntities((Entity)null, new AABB(BlockPos.containing((Position)origin)).inflate((double)distance), e -> {
            Vec3 direction;
            float angleDeg;
            if (e instanceof LivingEntity && (angleDeg = (float)(Math.acos(forward.dot(direction = e.position().subtract(origin).normalize())) * 180.0 / Math.PI)) >= minAngle && angleDeg <= maxAngle) {
                return selectionPredicate.test((LivingEntity)e);
            }
            return false;
        });
        return entities.stream().map(e -> (LivingEntity)e).collect(Collectors.toList());
    }

    public static float getSoulsRestored(Player soulRecipient, Entity target) {
        if (soulRecipient != null) {
            if (!(target instanceof LivingEntity) || SummonUtils.isSummon(target)) {
                return 0.0f;
            }
            if (target instanceof EntityDecoy) {
                return 0.0f;
            }
            MutableFloat restoreAmount = new MutableFloat(1.0f);
            if (target instanceof Player) {
                restoreAmount.setValue((float)GeneralConfigValues.SoulsForPlayerKill);
            } else if (target instanceof Villager) {
                restoreAmount.setValue((float)GeneralConfigValues.SoulsForVillagerKill);
            } else if (target instanceof IFactionEnemy) {
                restoreAmount.setValue((float)GeneralConfigValues.SoulsForFactionMobKill);
            } else if (((LivingEntity)target).isInvertedHealAndHarm()) {
                restoreAmount.setValue((float)GeneralConfigValues.SoulsForUndeadKill);
            } else if (target instanceof Animal) {
                restoreAmount.setValue((float)GeneralConfigValues.SoulsForAnimalKill);
            } else if (target instanceof AbstractGolem) {
                restoreAmount.setValue(0.0f);
            } else if (target instanceof Mob) {
                restoreAmount.setValue((float)GeneralConfigValues.SoulsForMobKill);
            }
            if (((LivingEntity)target).hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOUL_VULNERABILITY.get()))) {
                restoreAmount.setValue(restoreAmount.getValue().floatValue() * 5.0f);
            }
            if (((FactionSpecificSpellModifierRing)ItemInit.BONE_RING.get()).isEquippedAndHasMana((LivingEntity)soulRecipient, 3.5f, true)) {
                restoreAmount.setValue(restoreAmount.getValue().floatValue() * 2.25f);
            }
            return restoreAmount.getValue().floatValue();
        }
        return 0.0f;
    }

    @Nullable
    public static Vec3 getRetreatPos(Entity e, Vec3 direction, int maxDistance) {
        Vec3 startPos = e.position().add(0.0, 0.5, 0.0);
        Vec3 delta = direction.normalize();
        delta = new Vec3(delta.x, 0.0, delta.z);
        Vec3 endPos = startPos.add(delta.scale((double)maxDistance));
        int count = 0;
        while (!e.level().isEmptyBlock(BlockPos.containing((double)endPos.x, (double)endPos.y, (double)endPos.z)) && count < 10) {
            ++count;
            endPos = endPos.add(0.0, 1.0, 0.0);
        }
        if (count >= 10) {
            return null;
        }
        ClipContext ctx = new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e);
        BlockHitResult brtr = e.level().clip(ctx);
        if (brtr.getType() == HitResult.Type.MISS) {
            return endPos;
        }
        return brtr.getLocation().add(delta);
    }

    public static void removeInvisibility(LivingEntity living) {
        if (EntityUtil.shouldBeInvisible(living)) {
            return;
        }
        if (living.isInvisible()) {
            living.setInvisible(false);
            if (living instanceof Player) {
                Player player = (Player)living;
                player.onUpdateAbilities();
            }
        }
    }

    public static boolean shouldBeInvisible(LivingEntity living) {
        MobEffectInstance inst = living.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()));
        if (inst != null && inst.getAmplifier() == 2) {
            return true;
        }
        return living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get())) || living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GREATER_INVISIBILITY.get())) && !ComponentTrueInvisibility.IsDisabledFor(living) || EffectCamouflage.getCamoflagePercent(living) > 0.5f;
    }

    public static void suppressInvisibilityEffects(LivingEntity living) {
        ComponentTrueInvisibility.DisableForSeconds(living, 5);
        EffectCamouflage.setCamoflagePercent(living, 0.0f);
    }

    public static FakePlayer getFakePlayer(ServerLevel serverLevel, @Nullable UUID gameProfileID) {
        if (gameProfileID == null) {
            return FakePlayerFactory.getMinecraft((ServerLevel)serverLevel);
        }
        GameProfile gameProfile = new GameProfile(gameProfileID, "M&A Player Placeholder");
        return FakePlayerFactory.get((ServerLevel)serverLevel, (GameProfile)gameProfile);
    }
}

