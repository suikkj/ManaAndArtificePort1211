 
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
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.tools.BlockUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotTypePreset;
import com.mna.api.tools.MNACap;
import net.minecraft.world.level.block.BushBlock;

public class ComponentGust
extends SpellEffect {
    public ComponentGust(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f, 2.0f), new AttributeValuePair(Attribute.SPEED, 1.0f, 0.0f, 4.0f, 1.0f, 2.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        float speed = modificationData.getValue(Attribute.SPEED);
        float precision = modificationData.getValue(Attribute.PRECISION);
        if (target.isBlock() && precision == 0.0f) {
            if (!context.getServerLevel().isLoaded(target.getBlock())) {
                return ComponentApplicationResult.FAIL;
            }
            BlockState state = context.getServerLevel().getBlockState(target.getBlock());
            if (speed == 0.0f) {
                Block block = state.getBlock();
                if (block instanceof BrushableBlock) {
                    BrushableBlockEntity brushable;
                    BlockEntity blockentity;
                    BrushableBlock brushableBlock = (BrushableBlock)block;
                    if (source.isPlayerCaster() && (blockentity = context.getLevel().getBlockEntity(target.getBlock())) instanceof BrushableBlockEntity && (brushable = (BrushableBlockEntity)blockentity).brush(context.getLevel().getGameTime(), source.getPlayer(), target.getBlockFace(this))) {
                        context.getLevel().playSound(source.getPlayer(), target.getBlock(), brushableBlock.getBrushSound(), SoundSource.BLOCKS);
                    }
                }
            } else if (ComponentGust.BlockStateIsGustable(state) && BlockUtils.destroyBlock(source.getCaster(), (Level)context.getServerLevel(), target.getBlock(), true, (Tier)Tiers.WOOD)) {
                BlockUtils.updateBlockState((Level)context.getServerLevel(), target.getBlock());
                return ComponentApplicationResult.SUCCESS;
            }
        } else if (target.isEntity() && speed > 1.0f) {
            speed -= 1.0f;
            if (target.getEntity() instanceof ItemEntity) {
                Vec3 tPos = target.getEntity().position();
                Vec3 cPos = source.getOrigin();
                Vec3 delta = cPos.subtract(tPos).normalize();
                target.getEntity().setDeltaMovement(delta);
                return ComponentApplicationResult.SUCCESS;
            }
            if (target.getEntity() instanceof LivingEntity) {
                if (context.hasEntityBeenAffected(this, target.getEntity())) {
                    return ComponentApplicationResult.FAIL;
                }
                context.addAffectedEntity(this, target.getEntity());
                LivingEntity le = target.getLivingEntity();
                float max_velocity = 1.5f;
                if (CuriosInterop.IsItemInCurioSlot((Item)ItemInit.AIR_CAST_RING.get(), source.getCaster(), SlotTypePreset.RING)) {
                    max_velocity += 0.5f;
                }
                Vec3 motion = new Vec3(0.0, 0.25, 0.0);
                motion = motion.scale((double)(speed * 2.0f));
                if (le instanceof Player) {
                    Player targetPlayer = (Player)le;
                    if (targetPlayer == source.getCaster()) {
                        IPlayerMagic magic = MNACap.get((IPlayerMagic)source.getPlayer(), PlayerMagicProvider.MAGIC).orElse(null);
                        if (magic == null || magic.getAirCasts() >= magic.getAirCastLimit(source.getPlayer(), context.getSpell())) {
                            return ComponentApplicationResult.FAIL;
                        }
                        if (!source.getPlayer().onGround()) {
                            magic.incrementAirCasts(source.getPlayer(), context.getSpell());
                        }
                    }
                    targetPlayer.push(0.0, motion.y, 0.0);
                    motion = targetPlayer.getDeltaMovement();
                    if (motion.y() > (double)max_velocity) {
                        motion = new Vec3(motion.x, (double)max_velocity, motion.z);
                    }
                    targetPlayer.setDeltaMovement(motion.x, motion.y, motion.z);
                    targetPlayer.hurtMarked = true;
                    ((ServerPlayer)targetPlayer).connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)targetPlayer));
                    if (!CuriosInterop.IsItemInCurioSlot((Item)ItemInit.AIR_CAST_RING.get(), (LivingEntity)targetPlayer, SlotTypePreset.RING)) {
                        this.setFlags((LivingEntity)targetPlayer, speed);
                    }
                    return ComponentApplicationResult.SUCCESS;
                }
                le.push(0.0, motion.y, 0.0);
                motion = le.getDeltaMovement();
                if (motion.y() > (double)max_velocity) {
                    motion = new Vec3(motion.x, (double)max_velocity, motion.z);
                    le.setDeltaMovement(motion);
                }
                if (le instanceof PathfinderMob) {
                    ((PathfinderMob)le).getNavigation().stop();
                }
                this.setFlags(le, speed);
                return ComponentApplicationResult.SUCCESS;
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    private void setFlags(LivingEntity le, float strength) {
        le.getPersistentData().putFloat("mna:flung", strength);
        le.getPersistentData().putLong("mna:fling_time", le.level().getGameTime());
    }

    public static boolean BlockStateIsGustable(BlockState state) {
        return MATags.isBlockIn(state.getBlock(), MATags.Blocks.GUST_DESTRUCTIBLE_BLOCKS) || state.getBlock() instanceof BushBlock;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WIND;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.AoE.WIND;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age < 10) {
            float particle_spread = 1.0f;
            int particleCount = 5;
            for (int i = 0; i < particleCount; ++i) {
                world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.2f).setColor(10, 10, 10), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), Math.random() * (double)0.2f, (double)0.3f, 2.5);
            }
        }
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 50;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.WIND, Affinity.LIGHTNING, Affinity.WATER);
    }
}


