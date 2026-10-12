/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.blocks.tileentities.models;

import com.mna.blocks.tileentities.ImpaleSpikeTile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelImpaleSpike
extends GeoModel<ImpaleSpikeTile> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/block/impale_spike.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/block/impale_spike.animation.json");
    private static final ResourceLocation texFile = ResourceLocation.parse("textures/block/dripstone_block.png");

    public ResourceLocation getAnimationResource(ImpaleSpikeTile arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(ImpaleSpikeTile arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(ImpaleSpikeTile arg0) {
        return texFile;
    }
}

