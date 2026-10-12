/*
 * Decompiled with CFR 0.152.
 */
package com.mna.apibridge;

import com.mna.api.particles.IParticleMoveType;
import com.mna.api.particles.IParticleSerializerHelper;
import com.mna.particles.types.movers.ParticleBezierMover;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleOrbitMover;
import com.mna.particles.types.movers.ParticleVelocityMover;

public class ParticleSerializerHelper
implements IParticleSerializerHelper {
    @Override
    public IParticleMoveType fromID(int id) {
        switch (id) {
            case 3: {
                return new ParticleBezierMover();
            }
            case 2: {
                return new ParticleOrbitMover();
            }
            case 1: {
                return new ParticleLerpMover();
            }
        }
        return new ParticleVelocityMover();
    }
}

