/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.blocks.tileentities.models;

import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ModelInscriptionTable
extends GeoModel<InscriptionTableTile> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/inscription_table.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/inscription_table.animation.json");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/block/inscription_table_deco.png");

    public ResourceLocation getAnimationResource(InscriptionTableTile arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(InscriptionTableTile arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(InscriptionTableTile arg0) {
        return texFile;
    }
}

