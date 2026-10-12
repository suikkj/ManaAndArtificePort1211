/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mna.api.particles.parameters;

import java.util.Random;
import javax.annotation.Nullable;

public class ParticleColor {
    private final float r;
    private final float g;
    private final float b;
    private final float a;
    private final int color;

    public ParticleColor(int r, int g, int b) {
        this.r = (float)r / 255.0f;
        this.g = (float)g / 255.0f;
        this.b = (float)b / 255.0f;
        this.a = 1.0f;
        this.color = 0xFF000000 | r << 16 | g << 8 | b;
    }

    public ParticleColor(int r, int g, int b, int a) {
        this.r = (float)r / 255.0f;
        this.g = (float)g / 255.0f;
        this.b = (float)b / 255.0f;
        this.a = (float)a / 255.0f;
        this.color = a << 24 | r << 16 | g << 8 | b;
    }

    public static ParticleColor makeRandomColor(int r, int g, int b, Random random) {
        return new ParticleColor(random.nextInt(r), random.nextInt(g), random.nextInt(b));
    }

    public ParticleColor(float r, float g, float b) {
        this((int)r, (int)g, (int)b);
    }

    public ParticleColor(float r, float g, float b, float a) {
        this((int)r, (int)g, (int)b, (int)a);
    }

    public static ParticleColor fromInt(int color) {
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color >> 0 & 0xFF;
        return new ParticleColor(r, g, b, a);
    }

    public float getRed() {
        return this.r;
    }

    public float getGreen() {
        return this.g;
    }

    public float getBlue() {
        return this.b;
    }

    public float getAlpha() {
        return this.a;
    }

    public int getColor() {
        return this.color;
    }

    public String serialize() {
        return "" + this.color;
    }

    @Nullable
    public static ParticleColor deserialize(String string) {
        if (string == null) {
            return null;
        }
        try {
            int color = Integer.parseInt(string);
            return ParticleColor.fromInt(color);
        }
        catch (Exception ex) {
            return null;
        }
    }
}

