/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 */
package com.mna.entities.renderers.ritual;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.tools.RLoc;
import com.mna.entities.rituals.Portal;
import com.mna.factions.Factions;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class PortalRenderer
extends EntityRenderer<Portal> {
    public static final ResourceLocation model_mushroom = RLoc.create("fx/mushroom");
    public static final ResourceLocation model_pink_flower = RLoc.create("fx/flower_pink");
    public static final ResourceLocation model_yellow_flower = RLoc.create("fx/flower_yellow");
    public static final ResourceLocation model_purple_flower = RLoc.create("fx/flower_purple");
    public static final ResourceLocation model_council_wedge = RLoc.create("fx/wedge");
    public static final ResourceLocation model_undead_fissure = RLoc.create("fx/fissure");
    public static final ResourceLocation model_demon_portal = RLoc.create("fx/demon_portal");
    public static final ResourceLocation model_demon_portal_ring = RLoc.create("fx/demon_portal_ring");
    public static final ResourceLocation[] model_demon_portal_stones = new ResourceLocation[]{RLoc.create("fx/demon_portal_stone1"), RLoc.create("fx/demon_portal_stone2"), RLoc.create("fx/demon_portal_stone3"), RLoc.create("fx/demon_portal_stone4"), RLoc.create("fx/demon_portal_stone5"), RLoc.create("fx/demon_portal_stone6"), RLoc.create("fx/demon_portal_stone7")};
    public static final ResourceLocation[] model_demon_portal_runes = new ResourceLocation[]{RLoc.create("fx/demon_portal_runes1"), RLoc.create("fx/demon_portal_runes2"), RLoc.create("fx/demon_portal_runes3"), RLoc.create("fx/demon_portal_runes4"), RLoc.create("fx/demon_portal_runes5"), RLoc.create("fx/demon_portal_runes6"), RLoc.create("fx/demon_portal_runes7")};
    private static final int[] defaultColor = new int[]{19, 184, 74};

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(Portal entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        int[] portalDye;
        DyeColor dc = entityIn.getDyeColor();
        if (dc != null) {
            int[] nArray = new int[3];
            nArray[0] = (dc.getTextureDiffuseColor() >> 16) & 0xFF;
            nArray[1] = (dc.getTextureDiffuseColor() >> 8) & 0xFF;
            portalDye = nArray;
            nArray[2] = dc.getTextureDiffuseColor() & 0xFF;
        } else {
            portalDye = null;
        }
        if (entityIn.isRTP()) {
            this.renderDefaultPortal(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, new int[]{255, 255, 255});
        } else {
            Pair<IFaction, Byte> renderData = entityIn.getRenderData();
            if (renderData == null) {
                this.renderDefaultPortal(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, portalDye != null ? portalDye : defaultColor);
            } else if (renderData.getFirst() == Factions.COUNCIL) {
                this.renderCouncilPortal(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, portalDye != null ? portalDye : Affinity.ARCANE.getSecondaryColor());
            } else if (renderData.getFirst() == Factions.UNDEAD) {
                int[] nArray;
                if (portalDye != null) {
                    nArray = portalDye;
                } else {
                    int[] nArray2 = new int[3];
                    nArray2[0] = 113;
                    nArray2[1] = 221;
                    nArray = nArray2;
                    nArray2[2] = 151;
                }
                this.renderUndeadPortal(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, nArray);
            } else if (renderData.getFirst() == Factions.DEMONS) {
                int[] nArray;
                if (portalDye != null) {
                    nArray = portalDye;
                } else {
                    int[] nArray3 = new int[3];
                    nArray3[0] = 255;
                    nArray3[1] = 0;
                    nArray = nArray3;
                    nArray3[2] = 0;
                }
                this.renderDemonPortal(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, nArray);
            } else if (renderData.getFirst() == Factions.FEY) {
                if ((Byte)renderData.getSecond() == 0) {
                    int[] nArray;
                    if (portalDye != null) {
                        nArray = portalDye;
                    } else {
                        int[] nArray4 = new int[3];
                        nArray4[0] = 238;
                        nArray4[1] = 220;
                        nArray = nArray4;
                        nArray4[2] = 165;
                    }
                    this.renderSummerFeyPortal(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, nArray);
                } else {
                    this.renderWinterFeyPortal(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, portalDye != null ? portalDye : Affinity.ICE.getColor());
                }
            } else {
                this.renderDefaultPortal(entityIn, matrixStackIn, bufferIn, packedLightIn, partialTicks, portalDye != null ? portalDye : defaultColor);
            }
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private float getSpawnPct(Portal portal, int spawnFXDuration, float partialTick) {
        return Math.min((float)portal.getAge() + partialTick, (float)spawnFXDuration) / (float)spawnFXDuration;
    }

    private void renderDefaultPortal(Portal portal, PoseStack pose, MultiBufferSource bufferSource, int packedLight, float partialTick, int[] color) {
        float scaleFactor = 3.0f * this.getSpawnPct(portal, 20, partialTick);
        float portalSpinDegrees = portal.getAge() * 3 % 360;
        float verticalOffset = 1.25f;
        VertexConsumer vertexBuilder = bufferSource.getBuffer(portal.isRTP() ? MARenderTypes.RTP_PORTAL_RENDER : MARenderTypes.PORTAL_RENDER);
        this.renderPortalTexture(pose, vertexBuilder, packedLight, color, 230, verticalOffset, scaleFactor, portalSpinDegrees, 0.0f, true);
        pose.translate(0.0f, 0.0f, 0.05f);
        this.renderPortalTexture(pose, vertexBuilder, packedLight, color, 230, verticalOffset, scaleFactor, -portalSpinDegrees, 0.0f, true);
    }

    private void renderWinterFeyPortal(Portal portal, PoseStack pose, MultiBufferSource bufferSource, int packedLight, float partialTick, int[] color) {
        float spawnPct = this.getSpawnPct(portal, 80, partialTick);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());
        int numMushrooms = (int)Math.min(spawnPct * 20.0f, 10.0f);
        for (int i = 0; i < numMushrooms; ++i) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees((float)(36 * i)));
            pose.translate(1.5f, 0.0f, 0.0f);
            pose.mulPose(Axis.YP.rotationDegrees((float)(15 * i)));
            if (i == numMushrooms - 1 && spawnPct < 0.55f) {
                float mushroomScale = spawnPct % 0.05f * 20.0f;
                pose.scale(mushroomScale, mushroomScale, mushroomScale);
            }
            ModelUtils.renderEntityModel(builder, portal.level(), model_mushroom, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
            pose.popPose();
        }
        if (spawnPct < 0.6f) {
            return;
        }
        float raySpawnPct = Math.min(spawnPct - 0.6f, 0.3f) / 0.3f;
        int rayAngleStep = 2;
        int numRays = 15;
        pose.pushPose();
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
        pose.mulPose(portalRotation);
        pose.translate(-1.0f, 2.0f, 0.0f);
        Random rnd = new Random(13L);
        for (int i = 0; i < numRays; ++i) {
            int offset = rnd.nextInt(500);
            double alphaSin = Math.sin(((float)portal.getAge() + partialTick) / 5.0f + (float)(offset * i));
            int alpha = (int)((25.0 + 20.0 * alphaSin) * (double)raySpawnPct);
            WorldRenderUtils.renderLightRay(0.0f, pose, bufferSource, color, Affinity.ICE.getSecondaryColor(), alpha, 10.0f, 170 + rayAngleStep * i);
        }
        pose.popPose();
        if (spawnPct < 0.9f) {
            return;
        }
        float ringSpawnPct = (spawnPct - 0.9f) * 10.0f;
        pose.pushPose();
        int time = (int)(portal.level().getGameTime() % 24000L) / 600;
        int spin = Math.abs(20 - time);
        this.renderPortalTexture(pose, bufferSource.getBuffer(MARenderTypes.BRIGHT_LIGHT_RENDER), packedLight, color, (int)(230.0f * ringSpawnPct), 0.1f, 4.0f, spin, 90.0f, false);
        pose.popPose();
    }

    private void renderSummerFeyPortal(Portal portal, PoseStack pose, MultiBufferSource bufferSource, int packedLight, float partialTick, int[] color) {
        float spawnPct = this.getSpawnPct(portal, 80, partialTick);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());
        ResourceLocation[] flowers = new ResourceLocation[]{model_pink_flower, model_yellow_flower, model_purple_flower};
        int numFlowers = (int)Math.min(spawnPct * 20.0f, 9.0f);
        for (int i = 0; i < numFlowers; ++i) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(-40.56f * (float)i));
            pose.translate(1.5f, 0.0f, 0.0f);
            pose.mulPose(Axis.YP.rotationDegrees((float)(15 * i)));
            if (i == numFlowers - 1 && spawnPct < 0.5f) {
                float flowerScale = Math.min(spawnPct % 0.05f * 20.0f, 1.0f);
                pose.scale(flowerScale, flowerScale, flowerScale);
            }
            ModelUtils.renderEntityModel(builder, portal.level(), flowers[i % 3], pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
            pose.popPose();
        }
        if (spawnPct < 0.6f) {
            return;
        }
        float ringSpawnPct = Math.min((spawnPct - 0.6f) * 10.0f, 1.0f);
        int spin = 60;
        this.renderPortalTexture(pose, bufferSource.getBuffer(MARenderTypes.BRIGHT_LIGHT_RENDER), packedLight, color, (int)(230.0f * ringSpawnPct), 0.05f, 4.0f, spin, 90.0f, false);
        if (spawnPct < 0.7f) {
            return;
        }
        float raySpawnPct = Math.min(spawnPct - 0.7f, 0.3f) / 0.3f;
        int rayAngleStep = 15;
        int numRays = 10;
        pose.pushPose();
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
        pose.mulPose(portalRotation);
        Random rnd = new Random(13L);
        for (int i = 0; i < numRays; ++i) {
            int offset = rnd.nextInt(500);
            double alphaSin = Math.sin(((float)portal.getAge() + partialTick) / 5.0f + (float)(offset * i));
            int alpha = (int)(25.0 + 20.0 * alphaSin);
            WorldRenderUtils.renderLightRay(0.0f, pose, bufferSource, new int[]{242, 169, 78}, new int[]{255, 77, 0}, (int)((float)alpha * raySpawnPct), 10.0f, -90 + rayAngleStep * i);
        }
        pose.popPose();
    }

    private void renderCouncilPortal(Portal portal, PoseStack pose, MultiBufferSource bufferSource, int packedLight, float partialTick, int[] color) {
        float spawnPct = this.getSpawnPct(portal, 80, partialTick);
        Minecraft mc = Minecraft.getInstance();
        pose.pushPose();
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
        pose.mulPose(portalRotation);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        pose.translate(0.0, 1.5, 0.0);
        if (spawnPct > 0.9f) {
            PoseStack.Pose matrixstack$entry = pose.last();
            Matrix4f renderMatrix = matrixstack$entry.pose();
            VertexConsumer vertexBuilder = bufferSource.getBuffer(RenderType.lightning());
            int hexagonAlpha = (int)((double)Math.min(200 + (int)(32.0 * Math.sin((double)((float)mc.level.getGameTime() + partialTick) * 0.05)), 230) * ((double)spawnPct - 0.9) * 10.0);
            vertexBuilder.addVertex(matrixstack$entry, -0.5f, -1.0f, 0.0f).setColor(color[0], color[1], color[2], hexagonAlpha);
            vertexBuilder.addVertex(matrixstack$entry, 0.5f, -1.0f, 0.0f).setColor(color[0], color[1], color[2], hexagonAlpha);
            vertexBuilder.addVertex(matrixstack$entry, 1.0f, 0.0f, 0.0f).setColor(color[0], color[1], color[2], hexagonAlpha);
            vertexBuilder.addVertex(matrixstack$entry, -1.0f, 0.0f, 0.0f).setColor(color[0], color[1], color[2], hexagonAlpha);
            vertexBuilder.addVertex(matrixstack$entry, -1.0f, 0.0f, 0.0f).setColor(color[0], color[1], color[2], hexagonAlpha);
            vertexBuilder.addVertex(matrixstack$entry, 1.0f, 0.0f, 0.0f).setColor(color[0], color[1], color[2], hexagonAlpha);
            vertexBuilder.addVertex(matrixstack$entry, 0.5f, 1.0f, 0.0f).setColor(color[0], color[1], color[2], hexagonAlpha);
            vertexBuilder.addVertex(matrixstack$entry, -0.5f, 1.0f, 0.0f).setColor(color[0], color[1], color[2], hexagonAlpha);
        }
        float jitterScale = 0.025f;
        float jitter = ((float)mc.level.getGameTime() + partialTick) * 0.01f;
        float radiantAnimation = ((float)mc.level.getGameTime() + partialTick) * 0.2f;
        float oneSixth = 0.16666667f;
        if (spawnPct > 0.4f) {
            float radiantSpawnFactor = spawnPct - 0.4f;
            float radiantSpawnPct = Math.min(radiantSpawnFactor / 0.2f, 1.0f);
            float[] curRadiantAlphas = new float[]{MathUtils.clamp01(radiantSpawnPct / oneSixth), MathUtils.clamp01(Math.max(radiantSpawnPct - oneSixth, 0.0f) / oneSixth), MathUtils.clamp01(Math.max(radiantSpawnPct - oneSixth * 2.0f, 0.0f) / oneSixth), MathUtils.clamp01(Math.max(radiantSpawnPct - oneSixth * 3.0f, 0.0f) / oneSixth), MathUtils.clamp01(Math.max(radiantSpawnPct - oneSixth * 4.0f, 0.0f) / oneSixth), MathUtils.clamp01(Math.max(radiantSpawnPct - oneSixth * 5.0f, 0.0f) / oneSixth)};
            pose.pushPose();
            pose.translate(-0.5, -1.0, 0.0);
            pose.pushPose();
            pose.translate(Math.sin(jitter + 10.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
            WorldRenderUtils.renderRadiant(radiantAnimation, pose, bufferSource, color, color, (int)(128.0f * curRadiantAlphas[0]), 1.0f, false);
            pose.popPose();
            if (radiantSpawnPct > oneSixth) {
                pose.translate(1.0f, 0.0f, 0.0f);
                pose.pushPose();
                pose.translate(Math.sin(jitter + 10.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
                WorldRenderUtils.renderRadiant(radiantAnimation + 10.0f, pose, bufferSource, color, color, (int)(128.0f * curRadiantAlphas[1]), 1.0f, false);
                pose.popPose();
            }
            if (radiantSpawnPct > oneSixth * 2.0f) {
                pose.translate(0.5, 1.0, 0.0);
                pose.pushPose();
                pose.translate(Math.sin(jitter + 10.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
                WorldRenderUtils.renderRadiant(radiantAnimation + 20.0f, pose, bufferSource, color, color, (int)(128.0f * curRadiantAlphas[2]), 1.0f, false);
                pose.popPose();
            }
            if (radiantSpawnPct > oneSixth * 3.0f) {
                pose.translate(-0.5, 1.0, 0.0);
                pose.pushPose();
                pose.translate(Math.sin(jitter + 10.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
                WorldRenderUtils.renderRadiant(radiantAnimation + 30.0f, pose, bufferSource, color, color, (int)(128.0f * curRadiantAlphas[3]), 1.0f, false);
                pose.popPose();
            }
            if (radiantSpawnPct > oneSixth * 4.0f) {
                pose.translate(-1.0f, 0.0f, 0.0f);
                pose.pushPose();
                pose.translate(Math.sin(jitter + 10.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
                WorldRenderUtils.renderRadiant(radiantAnimation + 40.0f, pose, bufferSource, color, color, (int)(128.0f * curRadiantAlphas[4]), 1.0f, false);
                pose.popPose();
            }
            if (radiantSpawnPct > oneSixth * 5.0f) {
                pose.translate(-0.5, -1.0, 0.0);
                pose.pushPose();
                pose.translate(Math.sin(jitter + 10.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
                WorldRenderUtils.renderRadiant(radiantAnimation + 50.0f, pose, bufferSource, color, color, (int)(128.0f * curRadiantAlphas[5]), 1.0f, false);
                pose.popPose();
            }
            pose.popPose();
        }
        if (spawnPct > 0.62f) {
            float beamSpawnFactor = spawnPct - 0.62f;
            float beamSpawnPct = Math.min(beamSpawnFactor / 0.2f, 1.0f);
            float[] curBeamLengths = new float[]{MathUtils.clamp01(beamSpawnPct / oneSixth), MathUtils.clamp01(Math.max(beamSpawnPct - oneSixth, 0.0f) / oneSixth), MathUtils.clamp01(Math.max(beamSpawnPct - oneSixth * 2.0f, 0.0f) / oneSixth), MathUtils.clamp01(Math.max(beamSpawnPct - oneSixth * 3.0f, 0.0f) / oneSixth), MathUtils.clamp01(Math.max(beamSpawnPct - oneSixth * 4.0f, 0.0f) / oneSixth), MathUtils.clamp01(Math.max(beamSpawnPct - oneSixth * 5.0f, 0.0f) / oneSixth)};
            float beamWidth = 0.05f;
            pose.pushPose();
            pose.translate(-0.5, -1.0, 0.001);
            WorldRenderUtils.renderBeam((Level)mc.level, partialTick, pose, bufferSource, packedLight, new Vec3(0.0, 0.0, 0.0), new Vec3(1.0, 0.0, 0.0), curBeamLengths[0], new int[]{255, 255, 255}, 176, beamWidth, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            if (beamSpawnPct > oneSixth) {
                pose.translate(1.0f, 0.0f, 0.0f);
                WorldRenderUtils.renderBeam((Level)mc.level, partialTick, pose, bufferSource, packedLight, new Vec3(0.0, 0.0, 0.0), new Vec3(0.5, 1.0, 0.0), curBeamLengths[1], new int[]{255, 255, 255}, 176, beamWidth, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            }
            if (beamSpawnPct > oneSixth * 2.0f) {
                pose.translate(0.5, 1.0, 0.0);
                WorldRenderUtils.renderBeam((Level)mc.level, partialTick, pose, bufferSource, packedLight, new Vec3(0.0, 0.0, 0.0), new Vec3(-0.5, 1.0, 0.0), curBeamLengths[2], new int[]{255, 255, 255}, 176, beamWidth, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            }
            if (beamSpawnPct > oneSixth * 3.0f) {
                pose.translate(-0.5, 1.0, 0.0);
                WorldRenderUtils.renderBeam((Level)mc.level, partialTick, pose, bufferSource, packedLight, new Vec3(0.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 0.0), curBeamLengths[3], new int[]{255, 255, 255}, 176, beamWidth, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            }
            if (beamSpawnPct > oneSixth * 4.0f) {
                pose.translate(-1.0f, 0.0f, 0.0f);
                WorldRenderUtils.renderBeam((Level)mc.level, partialTick, pose, bufferSource, packedLight, new Vec3(0.0, 0.0, 0.0), new Vec3(-0.5, -1.0, 0.0), curBeamLengths[4], new int[]{255, 255, 255}, 176, beamWidth, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            }
            if (beamSpawnPct > oneSixth * 5.0f) {
                pose.translate(-0.5, -1.0, 0.0);
                WorldRenderUtils.renderBeam((Level)mc.level, partialTick, pose, bufferSource, packedLight, new Vec3(0.0, 0.0, 0.0), new Vec3(0.5, -1.0, 0.0), curBeamLengths[5], new int[]{255, 255, 255}, 176, beamWidth, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            }
            pose.popPose();
        }
        float wedgeSpawn = Math.min(spawnPct, 0.3f) / 0.4f + 0.25f;
        float wedgeInset = 0.15f;
        float wedgeScale = 0.75f;
        float wedgeTallFactor = 0.05f;
        VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
        pose.pushPose();
        pose.translate(-0.5 * (double)wedgeSpawn, (double)(-1.0f * wedgeSpawn), 0.0);
        pose.pushPose();
        pose.translate(wedgeInset - wedgeTallFactor, wedgeInset, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(60.0f));
        pose.translate(Math.sin(jitter + 10.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
        pose.scale(wedgeScale, wedgeScale, wedgeScale);
        ModelUtils.renderEntityModel(solid, portal.level(), model_council_wedge, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        pose.popPose();
        pose.translate(1.0f * wedgeSpawn, 0.0f, 0.0f);
        pose.pushPose();
        pose.translate(-wedgeInset + wedgeTallFactor, wedgeInset, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(120.0f));
        pose.translate(Math.sin(jitter + 15.0f) * (double)jitterScale, Math.cos(jitter + 30.0f) * (double)jitterScale, 0.0);
        pose.scale(wedgeScale, wedgeScale, wedgeScale);
        ModelUtils.renderEntityModel(solid, portal.level(), model_council_wedge, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        pose.popPose();
        pose.translate(0.5 * (double)wedgeSpawn, (double)(1.0f * wedgeSpawn), 0.0);
        pose.pushPose();
        pose.translate(-wedgeInset, 0.0f, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(Math.sin(jitter + 20.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
        pose.scale(wedgeScale, wedgeScale, wedgeScale);
        ModelUtils.renderEntityModel(solid, portal.level(), model_council_wedge, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        pose.popPose();
        pose.translate(-2.0f * wedgeSpawn, 0.0f, 0.0f);
        pose.pushPose();
        pose.translate(wedgeInset, 0.0f, 0.0f);
        pose.translate(Math.sin(jitter + 15.0f) * (double)jitterScale, Math.cos(jitter + 30.0f) * (double)jitterScale, 0.0);
        pose.scale(wedgeScale, wedgeScale, wedgeScale);
        ModelUtils.renderEntityModel(solid, portal.level(), model_council_wedge, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        pose.popPose();
        pose.translate(0.5 * (double)wedgeSpawn, (double)(1.0f * wedgeSpawn), 0.0);
        pose.pushPose();
        pose.translate(wedgeInset - wedgeTallFactor, -wedgeInset, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(-60.0f));
        pose.translate(Math.sin(jitter + 10.0f) * (double)jitterScale, Math.cos(jitter + 20.0f) * (double)jitterScale, 0.0);
        pose.scale(wedgeScale, wedgeScale, wedgeScale);
        ModelUtils.renderEntityModel(solid, portal.level(), model_council_wedge, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        pose.popPose();
        pose.translate(1.0f * wedgeSpawn, 0.0f, 0.0f);
        pose.pushPose();
        pose.translate(-wedgeInset + wedgeTallFactor, -wedgeInset, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(-120.0f));
        pose.translate(Math.sin(jitter + 25.0f) * (double)jitterScale, Math.cos(jitter + 50.0f) * (double)jitterScale, 0.0);
        pose.scale(wedgeScale, wedgeScale, wedgeScale);
        ModelUtils.renderEntityModel(solid, portal.level(), model_council_wedge, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        pose.popPose();
        pose.popPose();
        pose.popPose();
    }

    private void renderUndeadPortal(Portal portal, PoseStack pose, MultiBufferSource bufferSource, int packedLight, float partialTick, int[] color) {
        float spawnPct = this.getSpawnPct(portal, 60, partialTick);
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());
        pose.pushPose();
        float translatePct = 1.0f - Math.min(spawnPct * 2.0f, 1.0f);
        float hJitterS = spawnPct > 0.5f ? 0.0f : (float)Math.sin(portal.getAge() * 3) * 0.025f;
        float hJitterC = spawnPct > 0.5f ? 0.0f : (float)Math.cos(portal.getAge() * 3) * 0.025f;
        pose.translate((double)hJitterS, 0.05 - 0.5 * (double)translatePct, (double)(-hJitterC));
        ModelUtils.renderEntityModel(builder, portal.level(), model_undead_fissure, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 0.5f);
        pose.popPose();
        if (spawnPct > 0.5f) {
            float[] rayScales = new float[]{MathUtils.clamp01((spawnPct - 0.5f) * 2.0f), MathUtils.clamp01((spawnPct - 0.6f) / 0.4f), MathUtils.clamp01((spawnPct - 0.7f) / 0.3f), MathUtils.clamp01((spawnPct - 0.8f) / 0.2f), MathUtils.clamp01((spawnPct - 0.9f) / 0.1f)};
            Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
            Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
            int rayAlpha = (int)(150.0 + 50.0 * Math.sin(((float)portal.getAge() + partialTick) / 25.0f));
            float beamSeparation = 0.5f;
            float beamWidth = 0.05f;
            float beamFlare = 0.5f;
            pose.pushPose();
            pose.mulPose(portalRotation);
            pose.translate(-beamSeparation, 0.0f, 0.0f);
            WorldRenderUtils.renderLightBeam(0.0f, pose, bufferSource, color, color, rayAlpha, 10.0f * rayScales[0], beamWidth, beamFlare, 15.0f);
            pose.translate(beamSeparation, 0.0f, 0.0f);
            WorldRenderUtils.renderLightBeam(0.0f, pose, bufferSource, color, color, rayAlpha, 10.0f * rayScales[1], beamWidth, beamFlare, 0.0f);
            pose.translate(beamSeparation, 0.0f, 0.0f);
            WorldRenderUtils.renderLightBeam(0.0f, pose, bufferSource, color, color, rayAlpha, 10.0f * rayScales[2], beamWidth, beamFlare, -15.0f);
            pose.translate(-beamSeparation / 2.0f, 0.0f, 0.0f);
            WorldRenderUtils.renderLightBeam(0.0f, pose, bufferSource, color, color, rayAlpha, 5.0f * rayScales[3], beamWidth, beamFlare, -15.0f);
            pose.translate(-beamSeparation, 0.0f, 0.0f);
            WorldRenderUtils.renderLightBeam(0.0f, pose, bufferSource, color, color, rayAlpha, 5.0f * rayScales[4], beamWidth, beamFlare, 15.0f);
            pose.popPose();
        }
    }

    private void renderDemonPortal(Portal portal, PoseStack pose, MultiBufferSource bufferSource, int packedLight, float partialTick, int[] color) {
        float stonePct;
        float yFloatA = (float)Math.sin(((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTick) / 40.0f) * 0.05f;
        float yFloatB = (float)Math.sin(((float)(ManaAndArtifice.instance.proxy.getGameTicks() + 40L) + partialTick) / 40.0f) * 0.05f;
        float yFloatC = (float)Math.sin(((float)(ManaAndArtifice.instance.proxy.getGameTicks() + 80L) + partialTick) / 40.0f) * 0.05f;
        float[] offsets = new float[]{yFloatA, yFloatB, yFloatC};
        float[] colors = new float[]{(float)color[0] / 255.0f, (float)color[1] / 255.0f, (float)color[2] / 255.0f};
        VertexConsumer builder = bufferSource.getBuffer(RenderType.translucent());
        float spawnPct = this.getSpawnPct(portal, 40, partialTick);
        float oneSeventh = 0.14285715f;
        float[] ringOffsets = new float[]{-1.0f, -1.65f, -2.5f, -3.0f, -2.5f, -1.85f, -1.0f};
        float[] ringHOffsets = new float[]{1.0f, 1.0f, 1.0f, 0.0f, -1.0f, -1.0f, -1.0f};
        float stoneSpawnPct = Math.min(spawnPct, 0.6f) / 0.6f;
        float[] stoneSpawnPcts = new float[]{1.0f - MathUtils.clamp01(stoneSpawnPct / oneSeventh), 1.0f - MathUtils.clamp01(Math.max(stoneSpawnPct - oneSeventh, 0.0f) / oneSeventh), 1.0f - MathUtils.clamp01(Math.max(stoneSpawnPct - oneSeventh * 2.0f, 0.0f) / oneSeventh), 1.0f - MathUtils.clamp01(Math.max(stoneSpawnPct - oneSeventh * 3.0f, 0.0f) / oneSeventh), 1.0f - MathUtils.clamp01(Math.max(stoneSpawnPct - oneSeventh * 4.0f, 0.0f) / oneSeventh), 1.0f - MathUtils.clamp01(Math.max(stoneSpawnPct - oneSeventh * 5.0f, 0.0f) / oneSeventh), 1.0f - MathUtils.clamp01(Math.max(stoneSpawnPct - oneSeventh * 6.0f, 0.0f) / oneSeventh)};
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
        pose.mulPose(portalRotation);
        pose.mulPose(Axis.YP.rotationDegrees(90.0f));
        pose.translate(0.0f, 1.0f, 0.0f);
        if (spawnPct > 0.6f) {
            float time = (float)portal.level().getGameTime() + partialTick;
            float ringScale = (spawnPct - 0.6f) / 0.4f;
            pose.pushPose();
            pose.translate(-0.3, 0.25, 0.0);
            pose.mulPose(Axis.XP.rotationDegrees(time));
            pose.scale(ringScale, ringScale, ringScale);
            pose.translate(0.0, -0.25, 0.0);
            pose.translate(0.0, -0.95, 0.0);
            ModelUtils.renderEntityModel(builder, portal.level(), model_demon_portal, pose, 0xF000F0, OverlayTexture.NO_OVERLAY, colors, 1.0f);
            pose.popPose();
            pose.pushPose();
            pose.translate(-0.25, 0.25, 0.0);
            pose.mulPose(Axis.XP.rotationDegrees(-5.0f * time));
            pose.scale(ringScale, ringScale, ringScale);
            pose.translate(0.0, -0.25, 0.0);
            pose.translate(0.0, -0.95, 0.0);
            ModelUtils.renderEntityModel(builder, portal.level(), model_demon_portal_ring, pose, 0xF000F0, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 1.0f);
            pose.popPose();
        }
        pose.pushPose();
        pose.translate(0.0, -1.1, 0.0);
        int count = 0;
        builder = bufferSource.getBuffer(RenderType.solid());
        for (ResourceLocation model : model_demon_portal_stones) {
            stonePct = stoneSpawnPcts[count];
            pose.pushPose();
            pose.translate(0.0f, offsets[count % offsets.length] + ringOffsets[count] * stonePct, ringHOffsets[count] * stonePct);
            ModelUtils.renderEntityModel(builder, portal.level(), model, pose, packedLight, OverlayTexture.NO_OVERLAY, new float[]{1.0f, 1.0f, 1.0f}, 1.0f);
            pose.popPose();
            ++count;
        }
        count = 0;
        for (ResourceLocation model : model_demon_portal_runes) {
            stonePct = stoneSpawnPcts[count];
            pose.pushPose();
            pose.translate(0.0f, offsets[count % offsets.length] + ringOffsets[count] * stonePct, ringHOffsets[count] * stonePct);
            ModelUtils.renderEntityModel(builder, portal.level(), model, pose, 0xF000F0, OverlayTexture.NO_OVERLAY, colors, 1.0f);
            pose.popPose();
            ++count;
        }
        pose.popPose();
    }

    private void renderPortalTexture(PoseStack pose, VertexConsumer vertexBuilder, int packedLight, int[] color, int alpha, float verticalOffset, float scaleFactor, float spin, float tilt, boolean faceCamera) {
        pose.pushPose();
        if (faceCamera) {
            Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
            Quaternionf portalRotation = new Quaternionf(0.0f, cameraRotation.y(), 0.0f, cameraRotation.w());
            pose.mulPose(portalRotation);
        }
        pose.translate(0.0f, verticalOffset, 0.0f);
        pose.scale(scaleFactor, scaleFactor, scaleFactor);
        pose.mulPose(Axis.XP.rotationDegrees(tilt));
        pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(spin));
        pose.translate(0.0f, -0.25f, 0.0f);
        PoseStack.Pose matrixstack$entry = pose.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        float nrmV = (float)Math.cos((double)spin * Math.PI / 180.0);
        float nrmH = (float)Math.cos((double)(spin - 90.0f) * Math.PI / 180.0);
        PortalRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLight, 0.0f, 0.0f, 0.0f, 1.0f, nrmH, nrmV, color, alpha);
        PortalRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLight, 1.0f, 0.0f, 1.0f, 1.0f, nrmH, nrmV, color, alpha);
        PortalRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLight, 1.0f, 1.0f, 1.0f, 0.0f, nrmH, nrmV, color, alpha);
        PortalRenderer.addVertex(vertexBuilder, matrixstack$entry, packedLight, 0.0f, 1.0f, 0.0f, 0.0f, nrmH, nrmV, color, alpha);
        pose.popPose();
    }

    private static void addVertex(VertexConsumer vertexBuilder_, com.mojang.blaze3d.vertex.PoseStack.Pose pose, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] rgb, int a) {
        vertexBuilder_.addVertex(pose, x - 0.5f, y - 0.25f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], a).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nrmH, nrmV, nrmH);
    }

    public ResourceLocation getTextureLocation(Portal entity) {
        return entity.isRTP() ? MARenderTypes.RTP_PORTAL_TEXTURE : MARenderTypes.PORTAL_TEXTURE;
    }
}

