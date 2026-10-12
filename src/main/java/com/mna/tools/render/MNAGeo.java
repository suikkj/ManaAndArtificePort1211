package com.mna.tools.render;

/**
 * [PORT 1.21.1] GeckoLib 4.8.4 trocou os parametros float red/green/blue/alpha
 * por um unico int colour (ARGB empacotado).
 */
public final class MNAGeo {
    private MNAGeo() {}

    public static int packColour(float red, float green, float blue, float alpha) {
        int r = (int) (red * 255.0F) & 0xFF;
        int g = (int) (green * 255.0F) & 0xFF;
        int b = (int) (blue * 255.0F) & 0xFF;
        int a = (int) (alpha * 255.0F) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
