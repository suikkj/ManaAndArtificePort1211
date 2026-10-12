/*
 * Decompiled with CFR 0.152.
 */
package com.mna.capabilities.chunkdata;

import com.mna.api.capabilities.IChunkMagic;
import com.mna.capabilities.chunkdata.ChunkMagic;
import java.util.concurrent.Callable;

public class ChunkMagicFactory
implements Callable<IChunkMagic> {
    @Override
    public IChunkMagic call() throws Exception {
        return new ChunkMagic();
    }
}

