/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 */
package com.mna.items.renderers;

import com.mna.api.items.IPositionalItem;
import com.mna.api.tools.RLoc;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class MarkingRuneRenderer
extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation markingRuneItemModel = RLoc.create("item/special/rune_marking_item");
    private BakedModel resolvedModel;

    public MarkingRuneRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        IPositionalItem posItem;
        Block block;
        Item item;
        if (this.resolvedModel == null) {
            this.resolvedModel = Minecraft.getInstance().getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(markingRuneItemModel));
        }
        pPoseStack.translate(0.5f, 0.5f, 0.5f);
        Minecraft.getInstance().getItemRenderer().render(pStack, pDisplayContext, false, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, this.resolvedModel);
        if (pDisplayContext == ItemDisplayContext.GUI && (item = pStack.getItem()) instanceof IPositionalItem && (block = (posItem = (IPositionalItem)item).getMarkedBlock(pStack)) != null) {
            ItemStack blockStack = new ItemStack((ItemLike)block);
            pPoseStack.scale(0.6f, 0.6f, 0.6f);
            pPoseStack.translate(0.3, -0.3, 2.0);
            Minecraft.getInstance().getItemRenderer().renderStatic(blockStack, pDisplayContext, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, (Level)Minecraft.getInstance().level, pPackedOverlay);
        }
    }
}

