/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoArmorRenderer
 */
package com.mna.items.renderers;

import com.mna.items.armor.DemonArmorItem;
import com.mna.items.armor.models.DemonArmorModel;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DemonArmorRenderer
extends GeoArmorRenderer<DemonArmorItem> {
    private static final RenderType EMISSIVE = RenderType.eyes((ResourceLocation)ResourceLocation.fromNamespaceAndPath("mna", "textures/item/armor/demon_armor.png"));

    public DemonArmorRenderer() {
        super((GeoModel)new DemonArmorModel());
    }

    public RenderType getRenderType(DemonArmorItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return super.getRenderType((Item)animatable, texture, bufferSource, partialTick);
    }
}

