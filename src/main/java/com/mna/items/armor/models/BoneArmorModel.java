/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.items.armor.models;

import com.mna.items.armor.BoneArmorItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BoneArmorModel
extends GeoModel<BoneArmorItem> {
    public ResourceLocation getModelResource(BoneArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("mna", "geo/armor/undead_armor.geo.json");
    }

    public ResourceLocation getTextureResource(BoneArmorItem object) {
        return ResourceLocation.fromNamespaceAndPath("mna", "textures/item/armor/model/undead_armor.png");
    }

    public ResourceLocation getAnimationResource(BoneArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("mna", "animations/armor/undead_armor.animation.json");
    }
}

