/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HorseModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.AbstractHorseRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 */
package com.mna.entities.renderers.sorcery;

import com.mna.api.tools.RLoc;
import com.mna.entities.summon.SummonedSpectralHorse;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SpectralHorseRenderer
extends AbstractHorseRenderer<SummonedSpectralHorse, HorseModel<SummonedSpectralHorse>> {
    private static final ResourceLocation SPECTRAL_HORSE_TEXTURE = RLoc.create("textures/entity/spectral_horse.png");

    public SpectralHorseRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), 1.1f);
    }

    public ResourceLocation getTextureLocation(SummonedSpectralHorse entity) {
        return SPECTRAL_HORSE_TEXTURE;
    }

    protected RenderType getRenderType(SummonedSpectralHorse entity, boolean visible, boolean notInvisibleToPlayer, boolean glowing) {
        if (visible && !notInvisibleToPlayer && !glowing) {
            return RenderType.entityTranslucentCull((ResourceLocation)SPECTRAL_HORSE_TEXTURE);
        }
        return super.getRenderType(entity, visible, notInvisibleToPlayer, glowing);
    }
}

