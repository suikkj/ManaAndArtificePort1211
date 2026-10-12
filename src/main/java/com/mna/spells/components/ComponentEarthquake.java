/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.FallingBlockEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.util.FakePlayerFactory
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
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
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.spells.components.PotionEffectComponent;
import com.mna.tools.BlockUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public class ComponentEarthquake
extends PotionEffectComponent {
    public ComponentEarthquake(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.EARTHQUAKE, new AttributeValuePair(Attribute.DURATION, 1.0f, 1.0f, 10.0f, 1.0f, 4.0f), new AttributeValuePair(Attribute.DAMAGE, 10.0f, 10.0f, 30.0f, 5.0f, 15.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isBlock()) {
            BlockState state;
            BlockPos pos = target.getBlock();
            BlockEntity be = context.getServerLevel().getBlockEntity(pos);
            if (be == null && (state = context.getServerLevel().getBlockState(pos)).getFluidState().isEmpty() && !context.getServerLevel().isEmptyBlock(pos) && context.getServerLevel().isEmptyBlock(pos.above()) && !(state.getBlock() instanceof EntityBlock) && state.getBlock() != Blocks.BEDROCK) {
                Player player;
                Object object = player = source.isPlayerCaster() ? source.getPlayer() : FakePlayerFactory.getMinecraft((ServerLevel)context.getServerLevel());
                if (BlockUtils.canDestroyBlock((LivingEntity)player, (Level)context.getServerLevel(), target.getBlock(), (Tier)Tiers.DIAMOND) && context.getServerLevel().getEntities(null, new AABB(pos.above())).size() == 0) {
                    FallingBlockEntity fbe = new FallingBlockEntity(EntityType.FALLING_BLOCK, (Level)context.getServerLevel());
                    fbe.blocksBuilding = true;
                    fbe.setPos((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
                    fbe.xo = (double)pos.getX() + 0.5;
                    fbe.yo = pos.getY();
                    fbe.zo = pos.getZ();
                    fbe.setStartPos(pos);
                    fbe.setDeltaMovement(new Vec3(0.0, 0.25 + Math.random() * 0.75, 0.0));
                    fbe.blockState = state;
                    fbe.hasImpulse = true;
                    fbe.dropItem = false;
                    context.getServerLevel().addFreshEntity((Entity)fbe);
                    context.getServerLevel().setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
                    return ComponentApplicationResult.SUCCESS;
                }
            }
        } else if (target.isLivingEntity()) {
            target.getLivingEntity().hurt(target.getLivingEntity().damageSources().generic(), modificationData.getValue(Attribute.DAMAGE));
        }
        return super.ApplyEffect(source, target, modificationData, context);
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.AoE.EARTH;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.EARTH;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 10) {
            return;
        }
        float particle_spread = 1.0f;
        float v = 0.4f;
        int particleCount = 1;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 velocity = new Vec3(0.0, Math.random() * (double)v, 0.0);
            MAParticleType mpt = recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)caster);
            world.addParticle((ParticleOptions)mpt, impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return true;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ARCANE, Affinity.ENDER, Affinity.WATER, Affinity.WIND, Affinity.ICE, Affinity.LIGHTNING, Affinity.EARTH);
    }

    @Override
    public boolean isSilverSpell() {
        return true;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.COUNCIL;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}


