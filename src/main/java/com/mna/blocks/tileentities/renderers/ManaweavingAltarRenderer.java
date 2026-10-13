/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.EntityRenderDispatcher
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.mna.blocks.tileentities.renderers;

import com.mna.ManaAndArtifice;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.api.tools.RLoc;
import com.mna.blocks.manaweaving.ManaweavingAltarBlock;
import com.mna.blocks.tileentities.ManaweavingAltarTile;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class ManaweavingAltarRenderer
implements BlockEntityRenderer<ManaweavingAltarTile> {
    private static final ResourceLocation SPARKLE_TEXTURE = RLoc.create("textures/particle/sparkle.png");
    private static final ResourceLocation CRYSTAL_A = RLoc.create("block/manaweaving_altar_crystal_a");
    private static final ResourceLocation CRYSTAL_B = RLoc.create("block/manaweaving_altar_crystal_b");
    private static final ResourceLocation CRYSTAL_C = RLoc.create("block/manaweaving_altar_crystal_c");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucentCull((ResourceLocation)SPARKLE_TEXTURE);
    private final Minecraft mc = Minecraft.getInstance();
    private final ItemRenderer itemRenderer = this.mc.getItemRenderer();
    private final EntityRenderDispatcher renderDispatcher = this.mc.getEntityRenderDispatcher();
    private final Font font;

    public ManaweavingAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.font = this.mc.font;
    }

    public void render(ManaweavingAltarTile tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float yFloatA = (float)Math.sin(((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks) / 40.0f) * 0.01f;
        float yFloatB = (float)Math.sin(((float)(ManaAndArtifice.instance.proxy.getGameTicks() + 40L) + partialTicks) / 40.0f) * 0.01f;
        float yFloatC = (float)Math.sin(((float)(ManaAndArtifice.instance.proxy.getGameTicks() + 80L) + partialTicks) / 40.0f) * 0.01f;
        this.renderModelExtras(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, yFloatA, yFloatB, yFloatC);
        this.renderStacks(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, partialTicks);
        this.renderPatterns(tileEntityIn, matrixStackIn, bufferIn, yFloatA, yFloatB, yFloatC);
        this.renderLastRecipe(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    private void renderModelExtras(ManaweavingAltarTile tileEntityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, float yFloatA, float yFloatB, float yFloatC) {
        Level world = tileEntityIn.getLevel();
        BlockPos pos = tileEntityIn.getBlockPos();
        BlockState state = tileEntityIn.getBlockState();
        matrixStackIn.pushPose();
        switch ((Direction)state.getValue((Property)ManaweavingAltarBlock.FACING)) {
            case EAST: {
                matrixStackIn.translate(1.0f, 0.0f, 0.0f);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(-90.0f));
            }
            case SOUTH: {
                matrixStackIn.translate(1.0f, 0.0f, 1.0f);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0f));
            }
            case WEST: {
                matrixStackIn.translate(0.0f, 0.0f, 1.0f);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0f));
            }
        }
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0f, yFloatA, 0.0f);
        ModelUtils.renderModel(bufferIn, world, pos, state, CRYSTAL_A, matrixStackIn, combinedLightIn, combinedOverlayIn);
        matrixStackIn.popPose();
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0f, yFloatB, 0.0f);
        ModelUtils.renderModel(bufferIn, world, pos, state, CRYSTAL_B, matrixStackIn, combinedLightIn, combinedOverlayIn);
        matrixStackIn.popPose();
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0f, yFloatC, 0.0f);
        ModelUtils.renderModel(bufferIn, world, pos, state, CRYSTAL_C, matrixStackIn, combinedLightIn, combinedOverlayIn);
        matrixStackIn.popPose();
        matrixStackIn.popPose();
    }

    private void renderLastRecipe(ManaweavingAltarTile tileEntityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!tileEntityIn.isEmpty() || tileEntityIn.getAddedPatterns().size() != 0 || tileEntityIn.getReCraftOutput().isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockHitResult brtr = (BlockHitResult)mc.hitResult;
        if (!brtr.getBlockPos().equals(tileEntityIn.getBlockPos())) {
            return;
        }
        List<ItemStack> itemstacks = tileEntityIn.getReCraftInput();
        float scale = 0.15f;
        float xStep = 1.0f;
        float xOffset = (float)(itemstacks.size() - 1) * xStep / 2.0f;
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 1.3, 0.5);
        matrixStackIn.scale(scale, scale, scale);
        this.renderString(Component.translatable((String)"block.mna.manaweaving_altar.repeat").getString(), matrixStackIn, bufferIn, combinedLightIn);
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(this.renderDispatcher.cameraOrientation());
        matrixStackIn.translate(0.0, -0.75, 0.0);
        this.itemRenderer.renderStatic(tileEntityIn.getReCraftOutput(), ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)mc.level, 0);
        matrixStackIn.translate(0.0, -0.75, 0.0);
        scale = 0.5f;
        matrixStackIn.scale(scale, scale, scale);
        matrixStackIn.translate(xOffset, 0.0f, 0.0f);
        for (int i = 0; i < itemstacks.size(); ++i) {
            ItemStack stack = itemstacks.get(i);
            this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)mc.level, 0);
            matrixStackIn.translate(-xStep, 0.0f, 0.0f);
        }
        matrixStackIn.popPose();
        matrixStackIn.translate(0.0, -1.95, 0.0);
        this.renderString(Component.translatable((String)"block.mna.manaweaving_altar.repeat_mana_cost", (Object[])new Object[]{tileEntityIn.getReCraftManaCost()}).getString(), matrixStackIn, bufferIn, combinedLightIn);
        matrixStackIn.popPose();
    }

    protected void renderString(String text, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        stack.pushPose();
        stack.mulPose(this.renderDispatcher.cameraOrientation());
        stack.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = stack.last().pose();
        float opacity = 0.25f;
        int textColor = (int)(opacity * 255.0f) << 24;
        float hOffset = -this.font.width(text) / 2;
        this.font.drawInBatch(text, hOffset, 0.0f, 0x20FFFFFF, false, matrix4f, buffer, Font.DisplayMode.NORMAL, textColor, packedLight);
        this.font.drawInBatch(text, hOffset, 0.0f, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
        stack.popPose();
    }

    private void renderStacks(ManaweavingAltarTile tileEntityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, float partialTicks) {
        ItemStack[] itemstacks = tileEntityIn.getDisplayedItems();
        for (int i = 0; i < itemstacks.length; ++i) {
            Random r = new Random((long)i * 1234L);
            if (itemstacks[i].isEmpty()) continue;
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 1.3, 0.5);
            Vec3 randomPos = new Vec3(r.nextGaussian() * Math.sin((float)i / 4.5f), r.nextGaussian() * Math.sin((float)i / 4.5f), r.nextGaussian() * Math.sin((float)i / 4.5f)).normalize();
            randomPos = randomPos.scale((double)0.3f);
            if (!tileEntityIn.isCrafting()) {
                matrixStackIn.translate(randomPos.x(), randomPos.y(), randomPos.z());
            } else {
                float ringAngle = (float)i * (360.0f / (float)itemstacks.length);
                float craftPct = (float)tileEntityIn.getCraftTicks() / (float)tileEntityIn.getMaxCraftTicks();
                Vec3 ringPos = new Vec3(Math.cos(ringAngle), 0.0, Math.sin(ringAngle)).scale((double)(1.0f - craftPct));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks));
                if (tileEntityIn.getCraftTicks() < tileEntityIn.getMaxCraftTicks() / 5) {
                    float pct = (float)tileEntityIn.getCraftTicks() / ((float)tileEntityIn.getMaxCraftTicks() / 5.0f);
                    Vec3 lerpPos = MathUtils.lerpVector3d(randomPos, ringPos, pct);
                    matrixStackIn.translate(lerpPos.x(), lerpPos.y(), lerpPos.z());
                } else {
                    matrixStackIn.translate(ringPos.x(), ringPos.y(), ringPos.z());
                }
            }
            matrixStackIn.mulPose(Axis.XP.rotationDegrees((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks));
            matrixStackIn.mulPose(Axis.YP.rotationDegrees((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks));
            matrixStackIn.scale(0.25f, 0.25f, 0.25f);
            this.itemRenderer.renderStatic(itemstacks[i], ItemDisplayContext.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
            matrixStackIn.popPose();
        }
    }

    private void renderPatterns(ManaweavingAltarTile tileEntityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, float yFloatA, float yFloatB, float yFloatC) {
        int count = 7;
        float radius = 0.375f;
        float rotation = 0.0f;
        float rotationBase = 0.0f;
        float[] yOffsets = new float[]{0.8375f, 0.91f, 0.9525f};
        switch ((Direction)tileEntityIn.getBlockState().getValue((Property)ManaweavingAltarBlock.FACING)) {
            case SOUTH: {
                rotation = 180.0f;
                rotationBase = 180.0f;
            }
            case EAST: {
                rotation = 90.0f;
            }
            case WEST: {
                rotation = -90.0f;
            }
            default: {
                rotationBase = 180.0f;
            }
        }
        for (IManaweavePattern pattern : tileEntityIn.getAddedPatterns()) {
            float yOffset = count == 1 || count == 7 ? yOffsets[0] + yFloatA : (count == 2 || count == 6 ? yOffsets[1] + yFloatB : yOffsets[2] + yFloatC);
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5f, 0.0f, 0.5f);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(rotation));
            matrixStackIn.translate(-0.5f, 0.0f, -0.5f);
            Vector3f vec = new Vector3f(0.0f, yOffset, radius);
            vec.rotate((Quaternionfc)Axis.YP.rotationDegrees(rotationBase + (float)(45 * count)));
            matrixStackIn.translate(vec.x() + 0.5f, vec.y(), vec.z() + 0.5f);
            this.renderPattern(pattern, matrixStackIn, bufferIn, rotation);
            matrixStackIn.popPose();
            if (--count != 4) continue;
            --count;
        }
    }

    private void renderPattern(IManaweavePattern pattern, PoseStack matrixStackIn, MultiBufferSource bufferIn, float rotation) {
        byte[][] points = pattern.get();
        float pointSize = 0.5f;
        float baseScale = 0.03f;
        float offsetX = (float)points.length / 2.0f * pointSize;
        float offsetY = (float)points[0].length / 2.0f * pointSize;
        Quaternionf cameraRotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0, 0.2, 0.0);
        matrixStackIn.scale(baseScale, baseScale, baseScale);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(-rotation));
        matrixStackIn.mulPose(cameraRotation);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0f));
        PoseStack.Pose matrixstack$entry = matrixStackIn.last();
        Matrix4f renderMatrix = matrixstack$entry.pose();
        Matrix3f normalMatrix = matrixstack$entry.normal();
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RENDER_TYPE);
        for (int i = 0; i < points.length; ++i) {
            for (int j = 0; j < points[i].length; ++j) {
                if (points[i][j] != 1) continue;
                float originX = offsetX - (float)j * pointSize;
                float originY = offsetY - (float)i * pointSize;
                ManaweavingAltarRenderer.addVertex(vertexBuilder, matrixstack$entry, 0.0f + originX, 0.0f + originY, 0.0f, 1.0f);
                ManaweavingAltarRenderer.addVertex(vertexBuilder, matrixstack$entry, 1.0f + originX, 0.0f + originY, 1.0f, 1.0f);
                ManaweavingAltarRenderer.addVertex(vertexBuilder, matrixstack$entry, 1.0f + originX, 1.0f + originY, 1.0f, 0.0f);
                ManaweavingAltarRenderer.addVertex(vertexBuilder, matrixstack$entry, 0.0f + originX, 1.0f + originY, 0.0f, 0.0f);
            }
        }
        matrixStackIn.popPose();
    }

    private static void addVertex(VertexConsumer vertexBuilder_, com.mojang.blaze3d.vertex.PoseStack.Pose pose, float x, float y, float u, float v) {
        vertexBuilder_.addVertex(pose, x - 0.5f, y - 0.25f, 0.0f).setColor(109, 227, 220, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}

