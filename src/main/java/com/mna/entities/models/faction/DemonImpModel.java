/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models.faction;

import com.mna.entities.faction.DemonImp;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DemonImpModel
extends GeoModel<DemonImp> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/imp.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/imp.animation.json");
    private static final ResourceLocation texFile_t1 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/imp_t1.png");
    private static final ResourceLocation texFile_t2 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/imp_t2.png");
    private static final ResourceLocation texFile_t3 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/imp_t3.png");

    public ResourceLocation getAnimationResource(DemonImp arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(DemonImp arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(DemonImp arg0) {
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

