/*
 * Decompiled with CFR 0.152.
 */
package com.mna.capabilities.entity;

import com.mna.capabilities.entity.MAPFX;
import java.util.concurrent.Callable;

public class MAPFXFactory
implements Callable<MAPFX> {
    @Override
    public MAPFX call() throws Exception {
        return new MAPFX();
    }
}

