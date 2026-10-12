/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.DyeItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.util.FakePlayerFactory
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
import com.mna.blocks.sorcery.TransitoryTileBlock;
import com.mna.tools.BlockUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import com.mna.api.tools.MNADyeableItem;

public class ComponentTransitoryTile
extends SpellEffect {
    public ComponentTransitoryTile(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 5.0f, 5.0f, 15.0f, 5.0f, 3.5f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        BlockPos blockTarget = null;
        if (target.isEntity() && source.isPlayerCaster() && target.getEntity() == source.getCaster()) {
            blockTarget = source.getPlayer().blockPosition().below(1);
        } else if (target.isBlock()) {
            blockTarget = target.getBlock().offset(target.getBlockFace(this).getNormal());
        }
        if (blockTarget == null) {
            return ComponentApplicationResult.FAIL;
        }
        if (context.getServerLevel().isEmptyBlock(blockTarget) && context.getServerLevel().getEntities((Entity)null, new AABB(blockTarget), e -> e instanceof LivingEntity).size() == 0) {
            BlockState placeState = (BlockState)((TransitoryTileBlock)BlockInit.TRANSITORY_TILE.get()).defaultBlockState().setValue(TransitoryTileBlock.DURATION, Integer.valueOf((int)modificationData.getValue(Attribute.DURATION)));
            Player player = source.isPlayerCaster() ? source.getPlayer() : FakePlayerFactory.getMinecraft((ServerLevel)context.getServerLevel());
            BlockUtils.placeBlock(context.getServerLevel(), blockTarget, target.getBlockFace(this), placeState, player);
            context.getServerLevel().scheduleTick(blockTarget, (Block)BlockInit.TRANSITORY_TILE.get(), 20);
            this.setColor(player, source.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, (Level)context.getServerLevel(), blockTarget, context.getSpell().getParticleColorOverride());
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    private void setColor(Player player, InteractionHand hand, Level world, BlockPos pos, int overrideColor) {
        if (overrideColor == -1) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.getItem() instanceof DyeItem) {
                MNADyeableItem.setColor(world, pos, ((DyeItem)heldItem.getItem()).getDyeColor().getFireworkColor());
            }
        } else {
            MNADyeableItem.setColor(world, pos, overrideColor);
        }
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Buff.ENDER;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 10) {
            return;
        }
        float particle_spread = 1.0f;
        float v = 0.4f;
        int particleCount = 10;
        for (int i = 0; i < particleCount; ++i) {
            Vec3 velocity = new Vec3(0.0, Math.random() * (double)v, 0.0);
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public boolean targetsEntities() {
        return true;
    }

    @Override
    public Direction defaultBlockFace() {
        return Direction.DOWN;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ARCANE, Affinity.ENDER, Affinity.WIND, Affinity.ICE);
    }
}

