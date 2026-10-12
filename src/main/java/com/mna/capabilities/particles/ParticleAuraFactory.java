/*
 * Decompiled with CFR 0.152.
 */
package com.mna.capabilities.particles;

import com.mna.capabilities.particles.ParticleAura;
import java.util.concurrent.Callable;

public class ParticleAuraFactory
implements Callable<ParticleAura> {
    @Override
    public ParticleAura call() throws Exception {
        return new ParticleAura();
    }
}

