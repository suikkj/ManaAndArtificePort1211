/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 */
package com.mna.api.entities.construct.ai;

import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface IMutexManager {
    public boolean claimMutex(BlockPos var1, IConstruct<?> var2, ConstructAITask<?> var3);

    public void releaseMutex(BlockPos var1, IConstruct<?> var2, ConstructAITask<?> var3);

    public boolean claimMutex(Entity var1, IConstruct<?> var2, ConstructAITask<?> var3);

    public void releaseMutex(Entity var1, IConstruct<?> var2, ConstructAITask<?> var3);

    public void releaseAllMutexes(IConstruct<?> var1, ConstructAITask<?> var2);
}

