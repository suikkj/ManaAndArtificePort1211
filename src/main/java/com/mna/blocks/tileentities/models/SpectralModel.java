/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.blocks.tileentities.models;

import com.mna.blocks.tileentities.SpectralTile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SpectralModel
extends GeoModel<SpectralTile> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/block/spectral.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/block/spectral.animation.json");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/block/material/spectral/conjures.png");

    public ResourceLocation getAnimationResource(SpectralTile arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(SpectralTile arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(SpectralTile arg0) {
        return texFile;
    }
}

