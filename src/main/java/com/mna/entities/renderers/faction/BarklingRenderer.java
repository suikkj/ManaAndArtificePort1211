/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.animatable.GeoAnimatable
 */
package com.mna.entities.renderers.faction;

import com.mna.entities.faction.Barkling;
import com.mna.entities.models.faction.BarklingModel;
import com.mna.entities.renderers.faction.MAGeckoFactionRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.animatable.GeoAnimatable;

public class BarklingRenderer
extends MAGeckoFactionRenderer<Barkling> {
    public BarklingRenderer(EntityRendererProvider.Context context) {
        super(context, new BarklingModel());
        this.enableEmissive();
    }

    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, Barkling animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        poseStack.scale(0.7f, 0.7f, 0.7f);
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}

