/*
 * Decompiled with CFR 0.152.
 */
package com.mna.capabilities.worlddata;

import com.mna.api.capabilities.IWorldMagic;
import com.mna.capabilities.worlddata.WorldMagic;
import java.util.concurrent.Callable;

public final class WorldMagicFactory
implements Callable<IWorldMagic> {
    @Override
    public IWorldMagic call() throws Exception {
        return new WorldMagic();
    }
}

