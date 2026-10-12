/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.commands.arguments.EntityAnchorArgument$Anchor
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package com.mna.tools.math;

import com.mna.tools.math.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class MathUtils {
    public static float RandomBetween(float start, float end) {
        return (float)((double)start + Math.random() * (double)(end - start));
    }

    public static double RandomBetween(double start, double end) {
        return start + Math.random() * (end - start);
    }

    public static Vector3[] GetHorizontalBlocksInFrontOfCharacter(LivingEntity entity, int numBlocks, int x, int y, int z) {
        float speed = 0.1f;
        float factor = (float)Math.PI / 180;
        float sinYawRadians = Mth.sin((float)(entity.getYRot() * factor));
        float cosYawRadians = Mth.cos((float)(entity.getYRot() * factor));
        double motionZ = cosYawRadians * speed;
        double motionX = -sinYawRadians * speed;
        double curX = x;
        double curY = y;
        double curZ = z;
        float minimum = 0.01f;
        if (Math.abs(motionX) < (double)minimum) {
            motionX = 0.0;
        }
        if (Math.abs(motionZ) < (double)minimum) {
            motionZ = 0.0;
        }
        int lastX = x;
        int lastY = y;
        int lastZ = z;
        Vector3[] list = new Vector3[numBlocks];
        list[0] = new Vector3(x, y, z);
        int count = 1;
        while (count < numBlocks) {
            if ((int)Math.round(curX += motionX) == lastX && (int)Math.round(curY) == lastY && (int)Math.round(curZ += motionZ) == lastZ) continue;
            lastX = (int)Math.round(curX);
            lastY = (int)Math.round(curY);
            lastZ = (int)Math.round(curZ);
            list[count++] = new Vector3(lastX, lastY, lastZ);
        }
        return list;
    }

    public static Vector3[] GetBlocksInFrontOfCharacter(LivingEntity entity, int numBlocks, int x, int y, int z) {
        float speed = 0.1f;
        float factor = (float)Math.PI / 180;
        float sinYawRadians = Mth.sin((float)(entity.getYRot() * factor));
        float cosYawRadians = Mth.cos((float)(entity.getYRot() * factor));
        float sinPitchRadians = Mth.sin((float)(entity.getXRot() * factor));
        float cosPitchRadians = Mth.cos((float)(entity.getXRot() * factor));
        double motionZ = cosYawRadians * cosPitchRadians * speed;
        double motionX = -sinYawRadians * cosPitchRadians * speed;
        double motionY = -sinPitchRadians * speed;
        double curX = x;
        double curY = y;
        double curZ = z;
        float minimum = 0.01f;
        if (Math.abs(motionX) < (double)minimum) {
            motionX = 0.0;
        }
        if (Math.abs(motionY) < (double)minimum) {
            motionY = 0.0;
        }
        if (Math.abs(motionZ) < (double)minimum) {
            motionZ = 0.0;
        }
        int lastX = x;
        int lastY = y;
        int lastZ = z;
        Vector3[] list = new Vector3[numBlocks];
        list[0] = new Vector3(x, y, z);
        int count = 1;
        while (count < numBlocks) {
            if ((int)Math.round(curX += motionX) == lastX && (int)Math.round(curY += motionY) == lastY && (int)Math.round(curZ += motionZ) == lastZ) continue;
            lastX = (int)Math.round(curX);
            lastY = (int)Math.round(curY);
            lastZ = (int)Math.round(curZ);
            list[count++] = new Vector3(lastX, lastY, lastZ);
        }
        return list;
    }

    public static int getDistanceToGround(LivingEntity ent, Level world) {
        int yCoordOffset = 0;
        int distance = 0;
        while (distance < 20 && !world.isEmptyBlock(ent.blockPosition().offset(0, yCoordOffset, 0))) {
            ++distance;
            --yCoordOffset;
        }
        return distance;
    }

    public static Vec3 reflect(Vec3 incident, Vec3 reflectionNormal) {
        Vec3 rn = reflectionNormal.normalize();
        Vec3 i = incident.normalize();
        double scale = 2.0 * rn.dot(i);
        return rn.subtract(i).scale(scale);
    }

    public static float[] colorIntToFloats(int color) {
        float[] colors = new float[]{(float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f};
        return colors;
    }

    public static int colorFloatsToInt(float r, float g, float b) {
        return ((int)(r * 255.0f) << 16) + ((int)(g * 255.0f) << 8) + (int)(b * 255.0f);
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double clamp01(double val) {
        return MathUtils.clamp(val, 0.0, 1.0);
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float clamp01(float val) {
        return MathUtils.clamp(val, 0.0f, 1.0f);
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float lerpf(float start, float end, float t) {
        return start + (end - start) * t;
    }

    public static int lerpColor(int color_1, int color_2, float t) {
        float[] a = new float[]{(float)FastColor.ARGB32.alpha((int)color_1) / 255.0f, (float)FastColor.ARGB32.red((int)color_1) / 255.0f, (float)FastColor.ARGB32.green((int)color_1) / 255.0f, (float)FastColor.ARGB32.blue((int)color_1) / 255.0f};
        float[] b = new float[]{(float)FastColor.ARGB32.alpha((int)color_2) / 255.0f, (float)FastColor.ARGB32.red((int)color_2) / 255.0f, (float)FastColor.ARGB32.green((int)color_2) / 255.0f, (float)FastColor.ARGB32.blue((int)color_2) / 255.0f};
        return FastColor.ARGB32.color((int)((int)(MathUtils.lerpf(a[0], b[0], t) * 255.0f)), (int)((int)(MathUtils.lerpf(a[1], b[1], t) * 255.0f)), (int)((int)(MathUtils.lerpf(a[2], b[2], t) * 255.0f)), (int)((int)(MathUtils.lerpf(a[3], b[3], t) * 255.0f)));
    }

    public static Vec3 lerpVector3d(Vec3 a, Vec3 b, float t) {
        Vec3 diff = b.subtract(a);
        Vec3 scaled = diff.scale((double)t);
        return a.add(scaled);
    }

    public static Vec3 bezierVector3d(Vec3 start, Vec3 end, Vec3 control_1, Vec3 control_2, float time) {
        if (time < 0.0f) {
            time = 0.0f;
        } else if (time > 1.0f) {
            time = 1.0f;
        }
        float one_minus_t = 1.0f - time;
        Vec3 retValue = new Vec3(0.0, 0.0, 0.0);
        Vec3[] terms = new Vec3[]{start.scale((double)(one_minus_t * one_minus_t * one_minus_t)), control_1.scale((double)(3.0f * one_minus_t * one_minus_t * time)), control_2.scale((double)(3.0f * one_minus_t * time * time)), end.scale((double)(time * time * time))};
        for (int i = 0; i < 4; ++i) {
            retValue = retValue.add(terms[i]);
        }
        return retValue;
    }

    public static Vec3 rotateTowards(Vec3 current, Vec3 target, float maxAngleDegrees) {
        Vec3 fromDirection = current.normalize();
        Vec3 toDirection = target.normalize();
        double angleRadians = Math.acos(fromDirection.dot(toDirection));
        angleRadians = Math.min(Math.max(angleRadians, (double)(-maxAngleDegrees)), (double)maxAngleDegrees);
        Vec3 axis = fromDirection.cross(toDirection);
        Vector3f vec = new Vector3f((float)current.x, (float)current.y, (float)current.z);
        vec = vec.rotateAxis((float)angleRadians, (float)axis.x, (float)axis.y, (float)axis.z);
        return new Vec3((double)vec.x(), (double)vec.y(), (double)vec.z());
    }

    public static AABB createInclusiveBB(BlockPos a, BlockPos b) {
        AABB bb = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(a), net.minecraft.world.phys.Vec3.atLowerCornerOf(b)).expandTowards(1.0, 1.0, 1.0);
        return bb;
    }

    public static boolean rotateEntityLookTowards(EntityAnchorArgument.Anchor type, LivingEntity living, Vec3 position, float maxDelta) {
        double d0 = position.x() - living.getX();
        double d2 = position.z() - living.getZ();
        double d1 = position.y - living.getEyeY();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = (float)(Mth.atan2((double)d2, (double)d0) * 57.2957763671875) - 90.0f;
        float f1 = (float)(-(Mth.atan2((double)d1, (double)d3) * 57.2957763671875));
        Pair<Float, Boolean> xRot = MathUtils.rotlerp(living.getXRot(), f1, maxDelta);
        Pair<Float, Boolean> yRot = MathUtils.rotlerp(living.getYRot(), f, maxDelta);
        living.setXRot(((Float)xRot.getFirst()).floatValue());
        living.setYRot(((Float)yRot.getFirst()).floatValue());
        return (Boolean)yRot.getSecond() == false;
    }

    private static Pair<Float, Boolean> rotlerp(float pAngle, float pTargetAngle, float pMaxIncrease) {
        float f = Mth.wrapDegrees((float)(pTargetAngle - pAngle));
        boolean clamped = false;
        if (f > pMaxIncrease) {
            f = pMaxIncrease;
            clamped = true;
        }
        if (f < -pMaxIncrease) {
            f = -pMaxIncrease;
            clamped = true;
        }
        return new Pair(Float.valueOf(pAngle + f), clamped);
    }

    public static int weightedRandomNumber(int[] weights) {
        int sum_of_weight = 0;
        for (int i = 0; i < weights.length; ++i) {
            sum_of_weight += weights[i];
        }
        int target = (int)((double)sum_of_weight * Math.random());
        for (int i = 0; i < weights.length; ++i) {
            if (target <= weights[i]) {
                return i;
            }
            target -= weights[i];
        }
        return 0;
    }

    public static int weightedRandomNumber(int max) {
        int sum_of_weight = 0;
        for (int i = 0; i <= max; ++i) {
            sum_of_weight += i;
        }
        int target = (int)((double)sum_of_weight * Math.random());
        for (int i = 0; i < max; ++i) {
            if (target <= i) {
                return i;
            }
            target -= i;
        }
        return 0;
    }

    public static Pair<Vec3, Vec3> getLookCross(LivingEntity living) {
        Vec3 look = living.getLookAngle().normalize();
        if (Math.abs(look.x + look.z) < (double)1.0E-5f) {
            double rot = (double)(-living.getYRot() / 180.0f) * Math.PI;
            double x = Math.sin(rot);
            double z = Math.cos(rot);
            Vec3 dirOffset = new Vec3(x, 0.0, z).normalize().scale((double)0.01f);
            look = look.add(dirOffset);
        }
        Vec3 perp = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        return new Pair(look, perp);
    }
}

