/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.blocks.tileentities.renderers.models;

import com.mna.blocks.tileentities.wizard_lab.FumeBellowsTile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FumeBellowsModel
extends GeoModel<FumeBellowsTile> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/block/fume_filter_bellows.geo.json");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/block/artifice/wizard_lab/fume_bellows.png");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/block/fume_filter_bellows.animation.json");

    public ResourceLocation getModelResource(FumeBellowsTile animatable) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(FumeBellowsTile animatable) {
        return texFile;
    }

    public ResourceLocation getAnimationResource(FumeBellowsTile animatable) {
        return animFile;
    }
}

