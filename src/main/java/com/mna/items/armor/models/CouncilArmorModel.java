/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.items.armor.models;

import com.mna.items.armor.CouncilArmorItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CouncilArmorModel
extends GeoModel<CouncilArmorItem> {
    public ResourceLocation getModelResource(CouncilArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("mna", "geo/armor/council_armor.geo.json");
    }

    public ResourceLocation getTextureResource(CouncilArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("mna", "textures/item/armor/model/council_armor.png");
    }

    public ResourceLocation getAnimationResource(CouncilArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mna", "animations/armor/council_armor.animation.json");
    }

    public RenderType getRenderType(CouncilArmorItem animatable, ResourceLocation texture) {
        return RenderType.entityTranslucentEmissive((ResourceLocation)texture);
    }
}

