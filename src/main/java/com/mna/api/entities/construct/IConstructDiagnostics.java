/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.entities.construct;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface IConstructDiagnostics {
    public void pushDiagnosticMessage(String var1, @Nullable ResourceLocation var2, boolean var3);

    public void pushTaskUpdate(String var1, ResourceLocation var2, Status var3, Vec3 var4);

    public void pushTaskUpdate(String var1, ResourceLocation var2, Status var3, AABB var4);

    public void pushTaskUpdate(String var1, ResourceLocation var2, Status var3, int var4);

    public void setMovePos(Vec3 var1);

    public boolean needsUpdate();

    public static enum Status {
        RUNNING,
        SUCCESS,
        FAILURE;

    }
}

