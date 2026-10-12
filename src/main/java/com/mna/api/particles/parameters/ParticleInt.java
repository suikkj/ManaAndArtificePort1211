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

public class ParticleInt {
    private final int value;

    public ParticleInt(int life) {
        this.value = life;
    }

    public int value() {
        return this.value;
    }

    public String serialize() {
        return "" + this.value;
    }

    @Nullable
    public static ParticleInt deserialize(String string) {
        if (string == null || string == "") {
            return null;
        }
        try {
            return new ParticleInt(Integer.parseInt(string));
        }
        catch (Exception ex) {
            return null;
        }
    }

    @Nullable
    public static ParticleInt deserialize(FriendlyByteBuf packetBuffer) {
        try {
            if (packetBuffer.readBoolean()) {
                return new ParticleInt(packetBuffer.readInt());
            }
        }
        catch (Exception ex) {
            return null;
        }
        return null;
    }

    public static void serialize(@Nullable ParticleInt inst, FriendlyByteBuf packetBuffer) {
        if (inst != null) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeInt(inst.value());
        } else {
            packetBuffer.writeBoolean(false);
        }
    }
}

