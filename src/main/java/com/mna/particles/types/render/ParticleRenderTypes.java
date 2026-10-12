/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.particle.ParticleRenderType
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.particles.types.render;

import com.mna.api.config.ClientConfigValues;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class ParticleRenderTypes {
    public static final ParticleRenderType ADDITIVE = new ParticleRenderType(){

        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            Minecraft mc = Minecraft.getInstance();
            RenderSystem.depthMask((boolean)false);
            mc.gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.enableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc((int)GlStateManager.SourceFactor.SRC_ALPHA.value, (int)GlStateManager.DestFactor.ONE.value);
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)TextureAtlas.LOCATION_PARTICLES);
            AbstractTexture tex = textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES);
            tex.setBlurMipmap(ClientConfigValues.ParticleBlur, false);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(Tesselator tessellator) {
            tessellator.end();
            Minecraft mc = Minecraft.getInstance();
            mc.textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
            RenderSystem.disableBlend();
            RenderSystem.depthMask((boolean)true);
        }

        public String toString() {
            return "MNA_ADDITIVE";
        }
    };
    public static final ParticleRenderType INVERTED = new ParticleRenderType(){

        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.depthMask((boolean)false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate((int)GlStateManager.SourceFactor.SRC_ALPHA.value, (int)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value, (int)1, (int)0);
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)TextureAtlas.LOCATION_PARTICLES);
            AbstractTexture tex = textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES);
            tex.setBlurMipmap(ClientConfigValues.ParticleBlur, false);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(Tesselator tessellator) {
            tessellator.end();
            Minecraft mc = Minecraft.getInstance();
            mc.textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap();
            RenderSystem.disableBlend();
            RenderSystem.depthMask((boolean)true);
        }

        public String toString() {
            return "MNA_INVERTED";
        }
    };
}


