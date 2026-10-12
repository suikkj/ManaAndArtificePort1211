/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.border.WorldBorder
 *  net.minecraft.world.level.dimension.DimensionType
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.events.delayed;

import com.mna.api.timing.IDelayedEvent;
import com.mna.tools.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

public class DelayedDimensionTeleportEvent
implements IDelayedEvent {
    final LivingEntity target;
    final ServerLevel origin;
    final ServerLevel destination;

    public DelayedDimensionTeleportEvent(LivingEntity entity, ServerLevel origin, ServerLevel destination) {
        this.origin = origin;
        this.destination = destination;
        this.target = entity;
    }

    @Override
    public boolean tick() {
        this.teleportOnSchedule();
        return true;
    }

    @Override
    public String getID() {
        return "";
    }

    private void teleportOnSchedule() {
        Vec3 targetPosition = this.calculateDimensionDifferencePosition(this.target.position(), this.origin, this.destination);
        TeleportHelper.teleportEntity((Entity)this.target, (ResourceKey<Level>)this.destination.dimension(), targetPosition);
    }

    protected Vec3 calculateDimensionDifferencePosition(Vec3 origin, ServerLevel from, ServerLevel to) {
        WorldBorder worldborder = to.getWorldBorder();
        double minX = Math.max(-2.9999872E7, worldborder.getMinX() + 16.0);
        double minZ = Math.max(-2.9999872E7, worldborder.getMinZ() + 16.0);
        double maxX = Math.min(2.9999872E7, worldborder.getMaxX() - 16.0);
        double maxZ = Math.min(2.9999872E7, worldborder.getMaxZ() - 16.0);
        double scale = DimensionType.getTeleportationScale((DimensionType)from.dimensionType(), (DimensionType)to.dimensionType());
        BlockPos scaledPos = BlockPos.containing((double)Mth.clamp((double)(origin.x * scale), (double)minX, (double)maxX), (double)Mth.clamp((double)origin.y, (double)5.0, (double)(to.getMaxBuildHeight() - 2)), (double)Mth.clamp((double)(origin.z * scale), (double)minZ, (double)maxZ));
        BlockPos searchPos = new BlockPos((Vec3i)scaledPos);
        int MIN_Y = 4;
        if (searchPos.getY() >= to.dimensionType().logicalHeight()) {
            int delta = to.dimensionType().logicalHeight() - 3 - searchPos.getY();
            searchPos = searchPos.offset(0, delta, 0);
        }
        while (searchPos.getY() > MIN_Y && !to.isEmptyBlock(searchPos) && !to.isEmptyBlock(searchPos.above())) {
            searchPos = searchPos.below();
        }
        while (searchPos.getY() > MIN_Y && to.isEmptyBlock(searchPos.below())) {
            searchPos = searchPos.below();
        }
        while (searchPos.getY() < to.getHeight() - 3 && !to.isEmptyBlock(searchPos) && !to.isEmptyBlock(searchPos.above())) {
            searchPos = searchPos.above();
        }
        if (!to.isEmptyBlock(searchPos) && !to.isEmptyBlock(searchPos.above())) {
            searchPos.offset(0, to.getHeight() - searchPos.getY(), 0);
        }
        scaledPos = searchPos;
        return new Vec3((double)scaledPos.getX() + 0.5, (double)scaledPos.getY(), (double)scaledPos.getZ() + 0.5);
    }
}

