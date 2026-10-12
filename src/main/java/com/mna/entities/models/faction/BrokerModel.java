/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.entities.models.faction;

import com.mna.api.tools.RLoc;
import com.mna.entities.faction.Broker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class BrokerModel
extends GeoModel<Broker> {
    private static final ResourceLocation modelFile = RLoc.create("geo/the_broker.geo.json");
    private static final ResourceLocation animFile = RLoc.create("animations/the_broker.anim.json");
    private static final ResourceLocation texFile = RLoc.create("textures/entity/the_broker.png");

    public ResourceLocation getAnimationResource(Broker arg0) {
        return animFile;
    }

    public ResourceLocation getModelResource(Broker arg0) {
        return modelFile;
    }

    public ResourceLocation getTextureResource(Broker arg0) {
        return texFile;
    }

    public void setCustomAnimations(Broker animatable, long instanceId, AnimationState<Broker> animationState) {
        super.setCustomAnimations((GeoAnimatable)animatable, instanceId, animationState);
        GeoBone head = this.getAnimationProcessor().getBone("head");
        float f = Mth.rotLerp((float)animationState.getPartialTick(), (float)animatable.yBodyRotO, (float)animatable.yBodyRot);
        float f1 = Mth.rotLerp((float)animationState.getPartialTick(), (float)animatable.yHeadRotO, (float)animatable.yHeadRot);
        float f2 = f1 - f;
        float f6 = Mth.lerp((float)animationState.getPartialTick(), (float)animatable.xRotO, (float)animatable.getXRot());
        head.setRotY((float)((double)(360.0f - f2) * Math.PI / 180.0));
        head.setRotX((float)((double)f6 * Math.PI / 180.0));
    }
}

