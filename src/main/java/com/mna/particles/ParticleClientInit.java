/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleType
 *  net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent
 *  net.neoforged.bus.api.SubscribeEvent
 */
package com.mna.particles;

import com.mna.ManaAndArtifice;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.FXAir;
import com.mna.particles.FXArcane;
import com.mna.particles.FXBlood;
import com.mna.particles.FXBlueFlame;
import com.mna.particles.FXBlueSparkle;
import com.mna.particles.FXBone;
import com.mna.particles.FXCozySmoke;
import com.mna.particles.FXDrip;
import com.mna.particles.FXDroplet;
import com.mna.particles.FXDust;
import com.mna.particles.FXEarth;
import com.mna.particles.FXEnchant;
import com.mna.particles.FXEnder;
import com.mna.particles.FXFlame;
import com.mna.particles.FXFrost;
import com.mna.particles.FXGlow;
import com.mna.particles.FXHeart;
import com.mna.particles.FXItem;
import com.mna.particles.FXLight;
import com.mna.particles.FXSoul;
import com.mna.particles.FXSparkle;
import com.mna.particles.FXWater;
import com.mna.particles.bolt.FXLightningBolt;
import com.mna.particles.bolt.FXWraithBolt;
import com.mna.particles.trail.FXTrail;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class ParticleClientInit {
    @SubscribeEvent
    public static void onRegisterParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet((ParticleType)ParticleInit.SPARKLE_BEZIER_POINT.get(), FXSparkle.FXSparkleBezierPointFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.SPARKLE_GRAVITY.get(), FXSparkle.FXSparkleGravityFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.SPARKLE_LERP_POINT.get(), FXSparkle.FXSparkleLerpPointFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.SPARKLE_RANDOM.get(), FXSparkle.FXSparkleRandomFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.SPARKLE_VELOCITY.get(), FXSparkle.FXSparkleVelocityFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.SPARKLE_STATIONARY.get(), FXSparkle.FXSparkleStationaryFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BLUE_SPARKLE_STATIONARY.get(), FXBlueSparkle.FXBlueSparkleStationaryFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get(), FXBlueSparkle.FXBlueSparkleGravityFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get(), FXBlueSparkle.FXBlueSparkleVelocityFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BLUE_SPARKLE_SPHERE_ORBIT.get(), FXBlueSparkle.FXBlueSparkleSphereOrbitFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BLUE_SPARKLE_ORBIT.get(), FXBlueSparkle.FXBlueSparkleOrbitFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.LIGHT_VELOCITY.get(), FXLight.FXLightVelocity::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BLUE_FLAME.get(), FXBlueFlame.FXBlueFlameRandomFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.FLAME.get(), FXFlame.FXFlameFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.FLAME_LERP.get(), FXFlame.FXFlameLerpFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.FLAME_ORBIT.get(), FXFlame.FXFlameOrbitFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.HELLFIRE.get(), FXFlame.FXHellfireFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.FROST.get(), FXFrost.FXFrostFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.FROST_LERP.get(), FXFrost.FXFrostLerpFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.MIST.get(), FXFrost.FXMistFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.WATER.get(), FXWater.FXWaterFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.WATER_LERP.get(), FXWater.FXWaterLerpFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.ENDER.get(), FXEnder.FXEnderBezier::new);
        event.registerSpriteSet((ParticleType)ParticleInit.ENDER_VELOCITY.get(), FXEnder.FXEnderVelocity::new);
        event.registerSpriteSet((ParticleType)ParticleInit.ARCANE.get(), FXArcane.FXArcaneFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.ARCANE_LERP.get(), FXArcane.FXArcaneStretchLerp::new);
        event.registerSpriteSet((ParticleType)ParticleInit.ARCANE_RANDOM.get(), FXArcane.FXArcaneRandomFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get(), FXArcane.FXArcaneMagelight::new);
        event.registerSpriteSet((ParticleType)ParticleInit.AIR_VELOCITY.get(), FXAir.FXAirVelocity::new);
        event.registerSpriteSet((ParticleType)ParticleInit.AIR_ORBIT.get(), FXAir.FXAirOrbit::new);
        event.registerSpriteSet((ParticleType)ParticleInit.AIR_LERP.get(), FXAir.FXAirLerp::new);
        event.registerSpriteSet((ParticleType)ParticleInit.EARTH.get(), FXEarth.FXEarthFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.DUST.get(), FXDust.FXDustFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.DUST_LERP.get(), FXDust.FXDustLerpFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.GLOW.get(), FXGlow.FXGlowFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.SOUL.get(), FXSoul.FXSoulFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BLOOD.get(), FXBlood.FXBloodFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BONE.get(), FXBone.FXBoneFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.BONE_ORBIT.get(), FXBone.FXBoneOrbitFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.DRIP.get(), FXDrip.FXDripFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.DROPLET.get(), FXDroplet.FXDropletFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.DROPLET_LERP.get(), FXDroplet.FXDropletLerpFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.ITEM.get(), FXItem.Factory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.HEART.get(), FXHeart.FXHeartFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.ENCHANT.get(), FXEnchant.FXEnchantFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.COZY_SMOKE.get(), FXCozySmoke.FXCozySmokeFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.LIGHTNING_BOLT.get(), FXLightningBolt.FXLightningBoltFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.WRAITH_BOLT.get(), FXWraithBolt.FXWraithBoltFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.TRAIL.get(), FXTrail.FXTrailFollowEntityFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.TRAIL_VELOCITY.get(), FXTrail.FXTrailVelocityFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.TRAIL_ORBIT.get(), FXTrail.FXTrailOrbitFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.TRAIL_SPHERE_ORBIT.get(), FXTrail.FXTrailSphereOrbitFactory::new);
        event.registerSpriteSet((ParticleType)ParticleInit.TRAIL_BEZIER.get(), FXTrail.FXTrailBezierFactory::new);
        ManaAndArtifice.LOGGER.info("M&A -> Particle Factories Registered");
    }
}


