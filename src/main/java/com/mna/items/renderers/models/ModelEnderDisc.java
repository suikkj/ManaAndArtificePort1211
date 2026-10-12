/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.items.renderers.models;

import com.mna.api.tools.RLoc;
import com.mna.items.artifice.EnderDisk;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelEnderDisc
extends GeoModel<EnderDisk> {
    private static final ResourceLocation anim = RLoc.create("animations/ender_disc_armature.animation.json");
    private static final ResourceLocation model = RLoc.create("geo/ender_disc_armature.geo.json");
    private static final ResourceLocation texture = RLoc.create("textures/item/artifice/ender_disc.png");

    public ResourceLocation getAnimationResource(EnderDisk animatable) {
        return anim;
    }

    public ResourceLocation getModelResource(EnderDisk object) {
        return model;
    }

    public ResourceLocation getTextureResource(EnderDisk object) {
        return texture;
    }
}

