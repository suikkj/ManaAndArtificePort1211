/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.tools;

import com.mna.api.IPortalHelper;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.Portal;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PortalHelper
implements IPortalHelper {
    @Override
    public void openPortal(ServerLevel world, Vec3 position, DyeColor color, BlockPos teleportTarget, ResourceKey<Level> teleportDimension, boolean isPermanent) {
        Portal portal = new Portal((EntityType)EntityInit.PORTAL_ENTITY.get(), (Level)world);
        portal.setPos(position.x, position.y, position.z);
        portal.setDyeColor(color);
        portal.setTeleportBlockPos(teleportTarget, teleportDimension);
        if (isPermanent) {
            portal.setPermanent();
        }
        world.addFreshEntity((Entity)portal);
    }
}

