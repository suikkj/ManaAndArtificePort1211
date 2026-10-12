/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoItemRenderer
 */
package com.mna.items.renderers.bound;

import com.mna.items.renderers.models.ModelBoundShield;
import com.mna.items.sorcery.bound.BoundShield;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BoundShieldItemRenderer
extends GeoItemRenderer<BoundShield> {
    public BoundShieldItemRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super((GeoModel)new ModelBoundShield());
    }
}

