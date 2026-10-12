/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.api.tools;

import net.minecraft.resources.ResourceLocation;

public class RLoc {
    public static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath("mna", path);
    }
}

