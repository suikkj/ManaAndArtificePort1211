 
package com.mna.api.entities.construct;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.effects.MAEffects;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.events.construct.FluidParameterRegistrationEvent;
import com.mna.api.faction.FactionIDs;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import java.util.HashMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.apache.commons.lang3.function.TriFunction;
import com.mna.api.tools.MNACap;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class FluidParameterRegistry {
    Function<Vec3, Vec3> ADJUST_DEFAULT = vel -> vel.yRot((float)(-0.25 * Math.random() + 0.5)).normalize().scale((double)0.35f);
    HashMap<Fluid, FluidParameter> __registry = new HashMap();
    public static final FluidParameterRegistry INSTANCE = new FluidParameterRegistry();

    public void registerDefaults() {
        this.__registry.put(Fluids.EMPTY, new FluidParameter(Fluids.EMPTY, false, false, SFX.Entity.Construct.SPRAY_GENERIC, new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_VELOCITY.get())), 0.35f, 10, null, this.ADJUST_DEFAULT));
        this.__registry.put((Fluid)Fluids.WATER, new FluidParameter((Fluid)Fluids.WATER, true, true, SFX.Entity.Construct.SPRAY_GENERIC, (ParticleOptions)ParticleTypes.SPLASH, 0.5f, 20, (living, isFriendly, construct) -> {
            if (living == null) {
                return false;
            }
            if (isFriendly.booleanValue()) {
                if (living instanceof Player && !((Player)living).isCreative() && construct.getIntelligence() >= 16) {
                    IPlayerProgression progression = (IPlayerProgression)MNACap.get(living, ManaAndArtificeMod.getProgressionCapability()).orElse(null);
                    MobEffectInstance soaked = ((Player)living).getEffect(MAEffects.SOAKED);
                    if (progression != null && progression.getAlliedFaction() != null && progression.getAlliedFaction().is(FactionIDs.UNDEAD) && living.level().canSeeSky(living.blockPosition()) && (soaked == null || soaked.getDuration() < 20)) {
                        return true;
                    }
                }
                return living.isOnFire();
            }
            if (living instanceof Creeper && ((Creeper)living).hasEffect(MAEffects.SOAKED)) {
                return false;
            }
            return true;
        }, vel -> {
            Vec3 adjusted = vel.yRot((float)(-0.25 + Math.random() * 0.5)).normalize().scale(0.75 + Math.random() * 0.25);
            adjusted = adjusted.subtract(0.0, adjusted.y, 0.0);
            return adjusted;
        }));
        this.__registry.put((Fluid)Fluids.LAVA, new FluidParameter((Fluid)Fluids.LAVA, false, true, SFX.Entity.Construct.SPRAY_FIRE, new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setScale(0.13f).setPhysics(true), 0.35f, 10, null, null));
        this.__registry.put((Fluid)NeoForgeMod.MILK.get(), new FluidParameter((Fluid)NeoForgeMod.MILK.get(), true, true, SFX.Entity.Construct.SPRAY_GENERIC, new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setScale(0.15f).setColor(250, 250, 250), 0.35f, 10, (entity, isFriendly, construct) -> {
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                if (isFriendly.booleanValue()) {
                    return living.getActiveEffects().stream().anyMatch(effect -> !effect.getEffect().isBeneficial() && effect.isCurativeItem(new ItemStack((ItemLike)Items.MILK_BUCKET)));
                }
                return living.getActiveEffects().stream().anyMatch(effect -> effect.getEffect().isBeneficial() && effect.isCurativeItem(new ItemStack((ItemLike)Items.MILK_BUCKET)));
            }
            return false;
        }, null));
    }

    public void register(FluidParameter parameter) {
        this.__registry.put(parameter.fluid, parameter);
    }

    public FluidParameter forFluid(Fluid fluid) {
        return this.__registry.getOrDefault(fluid, this.__registry.get(Fluids.EMPTY));
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        INSTANCE.registerDefaults();
        FluidParameterRegistrationEvent evt = new FluidParameterRegistrationEvent(INSTANCE);
        NeoForge.EVENT_BUS.post((Event)evt);
    }

    public static class FluidParameter {
        final boolean targetsFriendly;
        final boolean targetsHostile;
        final Fluid fluid;
        final SoundEvent spraySound;
        @Nullable
        final TriFunction<Entity, Boolean, IConstruct<?>, Boolean> targetPredicate;
        final Function<Vec3, Vec3> adjustParticleVelocity;
        final ParticleOptions fluidParticle;
        final float particleVelocityScale;
        final int particleQuantity;

        public FluidParameter(Fluid forFluid, boolean targetAllies, boolean targetEnemies, SoundEvent spraySound, ParticleOptions type, float particleVelocityScale, int particleQuantity, @Nullable TriFunction<Entity, Boolean, IConstruct<?>, Boolean> targetPredicate, Function<Vec3, Vec3> adjustParticleVelocity) {
            this.fluid = forFluid;
            this.targetsFriendly = targetAllies;
            this.targetsHostile = targetEnemies;
            this.spraySound = spraySound;
            this.targetPredicate = targetPredicate;
            this.fluidParticle = type;
            this.particleVelocityScale = particleVelocityScale;
            this.adjustParticleVelocity = adjustParticleVelocity;
            this.particleQuantity = particleQuantity;
        }

        public boolean targetsFriendly() {
            return this.targetsFriendly;
        }

        public boolean targetsHostile() {
            return this.targetsHostile;
        }

        public Fluid fluid() {
            return this.fluid;
        }

        public boolean checkPredicate(Entity e, boolean isFriendly, IConstruct<?> construct) {
            if (this.targetPredicate == null) {
                return isFriendly ? this.targetsFriendly : this.targetsHostile;
            }
            return (Boolean)this.targetPredicate.apply(e, isFriendly, construct);
        }

        public ParticleOptions particle() {
            return this.fluidParticle;
        }

        public SoundEvent spraySound() {
            return this.spraySound;
        }

        public float getParticleVelocityScale() {
            return this.particleVelocityScale;
        }

        public Vec3 adjustVelocity(Vec3 vel) {
            if (this.adjustParticleVelocity != null) {
                return this.adjustParticleVelocity.apply(vel);
            }
            return vel;
        }

        public int getParticleQuantity() {
            return this.particleQuantity;
        }
    }
}


