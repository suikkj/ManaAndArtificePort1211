/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.blocks.tileentities.renderers.models;

import com.mna.blocks.tileentities.wizard_lab.FumePrismTile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FumePrismModel
extends GeoModel<FumePrismTile> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/block/fume_prism.geo.json");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/block/artifice/wizard_lab/fume_prism.png");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/block/fume_prism.animation.json");

    public ResourceLocation getModelResource(FumePrismTile animatable) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(FumePrismTile animatable) {
        return texFile;
    }

    public ResourceLocation getAnimationResource(FumePrismTile animatable) {
        return animFile;
    }
}

