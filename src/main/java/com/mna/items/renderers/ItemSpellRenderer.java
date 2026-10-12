/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.items.renderers;

import com.mna.items.ItemInit;
import com.mna.items.SpellIconList;
import com.mna.items.sorcery.ItemSpell;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemSpellRenderer
extends BlockEntityWithoutLevelRenderer {
    private static HashMap<Integer, BakedModel> bakedSpellModels;
    private BakedModel defaultSpellModel;
    private BakedModel defaultBangleModel;
    public static final ResourceLocation location_spell;
    public static final ResourceLocation location_bangle;

    public ItemSpellRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems);
        if (bakedSpellModels == null) {
            bakedSpellModels = new HashMap();
        }
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (itemDisplayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && itemDisplayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.RIGHT);
                WorldRenderUtils.spawnFirstPersonParticlesForStack(stack, HumanoidArm.RIGHT);
            } else if (itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.LEFT);
                WorldRenderUtils.spawnFirstPersonParticlesForStack(stack, HumanoidArm.LEFT);
            } else {
                this.renderDefaultSpellItem(stack, itemDisplayContext, matrixStack, buffer, combinedLight, combinedOverlay);
            }
        }
    }

    private void renderDefaultSpellItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (!stack.isEmpty()) {
            BakedModel spellModel = itemDisplayContext == ItemDisplayContext.GUI ? this.getSpellModel(stack) : this.getDefaultSpellModel(stack.getItem() == ItemInit.BANGLE.get());
            matrixStack.popPose();
            matrixStack.pushPose();
            if (itemDisplayContext != ItemDisplayContext.GUI) {
                Minecraft.getInstance().getItemRenderer().render(stack, itemDisplayContext, itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND, matrixStack, buffer, combinedLight, combinedOverlay, spellModel);
            } else {
                boolean flag;
                RenderSystem.enableBlend();
                RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                boolean bl = flag = !spellModel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }
                Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.GUI, false, matrixStack, (MultiBufferSource)bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, spellModel);
                bufferSource.endBatch();
                RenderSystem.enableDepthTest();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            }
        }
    }

    private BakedModel getSpellModel(ItemStack stack) {
        BakedModel customModel;
        if (stack.getItem() == ItemInit.BANGLE.get()) {
            return this.getDefaultSpellModel(true);
        }
        int customIconIndex = ItemSpell.getCustomIcon(stack);
        if (customIconIndex < 0 || customIconIndex >= SpellIconList.ALL.length) {
            return this.getDefaultSpellModel(false);
        }
        if (!bakedSpellModels.containsKey(customIconIndex)) {
            bakedSpellModels.put(customIconIndex, Minecraft.getInstance().getModelManager().getModel(SpellIconList.ALL[customIconIndex]));
        }
        return (customModel = bakedSpellModels.get(customIconIndex)) != null ? customModel : this.getDefaultSpellModel(false);
    }

    private BakedModel getDefaultSpellModel(boolean bangle) {
        if (this.defaultSpellModel == null) {
            this.defaultSpellModel = Minecraft.getInstance().getModelManager().getModel(location_spell);
            this.defaultBangleModel = Minecraft.getInstance().getModelManager().getModel(location_bangle);
        }
        return bangle ? this.defaultBangleModel : this.defaultSpellModel;
    }

    private void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, HumanoidArm side) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderer entityRenderer = mc.getEntityRenderDispatcher().getRenderer((Entity)mc.player);
        if (entityRenderer instanceof PlayerRenderer) {
            PlayerRenderer playerRenderer = (PlayerRenderer)entityRenderer;
            RenderSystem.setShaderTexture((int)0, (ResourceLocation)mc.player.getSkinTextureLocation());
            matrixStackIn.pushPose();
            if (side == HumanoidArm.RIGHT) {
                matrixStackIn.translate(0.75, -0.25, 0.0);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(95.0f));
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(-30.0f));
                playerRenderer.renderRightHand(matrixStackIn, bufferIn, combinedLightIn, (AbstractClientPlayer)mc.player);
            } else {
                matrixStackIn.translate(0.25, -0.25, 0.0);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(260.0f));
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(30.0f));
                playerRenderer.renderLeftHand(matrixStackIn, bufferIn, combinedLightIn, (AbstractClientPlayer)mc.player);
            }
            matrixStackIn.popPose();
        }
    }

    static {
        location_spell = ResourceLocation.fromNamespaceAndPath("mna", "item/spell_texture");
        location_bangle = ResourceLocation.fromNamespaceAndPath("mna", "item/bangle_texture");
    }
}

