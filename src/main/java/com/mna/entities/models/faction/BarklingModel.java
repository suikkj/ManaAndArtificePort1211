/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models.faction;

import com.mna.entities.faction.Barkling;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BarklingModel
extends GeoModel<Barkling> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/barkling.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/barkling.animation.json");
    private static final ResourceLocation texFile_t1 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/barkling_t1.png");
    private static final ResourceLocation texFile_t2 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/barkling_t2.png");
    private static final ResourceLocation texFile_t3 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/barkling_t3.png");

    public ResourceLocation getAnimationResource(Barkling arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(Barkling arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(Barkling arg0) {
        switch (arg0.getTier()) {
            case 0: {
                return texFile_t1;
            }
            case 1: {
                return texFile_t2;
            }
            case 2: {
                return texFile_t3;
            }
        }
        return texFile_t1;
    }
}

