 
package com.mna.items.artifact;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.entities.DamageHelper;
import com.mna.api.items.IShowHud;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.entities.boss.attacks.ThrownAllfatherAxe;
import com.mna.items.base.INoCreativeTab;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class AllfatherAxeControl
extends Item
implements IShowHud,
INoCreativeTab {
    private static final int TELEPORT_MANA_COST = 125;
    private static final int RADIUS = 5;

    public AllfatherAxeControl() {
        super(new Item.Properties().fireResistant().setNoRepair().stacksTo(1).rarity(Rarity.EPIC));
    }

    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        return false;
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        AllfatherAxeControl.recallAxe(context.getLevel(), stack);
        return InteractionResult.sidedSuccess((boolean)context.getLevel().isClientSide());
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack held = pPlayer.getItemInHand(pUsedHand);
        AllfatherAxeControl.recallAxe(pLevel, held);
        return InteractionResultHolder.sidedSuccess(held, (boolean)pLevel.isClientSide());
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        MutableBoolean consumed = new MutableBoolean();
        if (entity instanceof Player) {
            Player p = (Player)entity;
            if (!p.getCooldowns().isOnCooldown((Item)this)) {
                MNACap.get(p, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                    if (m.getCastingResource().hasEnough(entity, 125.0f)) {
                        m.getCastingResource().consume(entity, 125.0f);
                        consumed.setTrue();
                    }
                });
            }
        } else {
            consumed.setTrue();
        }
        if (consumed.getValue().booleanValue()) {
            ThrownAllfatherAxe axe = AllfatherAxeControl.getAxe(entity.level(), stack);
            stack.shrink(1);
            if (axe != null) {
                if (entity.level().isClientSide()) {
                    AllfatherAxeControl.spawnParticles(entity, axe);
                }
                BlockPos pos = BlockPos.containing((Position)axe.position());
                if (entity.level().isEmptyBlock(pos) && entity.level().isEmptyBlock(pos.above())) {
                    DelayedEventQueue.pushEvent(entity.level(), new TimedDelayedEvent<Pair>("damage_burst", 10, new Pair(entity, axe.position()), AllfatherAxeControl::teleportAndDamage));
                }
                axe.setReturning();
            }
        }
        return false;
    }

    private static void recallAxe(Level world, ItemStack stack) {
        ThrownAllfatherAxe axe = AllfatherAxeControl.getAxe(world, stack);
        stack.shrink(1);
        if (axe != null) {
            axe.setReturning();
        }
    }

    private static ThrownAllfatherAxe getAxe(Level world, ItemStack stack) {
        int entityID;
        Entity e;
        if (stack.getItem() instanceof AllfatherAxeControl && stack.has(DataComponents.CUSTOM_DATA) && (e = world.getEntity(entityID = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("axe_id"))) instanceof ThrownAllfatherAxe) {
            return (ThrownAllfatherAxe)e;
        }
        return null;
    }

    public static void setAxe(ItemStack stack, ThrownAllfatherAxe entity) {
        if (stack.getItem() instanceof AllfatherAxeControl) {
            stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("axe_id", entity.getId());
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private static void spawnParticles(LivingEntity entity, ThrownAllfatherAxe axe) {
        Vec3 pos;
        int i;
        for (i = 0; i < 50; ++i) {
            pos = entity.position().add(new Vec3(0.0, Math.random(), 0.0));
            entity.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setColor((float)Math.random() * 255.0f, (float)Math.random() * 255.0f, (float)Math.random() * 255.0f).setScale(0.08f).setMaxAge(50), pos.x, pos.y, pos.z, (double)0.6f * Math.random() + (double)0.1f, (double)0.2f, 0.5);
        }
        for (i = 0; i < 50; ++i) {
            pos = axe.position().add(new Vec3(0.0, 1.0 + Math.random(), 0.0));
            entity.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setColor((float)Math.random() * 255.0f, (float)Math.random() * 255.0f, (float)Math.random() * 255.0f).setScale(0.08f).setMaxAge(50), pos.x, pos.y, pos.z, (double)0.6f * Math.random() + (double)0.1f, (double)-0.2f, 0.5);
        }
        DelayedEventQueue.pushEvent(entity.level(), new TimedDelayedEvent<Pair>("frost_burst", 10, new Pair(axe.level(), axe.position()), AllfatherAxeControl::frostBurst));
    }

    private static void frostBurst(String identifier, Pair<Level, Vec3> data) {
        MAParticleType particle = (MAParticleType)(ParticleInit.FROST.get());
        int count = 180;
        double angleRads = 0.0;
        double step = Math.PI * 2 / (double)count;
        for (int i = 0; i < count; ++i) {
            Vec3 dir = new Vec3(Math.cos(angleRads += step), Math.random() * 0.1, Math.sin(angleRads)).normalize();
            Vec3 vel = dir.scale(1.5);
            dir = dir.scale(0.2);
            ((Level)data.getFirst()).addParticle((ParticleOptions)new MAParticleType(particle).setScale(0.5f), ((Vec3)data.getSecond()).x() + dir.x, ((Vec3)data.getSecond()).y() + Math.random(), ((Vec3)data.getSecond()).z() + dir.z, vel.x, vel.y, vel.z);
        }
    }

    private static void teleportAndDamage(String identifier, Pair<LivingEntity, Vec3> data) {
        BlockPos soundPos = BlockPos.containing((Position)((Position)data.getSecond()));
        ((LivingEntity)data.getFirst()).level().playSound(null, soundPos, SFX.Spell.Cast.ICE, SoundSource.PLAYERS, 1.0f, 1.0f);
        ((LivingEntity)data.getFirst()).level().playSound(null, soundPos, SFX.Spell.Impact.Single.ICE, SoundSource.PLAYERS, 1.0f, 1.0f);
        ((LivingEntity)data.getFirst()).level().getEntities(null, new AABB(soundPos).inflate(5.0)).forEach(e -> {
            if (e instanceof LivingEntity) {
                ((LivingEntity)e).hurt(DamageHelper.createSourcedType(DamageHelper.FROST, e.level().registryAccess(), (Entity)data.getFirst()), 15.0f);
            }
        });
        ((LivingEntity)data.getFirst()).setPos((Vec3)data.getSecond());
        ((LivingEntity)data.getFirst()).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 1, true, false));
    }
}


