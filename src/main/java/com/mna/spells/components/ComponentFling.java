 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.boss.BossMonster;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import top.theillusivec4.curios.api.SlotTypePreset;
import com.mna.api.tools.MNACap;

public class ComponentFling
extends SpellEffect {
    public static final String FLING_STRENGTH = "mna:flung";
    public static final String FLING_TIME = "mna:fling_time";

    public ComponentFling(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.SPEED, 1.0f, 1.0f, 3.0f, 1.0f, 5.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        Entity vehicle;
        float speed = modificationData.getValue(Attribute.SPEED) / 1.5f;
        if (!target.isEntity()) {
            return ComponentApplicationResult.FAIL;
        }
        Entity eTarget = target.getEntity();
        if (target.getEntity() == source.getCaster() && (vehicle = source.getCaster().getVehicle()) != null) {
            eTarget = vehicle;
        }
        if (eTarget instanceof LivingEntity) {
            MobEffectInstance effect;
            LivingEntity le = (LivingEntity)eTarget;
            float max_velocity = 1.0f + 0.25f * modificationData.getValue(Attribute.SPEED);
            if (CuriosInterop.IsItemInCurioSlot((Item)ItemInit.AIR_CAST_RING.get(), le, SlotTypePreset.RING)) {
                max_velocity = 4.0f;
                speed += 0.5f;
            }
            if ((effect = le.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()))) != null) {
                speed = (float)((double)speed * (1.0 - 0.1 * (double)(effect.getAmplifier() + 1)));
                max_velocity *= 0.5f;
            }
            if ((effect = le.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.REDUCE.get()))) != null) {
                speed = (float)((double)speed * (1.0 + 0.2 * (double)(effect.getAmplifier() + 1)));
                max_velocity = (float)((double)max_velocity * (1.0 + 0.2 * (double)(effect.getAmplifier() + 1)));
            }
            if (source.hasCasterReference() && le == source.getCaster()) {
                if (source.isPlayerCaster()) {
                    IPlayerMagic magic = MNACap.get((IPlayerMagic)source.getPlayer(), PlayerMagicProvider.MAGIC).orElse(null);
                    if (magic == null || magic.getAirCasts() >= magic.getAirCastLimit(source.getPlayer(), context.getSpell())) {
                        return ComponentApplicationResult.FAIL;
                    }
                    if (!source.getPlayer().onGround()) {
                        magic.incrementAirCasts(source.getPlayer(), context.getSpell());
                    }
                    if (source.getPlayer().getItemInHand(source.getHand()).getItem() == ItemInit.ICARIAN_FLIGHT.get()) {
                        max_velocity = 1000.0f;
                    }
                }
                Vec3 motion = source.getCaster().getLookAngle().normalize();
                motion = motion.scale((double)speed);
                source.getCaster().push(motion.x, motion.y, motion.z);
                motion = source.getCaster().getDeltaMovement();
                if (motion.length() > (double)max_velocity) {
                    double scale = (double)max_velocity / motion.length();
                    source.getCaster().setDeltaMovement(motion.scale(scale));
                }
                source.getCaster().hurtMarked = true;
                if (source.isPlayerCaster()) {
                    ((ServerPlayer)source.getPlayer()).connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)le));
                }
                ComponentFling.setFlags(le, speed);
                return ComponentApplicationResult.SUCCESS;
            }
            if (le instanceof Player) {
                Player targetPlayer = (Player)le;
                Vec3 motion = source.hasCasterReference() ? source.getCaster().getLookAngle() : targetPlayer.getLookAngle();
                ComponentFling.flingTarget((LivingEntity)targetPlayer, motion, speed, max_velocity, source.getCaster() instanceof BossMonster ? 0.5f : 1.0f);
                return ComponentApplicationResult.SUCCESS;
            }
            float mX = (float)(source.getOrigin().x() - target.getLivingEntity().getX());
            float mZ = (float)(source.getOrigin().z() - target.getLivingEntity().getZ());
            if (source.getCaster().getVehicle() == le) {
                mX = (float)(-source.getCaster().getForward().x);
                mZ = (float)(-source.getCaster().getForward().z);
            }
            ComponentFling.flingTarget(le, new Vec3((double)mX, (double)speed, (double)mZ), speed, max_velocity);
            return ComponentApplicationResult.SUCCESS;
        }
        float mX = (float)(source.getOrigin().x() - eTarget.getX());
        float mZ = (float)(source.getOrigin().z() - eTarget.getZ());
        if (source.getCaster().getVehicle() == eTarget) {
            mX = (float)(-source.getCaster().getForward().x);
            mZ = (float)(-source.getCaster().getForward().z);
        }
        eTarget.hasImpulse = true;
        Vec3 vector3d = eTarget.getDeltaMovement();
        Vec3 vector3d1 = new Vec3((double)mX, 0.0, (double)mZ).normalize().scale((double)speed);
        eTarget.setDeltaMovement(vector3d.x / 2.0 - vector3d1.x, eTarget.onGround() ? Math.min(0.4, vector3d.y / 2.0 + (double)speed) : vector3d.y, vector3d.z / 2.0 - vector3d1.z);
        return ComponentApplicationResult.SUCCESS;
    }

    public static void flingTarget(LivingEntity target, Vec3 direction, float strength, float kbResistFactor) {
        ComponentFling.flingTarget(target, direction, strength, 2.0f, kbResistFactor);
    }

    public static void flingTarget(LivingEntity target, Vec3 direction, float strength, float max_velocity, float kbResistFactor) {
        if (target == null) {
            return;
        }
        MobEffectInstance effect = target.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()));
        if (effect != null) {
            strength = (float)((double)strength * (1.0 - 0.1 * (double)(effect.getAmplifier() + 1)));
            max_velocity *= 0.5f;
        }
        if ((effect = target.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.REDUCE.get()))) != null) {
            strength = (float)((double)strength * (1.0 + 0.2 * (double)(effect.getAmplifier() + 1)));
            max_velocity = (float)((double)max_velocity * (1.0 + 0.2 * (double)(effect.getAmplifier() + 1)));
        }
        if (target instanceof Player) {
            Player targetPlayer = (Player)target;
            Vec3 motion = direction.normalize().scale((double)strength);
            targetPlayer.push(motion.x, motion.y, motion.z);
            motion = targetPlayer.getDeltaMovement();
            if (motion.length() > (double)max_velocity) {
                double scale = (double)max_velocity / motion.length();
                targetPlayer.setDeltaMovement(motion.scale(scale));
            }
            targetPlayer.hurtMarked = true;
            ((ServerPlayer)targetPlayer).connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)targetPlayer));
            if (!CuriosInterop.IsItemInCurioSlot((Item)ItemInit.AIR_CAST_RING.get(), (LivingEntity)targetPlayer, SlotTypePreset.RING)) {
                ComponentFling.setFlags((LivingEntity)targetPlayer, strength);
            }
        } else {
            LivingKnockBackEvent event = CommonHooks.onLivingKnockBack((LivingEntity)target, (float)strength, (double)direction.x, (double)direction.z);
            if (!event.isCanceled()) {
                double kbRes = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                strength = event.getStrength();
                direction = new Vec3(event.getRatioX(), 0.0, event.getRatioZ());
                if (!((double)(strength = (float)((double)strength * (1.0 - kbRes * (double)kbResistFactor))) <= 0.0)) {
                    target.hasImpulse = true;
                    Vec3 vec3 = target.getDeltaMovement();
                    Vec3 vec31 = new Vec3(direction.x, 0.0, direction.z).normalize().scale((double)strength);
                    target.setDeltaMovement(vec3.x / 2.0 - vec31.x, target.onGround() ? Math.min(0.4, vec3.y / 2.0 + (double)strength) : vec3.y, vec3.z / 2.0 - vec31.z);
                }
            }
            target.push(0.0, (double)(0.2f * strength), 0.0);
            if (target instanceof PathfinderMob) {
                ((PathfinderMob)target).getNavigation().stop();
            }
            ComponentFling.setFlags(target, strength);
        }
    }

    private static void setFlags(LivingEntity le, float strength) {
        le.getPersistentData().putFloat(FLING_STRENGTH, strength);
        le.getPersistentData().putLong(FLING_TIME, le.level().getGameTime());
        le.hasImpulse = true;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.WIND;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WIND;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 10) {
            return;
        }
        float particle_spread = 1.0f;
        int particleCount = 10;
        Vec3 velocity = caster != null ? caster.getLookAngle() : new Vec3(0.0, 0.1, 0.0);
        for (int i = 0; i < particleCount; ++i) {
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}


