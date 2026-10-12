/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.blocks.tileentities;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.SlipstreamGeneratorBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SlipstreamGeneratorTile
extends BlockEntity {
    private ArrayList<Entity> levitatingEntities = new ArrayList();
    private int updateTicks = 1;
    private Direction levitateDir = null;
    private boolean playersOnly = true;
    private AABB bb = null;
    private static final int EFFECT_HEIGHT = 50;
    private static final int MAX_STACK = 5;
    private static final float VELOCITY_SCALE = 0.5999999f;
    private static final float STOP_THRESHOLD = 0.2f;
    private static final float VELOCITY_ADD = 0.2f;
    private static final float PLAYER_VELOCITY_ADD = 0.75f;

    public SlipstreamGeneratorTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.SLIPSTREAM_GENERATOR.get(), pos, state);
    }

    private Direction getLevitateDir() {
        if (this.levitateDir == null) {
            BlockState state = this.getBlockState();
            this.levitateDir = state.hasProperty((Property)SlipstreamGeneratorBlock.FACING) ? (Direction)state.getValue((Property)SlipstreamGeneratorBlock.FACING) : Direction.UP;
        }
        return this.levitateDir;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, SlipstreamGeneratorTile tile) {
        if (tile.updateTicks++ > 10) {
            tile.refreshPushAxis();
            tile.refreshEntityList();
            tile.updateTicks = 0;
        }
        if (tile.bb == null) {
            return;
        }
        Iterator<Entity> it = tile.levitatingEntities.iterator();
        while (it.hasNext()) {
            Entity entity = it.next();
            if (entity instanceof Player) {
                if (tile.levitatePlayer((Player)entity, tile.getLevitateDir().getAxis())) continue;
                it.remove();
                continue;
            }
            if (tile.pushEntity(entity)) continue;
            it.remove();
        }
    }

    private boolean pushEntity(Entity entity) {
        if (!this.entityIsValid(entity)) {
            return false;
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setDeltaMovement((double)((float)this.getLevitateDir().getStepX() * 0.2f), (double)((float)this.getLevitateDir().getStepY() * 0.2f), (double)((float)this.getLevitateDir().getStepZ() * 0.2f));
        return true;
    }

    private boolean levitatePlayer(Player player, Direction.Axis axis) {
        if (!this.entityIsValid((Entity)player)) {
            return false;
        }
        this.slowPlayerDown(player, false);
        if (!player.isShiftKeyDown()) {
            if (axis == Direction.Axis.Y) {
                this.levitatePlayerYAxis(player);
            } else {
                this.levitatePlayerHorizontally(player, axis);
            }
        }
        if (this.getLevel().isClientSide()) {
            this.spawnParticles(player);
        }
        return true;
    }

    private void slowPlayerDown(Player player, boolean xz) {
        double x = player.getDeltaMovement().x;
        double y = player.getDeltaMovement().y * 0.5999999046325684;
        double z = player.getDeltaMovement().z;
        if (this.getLevitateDir().getAxis() == Direction.Axis.X) {
            x *= 0.5999999046325684;
        } else if (this.getLevitateDir().getAxis() == Direction.Axis.Z) {
            z *= 0.5999999046325684;
        }
        player.setDeltaMovement(x, y, z);
        if (Math.abs(player.getDeltaMovement().y) < (double)0.2f) {
            player.push(0.0, -player.getDeltaMovement().y, 0.0);
            player.fallDistance = 0.0f;
        } else {
            player.fallDistance -= 1.0f;
        }
        if (this.getLevitateDir().getAxis() == Direction.Axis.X && Math.abs(player.getDeltaMovement().x) < (double)0.2f) {
            player.push(-player.getDeltaMovement().x, 0.0, 0.0);
        }
        if (this.getLevitateDir().getAxis() == Direction.Axis.Z && Math.abs(player.getDeltaMovement().z) < (double)0.2f) {
            player.push(0.0, 0.0, -player.getDeltaMovement().z);
        }
    }

    private void levitatePlayerYAxis(Player player) {
        float pitch = player.getXRot();
        float factor = (pitch > 0.0f ? pitch - 10.0f : pitch + 10.0f) / -180.0f;
        if (Math.abs(pitch) > 10.0f) {
            player.move(MoverType.PLAYER, new Vec3(0.0, (double)factor, 0.0));
        }
    }

    private void levitatePlayerHorizontally(Player player, Direction.Axis axis) {
        float factorX = 0.0f;
        float factorZ = 0.0f;
        Vec3 posX = new Vec3(1.0, 0.0, 0.0);
        Vec3 negX = new Vec3(-1.0, 0.0, 0.0);
        Vec3 posZ = new Vec3(0.0, 0.0, 1.0);
        Vec3 negZ = new Vec3(0.0, 0.0, -1.0);
        Vec3 playerH = new Vec3(player.getLookAngle().x, 0.0, player.getLookAngle().z);
        if (axis == Direction.Axis.X) {
            double dotPosX = posX.dot(playerH);
            double dotNegX = negX.dot(playerH);
            if (dotPosX > 0.2) {
                factorX = (float)dotPosX;
            } else if (dotNegX > 0.2) {
                factorX = (float)dotNegX * -1.0f;
            }
            factorX *= 0.75f;
        } else if (axis == Direction.Axis.Z) {
            double dotPosZ = posZ.dot(playerH);
            double dotNegZ = negZ.dot(playerH);
            if (dotPosZ > 0.2) {
                factorZ = (float)dotPosZ;
            } else if (dotNegZ > 0.2) {
                factorZ = (float)dotNegZ * -1.0f;
            }
            factorZ *= 0.75f;
        }
        double deltaY = (double)this.getBlockPos().getY() - player.getY() + 0.5;
        float yThreshold = 0.1f;
        if (Math.abs(deltaY) < (double)yThreshold) {
            deltaY = 0.0;
        } else if (deltaY != 0.0) {
            deltaY = (double)yThreshold * Math.signum(deltaY);
        }
        if (Math.abs(factorX) > 0.0f || Math.abs(factorZ) > 0.0f || deltaY > 0.0) {
            player.move(MoverType.PLAYER, new Vec3((double)factorX, deltaY, (double)factorZ));
        }
    }

    private void spawnParticles(Player player) {
        float pitch = player.getXRot();
        float factor = (pitch > 0.0f ? pitch - 10.0f : pitch + 10.0f) / -180.0f;
        if (player.isShiftKeyDown()) {
            factor = 0.01f;
        }
        for (int i = 0; i < 5; ++i) {
            this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.2f).setColor(10, 10, 10), player.getX(), player.isShiftKeyDown() ? player.getY() : player.getY() + (double)factor + Math.random() * (double)player.getBbHeight(), player.getZ(), (double)0.1f, (double)factor, 1.0);
        }
    }

    private boolean entityIsValid(Entity player) {
        if (player == null || !player.isAlive()) {
            return false;
        }
        Vec3 myPos = new Vec3((double)this.getBlockPos().getX() + 0.5, (double)this.getBlockPos().getY() + 0.5, (double)this.getBlockPos().getZ() + 0.5);
        if (player.getBoundingBox().intersects(this.bb)) {
            BlockHitResult result = this.level.clip(new ClipContext(player.getEyePosition(0.0f), myPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (result.getType() == HitResult.Type.MISS) {
                return true;
            }
            if (result.getType() == HitResult.Type.BLOCK) {
                return result.getBlockPos().equals(this.getBlockPos());
            }
        }
        return false;
    }

    private void refreshEntityList() {
        this.levitatingEntities.clear();
        if (this.bb == null) {
            return;
        }
        if (this.playersOnly) {
            this.levitatingEntities.addAll(this.getLevel().players().stream().filter(p -> this.entityIsValid((Entity)p) && !this.levitatingEntities.contains(p)).collect(Collectors.toList()));
        } else {
            this.levitatingEntities.addAll(this.getLevel().getEntitiesOfClass(Entity.class, this.bb));
        }
    }

    private void refreshPushAxis() {
        this.bb = null;
        int axisStacks = this.countStacksAlongAxis(this.getLevitateDir().getOpposite());
        BlockPos bbOffset = this.getBlockPos().relative(this.getLevitateDir(), (axisStacks + 1) * 50);
        this.bb = new AABB(this.getBlockPos(), bbOffset.offset(this.getLevitateDir().getStepX() == 0 ? 1 : 0, this.getLevitateDir().getStepY() == 0 ? 1 : 0, this.getLevitateDir().getStepZ() == 0 ? 1 : 0));
    }

    private int countStacksAlongAxis(Direction axis) {
        int count = 0;
        BlockPos checkPos = this.getBlockPos().offset(axis.getNormal());
        while (this.getLevel().getBlockState(checkPos).getBlock() == BlockInit.SLIPSTREAM_GENERATOR.get() && count < 5) {
            ++count;
            checkPos = checkPos.offset(axis.getNormal());
        }
        return count;
    }
}

