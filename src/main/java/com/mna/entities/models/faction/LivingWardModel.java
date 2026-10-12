/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models.faction;

import com.mna.entities.faction.LivingWard;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LivingWardModel
extends GeoModel<LivingWard> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/arcane_eye.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/arcane_eye.animation.json");
    private static final ResourceLocation texFile_t1 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/arcane_eye_t1.png");
    private static final ResourceLocation texFile_t2 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/arcane_eye_t1.png");
    private static final ResourceLocation texFile_t3 = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/arcane_eye_t1.png");

    public ResourceLocation getAnimationResource(LivingWard arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(LivingWard arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(LivingWard arg0) {
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

