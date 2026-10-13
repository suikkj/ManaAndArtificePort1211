 
package com.mna.tools.render;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.particles.IWorldRenderUtils;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.api.tools.RLoc;
import com.mna.items.sorcery.ItemSpell;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.MARenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class WorldRenderUtils
implements IWorldRenderUtils {
    public static final WorldRenderUtils INSTANCE = new WorldRenderUtils();
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    public static final int FULL_BRIGHTNESS = 0xF000F0;
    private static final float[] uLookup = new float[]{0.0f, 0.25f, 0.5f, 0.75f, 0.0f, 0.25f, 0.5f, 0.75f, 0.0f, 0.25f, 0.5f, 0.75f, 0.0f, 0.25f, 0.5f, 0.75f};
    private static final float[] vLookup = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.25f, 0.25f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f, 0.5f, 0.75f, 0.75f, 0.75f, 0.75f};

    public static void renderFlatRadiant(Entity entityIn, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale) {
        WorldRenderUtils.renderFlatRadiant(entityIn.tickCount, pose, bufferSource, innerColor, outerColor, alpha, scale, true);
    }

    public static void renderFlatRadiant(float ageTicks, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale, boolean grow) {
        float rotationByAge = ageTicks / 100.0f;
        Random random = new Random(432L);
        VertexConsumer vertexconsumer2 = bufferSource.getBuffer(MARenderTypes.RADIANT_RENDER_TYPE);
        pose.pushPose();
        if (grow) {
            float growth = 1.0f + scale * 25.0f;
            pose.scale(growth, growth, growth);
        } else {
            pose.scale(scale, scale, scale);
        }
        alpha = Math.min(alpha, 255);
        for (int i = 0; i < 20; ++i) {
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + 90.0f * rotationByAge / 5.0f));
            float hOffset = random.nextFloat() * 0.25f;
            float vOffset = random.nextFloat() * 0.25f;
            com.mojang.blaze3d.vertex.PoseStack.Pose matrix4f = pose.last();
            WorldRenderUtils.vertex01(vertexconsumer2, matrix4f, innerColor, alpha);
            WorldRenderUtils.vertex2(vertexconsumer2, matrix4f, hOffset, vOffset, outerColor);
            WorldRenderUtils.vertex3(vertexconsumer2, matrix4f, hOffset, 0.0f, innerColor);
        }
        pose.popPose();
    }

    public static void renderRadiant(float ageTicks, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale, boolean grow) {
        float rotationByAge = ageTicks / 200.0f;
        Random random = new Random(432L);
        VertexConsumer vertexconsumer2 = bufferSource.getBuffer(MARenderTypes.RADIANT_RENDER_TYPE);
        pose.pushPose();
        if (grow) {
            float growth = 1.0f + scale * 25.0f;
            pose.scale(growth, growth, growth);
        } else {
            pose.scale(scale, scale, scale);
        }
        alpha = Math.min(alpha, 64);
        for (int i = 0; i < 40; ++i) {
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + 90.0f * rotationByAge));
            float hOffset = i % 3 == 0 ? random.nextFloat() * 0.55f : random.nextFloat() * 0.25f;
            float vOffset = random.nextFloat() * 0.25f;
            com.mojang.blaze3d.vertex.PoseStack.Pose matrix4f = pose.last();
            WorldRenderUtils.vertex01(vertexconsumer2, matrix4f, innerColor, alpha);
            WorldRenderUtils.vertex2(vertexconsumer2, matrix4f, hOffset, vOffset, outerColor);
            WorldRenderUtils.vertex3(vertexconsumer2, matrix4f, hOffset, vOffset, innerColor);
            WorldRenderUtils.vertex01(vertexconsumer2, matrix4f, innerColor, alpha);
            WorldRenderUtils.vertex3(vertexconsumer2, matrix4f, hOffset, vOffset, outerColor);
            WorldRenderUtils.vertex4(vertexconsumer2, matrix4f, hOffset, vOffset, innerColor);
            WorldRenderUtils.vertex01(vertexconsumer2, matrix4f, innerColor, alpha);
            WorldRenderUtils.vertex4(vertexconsumer2, matrix4f, hOffset, vOffset, outerColor);
            WorldRenderUtils.vertex2(vertexconsumer2, matrix4f, hOffset, vOffset, innerColor);
        }
        pose.popPose();
    }

    public static void renderRadiant(Entity entityIn, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale) {
        WorldRenderUtils.renderRadiant(entityIn.tickCount, pose, bufferSource, innerColor, outerColor, alpha, scale, true);
    }

    public static void renderRadiantWithDirection(Entity entityIn, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale) {
        float rotationByAge = (float)entityIn.tickCount / 220.0f;
        Random random = new Random(432L);
        VertexConsumer lightingBuilder = bufferSource.getBuffer(MARenderTypes.RADIANT_RENDER_TYPE);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(entityIn.getYRot() - 90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(entityIn.getXRot() + 90.0f));
        pose.translate(0.0, -0.25, 0.0);
        pose.scale(scale, scale * 3.0f, scale);
        for (int i = 0; i < 20; ++i) {
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f + 90.0f * rotationByAge));
            float hOffset = random.nextFloat() * 0.25f;
            float vOffset = random.nextFloat() * 0.25f;
            com.mojang.blaze3d.vertex.PoseStack.Pose currentMatrix = pose.last();
            WorldRenderUtils.vertex01(lightingBuilder, currentMatrix, innerColor, alpha);
            WorldRenderUtils.vertex2(lightingBuilder, currentMatrix, hOffset, vOffset, outerColor);
            WorldRenderUtils.vertex3(lightingBuilder, currentMatrix, hOffset, vOffset, innerColor);
            WorldRenderUtils.vertex01(lightingBuilder, currentMatrix, innerColor, alpha);
            WorldRenderUtils.vertex3(lightingBuilder, currentMatrix, hOffset, vOffset, outerColor);
            WorldRenderUtils.vertex4(lightingBuilder, currentMatrix, hOffset, vOffset, innerColor);
            WorldRenderUtils.vertex01(lightingBuilder, currentMatrix, innerColor, alpha);
            WorldRenderUtils.vertex4(lightingBuilder, currentMatrix, hOffset, vOffset, outerColor);
            WorldRenderUtils.vertex2(lightingBuilder, currentMatrix, hOffset, vOffset, innerColor);
        }
        pose.popPose();
    }

    public static void renderLightRay(float ageTicks, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale, float angle) {
        Random random = new Random(432L);
        VertexConsumer vertexconsumer2 = bufferSource.getBuffer(MARenderTypes.RADIANT_RENDER_TYPE);
        alpha = Math.min(alpha, 255);
        pose.pushPose();
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        float hOffset = random.nextFloat() * 0.25f;
        float vOffset = random.nextFloat() * 0.25f;
        com.mojang.blaze3d.vertex.PoseStack.Pose matrix4f = pose.last();
        WorldRenderUtils.vertex1(vertexconsumer2, matrix4f, innerColor, alpha);
        WorldRenderUtils.vertex2(vertexconsumer2, matrix4f, hOffset, vOffset, outerColor);
        WorldRenderUtils.vertex3(vertexconsumer2, matrix4f, hOffset, 0.0f, innerColor);
        pose.popPose();
    }

    public static void renderLightBeam(float ageTicks, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale, float width, float flareScale, float angle) {
        Random random = new Random(432L);
        VertexConsumer vertexconsumer2 = bufferSource.getBuffer(MARenderTypes.RADIANT_RENDER_TYPE);
        alpha = Math.min(alpha, 255);
        pose.pushPose();
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        float vOffset = random.nextFloat() * 0.25f;
        com.mojang.blaze3d.vertex.PoseStack.Pose matrix4f = pose.last();
        vertexconsumer2.addVertex(matrix4f, -width * flareScale, 0.0f, 0.0f).setColor(innerColor[0], innerColor[1], innerColor[2], alpha);
        vertexconsumer2.addVertex(matrix4f, -width, vOffset, 0.0f).setColor(outerColor[0], outerColor[1], outerColor[2], 0);
        vertexconsumer2.addVertex(matrix4f, width, vOffset, 0.0f).setColor(outerColor[0], outerColor[1], outerColor[2], 0);
        vertexconsumer2.addVertex(matrix4f, width * flareScale, 0.0f, 0.0f).setColor(innerColor[0], innerColor[1], innerColor[2], alpha);
        pose.popPose();
    }

    public static void renderBeam(Level world, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, Vec3 start, Vec3 end, float lengthPct, int[] color, RenderType renderType) {
        WorldRenderUtils.renderBeam(world, partialTicks, pose, bufferSource, packedLight, start, end, lengthPct, color, 0.01f, renderType);
    }

    public static void renderBeam(Level world, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, Vec3 start, Vec3 end, float lengthPct, int[] color, float width, RenderType renderType) {
        WorldRenderUtils.renderBeam(world, partialTicks, pose, bufferSource, packedLight, start, end, lengthPct, color, 176, width, renderType);
    }

    public static void renderBeam(Level world, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, Vec3 start, Vec3 end, float lengthPct, int[] color, int alpha, float width, RenderType renderType) {
        float worldPartialTicks = (float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks;
        float uvSlideRate = worldPartialTicks * 0.35f % 1.0f;
        Vec3 direction = end.subtract(start);
        float beamLength = (float)direction.length() * lengthPct;
        direction = direction.normalize();
        float aCosDirectionY = (float)Math.acos(direction.y);
        float atan2DirectionXZ = (float)Math.atan2(direction.z, direction.x);
        float rotation = 0.0f;
        int red = color[0];
        int green = color[1];
        int blue = color[2];
        float beamWidth = width;
        float x1Start = Mth.cos((float)(rotation + (float)Math.PI)) * beamWidth;
        float x1End = Mth.cos((float)rotation) * beamWidth;
        float y1Start = Mth.sin((float)(rotation + (float)Math.PI)) * beamWidth;
        float y1End = Mth.sin((float)rotation) * beamWidth;
        float x2Start = Mth.cos((float)(rotation + 1.5707964f)) * beamWidth;
        float x2End = Mth.cos((float)(rotation + 4.712389f)) * beamWidth;
        float y2Start = Mth.sin((float)(rotation + 1.5707964f)) * beamWidth;
        float y2End = Mth.sin((float)(rotation + 4.712389f)) * beamWidth;
        float vSlide = -1.0f - uvSlideRate;
        float f30 = beamLength * 2.5f + vSlide;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees((1.5707964f - atan2DirectionXZ) * 57.295776f));
        pose.mulPose(Axis.XP.rotationDegrees(aCosDirectionY * 57.295776f));
        VertexConsumer vertexBuilder = bufferSource.getBuffer(renderType);
        PoseStack.Pose activeStack = pose.last();
        Matrix4f renderMatrix = activeStack.pose();
        PoseStack.Pose normalMatrix = activeStack;
        float uMin = 0.05f;
        float uMax = 0.95f;
        WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, x1Start, beamLength, y1Start, red, green, blue, alpha, uMax, f30);
        WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, x1Start, 0.0f, y1Start, red, green, blue, alpha, uMax, vSlide);
        WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, x1End, 0.0f, y1End, red, green, blue, alpha, uMin, vSlide);
        WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, x1End, beamLength, y1End, red, green, blue, alpha, uMin, f30);
        WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, x2Start, beamLength, y2Start, red, green, blue, alpha, uMax, f30);
        WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, x2Start, 0.0f, y2Start, red, green, blue, alpha, uMax, vSlide);
        WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, x2End, 0.0f, y2End, red, green, blue, alpha, uMin, vSlide);
        WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, x2End, beamLength, y2End, red, green, blue, alpha, uMin, f30);
        pose.popPose();
    }

    public static void renderManaweavePatternNoTransparent(ManaweavingPattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory) {
        WorldRenderUtils.renderManaweavePattern_internal(pattern, rotation, pose, bufferSource, inventory, RenderType.entityCutout((ResourceLocation)RLoc.create("textures/particle/sparkle.png")), new int[]{109, 227, 220, 176});
    }

    public static void renderManaweavePatternNoTransparent(ManaweavingPattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory, int[] color) {
        WorldRenderUtils.renderManaweavePattern_internal(pattern, rotation, pose, bufferSource, inventory, RenderType.entityCutout((ResourceLocation)RLoc.create("textures/particle/sparkle.png")), color);
    }

    public static void renderManaweavePattern(IManaweavePattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory) {
        WorldRenderUtils.renderManaweavePattern_internal(pattern, rotation, pose, bufferSource, inventory, MARenderTypes.RENDER_TYPE_MANAWEAVE, new int[]{109, 227, 220, 176});
    }

    public static void renderManaweavePattern(IManaweavePattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory, int[] color) {
        WorldRenderUtils.renderManaweavePattern_internal(pattern, rotation, pose, bufferSource, inventory, MARenderTypes.RENDER_TYPE_MANAWEAVE, color);
    }

    public static void renderManaweavePattern_internal(IManaweavePattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory, RenderType renderType, int[] color) {
        byte[][] points = pattern.get();
        float offsetX = (float)points.length / 2.0f;
        float offsetY = (float)points[0].length / 2.0f;
        float baseScale = 0.15f;
        pose.pushPose();
        pose.translate(0.0f, 2.0f, 0.0f);
        pose.mulPose(rotation);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        pose.translate(-3.6 * (double)baseScale, -3.6 * (double)baseScale, 0.0);
        pose.scale(baseScale, baseScale, baseScale);
        PoseStack.Pose pose$entry = pose.last();
        Matrix4f renderMatrix = pose$entry.pose();
        PoseStack.Pose normalMatrix = pose$entry;
        VertexConsumer vertexBuilder = bufferSource.getBuffer(renderType);
        for (int i = 0; i < points.length; ++i) {
            for (int j = 0; j < points[i].length; ++j) {
                if (points[i][j] != 1) continue;
                float originX = offsetX - (float)j * 0.5f;
                float originY = offsetY - (float)i * 0.5f;
                WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, 0.0f + originX, 0.0f + originY, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 1.0f);
                WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, 1.0f + originX, 0.0f + originY, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 1.0f);
                WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, 1.0f + originX, 1.0f + originY, 0.0f, color[0], color[1], color[2], color[3], 1.0f, 0.0f);
                WorldRenderUtils.createVertex(vertexBuilder, normalMatrix, 0.0f + originX, 1.0f + originY, 0.0f, color[0], color[1], color[2], color[3], 0.0f, 0.0f);
            }
        }
        pose.popPose();
    }

    public static void renderProgressBar(PoseStack pose, MultiBufferSource bufferSource, float percent, int[] rgb, int alpha) {
        com.mojang.blaze3d.vertex.PoseStack.Pose matrix4f = pose.last();
        PoseStack.Pose normalMatrix = pose.last();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(MARenderTypes.SOLID_RENDER_TYPE);
        vertexconsumer.addVertex(matrix4f, 0.5f, -0.5f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], alpha).setLight(0xF000F0).setNormal(normalMatrix, 0.0f, 0.0f, 1.0f);
        vertexconsumer.addVertex(matrix4f, 0.5f - percent, -0.5f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], alpha).setLight(0xF000F0).setNormal(normalMatrix, 0.0f, 0.0f, 1.0f);
        vertexconsumer.addVertex(matrix4f, 0.5f - percent, 0.5f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], alpha).setLight(0xF000F0).setNormal(normalMatrix, 0.0f, 0.0f, 1.0f);
        vertexconsumer.addVertex(matrix4f, 0.5f, 0.5f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], alpha).setLight(0xF000F0).setNormal(normalMatrix, 0.0f, 0.0f, 1.0f);
    }

    public static void RenderRuneWord(PoseStack matrixStackIn, MultiBufferSource bufferIn, ResourceLocation buffTexture, int packedLightIn, int[] color, int[] indices, float powerUpPct) {
        WorldRenderUtils.RenderRuneWord(matrixStackIn, bufferIn, buffTexture, packedLightIn, color, indices, powerUpPct, 7);
    }

    public static void RenderRuneWord(PoseStack matrixStackIn, MultiBufferSource bufferIn, ResourceLocation buffTexture, int packedLightIn, int[] color, int[] indices, float powerUpPct, int charactersPerLine) {
        float scaleFactor = 0.2f;
        float xStep = 0.5f;
        float yStep = -0.75f;
        int count = 0;
        int numLines = (int)Math.ceil((float)indices.length / (float)charactersPerLine);
        float[] linePcts = new float[numLines];
        float linePctAffect = 1.0f / (float)numLines;
        for (int i = 0; i < numLines; ++i) {
            float powerUpForFullBlue = 1.0f - linePctAffect * (float)i;
            linePcts[i] = powerUpPct > powerUpForFullBlue ? 1.0f : (powerUpPct > powerUpForFullBlue - linePctAffect ? (powerUpPct - powerUpForFullBlue + linePctAffect) / linePctAffect : 0.0f);
        }
        int curLine = 0;
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityTranslucent((ResourceLocation)buffTexture));
        matrixStackIn.pushPose();
        matrixStackIn.scale(scaleFactor, scaleFactor, scaleFactor);
        for (int i = 0; i < indices.length; ++i) {
            int index = indices[i];
            if (index >= 0) {
                WorldRenderUtils.renderRune(matrixStackIn, vertexBuilder, 0xF000F0, index, linePcts[curLine], color);
            }
            matrixStackIn.translate(xStep, 0.0f, 0.0f);
            if (++count < charactersPerLine) continue;
            matrixStackIn.translate((float)(-count) * xStep, yStep, 0.0f);
            count = 0;
            ++curLine;
        }
        matrixStackIn.popPose();
    }

    public static void renderRune(PoseStack matrixStackIn, VertexConsumer vertexBuilder, int packedLightIn, int index, float colorShiftPct, int[] activeColor) {
        boolean flip;
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        PoseStack.Pose normalMatrix = matrixstack$entry;
        boolean bl = flip = (index & 0x10) > 0;
        if ((index &= 0xF) < 0 || index > 15) {
            return;
        }
        float nrmV = 1.0f;
        float nrmH = 1.0f;
        float u = uLookup[index];
        float v = vLookup[index];
        float u2 = u + 0.25f;
        float v2 = v + 0.25f;
        int[] inactiveColor = new int[]{0, 0, 0};
        if (flip) {
            float tmp = u;
            u = u2;
            u2 = tmp;
        }
        if (colorShiftPct == 1.0f) {
            WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 0.0f, 0.0f, u, v2, nrmH, nrmV, activeColor);
            WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 1.0f, 0.0f, u2, v2, nrmH, nrmV, activeColor);
            WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 1.0f, 1.0f, u2, v, nrmH, nrmV, activeColor);
            WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 0.0f, 1.0f, u, v, nrmH, nrmV, activeColor);
        } else if (colorShiftPct == 0.0f) {
            WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 0.0f, 0.0f, u, v2, nrmH, nrmV, inactiveColor);
            WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 1.0f, 0.0f, u2, v2, nrmH, nrmV, inactiveColor);
            WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 1.0f, 1.0f, u2, v, nrmH, nrmV, inactiveColor);
            WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 0.0f, 1.0f, u, v, nrmH, nrmV, inactiveColor);
        } else {
            if (colorShiftPct > 0.0f) {
                v2 = v + 0.25f * (1.0f - colorShiftPct);
                WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 0.0f, colorShiftPct, u, v2, nrmH, nrmV, inactiveColor);
                WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 1.0f, colorShiftPct, u2, v2, nrmH, nrmV, inactiveColor);
                WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 1.0f, 1.0f, u2, v, nrmH, nrmV, inactiveColor);
                WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 0.0f, 1.0f, u, v, nrmH, nrmV, inactiveColor);
            }
            if (colorShiftPct < 1.0f) {
                v2 = v + 0.25f;
                WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 0.0f, 0.0f, u, v2, nrmH, nrmV, activeColor);
                WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 1.0f, 0.0f, u2, v2, nrmH, nrmV, activeColor);
                WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 1.0f, colorShiftPct, u2, v += 0.25f * (1.0f - colorShiftPct), nrmH, nrmV, activeColor);
                WorldRenderUtils.addVertex(vertexBuilder, normalMatrix, packedLightIn, 0.0f, colorShiftPct, u, v, nrmH, nrmV, activeColor);
            }
        }
    }

    public static void renderAxisPoint(PoseStack matrixStack, VertexConsumer consumer, BlockPos point, Direction dir, float r, float g, float b, float a) {
        matrixStack.pushPose();
        matrixStack.translate((double)point.getX() + 0.5, (double)point.getY() + 0.5, (double)point.getZ() + 0.5);
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        WorldRenderUtils.renderTranslucentBox(matrixStack, consumer, r, g, b, a);
        if (dir.getStepY() == -1) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        } else if (dir.getStepX() == 1) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            matrixStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
        } else if (dir.getStepX() == -1) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            matrixStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        } else if (dir.getStepZ() == 1) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        } else if (dir.getStepZ() == -1) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        }
        matrixStack.translate(0.0, 0.55, 0.0);
        matrixStack.pushPose();
        matrixStack.scale(0.05f, 0.35f, 0.05f);
        WorldRenderUtils.renderTranslucentBox(matrixStack, consumer, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.popPose();
        matrixStack.translate(0.0, 0.15, 0.0);
        matrixStack.scale(0.15f, 0.25f, 0.15f);
        Matrix4f matrix4f = matrixStack.last().pose();
        PoseStack.Pose matrix3f = matrixStack.last();
        float y1 = 0.0f;
        float y2 = 1.0f;
        float hw1 = -0.5f;
        float hw2 = 0.5f;
        float ht1 = -0.01f;
        float ht2 = 0.01f;
        float alpha = 1.0f;
        r = 1.0f;
        g = 1.0f;
        b = 0.0f;
        consumer.addVertex(matrix3f, hw1, y1, hw1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, hw2, y1, hw1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, hw2, y1, hw2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw1, y1, hw2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, ht1, y2, ht1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht2, y2, ht1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht2, y2, ht2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, ht1, y2, ht2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw1, y1, hw1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht1, y2, ht1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht1, y2, ht2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw1, y1, hw2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw2, y1, hw1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht2, y2, ht1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht2, y2, ht2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw2, y1, hw2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw1, y1, hw1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht1, y2, ht1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht2, y2, ht1).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw2, y1, hw1).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw1, y1, hw2).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht1, y2, ht2).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, ht2, y2, ht2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, hw2, y1, hw2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        matrixStack.popPose();
    }

    public static void renderAxisBox(PoseStack matrixStack, VertexConsumer consumer, AABB renderBounds) {
        Matrix4f matrix4f = matrixStack.last().pose();
        PoseStack.Pose matrix3f = matrixStack.last();
        float x1 = (float)renderBounds.minX;
        float y1 = (float)renderBounds.minY;
        float z1 = (float)renderBounds.minZ;
        float x2 = (float)renderBounds.maxX;
        float y2 = (float)renderBounds.maxY;
        float z2 = (float)renderBounds.maxZ;
        float alpha = 0.25f;
        float alpha2 = 0.375f;
        consumer.addVertex(matrix3f, x1, y1, z1).setColor(0.0f, 1.0f, 0.0f, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z1).setColor(0.0f, 1.0f, 0.0f, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z2).setColor(0.0f, 1.0f, 0.0f, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z2).setColor(0.0f, 1.0f, 0.0f, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z1).setColor(0.0f, 1.0f, 0.0f, alpha2).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z1).setColor(0.0f, 1.0f, 0.0f, alpha2).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z2).setColor(0.0f, 1.0f, 0.0f, alpha2).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z2).setColor(0.0f, 1.0f, 0.0f, alpha2).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z1).setColor(1.0f, 0.0f, 0.0f, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z1).setColor(1.0f, 0.0f, 0.0f, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z2).setColor(1.0f, 0.0f, 0.0f, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z2).setColor(1.0f, 0.0f, 0.0f, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z1).setColor(1.0f, 0.0f, 0.0f, alpha2).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z1).setColor(1.0f, 0.0f, 0.0f, alpha2).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z2).setColor(1.0f, 0.0f, 0.0f, alpha2).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z2).setColor(1.0f, 0.0f, 0.0f, alpha2).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z1).setColor(0.0f, 0.0f, 1.0f, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z1).setColor(0.0f, 0.0f, 1.0f, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z1).setColor(0.0f, 0.0f, 1.0f, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z1).setColor(0.0f, 0.0f, 1.0f, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z2).setColor(0.0f, 0.0f, 1.0f, alpha2).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z2).setColor(0.0f, 0.0f, 1.0f, alpha2).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z2).setColor(0.0f, 0.0f, 1.0f, alpha2).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z2).setColor(0.0f, 0.0f, 1.0f, alpha2).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
    }

    public static void renderWardBox(PoseStack matrixStack, VertexConsumer consumer, AABB renderBounds, int alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        PoseStack.Pose matrix3f = matrixStack.last();
        float x1 = (float)renderBounds.minX;
        float y1 = (float)renderBounds.minY;
        float z1 = (float)renderBounds.minZ;
        float x2 = (float)renderBounds.maxX;
        float y2 = (float)renderBounds.maxY;
        float z2 = (float)renderBounds.maxZ;
        int r = 255;
        int g = 255;
        int b = 255;
        float uvXSize = 1.0f - (float)Math.min(renderBounds.getXsize(), 1.0);
        float uvMinX = uvXSize / 2.0f;
        float uvMaxX = 1.0f - uvMinX;
        float uvYSize = 1.0f - (float)Math.min(renderBounds.getYsize(), 1.0);
        float uvMinY = uvYSize / 2.0f;
        float uvMaxY = 1.0f - uvMinY;
        float uvZSize = 1.0f - (float)Math.min(renderBounds.getZsize(), 1.0);
        float uvMinZ = uvZSize / 2.0f;
        float uvMaxZ = 1.0f - uvMinZ;
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y1, z1, r, g, b, alpha, uvMinZ, uvMinX, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y1, z1, r, g, b, alpha, uvMaxZ, uvMinX, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y1, z2, r, g, b, alpha, uvMaxZ, uvMaxX, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y1, z2, r, g, b, alpha, uvMinZ, uvMaxX, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y2, z1, r, g, b, alpha, uvMinZ, uvMinX, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y2, z2, r, g, b, alpha, uvMaxZ, uvMinX, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y2, z2, r, g, b, alpha, uvMaxZ, uvMaxX, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y2, z1, r, g, b, alpha, uvMinZ, uvMaxX, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y1, z1, r, g, b, alpha, uvMinZ, uvMaxY, -1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y1, z2, r, g, b, alpha, uvMaxZ, uvMaxY, -1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y2, z2, r, g, b, alpha, uvMaxZ, uvMinY, -1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y2, z1, r, g, b, alpha, uvMinZ, uvMinY, -1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y2, z1, r, g, b, alpha, uvMinZ, uvMinY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y2, z2, r, g, b, alpha, uvMaxZ, uvMinY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y1, z2, r, g, b, alpha, uvMaxZ, uvMaxY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y1, z1, r, g, b, alpha, uvMinZ, uvMaxY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y2, z1, r, g, b, alpha, uvMinX, uvMinY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y2, z1, r, g, b, alpha, uvMaxX, uvMinY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y1, z1, r, g, b, alpha, uvMaxX, uvMaxY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y1, z1, r, g, b, alpha, uvMinX, uvMaxY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y2, z2, r, g, b, alpha, uvMinX, uvMinY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y2, z2, r, g, b, alpha, uvMaxX, uvMinY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x1, y1, z2, r, g, b, alpha, uvMaxX, uvMaxY, 1.0f, 0.0f, 0.0f);
        WorldRenderUtils.createVertex(consumer, matrix3f, x2, y1, z2, r, g, b, alpha, uvMinX, uvMaxY, 1.0f, 0.0f, 0.0f);
    }

    public static void spawnFirstPersonParticlesForStack(ItemStack stack, HumanoidArm hand) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        if (!(stack.getItem() instanceof ItemSpell)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean playerIsRightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
        InteractionHand activeHand = player.getUsedItemHand();
        if (!itemIsInUse || playerIsRightHanded && activeHand == InteractionHand.MAIN_HAND && hand == HumanoidArm.LEFT || !playerIsRightHanded && activeHand == InteractionHand.MAIN_HAND && hand == HumanoidArm.RIGHT) {
            Vec3 particlePos = player.position().add(0.0, (double)(player.getEyeHeight() - 0.2f), 0.0);
            Pair<Vec3, Vec3> lookCross = MathUtils.getLookCross((LivingEntity)player);
            Vec3 look = ((Vec3)lookCross.getFirst()).scale(0.5);
            Vec3 perp = ((Vec3)lookCross.getSecond()).scale(hand == HumanoidArm.LEFT ? (double)-0.4f : (double)0.4f);
            particlePos = particlePos.add(look).add(perp);
            SpellRecipe recipe = SpellRecipe.fromNBT(((ItemSpell)stack.getItem()).getSpellCompound(stack, (Player)player));
            Affinity[] affs = recipe.getAffinity().keySet().toArray(new Affinity[0]);
            switch (affs[(int)(Math.random() * (double)affs.length)]) {
                case ARCANE: {
                    Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
                    Vec3 offset = new Vec3(player.level().random.nextGaussian(), player.level().random.nextGaussian(), player.level().random.nextGaussian()).normalize().scale((double)0.3f);
                    origin = origin.add(offset);
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_LERP.get())), (Entity)player), origin.x, origin.y, origin.z, particlePos.x, particlePos.y, particlePos.z);
                }
                case EARTH: {
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)player), particlePos.x, particlePos.y, particlePos.z, (double)-0.005f + Math.random() * (double)0.01f, (double)0.03f, (double)-0.005f + Math.random() * (double)0.01f);
                }
                case ENDER: {
                    Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
                    Vec3 offset = new Vec3(player.level().random.nextGaussian(), player.level().random.nextGaussian(), player.level().random.nextGaussian()).normalize().scale((double)0.3f);
                    origin = origin.add(offset);
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (Entity)player), origin.x, origin.y, origin.z, particlePos.x, particlePos.y, particlePos.z);
                }
                case FIRE: {
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), (Entity)player), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
                }
                case HELLFIRE: {
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), (Entity)player), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
                }
                case LIGHTNING: {
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)player), particlePos.x, particlePos.y, particlePos.z, particlePos.x - (double)0.2f + Math.random() * (double)0.4f, particlePos.y - (double)0.2f + Math.random() * (double)0.4f, particlePos.z - (double)0.2f + Math.random() * (double)0.4f);
                }
                case WATER: {
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())), (Entity)player), particlePos.x, particlePos.y, particlePos.z, -0.05 * Math.random() * 0.1, Math.random() * 0.05, -0.05 * Math.random() * 0.1);
                }
                case BLOOD: {
                    if (!(Math.random() < 0.05)) break;
                    Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
                    Vec3 offset = new Vec3(player.level().getRandom().nextGaussian(), player.level().getRandom().nextGaussian(), player.level().getRandom().nextGaussian()).normalize().scale((double)0.05f);
                    origin = origin.add(offset);
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setGravity(0.005f).setColor(91, 15, 14), (Entity)player).setPhysics(true), origin.x, origin.y, origin.z, 0.0, 0.0, 0.0);
                }
                case ICE: {
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), (Entity)player), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
                }
                case WIND: {
                    player.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.02f).setColor(20, 20, 20), (Entity)player), particlePos.x, particlePos.y, particlePos.z, (double)0.3f, (double)0.01f, (double)0.05f);
                }
            }
        }
    }

    private static void renderTranslucentBox(PoseStack matrixStack, VertexConsumer consumer, float r, float g, float b, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        PoseStack.Pose matrix3f = matrixStack.last();
        float x1 = -0.5f;
        float y1 = -0.5f;
        float z1 = -0.5f;
        float x2 = 0.5f;
        float y2 = 0.5f;
        float z2 = 0.5f;
        consumer.addVertex(matrix3f, x1, y1, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z1).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y1, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x1, y2, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 1.0f, 0.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y2, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
        consumer.addVertex(matrix3f, x2, y1, z2).setColor(r, g, b, alpha).setNormal(matrix3f, 0.0f, 1.0f, 0.0f);
    }

    private static void vertex01(VertexConsumer vertexConsumer, com.mojang.blaze3d.vertex.PoseStack.Pose pose, int[] rgb, int alpha) {
        vertexConsumer.addVertex(pose, 0.0f, 0.0f, 0.0f).setColor(255, 255, 255, alpha);
        vertexConsumer.addVertex(pose, 0.0f, 0.0f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], alpha);
    }

    private static void vertex1(VertexConsumer vertexConsumer, com.mojang.blaze3d.vertex.PoseStack.Pose pose, int[] rgb, int alpha) {
        vertexConsumer.addVertex(pose, 0.0f, 0.0f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], alpha);
        vertexConsumer.addVertex(pose, 0.0f, 0.0f, 0.0f).setColor(rgb[0], rgb[1], rgb[2], alpha);
    }

    private static void vertex2(VertexConsumer vertexConsumer, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float y, float length, int[] rgb) {
        vertexConsumer.addVertex(pose, -HALF_SQRT_3 * length, y, -0.2f * length).setColor(rgb[0], rgb[1], rgb[2], 0);
    }

    private static void vertex3(VertexConsumer vertexConsumer, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float y, float length, int[] rgb) {
        vertexConsumer.addVertex(pose, HALF_SQRT_3 * length, y, -0.2f * length).setColor(rgb[0], rgb[1], rgb[2], 0);
    }

    private static void vertex4(VertexConsumer vertexConsumer, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float y, float length, int[] rgb) {
        vertexConsumer.addVertex(pose, 0.0f, y, 1.0f * length).setColor(rgb[0], rgb[1], rgb[2], 0);
    }

    private static void createVertex(VertexConsumer vertexBuilder, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float x, float y, float z, int colorRed, int colorGreen, int colorBlue, int alpha, float u, float v) {
        vertexBuilder.addVertex(pose, x, y, z).setColor(colorRed, colorGreen, colorBlue, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 1.0f, 0.0f, 0.0f);
    }

    public static void createVertex(VertexConsumer vertexBuilder, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float x, float y, float z, int colorRed, int colorGreen, int colorBlue, int alpha, float u, float v, float nrmX, float nrmY, float nrmZ) {
        vertexBuilder.addVertex(pose, x, y, z).setColor(colorRed, colorGreen, colorBlue, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, nrmX, nrmY, nrmZ);
    }

    private static void addVertex(VertexConsumer vertexBuilder_, com.mojang.blaze3d.vertex.PoseStack.Pose pose, int packedLight, float x, float y, float u, float v, float nrmH, float nrmV, int[] color) {
        vertexBuilder_.addVertex(pose, x - 0.5f, y - 0.25f, 0.0f).setColor(color[0], color[1], color[2], 230).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nrmH, nrmV, nrmH);
    }

    @Override
    public void radiant(Entity entityIn, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale) {
        WorldRenderUtils.renderRadiant(entityIn, pose, bufferSource, innerColor, outerColor, alpha, scale);
    }

    @Override
    public void radiant(float ageTicks, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale, boolean grow) {
        WorldRenderUtils.renderRadiant(ageTicks, pose, bufferSource, innerColor, outerColor, alpha, scale, grow);
    }

    @Override
    public void directionalRadiant(Entity entityIn, PoseStack pose, MultiBufferSource bufferSource, int[] innerColor, int[] outerColor, int alpha, float scale) {
        WorldRenderUtils.renderRadiantWithDirection(entityIn, pose, bufferSource, innerColor, outerColor, alpha, scale);
    }

    @Override
    public void beam(Level world, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, Vec3 start, Vec3 end, float lengthPct, int[] color, float width, RenderType renderType) {
        WorldRenderUtils.renderBeam(world, partialTicks, pose, bufferSource, packedLight, start, end, lengthPct, color, renderType);
    }

    @Override
    public void beam(Level world, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, Vec3 start, Vec3 end, float lengthPct, int[] color, int alpha, float width, RenderType renderType) {
        WorldRenderUtils.renderBeam(world, partialTicks, pose, bufferSource, packedLight, start, end, lengthPct, color, renderType);
    }

    @Override
    public void beam(Level world, float partialTicks, PoseStack pose, MultiBufferSource bufferSource, int packedLight, Vec3 start, Vec3 end, float lengthPct, int[] color, RenderType renderType) {
        WorldRenderUtils.renderBeam(world, partialTicks, pose, bufferSource, packedLight, start, end, lengthPct, color, packedLight, lengthPct, renderType);
    }

    @Override
    public void manaweavePattern(ManaweavingPattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory) {
        WorldRenderUtils.renderManaweavePattern(pattern, rotation, pose, bufferSource, inventory);
    }

    @Override
    public void manaweavePattern(ManaweavingPattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory, int[] color) {
        WorldRenderUtils.renderManaweavePattern(pattern, rotation, pose, bufferSource, inventory, color);
    }

    @Override
    public void manaweavePatternNoTransparent(ManaweavingPattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory) {
        WorldRenderUtils.renderManaweavePatternNoTransparent(pattern, rotation, pose, bufferSource, inventory);
    }

    @Override
    public void manaweavePatternNoTransparent(ManaweavingPattern pattern, Quaternionf rotation, PoseStack pose, MultiBufferSource bufferSource, boolean inventory, int[] color) {
        WorldRenderUtils.renderManaweavePatternNoTransparent(pattern, rotation, pose, bufferSource, inventory, color);
    }
}

