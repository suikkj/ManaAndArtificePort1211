/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.network.FriendlyByteBuf
 */
package com.mna.api.particles.parameters;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public class ParticleFloat {
    private final float value;

    public ParticleFloat(float scale) {
        this.value = scale;
    }

    public float value() {
        return this.value;
    }

    public String serialize() {
        return "" + this.value;
    }

    @Nullable
    public static ParticleFloat deserialize(String string) {
        if (string == null || string == "") {
            return null;
        }
        try {
            return new ParticleFloat(Float.parseFloat(string));
        }
        catch (Exception ex) {
            return null;
        }
    }

    @Nullable
    public static ParticleFloat deserialize(FriendlyByteBuf packetBuffer) {
        try {
            if (packetBuffer.readBoolean()) {
                return new ParticleFloat(packetBuffer.readFloat());
            }
        }
        catch (Exception ex) {
            return null;
        }
        return null;
    }

    public static void serialize(@Nullable ParticleFloat inst, FriendlyByteBuf packetBuffer) {
        if (inst != null) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeFloat(inst.value());
        } else {
            packetBuffer.writeBoolean(false);
        }
    }
}

