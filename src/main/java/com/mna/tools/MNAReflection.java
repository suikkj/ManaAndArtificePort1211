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

    public static net.minecraft.world.phys.Vec3 explosionPos(net.minecraft.world.level.Explosion explosion) {
        try {
            java.lang.reflect.Field fx = net.minecraft.world.level.Explosion.class.getDeclaredField("x");
            java.lang.reflect.Field fy = net.minecraft.world.level.Explosion.class.getDeclaredField("y");
            java.lang.reflect.Field fz = net.minecraft.world.level.Explosion.class.getDeclaredField("z");
            fx.setAccessible(true);
            fy.setAccessible(true);
            fz.setAccessible(true);
            return new net.minecraft.world.phys.Vec3(fx.getDouble(explosion), fy.getDouble(explosion), fz.getDouble(explosion));
        } catch (Throwable t) {
            return net.minecraft.world.phys.Vec3.ZERO;
        }
    }

    public static void startConverting(net.minecraft.world.entity.monster.ZombieVillager zomb, java.util.UUID uuid, int time) {
        try {
            java.lang.reflect.Method m = net.minecraft.world.entity.monster.ZombieVillager.class.getDeclaredMethod("startConverting", java.util.UUID.class, int.class);
            m.setAccessible(true);
            m.invoke(zomb, uuid, time);
        } catch (Throwable t) {
        }
    }
}
