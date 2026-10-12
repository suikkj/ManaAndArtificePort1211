/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.mna.tools.math;

import com.mna.tools.math.Matrix4;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Vector3 {
    public float x;
    public float y;
    public float z;

    public Vector3(double x, double y, double z) {
        this.x = (float)x;
        this.y = (float)y;
        this.z = (float)z;
    }

    public Vector3(BlockEntity tile) {
        this.x = tile.getBlockPos().getX();
        this.y = tile.getBlockPos().getY();
        this.z = tile.getBlockPos().getZ();
    }

    public Vector3(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public Vector3(Vec3 vec) {
        this.x = (float)vec.x;
        this.y = (float)vec.y;
        this.z = (float)vec.z;
    }

    public Vector3(Entity entity) {
        this(entity.position().x, entity.position().y, entity.position().z);
    }

    public Vector3 add(Vector3 vec) {
        return new Vector3(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }

    public Vector3 sub(Vector3 vec) {
        return new Vector3(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }

    public Vector3 scale(float scale) {
        return this.scale(scale, scale, scale);
    }

    public Vector3 scale(float scalex, float scaley, float scalez) {
        return new Vector3(this.x * scalex, this.y * scaley, this.z * scalez);
    }

    public Vector3 modulo(float divisor) {
        return new Vector3(this.x % divisor, this.y % divisor, this.z % divisor);
    }

    public Vector3 normalize() {
        float length = this.length();
        return new Vector3(this.x / length, this.y / length, this.z / length);
    }

    public Vector3 rotatePitch(float pitch) {
        float f = Mth.cos((float)pitch);
        float f1 = Mth.sin((float)pitch);
        double d0 = this.x;
        double d1 = (double)this.y * (double)f + (double)this.z * (double)f1;
        double d2 = (double)this.z * (double)f - (double)this.y * (double)f1;
        return new Vector3(d0, d1, d2);
    }

    public Vector3 rotateYaw(float yaw) {
        float f = Mth.cos((float)yaw);
        float f1 = Mth.sin((float)yaw);
        double d0 = (double)this.x * (double)f + (double)this.z * (double)f1;
        double d1 = this.y;
        double d2 = (double)this.z * (double)f - (double)this.x * (double)f1;
        return new Vector3(d0, d1, d2);
    }

    public Vector3 rotate(float angle, Vector3 axis) {
        return Matrix4.eulerAngles(angle, axis).translate(this);
    }

    public float length() {
        return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float lengthPow2() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vector3 copy() {
        return new Vector3(this.x, this.y, this.z);
    }

    public static Vector3 crossProduct(Vector3 vec1, Vector3 vec2) {
        return new Vector3(vec1.y * vec2.z - vec1.z * vec2.y, vec1.z * vec2.x - vec1.x * vec2.z, vec1.x * vec2.y - vec1.y * vec2.x);
    }

    public static Vector3 xCrossProduct(Vector3 vec) {
        return new Vector3(0.0, vec.z, -vec.y);
    }

    public static Vector3 zCrossProduct(Vector3 vec) {
        return new Vector3(-vec.y, vec.x, 0.0);
    }

    public static float dotProduct(Vector3 vec1, Vector3 vec2) {
        return vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z;
    }

    public static Quaternionf LookAt(Vector3 source, Vector3 dest) {
        Vector3 forwardVec = dest.sub(source).normalize();
        float dot = Vector3.dotProduct(Vector3.forward(), forwardVec);
        if (Math.abs(dot - -1.0f) < 1.0E-6f) {
            return new Quaternionf(0.0f, 1.0f, 0.0f, (float)Math.PI);
        }
        if (Math.abs(dot - 1.0f) < 1.0E-6f) {
            return new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f);
        }
        float rotationAngle = (float)Math.acos(dot);
        Vector3 rotAxis = Vector3.crossProduct(Vector3.forward(), forwardVec);
        rotAxis = rotAxis.normalize();
        Quaternionf out = new Quaternionf();
        return out.rotateAxis(rotationAngle, (Vector3fc)new Vector3f(rotAxis.x, rotAxis.y, rotAxis.z));
    }

    public static float angle(Vector3 vec1, Vector3 vec2) {
        return Vector3.anglePreNorm(vec1.copy().normalize(), vec2.copy().normalize());
    }

    public static float anglePreNorm(Vector3 vec1, Vector3 vec2) {
        return (float)Math.acos(Vector3.dotProduct(vec1, vec2));
    }

    public static Vector3 zero() {
        return new Vector3(0.0, 0.0, 0.0);
    }

    public String toString() {
        return "[" + this.x + "," + this.y + "," + this.z + "]";
    }

    public Vec3 toVec3D() {
        return new Vec3((double)this.x, (double)this.y, (double)this.z);
    }

    public Vector3f toVector3f() {
        return new Vector3f(this.x, this.y, this.z);
    }

    public static Vector3 getPerpendicular(Vector3 vec) {
        if (vec.z == 0.0f) {
            return Vector3.zCrossProduct(vec);
        }
        return Vector3.xCrossProduct(vec);
    }

    public static Vector3 lerp(Vector3 a, Vector3 b, float t) {
        Vector3 diff = b.sub(a);
        Vector3 scaled = diff.scale(t);
        return a.add(scaled);
    }

    public static Vector3 bezier(Vector3 start, Vector3 end, Vector3 control_1, Vector3 control_2, float time) {
        if (time < 0.0f) {
            time = 0.0f;
        } else if (time > 1.0f) {
            time = 1.0f;
        }
        float one_minus_t = 1.0f - time;
        Vector3 retValue = new Vector3(0.0, 0.0, 0.0);
        Vector3[] terms = new Vector3[]{start.scale(one_minus_t * one_minus_t * one_minus_t), control_1.scale(3.0f * one_minus_t * one_minus_t * time), control_2.scale(3.0f * one_minus_t * time * time), end.scale(time * time * time)};
        for (int i = 0; i < 4; ++i) {
            retValue = retValue.add(terms[i]);
        }
        return retValue;
    }

    public static Vector3 forward() {
        return new Vector3(1.0, 0.0, 0.0);
    }

    public static Vector3 up() {
        return new Vector3(0.0, 1.0, 0.0);
    }

    public static Vector3 right() {
        return new Vector3(0.0, 0.0, 1.0);
    }

    public boolean isZero() {
        return this.x == 0.0f && this.y == 0.0f && this.z == 0.0f;
    }

    public boolean isWithinRange(float min, float max) {
        return this.x >= min && this.x <= max && this.y >= min && this.y <= max && this.z >= min && this.z <= max;
    }

    public double distanceTo(Vector3 target) {
        double var2 = target.x - this.x;
        double var4 = target.y - this.y;
        double var6 = target.z - this.z;
        return Mth.sqrt((float)((float)(var2 * var2 + var4 * var4 + var6 * var6)));
    }

    public double distanceSqTo(Vector3 target) {
        double var2 = target.x - this.x;
        double var4 = target.y - this.y;
        double var6 = target.z - this.z;
        return var2 * var2 + var4 * var4 + var6 * var6;
    }

    public void floorToI() {
        this.x = (float)Math.floor(this.x);
        this.y = (float)Math.floor(this.y);
        this.z = (float)Math.floor(this.z);
    }

    public void roundToI() {
        this.x = Math.round(this.x);
        this.y = Math.round(this.y);
        this.z = Math.round(this.z);
    }

    public void ceilToI() {
        this.x = (float)Math.ceil(this.x);
        this.y = (float)Math.ceil(this.y);
        this.z = (float)Math.ceil(this.z);
    }

    public void writeToNBT(CompoundTag compound) {
        compound.putFloat("Vec3_x", this.x);
        compound.putFloat("Vec3_y", this.y);
        compound.putFloat("Vec3_z", this.z);
    }

    public static Vector3 readFromNBT(CompoundTag compound) {
        return new Vector3(compound.getFloat("Vec3_x"), compound.getFloat("Vec3_y"), compound.getFloat("Vec3_z"));
    }

    public boolean equals(Object obj) {
        if (obj instanceof Vector3) {
            Vector3 comp = (Vector3)obj;
            return comp.x == this.x && comp.y == this.y && comp.z == this.z;
        }
        return false;
    }

    public int hashCode() {
        return (int)(this.x + this.y + this.z);
    }
}

