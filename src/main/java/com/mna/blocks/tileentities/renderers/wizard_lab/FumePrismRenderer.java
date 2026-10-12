/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoBlockRenderer
 */
package com.mna.blocks.tileentities.renderers.wizard_lab;

import com.mna.blocks.tileentities.renderers.models.FumePrismModel;
import com.mna.blocks.tileentities.wizard_lab.FumePrismTile;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FumePrismRenderer
extends GeoBlockRenderer<FumePrismTile> {
    public FumePrismRenderer(BlockEntityRendererProvider.Context context) {
        super((GeoModel)new FumePrismModel());
    }
}

