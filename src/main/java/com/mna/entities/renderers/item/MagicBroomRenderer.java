/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.entities.renderers.item;

import com.mna.entities.constructs.MagicBroom;
import com.mna.entities.models.MagicBroomModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class MagicBroomRenderer
extends LivingEntityRenderer<MagicBroom, MagicBroomModel<MagicBroom>> {
    public static final ResourceLocation MAGIC_BROOM_TEXTURE = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/broom.png");
    public static final ResourceLocation VORTEX_BROOM_TEXTURE = ResourceLocation.fromNamespaceAndPath("mna", "textures/entity/vortex_broom.png");

    public MagicBroomRenderer(EntityRendererProvider.Context context) {
        super(context, new MagicBroomModel(Minecraft.getInstance().getEntityModels().bakeLayer(MagicBroomModel.LAYER_LOCATION)), 0.5f);
    }

    public ResourceLocation getTextureLocation(MagicBroom entity) {
        return entity.getBroomType() == 0 ? MAGIC_BROOM_TEXTURE : VORTEX_BROOM_TEXTURE;
    }

    protected boolean shouldShowName(MagicBroom entity) {
        return false;
    }
}

