/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models;

import com.mna.entities.summon.BloodGolem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BloodGolemModel
extends GeoModel<BloodGolem> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/blood_golem.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/blood_golem.animation.json");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/blood_golem.png");

    public ResourceLocation getAnimationResource(BloodGolem arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(BloodGolem arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(BloodGolem arg0) {
        return texFile;
    }
}

