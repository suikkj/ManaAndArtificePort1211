/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.items.renderers.models;

import com.mna.api.tools.RLoc;
import com.mna.items.relic.RunicMalus;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelRunicMalus
extends GeoModel<RunicMalus> {
    private static final ResourceLocation anim = RLoc.create("animations/runic_malus.animation.json");
    private static final ResourceLocation model = RLoc.create("geo/runic_malus_armature.geo.json");
    private static final ResourceLocation texture = RLoc.create("textures/item/artifice/runic_malus.png");

    public ResourceLocation getAnimationResource(RunicMalus animatable) {
        return anim;
    }

    public ResourceLocation getModelResource(RunicMalus object) {
        return model;
    }

    public ResourceLocation getTextureResource(RunicMalus object) {
        return texture;
    }
}

