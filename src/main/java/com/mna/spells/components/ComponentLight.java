/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.DyeItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.util.FakePlayerFactory
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
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
import com.mna.blocks.BlockInit;
import com.mna.blocks.sorcery.MagelightBlock;
import com.mna.tools.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public class ComponentLight
extends SpellEffect {
    public ComponentLight(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 15.0f, 15.0f, 180.0f, 5.0f, 2.0f), new AttributeValuePair(Attribute.RANGE, 0.0f, 0.0f, 5.0f, 1.0f, 1.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isBlock() && context.getServerLevel().isLoaded(target.getBlock())) {
            if (context.countAffectedBlocks(this) > 0) {
                return ComponentApplicationResult.FAIL;
            }
            Vec3i offset = target.getBlockFace(this).getNormal();
            BlockPos pos = target.getBlock().offset(offset);
            int count = 0;
            int range = (int)modificationData.getValue(Attribute.RANGE);
            if (range > 0) {
                while (count++ < range && this.isPosValidForPlacement((Level)context.getServerLevel(), pos)) {
                    pos = pos.offset(offset);
                }
                if (!this.isPosValidForPlacement((Level)context.getServerLevel(), pos)) {
                    pos = pos.offset(offset.multiply(-1));
                }
                count = 0;
            }
            while (count++ < 5 && !this.isPosValidForPlacement((Level)context.getServerLevel(), pos)) {
                pos = pos.above();
            }
            if (this.isPosValidForPlacement((Level)context.getServerLevel(), pos)) {
                Player player = source.isPlayerCaster() ? source.getPlayer() : FakePlayerFactory.getMinecraft((ServerLevel)context.getServerLevel());
                BlockHitResult brtr = new BlockHitResult(new Vec3((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), target.getBlockFace(this), pos, true);
                BlockPlaceContext biuc = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, brtr);
                BlockUtils.placeBlock(context.getServerLevel(), pos, target.getBlockFace(this), ((MagelightBlock)BlockInit.MAGE_LIGHT.get()).getStateForPlacement(biuc), source.getPlayer());
                this.setLightColor(player, source.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, (Level)context.getServerLevel(), pos, context.getSpell().getParticleColorOverride());
                return ComponentApplicationResult.SUCCESS;
            }
        } else if (target.isLivingEntity() && context.getServerLevel().isLoaded(target.getEntity().blockPosition())) {
            target.getLivingEntity().addEffect(new MobEffectInstance(MobEffects.GLOWING, (int)modificationData.getValue(Attribute.DURATION) * 20, 0, false, false));
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    private void setLightColor(Player player, InteractionHand hand, Level world, BlockPos pos, int overrideColor) {
        if (overrideColor == -1) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.getItem() instanceof DyeItem) {
                ((MagelightBlock)BlockInit.MAGE_LIGHT.get()).setLightColor(world, pos, ((DyeItem)heldItem.getItem()).getDyeColor());
            }
        } else {
            ((MagelightBlock)BlockInit.MAGE_LIGHT.get()).setLightColor(world, pos, overrideColor);
        }
    }

    private boolean isPosValidForPlacement(Level world, BlockPos pos) {
        if (world.isEmptyBlock(pos)) {
            return true;
        }
        return world.getFluidState(pos).isSource() && world.getBlockState(pos).getBlock() == Blocks.WATER;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.ARCANE;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ARCANE;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 1) {
            return;
        }
        BlockPos imp = BlockPos.containing((Position)impact_position);
        for (int i = 0; i < 50; ++i) {
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), (Entity)caster), (double)imp.getX() + 0.5, (double)imp.getY() + 0.5, (double)imp.getZ() + 0.5, -0.25 + Math.random() * 0.5, Math.random() * 0.25, -0.25 + Math.random() * 0.5);
        }
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public float initialComplexity() {
        return 5.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.NEUTRAL;
    }
}


