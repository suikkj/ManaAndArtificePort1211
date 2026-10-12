/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.items.renderers.models;

import com.mna.api.tools.RLoc;
import com.mna.items.sorcery.bound.BoundAxe;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelBoundAxe
extends GeoModel<BoundAxe> {
    private static final ResourceLocation anim = RLoc.create("animations/bound_swordaxe.animation.json");
    private static final ResourceLocation model = RLoc.create("geo/bound_axe.geo.json");
    private static final ResourceLocation texture = RLoc.create("textures/item/sorcery/bound_item.png");

    public ResourceLocation getAnimationResource(BoundAxe animatable) {
        return anim;
    }

    public ResourceLocation getModelResource(BoundAxe object) {
        return model;
    }

    public ResourceLocation getTextureResource(BoundAxe object) {
        return texture;
    }
}

