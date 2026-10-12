/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models.boss;

import com.mna.entities.boss.Odin;
import com.mna.events.seasonal.SeasonalHelper;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OdinModel
extends GeoModel<Odin> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/bosses/odin.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/bosses/odin.animation.json");
    private static final ResourceLocation texFile_xmas = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/boss/odin_holiday.png");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/boss/odin.png");

    public ResourceLocation getAnimationResource(Odin arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(Odin arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(Odin arg0) {
        if (SeasonalHelper.isChristmas()) {
            return texFile_xmas;
        }
        return texFile;
    }
}

