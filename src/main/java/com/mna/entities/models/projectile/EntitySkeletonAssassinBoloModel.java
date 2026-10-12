/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models.projectile;

import com.mna.entities.projectile.SkeletonAssassinBolo;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EntitySkeletonAssassinBoloModel
extends GeoModel<SkeletonAssassinBolo> {
    private static final ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath("mna", "geo/bolo.geo.json");
    private static final ResourceLocation animFile = ResourceLocation.fromNamespaceAndPath("mna", "animations/none.anim.json");
    private static final ResourceLocation texFile = ResourceLocation.parse("textures/entity/lead_knot.png");

    public ResourceLocation getAnimationResource(SkeletonAssassinBolo arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(SkeletonAssassinBolo arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(SkeletonAssassinBolo arg0) {
        return texFile;
    }
}

