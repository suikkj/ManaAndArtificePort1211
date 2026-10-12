/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.blocks.tileentities.models;

import com.mna.blocks.tileentities.IceSpikeTile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelIceSpike
extends GeoModel<IceSpikeTile> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/block/ice_spike.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/block/ice_spike.animation.json");
    private static final ResourceLocation texFile = ResourceLocation.parse("textures/block/ice.png");

    public ResourceLocation getAnimationResource(IceSpikeTile arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(IceSpikeTile arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(IceSpikeTile arg0) {
        return texFile;
    }
}

