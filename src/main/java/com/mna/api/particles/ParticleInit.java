/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleType
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  net.neoforged.neoforge.registries.IForgeRegistry
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.api.particles;

import com.mna.api.particles.MAParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ParticleInit {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, "mna");
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> SPARKLE_RANDOM = PARTICLES.register("sparkle", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> SPARKLE_LERP_POINT = PARTICLES.register("sparkle_lerp_point", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> SPARKLE_BEZIER_POINT = PARTICLES.register("sparkle_bezier_point", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> SPARKLE_VELOCITY = PARTICLES.register("sparkle_velocity", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> SPARKLE_GRAVITY = PARTICLES.register("sparkle_gravity", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> SPARKLE_STATIONARY = PARTICLES.register("sparkle_stationary", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BLUE_SPARKLE_STATIONARY = PARTICLES.register("blue_sparkle_stationary", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BLUE_SPARKLE_GRAVITY = PARTICLES.register("blue_sparkle_gravity", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BLUE_SPARKLE_VELOCITY = PARTICLES.register("blue_sparkle_velocity", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BLUE_SPARKLE_SPHERE_ORBIT = PARTICLES.register("blue_sparkle_sphere_orbit", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BLUE_SPARKLE_ORBIT = PARTICLES.register("blue_sparkle_orbit", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> LIGHT_VELOCITY = PARTICLES.register("light_velocity", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BLUE_FLAME = PARTICLES.register("blue_flame", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> FLAME = PARTICLES.register("flame", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> FLAME_LERP = PARTICLES.register("flame_lerp", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> FLAME_ORBIT = PARTICLES.register("flame_orbit", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> HELLFIRE = PARTICLES.register("hellfire", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> FROST = PARTICLES.register("frost", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> FROST_LERP = PARTICLES.register("frost_lerp", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> MIST = PARTICLES.register("mist", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> WATER = PARTICLES.register("water", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> WATER_LERP = PARTICLES.register("water_lerp", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> ENDER = PARTICLES.register("ender", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> ENDER_VELOCITY = PARTICLES.register("ender_velocity", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> ARCANE = PARTICLES.register("arcane", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> ARCANE_LERP = PARTICLES.register("arcane_lerp", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> ARCANE_RANDOM = PARTICLES.register("arcane_random", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> ARCANE_MAGELIGHT = PARTICLES.register("arcane_magelight", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> EARTH = PARTICLES.register("earth", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> AIR_VELOCITY = PARTICLES.register("air_velocity", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> AIR_ORBIT = PARTICLES.register("air_orbit", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> AIR_LERP = PARTICLES.register("air_lerp", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> DUST = PARTICLES.register("dust", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> DUST_LERP = PARTICLES.register("dust_lerp", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> GLOW = PARTICLES.register("glow", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> SOUL = PARTICLES.register("soul", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BLOOD = PARTICLES.register("blood", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> HEART = PARTICLES.register("heart", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BONE = PARTICLES.register("bone", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> BONE_ORBIT = PARTICLES.register("bone_orbit", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> DRIP = PARTICLES.register("drip", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> DROPLET = PARTICLES.register("droplet", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> DROPLET_LERP = PARTICLES.register("droplet_lerp", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> ITEM = PARTICLES.register("item", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> ENCHANT = PARTICLES.register("enchant", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> COZY_SMOKE = PARTICLES.register("cozy_smoke", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> LIGHTNING_BOLT = PARTICLES.register("lightning_bolt", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> WRAITH_BOLT = PARTICLES.register("wraith_bolt", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> TRAIL = PARTICLES.register("trail", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> TRAIL_VELOCITY = PARTICLES.register("trail_velocity", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> TRAIL_ORBIT = PARTICLES.register("trail_orbit", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> TRAIL_SPHERE_ORBIT = PARTICLES.register("trail_sphere_orbit", () -> new MAParticleType());
    public static DeferredHolder<ParticleType<?>, ? extends MAParticleType> TRAIL_BEZIER = PARTICLES.register("trail_bezier", () -> new MAParticleType());
}


