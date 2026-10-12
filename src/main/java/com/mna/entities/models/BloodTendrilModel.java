/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models;

import com.mna.entities.summon.BloodTendril;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BloodTendrilModel
extends GeoModel<BloodTendril> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/blood_tendril.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/blood_tendril.animation.json");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/blood_tendril.png");

    public ResourceLocation getAnimationResource(BloodTendril arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(BloodTendril arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(BloodTendril arg0) {
        return texFile;
    }
}

