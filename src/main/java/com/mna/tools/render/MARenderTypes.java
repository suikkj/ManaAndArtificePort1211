/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.Util
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$LineStateShard
 *  net.minecraft.client.renderer.RenderStateShard$ShaderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TextureStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterShadersEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.tools.render;

import com.mna.api.tools.RLoc;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.OptionalDouble;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

public class MARenderTypes
extends RenderType {
    private static final ResourceLocation SPARKLE_TEXTURE = RLoc.create("textures/particle/sparkle.png");
    public static final ResourceLocation TEXTURE_BEACON_BEAM = RLoc.create("textures/particle/beam_b.png");
    public static final ResourceLocation TEXTURE_BEAM_C = RLoc.create("textures/particle/beam_c.png");
    public static final ResourceLocation TEXTURE_VINE = RLoc.create("textures/entity/boss/vine.png");
    public static final ResourceLocation PORTAL_TEXTURE = RLoc.create("textures/entity/vortex.png");
    public static final ResourceLocation RTP_PORTAL_TEXTURE = RLoc.create("textures/entity/vortex_rainbow.png");
    public static final ResourceLocation BRIGHT_LIGHT_TEXTURE = RLoc.create("textures/entity/fx/bright_light.png");
    public static final ResourceLocation MAGELIGHT_TEXTURE = RLoc.create("textures/block/magelight_ring.png");
    public static final ResourceLocation PORTAL_TEXTURE_RAID = RLoc.create("textures/entity/vortex_raid.png");
    public static final ResourceLocation TEXTURE_ORB = RLoc.create("textures/block/lightorb.png");
    public static final ResourceLocation TEXTURE_WARD = RLoc.create("textures/block/ward.png");
    public static final RenderType MARKING_RUNE_MARK = MARenderTypes.create((String)"marking_rune_loc", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_NORMAL, (VertexFormat.Mode)VertexFormat.Mode.LINES, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(OUTLINE_TARGET).setWriteMaskState(COLOR_WRITE).setDepthTestState(NO_DEPTH_TEST).setCullState(NO_CULL).createCompositeState(false));
    public static final RenderType MARKING_RUNE_AREA = MARenderTypes.create((String)"marking_area_loc", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_NORMAL, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setTextureState(NO_TEXTURE).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(OUTLINE_TARGET).setWriteMaskState(COLOR_WRITE).setDepthTestState(NO_DEPTH_TEST).setCullState(NO_CULL).createCompositeState(false));
    public static final RenderType WARD_BLOCK = MARenderTypes.create((String)"ward_block", (VertexFormat)DefaultVertexFormat.BLOCK, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TRANSLUCENT_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(TEXTURE_WARD, false, true)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(OUTLINE_TARGET).createCompositeState(false));
    public static final RenderType RENDER_TYPE_MANAWEAVE = MARenderTypes.create((String)"manaweave_dots", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_BEACON_BEAM_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(SPARKLE_TEXTURE, false, false)).setTransparencyState(LIGHTNING_TRANSPARENCY).setOutputState(TRANSLUCENT_TARGET).setLightmapState(LIGHTMAP).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(true));
    public static final RenderType RENDER_TYPE_MANAWEAVE_SOLID = MARenderTypes.create((String)"manaweave_dots_solid", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)true, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(SPARKLE_TEXTURE, false, false)).setTransparencyState(NO_TRANSPARENCY).setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(true));
    public static final RenderType RADIANT_RENDER_TYPE = MARenderTypes.create((String)"radiant", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setWriteMaskState(COLOR_WRITE).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(PARTICLES_TARGET).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).createCompositeState(false));
    public static final RenderType SOLID_RENDER_TYPE = MARenderTypes.create((String)"mna_solid", (VertexFormat)DefaultVertexFormat.POSITION_COLOR, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LIGHTNING_SHADER).setWriteMaskState(COLOR_WRITE).setTransparencyState(NO_TRANSPARENCY).setOutputState(PARTICLES_TARGET).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).createCompositeState(false));
    public static final RenderType RITUAL_BEAM_RENDER_TYPE = MARenderTypes.create((String)"beam", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_BEACON_BEAM_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(TEXTURE_BEACON_BEAM, false, false)).setOutputState(PARTICLES_TARGET).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
    public static final RenderType SPELL_BEAM_RENDER_TYPE = MARenderTypes.create((String)"beam", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_BEACON_BEAM_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(TEXTURE_BEAM_C, false, false)).setOutputState(PARTICLES_TARGET).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
    public static final RenderType BOSS_VINE = MARenderTypes.create((String)"beam", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER).setLightmapState(LIGHTMAP).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(TEXTURE_VINE, false, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).createCompositeState(false));
    public static final RenderType PORTAL_RENDER = MARenderTypes.create((String)"portal_render_type", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(PORTAL_TEXTURE, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(TRANSLUCENT_TARGET).setLightmapState(LIGHTMAP).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setOverlayState(NO_OVERLAY).createCompositeState(false));
    public static final RenderType RTP_PORTAL_RENDER = MARenderTypes.create((String)"portal_render_type", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(RTP_PORTAL_TEXTURE, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(TRANSLUCENT_TARGET).setLightmapState(LIGHTMAP).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setOverlayState(NO_OVERLAY).createCompositeState(false));
    public static final RenderType BRIGHT_LIGHT_RENDER = MARenderTypes.create((String)"portal_render_type", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(BRIGHT_LIGHT_TEXTURE, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(TRANSLUCENT_TARGET).setLightmapState(LIGHTMAP).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setOverlayState(NO_OVERLAY).createCompositeState(false));
    public static final RenderType ORB_RENDER = MARenderTypes.create((String)"orb_render_type", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(TEXTURE_ORB, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(TRANSLUCENT_TARGET).setLightmapState(LIGHTMAP).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).setOverlayState(NO_OVERLAY).createCompositeState(false));
    public static final RenderType RAID_PORTAL_RENDER = MARenderTypes.create((String)"portal_render_type", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(PORTAL_TEXTURE_RAID, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setWriteMaskState(COLOR_WRITE).setOverlayState(OVERLAY).createCompositeState(true));

    public MARenderTypes(String name, VertexFormat format, VertexFormat.Mode p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable runnablePre, Runnable runnablePost) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, runnablePre, runnablePost);
    }

    public static RenderType PORTAL_UVROTATE(ResourceLocation texture) {
        return CustomRenderTypes.PORTAL_UVROTATE.apply(texture);
    }

    private static class CustomRenderTypes {
        private static ShaderInstance portalUVRotate;
        private static final RenderStateShard.ShaderStateShard RENDERTYPE_PORTAL_UVROTATE;
        public static Function<ResourceLocation, RenderType> PORTAL_UVROTATE;

        private CustomRenderTypes() {
        }

        private static RenderType uvRotate(ResourceLocation rLoc) {
            RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder().setShaderState(RENDERTYPE_PORTAL_UVROTATE).setTextureState((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(rLoc, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).createCompositeState(false);
            return RenderType.create((String)"mna_portal_uvrotate", (VertexFormat)DefaultVertexFormat.NEW_ENTITY, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)256, (boolean)false, (boolean)false, (RenderType.CompositeState)rendertype$state);
        }

        static {
            RENDERTYPE_PORTAL_UVROTATE = new RenderStateShard.ShaderStateShard(() -> portalUVRotate);
            PORTAL_UVROTATE = Util.memoize(CustomRenderTypes::uvRotate);
        }
    }

    @EventBusSubscriber(value={Dist.CLIENT}, modid="mna", bus=EventBusSubscriber.Bus.MOD)
    public static class MARenderClientEvents {
        @SubscribeEvent
        public static void registerCustomShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), RLoc.create("portal_uv_rotate"), DefaultVertexFormat.NEW_ENTITY), shaderInstance -> {
                CustomRenderTypes.portalUVRotate = shaderInstance;
            });
        }
    }
}

