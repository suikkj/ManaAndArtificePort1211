/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  software.bernie.geckolib.model.GeoModel
 */
package com.mna.blocks.tileentities.models;

import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.model.GeoModel;

public abstract class WizardLabModel<T extends WizardLabTile>
extends GeoModel<T> {
    protected final List<GeoBoneRenderer> boneOverrides = new ArrayList<GeoBoneRenderer>();

    public void renderBoneAdditions(WizardLabTile tile, String bone, PoseStack stack, MultiBufferSource bufferIn, RenderType renderType, int packedLightIn, int packedOverlayIn) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        this.boneOverrides.stream().filter(b -> b.bone.equals(bone) && (b.slot == -1 || tile.hasStack(b.slot))).forEach(b -> {
            if (b.poseAdjuster != null) {
                stack.pushPose();
                b.poseAdjuster.accept(stack);
            }
            if (b.model != null) {
                ModelUtils.renderModel(bufferIn.getBuffer(RenderType.solid()), tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), b.model, stack, packedLightIn, packedOverlayIn);
            } else {
                ItemStack stackToRender = !b.stack.isEmpty() ? b.stack : tile.getItem(b.slot);
                itemRenderer.renderStatic(stackToRender, ItemDisplayContext.GROUND, packedLightIn, packedOverlayIn, stack, bufferIn, (Level)mc.level, 0);
            }
            if (b.poseAdjuster != null) {
                stack.popPose();
            }
        });
    }

    public static class GeoBoneRenderer {
        public final ResourceLocation model;
        public final ItemStack stack;
        public final int slot;
        public final String bone;
        public final Consumer<PoseStack> poseAdjuster;

        public GeoBoneRenderer(int slot, String bone, ResourceLocation model) {
            this(slot, bone, model, null);
        }

        public GeoBoneRenderer(int slot, String bone, ResourceLocation model, Consumer<PoseStack> poseAdjuster) {
            this.slot = slot;
            this.bone = bone;
            this.model = model;
            this.poseAdjuster = poseAdjuster;
            this.stack = ItemStack.EMPTY;
        }

        public GeoBoneRenderer(int slot, String bone, ItemStack renderItem) {
            this(slot, bone, renderItem, null);
        }

        public GeoBoneRenderer(int slot, String bone, ItemStack renderItem, Consumer<PoseStack> poseAdjuster) {
            this.slot = slot;
            this.bone = bone;
            this.model = null;
            this.poseAdjuster = poseAdjuster;
            this.stack = renderItem.copy();
        }
    }
}

