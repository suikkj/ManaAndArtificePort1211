/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Tuple
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package com.mna.spells.shapes;

import com.google.common.collect.Lists;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.network.ServerMessageDispatcher;
import com.mna.spells.shapes.ShapeRaytrace;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

public class ShapeChain
extends ShapeRaytrace {
    public ShapeChain(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RADIUS, 1.0f, 1.0f, 5.0f, 0.5f, 1.0f), new AttributeValuePair(Attribute.MAGNITUDE, 10.0f, 1.0f, 100.0f, 1.0f, 0.5f), new AttributeValuePair(Attribute.RANGE, 8.0f, 8.0f, 32.0f, 1.0f, 1.0f), new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f, 2.0f));
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        List<SpellTarget> tgts = super.Target(source, world, modificationData, recipe);
        SpellTarget tgt = tgts.get(0);
        if (tgt == SpellTarget.NONE) {
            return tgts;
        }
        return this.performChaining(source, world, modificationData, recipe, tgt);
    }

    @Override
    public List<SpellTarget> TargetNPCCast(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe, SpellTarget targetHint) {
        List<SpellTarget> tgts = super.TargetNPCCast(source, world, modificationData, recipe, targetHint);
        SpellTarget tgt = tgts.get(0);
        if (tgt == SpellTarget.NONE) {
            return tgts;
        }
        return this.performChaining(source, world, modificationData, recipe, tgt);
    }

    private List<SpellTarget> performChaining(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe, SpellTarget tgt) {
        int radius = (int)modificationData.getValue(Attribute.RADIUS);
        int arcChance = (int)modificationData.getValue(Attribute.MAGNITUDE);
        ArrayList<SpellTarget> targetsList = new ArrayList<SpellTarget>();
        targetsList.add(tgt);
        if (tgt.isBlock()) {
            Block search = world.getBlockState(tgt.getBlock()).getBlock();
            ArrayList<Long> blockTargets = new ArrayList<Long>();
            blockTargets.add(tgt.getBlock().asLong());
            boolean chainDiagonally = modificationData.getValue(Attribute.PRECISION) == 1.0f;
            this.chainTargetBlocks(world, search, tgt.getBlockFace(null), tgt.getBlock(), radius, arcChance, chainDiagonally, blockTargets, targetsList);
        } else if (tgt.isEntity()) {
            ArrayList<Integer> entityTargets = new ArrayList<Integer>();
            entityTargets.add(tgt.getEntity().getId());
            this.chainTargetEntities(world, source.getCaster(), tgt.getEntity(), radius, arcChance, entityTargets, targetsList);
        }
        if (!world.isClientSide) {
            int color = recipe.getParticleColorOverride() == -1 ? 0xFF000000 | recipe.getHighestAffinity().getColor()[0] << 16 | recipe.getHighestAffinity().getColor()[1] << 8 | recipe.getHighestAffinity().getColor()[2] : recipe.getParticleColorOverride();
            for (int i = 0; i < targetsList.size(); ++i) {
                Vec3 prevPos;
                SpellTarget prev = i == 0 ? new SpellTarget((Entity)source.getCaster()) : targetsList.get(i - 1);
                SpellTarget next = targetsList.get(i);
                if (prev.isBlock()) {
                    prevPos = Vec3.atCenterOf((Vec3i)prev.getBlock());
                } else {
                    prevPos = prev.getEntity().position().add(0.0, (double)(prev.getEntity().getBbHeight() / 2.0f), 0.0);
                    if (i == 0) {
                        Pair<Vec3, Vec3> fwdCross = MathUtils.getLookCross(prev.getLivingEntity());
                        double scale = prev.getEntity().getBbWidth() / 2.0f;
                        if (source.getHand() == InteractionHand.MAIN_HAND && source.getCaster().getMainArm() == HumanoidArm.LEFT || source.getHand() == InteractionHand.OFF_HAND) {
                            scale *= -1.0;
                        }
                        prevPos = prevPos.add(((Vec3)fwdCross.getSecond()).scale(scale));
                    }
                }
                Vec3 nextPos = next.isBlock() ? Vec3.atCenterOf((Vec3i)next.getBlock()) : next.getEntity().position().add(0.0, (double)(next.getEntity().getBbHeight() / 2.0f), 0.0);
                ServerMessageDispatcher.sendParticleSpawn(prevPos.x, prevPos.y, prevPos.z, nextPos.x, nextPos.y, nextPos.z, color, 64.0f, (ResourceKey<Level>)world.dimension(), (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()));
            }
        }
        return targetsList;
    }

    private void chainTargetBlocks(Level world, Block searchType, Direction castDir, BlockPos origin, int range, int max, boolean targetDiagonal, List<Long> searchedPositions, List<SpellTarget> targets) {
        LinkedList queue = Lists.newLinkedList();
        queue.add(new Tuple(origin, 0));
        int searchedBlocks = 0;
        ArrayList<Vec3i> offsets = new ArrayList<Vec3i>();
        for (Direction direction : Direction.values()) {
            offsets.add(direction.getNormal());
        }
        if (targetDiagonal) {
            for (int i = -1; i <= 1; ++i) {
                offsets.add(new Vec3i(-1, i, -1));
                offsets.add(new Vec3i(-1, i, 1));
                offsets.add(new Vec3i(1, i, 1));
                offsets.add(new Vec3i(1, i, -1));
                if (i == 0) continue;
                offsets.add(new Vec3i(1, i, 0));
                offsets.add(new Vec3i(-1, i, 0));
                offsets.add(new Vec3i(0, i, 1));
                offsets.add(new Vec3i(0, i, -1));
            }
        }
        while (!queue.isEmpty()) {
            Tuple tuple = (Tuple)queue.poll();
            BlockPos blockpos = (BlockPos)tuple.getA();
            int idx = (Integer)tuple.getB();
            for (Vec3i offset : offsets) {
                BlockPos relative = blockpos.offset(offset);
                if (searchedPositions.contains(relative.asLong())) continue;
                searchedPositions.add(relative.asLong());
                BlockState blockstate = world.getBlockState(relative);
                if (blockstate.getBlock() == searchType) {
                    targets.add(new SpellTarget(relative, castDir));
                    ++searchedBlocks;
                    queue.add(new Tuple(relative, (idx + 1)));
                }
                if (searchedBlocks < max - 1) continue;
                break;
            }
            if (searchedBlocks < max - 1) continue;
            break;
        }
    }

    private void chainTargetEntities(Level world, LivingEntity caster, Entity origin, int range, int max, List<Integer> searchedEntities, List<SpellTarget> targets) {
        MutableObject lastEntity = new MutableObject(null);
        world.getEntitiesOfClass(Mob.class, new AABB(origin.blockPosition()).inflate((double)range), e -> {
            if (SummonUtils.isSummon((Entity)e) && SummonUtils.getSummoner((LivingEntity)e) == caster) {
                return false;
            }
            if (e.isDeadOrDying() || e.isRemoved()) {
                return false;
            }
            return e.isAlive() && !searchedEntities.contains(e.getId());
        }).stream().sorted((o1, o2) -> {
            double d2;
            double d1 = o1.distanceToSqr(origin);
            return d1 < (d2 = o2.distanceToSqr(origin)) ? -1 : (d1 > d2 ? 1 : 0);
        }).forEach(e -> {
            if (targets.size() < max) {
                lastEntity.setValue(e);
                targets.add(new SpellTarget((Entity)e));
                searchedEntities.add(e.getId());
            }
        });
        if (lastEntity.getValue() != null && searchedEntities.size() < max) {
            this.chainTargetEntities(world, caster, (Entity)lastEntity.getValue(), range, max, searchedEntities, targets);
        }
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 500;
    }
}

