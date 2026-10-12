/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.blocks.tileentities.models;

import com.mna.blocks.tileentities.LodestarTile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelLodestar
extends GeoModel<LodestarTile> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/block/lodestar_armature.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/block/lodestar_armature.animation.json");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/block/material/lodestar.png");

    public ResourceLocation getAnimationResource(LodestarTile arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(LodestarTile arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(LodestarTile arg0) {
        return texFile;
    }
}

