/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.world.level.Level
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.api.config.ClientConfigValues;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.tileentities.MagelightTile;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.Level;

public class MageLightRenderer
implements BlockEntityRenderer<MagelightTile> {
    public MageLightRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void render(MagelightTile tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!Minecraft.getInstance().isPaused()) {
            this.spawnGlowParticles(tileEntityIn);
        }
    }

    private void spawnGlowParticles(MagelightTile tileEntityIn) {
        MAParticleType particle;
        Level level = tileEntityIn.getLevel();
        BlockPos worldPosition = tileEntityIn.getBlockPos();
        Random rnd = new Random();
        float offsetScale = 0.25f;
        float offset = rnd.nextFloat();
        offset *= offsetScale;
        float flitChance = ClientConfigValues.FancyMagelights ? 0.25f : 0.05f;
        float centerChance = ClientConfigValues.FancyMagelights ? 0.15f : 0.05f;
        int color = tileEntityIn.getColor();
        int[] colors = null;
        if (color != -1) {
            colors = new int[]{FastColor.ARGB32.red((int)color), FastColor.ARGB32.green((int)color), FastColor.ARGB32.blue((int)color)};
        }
        if (Math.random() < (double)flitChance) {
            particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get()));
            if (color != -1) {
                particle.setColor(colors[0], colors[1], colors[2]);
            }
            level.addParticle((ParticleOptions)particle, (double)worldPosition.getX() + 0.5 + ((double)(-offset) + rnd.nextDouble() * (double)offset * 2.0), (double)worldPosition.getY() + 0.5 + ((double)(-offset) + rnd.nextDouble() * (double)offset * 2.0), (double)worldPosition.getZ() + 0.5 + ((double)(-offset) + rnd.nextDouble() * (double)offset * 2.0), 0.0, (double)0.05f, 0.0);
        }
        if (Math.random() < (double)centerChance) {
            particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get()));
            if (color != -1) {
                particle.setColor(colors[0], colors[1], colors[2]);
            }
            level.addParticle((ParticleOptions)particle, (double)worldPosition.getX() + 0.5, (double)worldPosition.getY() + 0.5, (double)worldPosition.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }
}

