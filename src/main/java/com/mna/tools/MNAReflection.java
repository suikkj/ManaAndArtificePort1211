package com.mna.tools;

/**
 * [PORT 1.21.1] Acessos reflexivos genericos (campos que viraram private).
 */
public final class MNAReflection {

    private MNAReflection() {
    }

    public static float explosionRadius(net.minecraft.world.level.Explosion explosion) {
        try {
            java.lang.reflect.Field f = net.minecraft.world.level.Explosion.class.getDeclaredField("radius");
            f.setAccessible(true);
            return f.getFloat(explosion);
        } catch (Throwable t) {
            return 0.0f;
        }
    }
}
