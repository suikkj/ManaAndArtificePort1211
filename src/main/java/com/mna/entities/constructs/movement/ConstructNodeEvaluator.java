/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.PathNavigationRegion
 *  net.minecraft.world.level.block.BaseRailBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.CampfireBlock
 *  net.minecraft.world.level.block.DoorBlock
 *  net.minecraft.world.level.block.FenceGateBlock
 *  net.minecraft.world.level.block.LeavesBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.material.Fluids
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.level.pathfinder.Node
 *  net.minecraft.world.level.pathfinder.NodeEvaluator
 *  net.minecraft.world.level.pathfinder.PathComputationType
 *  net.minecraft.world.level.pathfinder.Target
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.entities.constructs.movement;

import com.google.common.collect.ImmutableSet;
import com.mna.api.tools.MATags;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ConstructNodeEvaluator
extends NodeEvaluator {
    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
    protected float oldWaterCost;
    private float oldWalkableCost;
    private float oldWaterBorderCost;
    private boolean amphibious = true;
    private boolean flying = false;
    private boolean isInWater = false;
    private final Long2ObjectMap<PathType> pathTypeByPosCache = new Long2ObjectOpenHashMap();
    private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap();

    public void prepare(PathNavigationRegion region, Mob mob) {
        super.prepare(region, mob);
        this.isInWater = mob.isInWater();
        if (this.flying && !this.isInWater) {
            this.prepare_flying(mob);
        } else {
            this.prepare_ground(mob);
        }
    }

    public void done() {
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        this.mob.setPathfindingMalus(PathType.WALKABLE, this.oldWalkableCost);
        this.mob.setPathfindingMalus(PathType.WATER_BORDER, this.oldWaterBorderCost);
        this.pathTypeByPosCache.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Nullable
    protected Node getNode(int x, int y, int z) {
        if (this.flying && !this.isInWater) {
            Node node = null;
            PathType pathType = this.getCachedBlockPathType(x, y, z);
            float f = this.mob.getPathfindingMalus(pathType);
            if (f >= 0.0f) {
                node = super.getNode(x, y, z);
                node.type = pathType;
                node.costMalus = Math.max(node.costMalus, f);
                if (pathType == PathType.WALKABLE) {
                    node.costMalus += 1.0f;
                }
            }
            return node;
        }
        return super.getNode(x, y, z);
    }

    @Override
    public net.minecraft.world.level.pathfinder.Target getTarget(double x, double y, double z) {
        return this.getTargetNodeAt(x, y, z);
    }

    public Node getStart() {
        if (this.flying && !this.isInWater) {
            return this.getStart_flying();
        }
        return this.getStart_ground();
    }

    public Target getGoal(double x, double y, double z) {
        if (this.flying && !this.isInWater) {
            return new Target(super.getNode(Mth.floor((double)x), Mth.floor((double)y), Mth.floor((double)z)));
        }
        return new Target(this.getNode(Mth.floor((double)x), Mth.floor((double)y), Mth.floor((double)z)));
    }

    public int getNeighbors(Node[] p_77640_, Node current) {
        if (this.flying && !this.isInWater) {
            return this.getNeighbors_flying(p_77640_, current);
        }
        return this.getNeighbors_ground(p_77640_, current);
    }

    public PathType getBlockPathType(BlockGetter blockGetter, int x, int y, int z, Mob mob) {
        if (this.flying && !this.isInWater) {
            return this.getBlockPathType_flying(blockGetter, x, y, z, mob);
        }
        return this.getBlockPathType_ground(blockGetter, x, y, z, mob);
    }

    public PathType getBlockPathType(BlockGetter blockGetter, int x, int y, int z) {
        return ConstructNodeEvaluator.getBlockPathTypeStatic(blockGetter, new BlockPos.MutableBlockPos(x, y, z), this.mob);
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public void setAmphibious(boolean amphibious) {
        this.amphibious = amphibious;
    }

    protected boolean isNeighborValid(@Nullable Node neighbor, Node current) {
        return neighbor != null && !neighbor.closed && (neighbor.costMalus >= 0.0f || current.costMalus < 0.0f);
    }

    private boolean hasMalus(@Nullable Node node) {
        return node != null && node.costMalus >= 0.0f;
    }

    private boolean isOpen(@Nullable Node node) {
        return node != null && !node.closed;
    }

    protected boolean isDiagonalValid(Node pRoot, @Nullable Node pXNode, @Nullable Node pZNode, @Nullable Node pDiagonal) {
        if (pDiagonal != null && pZNode != null && pXNode != null) {
            if (pDiagonal.closed) {
                return false;
            }
            if (pZNode.y <= pRoot.y && pXNode.y <= pRoot.y) {
                if (pXNode.type != PathType.WALKABLE_DOOR && pZNode.type != PathType.WALKABLE_DOOR && pDiagonal.type != PathType.WALKABLE_DOOR) {
                    boolean flag = pZNode.type == PathType.FENCE && pXNode.type == PathType.FENCE && (double)this.mob.getBbWidth() < 0.5;
                    return pDiagonal.costMalus >= 0.0f && (pZNode.y < pRoot.y || pZNode.costMalus >= 0.0f || flag) && (pXNode.y < pRoot.y || pXNode.costMalus >= 0.0f || flag);
                }
                return false;
            }
            return false;
        }
        return false;
    }

    private boolean canReachWithoutCollision(Node target) {
        AABB aabb = this.mob.getBoundingBox();
        Vec3 vec3 = new Vec3((double)target.x - this.mob.getX() + aabb.getXsize() / 2.0, (double)target.y - this.mob.getY() + aabb.getYsize() / 2.0, (double)target.z - this.mob.getZ() + aabb.getZsize() / 2.0);
        int i = Mth.ceil((double)(vec3.length() / aabb.getSize()));
        vec3 = vec3.scale((double)(1.0f / (float)i));
        for (int j = 1; j <= i; ++j) {
            if (!this.hasCollisions(aabb = aabb.move(vec3))) continue;
            return false;
        }
        return true;
    }

    protected double getFloorLevel(BlockPos pos) {
        return (this.canFloat() || this.isAmphibious()) && this.mob.level().getFluidState(pos).is(FluidTags.WATER) ? (double)pos.getY() + 0.5 : ConstructNodeEvaluator.getFloorLevel((BlockGetter)this.mob.level(), pos);
    }

    protected boolean isAmphibious() {
        return this.amphibious;
    }

    @Nullable
    protected Node findAcceptedNode(int pX, int pY, int pZ, int pVerticalDeltaLimit, double pNodeFloorLevel, Direction pDirection, PathType pPathType) {
        Node node = null;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        double d0 = this.getFloorLevel((BlockPos)blockpos$mutableblockpos.set(pX, pY, pZ));
        if (d0 - pNodeFloorLevel > this.getMobJumpHeight()) {
            return null;
        }
        PathType blockpathtypes = this.getCachedBlockType(this.mob, pX, pY, pZ);
        float f = this.mob.getPathfindingMalus(blockpathtypes);
        double d1 = (double)this.mob.getBbWidth() / 2.0;
        if (f >= 0.0f) {
            node = this.getNodeAndUpdateCostToMax(pX, pY, pZ, blockpathtypes, f);
        }
        if (ConstructNodeEvaluator.doesBlockHavePartialCollision(pPathType) && node != null && node.costMalus >= 0.0f && !this.canReachWithoutCollision(node)) {
            node = null;
        }
        if (!(blockpathtypes == PathType.WALKABLE || this.isAmphibious() && blockpathtypes == PathType.WATER)) {
            double d3;
            double d2;
            AABB aabb;
            if ((node == null || node.costMalus < 0.0f) && pVerticalDeltaLimit > 0 && (blockpathtypes != PathType.FENCE || this.canWalkOverFences()) && blockpathtypes != PathType.UNPASSABLE_RAIL && blockpathtypes != PathType.TRAPDOOR && blockpathtypes != PathType.POWDER_SNOW && (node = this.findAcceptedNode(pX, pY + 1, pZ, pVerticalDeltaLimit - 1, pNodeFloorLevel, pDirection, pPathType)) != null && (node.type == PathType.OPEN || node.type == PathType.WALKABLE) && this.mob.getBbWidth() < 1.0f && this.hasCollisions(aabb = new AABB((d2 = (double)(pX - pDirection.getStepX()) + 0.5) - d1, this.getFloorLevel((BlockPos)blockpos$mutableblockpos.set(d2, (double)(pY + 1), d3 = (double)(pZ - pDirection.getStepZ()) + 0.5)) + 0.001, d3 - d1, d2 + d1, (double)this.mob.getBbHeight() + this.getFloorLevel((BlockPos)blockpos$mutableblockpos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002, d3 + d1))) {
                node = null;
            }
            if (!this.isAmphibious() && blockpathtypes == PathType.WATER && !this.canFloat()) {
                if (this.getCachedBlockType(this.mob, pX, pY - 1, pZ) != PathType.WATER) {
                    return node;
                }
                while (pY > this.mob.level().getMinBuildHeight()) {
                    if ((blockpathtypes = this.getCachedBlockType(this.mob, pX, --pY, pZ)) != PathType.WATER) {
                        return node;
                    }
                    node = this.getNodeAndUpdateCostToMax(pX, pY, pZ, blockpathtypes, this.mob.getPathfindingMalus(blockpathtypes));
                }
            }
            if (blockpathtypes == PathType.OPEN) {
                boolean j = false;
                int i = pY;
                while (blockpathtypes == PathType.OPEN) {
                    if (--pY < this.mob.level().getMinBuildHeight()) {
                        return this.getBlockedNode(pX, i, pZ);
                    }
                    blockpathtypes = this.getCachedBlockType(this.mob, pX, pY, pZ);
                    f = this.mob.getPathfindingMalus(blockpathtypes);
                    if (blockpathtypes != PathType.OPEN && f >= 0.0f) {
                        node = this.getNodeAndUpdateCostToMax(pX, pY, pZ, blockpathtypes, f);
                        break;
                    }
                    if (!(f < 0.0f)) continue;
                    return this.getBlockedNode(pX, pY, pZ);
                }
            }
            if (ConstructNodeEvaluator.doesBlockHavePartialCollision(blockpathtypes) && node == null) {
                node = this.getNode(pX, pY, pZ);
                node.closed = true;
                node.type = blockpathtypes;
                node.costMalus = blockpathtypes.getMalus();
            }
            return node;
        }
        return node;
    }

    private double getMobJumpHeight() {
        return Math.max(1.125, (double)this.mob.maxUpStep());
    }

    private Node getNodeAndUpdateCostToMax(int pX, int pY, int pZ, PathType pType, float pCostMalus) {
        Node node = this.getNode(pX, pY, pZ);
        node.type = pType;
        node.costMalus = Math.max(node.costMalus, pCostMalus);
        return node;
    }

    private Node getBlockedNode(int pX, int pY, int pZ) {
        Node node = this.getNode(pX, pY, pZ);
        node.type = PathType.BLOCKED;
        node.costMalus = -1.0f;
        return node;
    }

    private boolean hasCollisions(AABB pBoundingBox) {
        return this.collisionCache.computeIfAbsent(pBoundingBox, p_192973_ -> !this.mob.level().noCollision((Entity)this.mob, pBoundingBox));
    }

    public PathType getBlockPathTypes(BlockGetter pLevel, int pXOffset, int pYOffset, int pZOffset, EnumSet<PathType> pOutput, PathType pFallbackPathType, BlockPos pPos) {
        for (int i = 0; i < this.entityWidth; ++i) {
            for (int j = 0; j < this.entityHeight; ++j) {
                for (int k = 0; k < this.entityDepth; ++k) {
                    int l = i + pXOffset;
                    int i1 = j + pYOffset;
                    int j1 = k + pZOffset;
                    PathType blockpathtypes = this.getBlockPathType(pLevel, l, i1, j1);
                    blockpathtypes = this.evaluateBlockPathType(pLevel, pPos, blockpathtypes);
                    if (i == 0 && j == 0 && k == 0) {
                        pFallbackPathType = blockpathtypes;
                    }
                    pOutput.add(blockpathtypes);
                }
            }
        }
        return pFallbackPathType;
    }

    protected PathType evaluateBlockPathType(BlockGetter pLevel, BlockPos pPos, PathType pPathTypes) {
        boolean flag = this.canPassDoors();
        if (pPathTypes == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && flag) {
            pPathTypes = PathType.WALKABLE_DOOR;
        }
        if (pPathTypes == PathType.DOOR_OPEN && !flag) {
            pPathTypes = PathType.BLOCKED;
        }
        if (pPathTypes == PathType.RAIL && !(pLevel.getBlockState(pPos).getBlock() instanceof BaseRailBlock) && !(pLevel.getBlockState(pPos.below()).getBlock() instanceof BaseRailBlock)) {
            pPathTypes = PathType.UNPASSABLE_RAIL;
        }
        return pPathTypes;
    }

    private PathType getBlockPathType(Mob mob, BlockPos position) {
        return this.getCachedBlockType(mob, position.getX(), position.getY(), position.getZ());
    }

    private PathType getCachedBlockPathType(int pX, int pY, int pZ) {
        return (PathType)this.pathTypeByPosCache.computeIfAbsent(BlockPos.asLong((int)pX, (int)pY, (int)pZ), p_265010_ -> this.getBlockPathType((BlockGetter)this.mob.level(), pX, pY, pZ, this.mob));
    }

    protected PathType getCachedBlockType(Mob pEntity, int pX, int pY, int pZ) {
        return (PathType)this.pathTypeByPosCache.computeIfAbsent(BlockPos.asLong((int)pX, (int)pY, (int)pZ), p_265015_ -> this.getBlockPathType((BlockGetter)this.mob.level(), pX, pY, pZ, pEntity));
    }

    protected void prepare_ground(Mob mob) {
        this.oldWalkableCost = mob.getPathfindingMalus(PathType.WALKABLE);
        mob.setPathfindingMalus(PathType.WALKABLE, 0.0f);
        this.oldWaterBorderCost = mob.getPathfindingMalus(PathType.WATER_BORDER);
        mob.setPathfindingMalus(PathType.WATER_BORDER, 4.0f);
        this.oldWaterCost = mob.getPathfindingMalus(PathType.WATER);
        mob.setPathfindingMalus(PathType.WATER, 6.0f);
    }

    protected void prepare_flying(Mob mob) {
        this.pathTypeByPosCache.clear();
        this.oldWaterBorderCost = mob.getPathfindingMalus(PathType.WATER_BORDER);
        mob.setPathfindingMalus(PathType.WATER_BORDER, 4.0f);
        this.oldWaterCost = mob.getPathfindingMalus(PathType.WATER);
        mob.setPathfindingMalus(PathType.WATER, 6.0f);
    }

    protected Node getStart_ground() {
        int i;
        BlockPos.MutableBlockPos blockpos$mutableblockpos;
        block11: {
            blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            i = this.mob.getBlockY();
            BlockState blockstate = this.mob.level().getBlockState((BlockPos)blockpos$mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ()));
            if (!this.mob.canStandOnFluid(blockstate.getFluidState())) {
                if (this.canFloat() && this.mob.isInWater()) {
                    while (true) {
                        if (!blockstate.is(Blocks.WATER) && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
                            --i;
                            break block11;
                        }
                        blockstate = this.mob.level().getBlockState((BlockPos)blockpos$mutableblockpos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
                    }
                }
                if (this.mob.onGround()) {
                    i = Mth.floor((double)(this.mob.getY() + 0.5));
                } else {
                    BlockPos blockpos = this.mob.blockPosition();
                    while ((this.mob.level().getBlockState(blockpos).isAir() || this.mob.level().getBlockState(blockpos).isPathfindable(PathComputationType.LAND)) && blockpos.getY() > this.mob.level().getMinBuildHeight()) {
                        blockpos = blockpos.below();
                    }
                    i = blockpos.above().getY();
                }
            } else {
                while (this.mob.canStandOnFluid(blockstate.getFluidState())) {
                    blockstate = this.mob.level().getBlockState((BlockPos)blockpos$mutableblockpos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
                }
                --i;
            }
        }
        BlockPos blockpos1 = this.mob.blockPosition();
        if (!this.canStartAt((BlockPos)blockpos$mutableblockpos.set(blockpos1.getX(), i, blockpos1.getZ()))) {
            AABB aabb = this.mob.getBoundingBox();
            if (this.canStartAt((BlockPos)blockpos$mutableblockpos.set(aabb.minX, (double)i, aabb.minZ)) || this.canStartAt((BlockPos)blockpos$mutableblockpos.set(aabb.minX, (double)i, aabb.maxZ)) || this.canStartAt((BlockPos)blockpos$mutableblockpos.set(aabb.maxX, (double)i, aabb.minZ)) || this.canStartAt((BlockPos)blockpos$mutableblockpos.set(aabb.maxX, (double)i, aabb.maxZ))) {
                return this.getStartNode((BlockPos)blockpos$mutableblockpos);
            }
        }
        return this.getStartNode(new BlockPos(blockpos1.getX(), i, blockpos1.getZ()));
    }

    protected Node getStartNode(BlockPos pPos) {
        Node node = this.getNode(pPos);
        node.type = this.getBlockPathType(this.mob, node.asBlockPos());
        node.costMalus = this.mob.getPathfindingMalus(node.type);
        return node;
    }

    protected boolean canStartAt(BlockPos pPos) {
        PathType blockpathtypes = this.getBlockPathType(this.mob, pPos);
        return blockpathtypes != PathType.OPEN && this.mob.getPathfindingMalus(blockpathtypes) >= 0.0f;
    }

    protected Node getStart_flying() {
        BlockPos blockpos1;
        PathType blockpathtypes1;
        int i;
        if (this.canFloat() && this.mob.isInWater()) {
            i = this.mob.getBlockY();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(this.mob.getX(), (double)i, this.mob.getZ());
            BlockState blockstate = this.mob.level().getBlockState((BlockPos)blockpos$mutableblockpos);
            while (blockstate.is(Blocks.WATER)) {
                blockpos$mutableblockpos.set(this.mob.getX(), (double)(++i), this.mob.getZ());
                blockstate = this.mob.level().getBlockState((BlockPos)blockpos$mutableblockpos);
            }
        } else {
            i = Mth.floor((double)(this.mob.getY() + 0.5));
        }
        if (this.mob.getPathfindingMalus(blockpathtypes1 = this.getCachedBlockPathType((blockpos1 = this.mob.blockPosition()).getX(), i, blockpos1.getZ())) < 0.0f) {
            for (BlockPos blockpos : ImmutableSet.of(BlockPos.containing((double)this.mob.getBoundingBox().minX, (double)i, (double)this.mob.getBoundingBox().minZ), BlockPos.containing((double)this.mob.getBoundingBox().minX, (double)i, (double)this.mob.getBoundingBox().maxZ), BlockPos.containing((double)this.mob.getBoundingBox().maxX, (double)i, (double)this.mob.getBoundingBox().minZ), BlockPos.containing((double)this.mob.getBoundingBox().maxX, (double)i, (double)this.mob.getBoundingBox().maxZ))) {
                PathType blockpathtypes = this.getCachedBlockPathType(blockpos1.getX(), i, blockpos1.getZ());
                if (!(this.mob.getPathfindingMalus(blockpathtypes) >= 0.0f)) continue;
                return super.getNode(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            }
        }
        return super.getNode(blockpos1.getX(), i, blockpos1.getZ());
    }

    protected int getNeighbors_ground(Node[] neighbors, Node current) {
        Node node7;
        Node node6;
        Node node5;
        Node node4;
        Node node3;
        Node node2;
        Node node1;
        double d0;
        Node node;
        int i = 0;
        int j = 0;
        PathType blockpathtypes = this.getCachedBlockType(this.mob, current.x, current.y + 1, current.z);
        PathType blockpathtypes1 = this.getCachedBlockType(this.mob, current.x, current.y, current.z);
        if (this.mob.getPathfindingMalus(blockpathtypes) >= 0.0f && blockpathtypes1 != PathType.STICKY_HONEY) {
            j = Mth.floor((float)Math.max(1.0f, this.mob.maxUpStep()));
        }
        if (this.isNeighborValid(node = this.findAcceptedNode(current.x, current.y, current.z + 1, j, d0 = this.getFloorLevel(new BlockPos(current.x, current.y, current.z)), Direction.SOUTH, blockpathtypes1), current)) {
            neighbors[i++] = node;
        }
        if (this.isNeighborValid(node1 = this.findAcceptedNode(current.x - 1, current.y, current.z, j, d0, Direction.WEST, blockpathtypes1), current)) {
            neighbors[i++] = node1;
        }
        if (this.isNeighborValid(node2 = this.findAcceptedNode(current.x + 1, current.y, current.z, j, d0, Direction.EAST, blockpathtypes1), current)) {
            neighbors[i++] = node2;
        }
        if (this.isNeighborValid(node3 = this.findAcceptedNode(current.x, current.y, current.z - 1, j, d0, Direction.NORTH, blockpathtypes1), current)) {
            neighbors[i++] = node3;
        }
        if (this.isDiagonalValid(current, node1, node3, node4 = this.findAcceptedNode(current.x - 1, current.y, current.z - 1, j, d0, Direction.NORTH, blockpathtypes1))) {
            neighbors[i++] = node4;
        }
        if (this.isDiagonalValid(current, node2, node3, node5 = this.findAcceptedNode(current.x + 1, current.y, current.z - 1, j, d0, Direction.NORTH, blockpathtypes1))) {
            neighbors[i++] = node5;
        }
        if (this.isDiagonalValid(current, node1, node, node6 = this.findAcceptedNode(current.x - 1, current.y, current.z + 1, j, d0, Direction.SOUTH, blockpathtypes1))) {
            neighbors[i++] = node6;
        }
        if (this.isDiagonalValid(current, node2, node, node7 = this.findAcceptedNode(current.x + 1, current.y, current.z + 1, j, d0, Direction.SOUTH, blockpathtypes1))) {
            neighbors[i++] = node7;
        }
        return i;
    }

    protected int getNeighbors_flying(Node[] neighbors, Node current) {
        Node xNegYNegZPos;
        Node xNegYNegZNeg;
        Node xPosYNegZPos;
        Node xPosYNegZNeg;
        Node xNegYPosZPos;
        Node xNegYPosZNeg;
        Node xPosYPosZPos;
        Node xposYPosZNeg;
        Node xNegZPos;
        Node xNegZNeg;
        Node xPosZPos;
        Node xPosZNeg;
        Node yNegZNeg;
        Node xPosYNeg;
        Node xNegYNeg;
        Node yNegZPos;
        Node yPosZNeg;
        Node xPosYPos;
        Node xNegYPos;
        Node yPoszPos;
        Node yNeg;
        Node yPos;
        Node zNeg;
        Node xPos;
        Node xNeg;
        int numOpenNeighbors = 0;
        Node zPos = this.getNode(current.x, current.y, current.z + 1);
        if (this.isOpen(zPos)) {
            neighbors[numOpenNeighbors++] = zPos;
        }
        if (this.isOpen(xNeg = this.getNode(current.x - 1, current.y, current.z))) {
            neighbors[numOpenNeighbors++] = xNeg;
        }
        if (this.isOpen(xPos = this.getNode(current.x + 1, current.y, current.z))) {
            neighbors[numOpenNeighbors++] = xPos;
        }
        if (this.isOpen(zNeg = this.getNode(current.x, current.y, current.z - 1))) {
            neighbors[numOpenNeighbors++] = zNeg;
        }
        if (this.isOpen(yPos = this.getNode(current.x, current.y + 1, current.z))) {
            neighbors[numOpenNeighbors++] = yPos;
        }
        if (this.isOpen(yNeg = this.getNode(current.x, current.y - 1, current.z))) {
            neighbors[numOpenNeighbors++] = yNeg;
        }
        if (this.isOpen(yPoszPos = this.getNode(current.x, current.y + 1, current.z + 1)) && this.hasMalus(zPos) && this.hasMalus(yPos)) {
            neighbors[numOpenNeighbors++] = yPoszPos;
        }
        if (this.isOpen(xNegYPos = this.getNode(current.x - 1, current.y + 1, current.z)) && this.hasMalus(xNeg) && this.hasMalus(yPos)) {
            neighbors[numOpenNeighbors++] = xNegYPos;
        }
        if (this.isOpen(xPosYPos = this.getNode(current.x + 1, current.y + 1, current.z)) && this.hasMalus(xPos) && this.hasMalus(yPos)) {
            neighbors[numOpenNeighbors++] = xPosYPos;
        }
        if (this.isOpen(yPosZNeg = this.getNode(current.x, current.y + 1, current.z - 1)) && this.hasMalus(zNeg) && this.hasMalus(yPos)) {
            neighbors[numOpenNeighbors++] = yPosZNeg;
        }
        if (this.isOpen(yNegZPos = this.getNode(current.x, current.y - 1, current.z + 1)) && this.hasMalus(zPos) && this.hasMalus(yNeg)) {
            neighbors[numOpenNeighbors++] = yNegZPos;
        }
        if (this.isOpen(xNegYNeg = this.getNode(current.x - 1, current.y - 1, current.z)) && this.hasMalus(xNeg) && this.hasMalus(yNeg)) {
            neighbors[numOpenNeighbors++] = xNegYNeg;
        }
        if (this.isOpen(xPosYNeg = this.getNode(current.x + 1, current.y - 1, current.z)) && this.hasMalus(xPos) && this.hasMalus(yNeg)) {
            neighbors[numOpenNeighbors++] = xPosYNeg;
        }
        if (this.isOpen(yNegZNeg = this.getNode(current.x, current.y - 1, current.z - 1)) && this.hasMalus(zNeg) && this.hasMalus(yNeg)) {
            neighbors[numOpenNeighbors++] = yNegZNeg;
        }
        if (this.isOpen(xPosZNeg = this.getNode(current.x + 1, current.y, current.z - 1)) && this.hasMalus(zNeg) && this.hasMalus(xPos)) {
            neighbors[numOpenNeighbors++] = xPosZNeg;
        }
        if (this.isOpen(xPosZPos = this.getNode(current.x + 1, current.y, current.z + 1)) && this.hasMalus(zPos) && this.hasMalus(xPos)) {
            neighbors[numOpenNeighbors++] = xPosZPos;
        }
        if (this.isOpen(xNegZNeg = this.getNode(current.x - 1, current.y, current.z - 1)) && this.hasMalus(zNeg) && this.hasMalus(xNeg)) {
            neighbors[numOpenNeighbors++] = xNegZNeg;
        }
        if (this.isOpen(xNegZPos = this.getNode(current.x - 1, current.y, current.z + 1)) && this.hasMalus(zPos) && this.hasMalus(xNeg)) {
            neighbors[numOpenNeighbors++] = xNegZPos;
        }
        if (this.isOpen(xposYPosZNeg = this.getNode(current.x + 1, current.y + 1, current.z - 1)) && this.hasMalus(xPosZNeg) && this.hasMalus(zNeg) && this.hasMalus(xPos) && this.hasMalus(yPos) && this.hasMalus(yPosZNeg) && this.hasMalus(xPosYPos)) {
            neighbors[numOpenNeighbors++] = xposYPosZNeg;
        }
        if (this.isOpen(xPosYPosZPos = this.getNode(current.x + 1, current.y + 1, current.z + 1)) && this.hasMalus(xPosZPos) && this.hasMalus(zPos) && this.hasMalus(xPos) && this.hasMalus(yPos) && this.hasMalus(yPoszPos) && this.hasMalus(xPosYPos)) {
            neighbors[numOpenNeighbors++] = xPosYPosZPos;
        }
        if (this.isOpen(xNegYPosZNeg = this.getNode(current.x - 1, current.y + 1, current.z - 1)) && this.hasMalus(xNegZNeg) && this.hasMalus(zNeg) && this.hasMalus(xNeg) && this.hasMalus(yPos) && this.hasMalus(yPosZNeg) && this.hasMalus(xNegYPos)) {
            neighbors[numOpenNeighbors++] = xNegYPosZNeg;
        }
        if (this.isOpen(xNegYPosZPos = this.getNode(current.x - 1, current.y + 1, current.z + 1)) && this.hasMalus(xNegZPos) && this.hasMalus(zPos) && this.hasMalus(xNeg) && this.hasMalus(yPos) && this.hasMalus(yPoszPos) && this.hasMalus(xNegYPos)) {
            neighbors[numOpenNeighbors++] = xNegYPosZPos;
        }
        if (this.isOpen(xPosYNegZNeg = this.getNode(current.x + 1, current.y - 1, current.z - 1)) && this.hasMalus(xPosZNeg) && this.hasMalus(zNeg) && this.hasMalus(xPos) && this.hasMalus(yNeg) && this.hasMalus(yNegZNeg) && this.hasMalus(xPosYNeg)) {
            neighbors[numOpenNeighbors++] = xPosYNegZNeg;
        }
        if (this.isOpen(xPosYNegZPos = this.getNode(current.x + 1, current.y - 1, current.z + 1)) && this.hasMalus(xPosZPos) && this.hasMalus(zPos) && this.hasMalus(xPos) && this.hasMalus(yNeg) && this.hasMalus(yNegZPos) && this.hasMalus(xPosYNeg)) {
            neighbors[numOpenNeighbors++] = xPosYNegZPos;
        }
        if (this.isOpen(xNegYNegZNeg = this.getNode(current.x - 1, current.y - 1, current.z - 1)) && this.hasMalus(xNegZNeg) && this.hasMalus(zNeg) && this.hasMalus(xNeg) && this.hasMalus(yNeg) && this.hasMalus(yNegZNeg) && this.hasMalus(xNegYNeg)) {
            neighbors[numOpenNeighbors++] = xNegYNegZNeg;
        }
        if (this.isOpen(xNegYNegZPos = this.getNode(current.x - 1, current.y - 1, current.z + 1)) && this.hasMalus(xNegZPos) && this.hasMalus(zPos) && this.hasMalus(xNeg) && this.hasMalus(yNeg) && this.hasMalus(yNegZPos) && this.hasMalus(xNegYNeg)) {
            neighbors[numOpenNeighbors++] = xNegYNegZPos;
        }
        return numOpenNeighbors;
    }

    protected PathType getBlockPathType_ground(BlockGetter blockGetter, int x, int y, int z, Mob mob) {
        EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);
        PathType blockpathtypes = PathType.BLOCKED;
        blockpathtypes = this.getBlockPathTypes(blockGetter, x, y, z, enumset, blockpathtypes, mob.blockPosition());
        if (enumset.contains(PathType.FENCE)) {
            return PathType.FENCE;
        }
        if (enumset.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        }
        PathType blockpathtypes1 = PathType.BLOCKED;
        for (PathType blockpathtypes2 : enumset) {
            if (mob.getPathfindingMalus(blockpathtypes2) < 0.0f) {
                return blockpathtypes2;
            }
            if (!(mob.getPathfindingMalus(blockpathtypes2) >= mob.getPathfindingMalus(blockpathtypes1))) continue;
            blockpathtypes1 = blockpathtypes2;
        }
        return blockpathtypes == PathType.OPEN && mob.getPathfindingMalus(blockpathtypes1) == 0.0f && this.entityWidth <= 1 ? PathType.OPEN : blockpathtypes1;
    }

    protected PathType getBlockPathType_flying(BlockGetter pLevel, int pX, int pY, int pZ, Mob pMob) {
        EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);
        PathType blockpathtypes = PathType.BLOCKED;
        BlockPos blockpos = pMob.blockPosition();
        blockpathtypes = this.getBlockPathTypes(pLevel, pX, pY, pZ, enumset, blockpathtypes, blockpos);
        if (enumset.contains(PathType.FENCE)) {
            return PathType.FENCE;
        }
        PathType blockpathtypes1 = PathType.BLOCKED;
        for (PathType blockpathtypes2 : enumset) {
            if (pMob.getPathfindingMalus(blockpathtypes2) < 0.0f) {
                return blockpathtypes2;
            }
            if (!(pMob.getPathfindingMalus(blockpathtypes2) >= pMob.getPathfindingMalus(blockpathtypes1))) continue;
            blockpathtypes1 = blockpathtypes2;
        }
        return blockpathtypes == PathType.OPEN && pMob.getPathfindingMalus(blockpathtypes1) == 0.0f ? PathType.OPEN : blockpathtypes1;
    }

    public static PathType getBlockPathTypeStatic(BlockGetter getter, BlockPos.MutableBlockPos position, Mob mob) {
        int i = position.getX();
        int j = position.getY();
        int k = position.getZ();
        PathType blockPathType = ConstructNodeEvaluator.getBlockPathTypeRaw(getter, (BlockPos)position);
        if (blockPathType == PathType.OPEN && j >= getter.getMinBuildHeight() + 1) {
            PathType pathTypeBelow = ConstructNodeEvaluator.getBlockPathTypeRaw(getter, (BlockPos)position.set(i, j - 1, k));
            PathType blockPathTypes = blockPathType = pathTypeBelow != PathType.WALKABLE && pathTypeBelow != PathType.OPEN && pathTypeBelow != PathType.WATER && pathTypeBelow != PathType.LAVA ? PathType.WALKABLE : PathType.OPEN;
            if (pathTypeBelow == PathType.DAMAGE_FIRE) {
                blockPathType = PathType.DAMAGE_FIRE;
            }
            if (pathTypeBelow == PathType.DAMAGE_OTHER) {
                blockPathType = PathType.DAMAGE_OTHER;
            }
            if (pathTypeBelow == PathType.STICKY_HONEY) {
                blockPathType = PathType.STICKY_HONEY;
            }
            if (pathTypeBelow == PathType.POWDER_SNOW) {
                blockPathType = PathType.DANGER_POWDER_SNOW;
            }
            if (pathTypeBelow == PathType.DAMAGE_CAUTIOUS) {
                blockPathType = PathType.DAMAGE_CAUTIOUS;
            }
        }
        if (blockPathType == PathType.WALKABLE) {
            blockPathType = ConstructNodeEvaluator.checkNeighbourBlocks(getter, position.set(i, j, k), blockPathType);
        }
        return blockPathType;
    }

    public static PathType checkNeighbourBlocks(BlockGetter pLevel, BlockPos.MutableBlockPos pCenterPos, PathType pNodeType) {
        int x = pCenterPos.getX();
        int y = pCenterPos.getY();
        int z = pCenterPos.getZ();
        for (int xOffset = -1; xOffset <= 1; ++xOffset) {
            for (int yOffset = -1; yOffset <= 1; ++yOffset) {
                for (int zOffset = -1; zOffset <= 1; ++zOffset) {
                    if (xOffset == 0 && zOffset == 0) continue;
                    pCenterPos.set(x + xOffset, y + yOffset, z + zOffset);
                    BlockState blockstate = pLevel.getBlockState((BlockPos)pCenterPos);
                    PathType blockPathType = blockstate.getAdjacentBlockPathType(pLevel, (BlockPos)pCenterPos, null, pNodeType);
                    if (blockPathType != null) {
                        return blockPathType;
                    }
                    FluidState fluidState = blockstate.getFluidState();
                    PathType fluidPathType = fluidState.getAdjacentBlockPathType(pLevel, (BlockPos)pCenterPos, null, pNodeType);
                    if (fluidPathType != null) {
                        return fluidPathType;
                    }
                    if (blockstate.is(Blocks.CACTUS) || blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                        return PathType.DANGER_OTHER;
                    }
                    if (ConstructNodeEvaluator.isBurningBlock(blockstate)) {
                        return PathType.DANGER_FIRE;
                    }
                    if (pLevel.getFluidState((BlockPos)pCenterPos).is(FluidTags.WATER)) {
                        return PathType.WATER_BORDER;
                    }
                    if (!blockstate.is(Blocks.WITHER_ROSE) && !blockstate.is(Blocks.POINTED_DRIPSTONE)) continue;
                    return PathType.DAMAGE_CAUTIOUS;
                }
            }
        }
        return pNodeType;
    }

    protected static PathType getBlockPathTypeRaw(BlockGetter pLevel, BlockPos pPos) {
        BlockState blockstate = pLevel.getBlockState(pPos);
        PathType type = blockstate.getBlockPathType(pLevel, pPos, null);
        if (type != null) {
            return type;
        }
        Block block = blockstate.getBlock();
        if (blockstate.isAir() || MATags.isBlockIn(block, MATags.Blocks.CONSTRUCT_WALK_THROUGH)) {
            return PathType.OPEN;
        }
        if (!(blockstate.is(BlockTags.TRAPDOORS) || blockstate.is(Blocks.LILY_PAD) || blockstate.is(Blocks.BIG_DRIPLEAF))) {
            if (blockstate.is(Blocks.POWDER_SNOW)) {
                return PathType.POWDER_SNOW;
            }
            if (!blockstate.is(Blocks.CACTUS) && !blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                if (blockstate.is(Blocks.HONEY_BLOCK)) {
                    return PathType.STICKY_HONEY;
                }
                if (blockstate.is(Blocks.COCOA)) {
                    return PathType.COCOA;
                }
                if (!blockstate.is(Blocks.WITHER_ROSE) && !blockstate.is(Blocks.POINTED_DRIPSTONE)) {
                    FluidState fluidstate = pLevel.getFluidState(pPos);
                    PathType nonLoggableFluidPathType = fluidstate.getBlockPathType(pLevel, pPos, null, false);
                    if (nonLoggableFluidPathType != null) {
                        return nonLoggableFluidPathType;
                    }
                    if (fluidstate.is(FluidTags.LAVA)) {
                        return PathType.LAVA;
                    }
                    if (ConstructNodeEvaluator.isBurningBlock(blockstate)) {
                        return PathType.DAMAGE_FIRE;
                    }
                    if (block instanceof DoorBlock) {
                        DoorBlock doorblock = (DoorBlock)block;
                        if (((Boolean)blockstate.getValue((Property)DoorBlock.OPEN)).booleanValue()) {
                            return PathType.DOOR_OPEN;
                        }
                        return doorblock.type().canOpenByHand() ? PathType.DOOR_WOOD_CLOSED : PathType.DOOR_IRON_CLOSED;
                    }
                    if (block instanceof BaseRailBlock) {
                        return PathType.RAIL;
                    }
                    if (block instanceof LeavesBlock) {
                        return PathType.LEAVES;
                    }
                    if (!(blockstate.is(BlockTags.FENCES) || blockstate.is(BlockTags.WALLS) || block instanceof FenceGateBlock && !((Boolean)blockstate.getValue((Property)FenceGateBlock.OPEN)).booleanValue())) {
                        if (!blockstate.isPathfindable(PathComputationType.LAND)) {
                            return PathType.BLOCKED;
                        }
                        PathType loggableFluidPathType = fluidstate.getBlockPathType(pLevel, pPos, null, true);
                        if (loggableFluidPathType != null) {
                            return loggableFluidPathType;
                        }
                        return fluidstate.is(FluidTags.WATER) ? PathType.WATER : PathType.OPEN;
                    }
                    return PathType.FENCE;
                }
                return PathType.DAMAGE_CAUTIOUS;
            }
            return PathType.DAMAGE_OTHER;
        }
        return PathType.TRAPDOOR;
    }

    public static boolean isBurningBlock(BlockState state) {
        return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire((BlockState)state) || state.is(Blocks.LAVA_CAULDRON);
    }

    public static double getFloorLevel(BlockGetter blockGetter, BlockPos pos) {
        BlockPos below = pos.below();
        VoxelShape voxelshape = blockGetter.getBlockState(below).getCollisionShape(blockGetter, below);
        return (double)below.getY() + (voxelshape.isEmpty() ? 0.0 : voxelshape.max(Direction.Axis.Y));
    }

    private static boolean doesBlockHavePartialCollision(PathType pBlockPathType) {
        return pBlockPathType == PathType.FENCE || pBlockPathType == PathType.DOOR_WOOD_CLOSED || pBlockPathType == PathType.DOOR_IRON_CLOSED;
    }

    @Override
    public PathType getPathType(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z);
    }

    public net.minecraft.world.level.pathfinder.PathType getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext ctx, int x, int y, int z, net.minecraft.world.entity.Mob mob) {
        return net.minecraft.world.level.pathfinder.PathType.OPEN;
    }
}

