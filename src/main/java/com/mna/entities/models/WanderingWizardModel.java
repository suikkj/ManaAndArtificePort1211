/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models;

import com.mna.entities.utility.WanderingWizard;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WanderingWizardModel
extends GeoModel<WanderingWizard> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/wandering_wizard.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/wandering_wizard.anim.json");
    private static final ResourceLocation texFile = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/wandering_wizard.png");

    public ResourceLocation getAnimationResource(WanderingWizard arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(WanderingWizard arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(WanderingWizard arg0) {
        return texFile;
    }
}

