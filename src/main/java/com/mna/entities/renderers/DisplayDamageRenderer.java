 
package com.mna.entities.renderers;

import com.mna.ManaAndArtifice;
import com.mna.effects.EffectInit;
import com.mna.entities.utility.DisplayDamage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.joml.Matrix4f;

public class DisplayDamageRenderer
extends EntityRenderer<DisplayDamage> {
    public DisplayDamageRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(DisplayDamage pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (!ManaAndArtifice.instance.proxy.getClientPlayer().hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.INSIGHT.get()))) {
            return;
        }
        float f = pEntity.getBbHeight() + 0.5f;
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.0, (double)f, 0.0);
        pMatrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        pMatrixStack.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = pMatrixStack.last().pose();
        Font font = this.getFont();
        MutableComponent amount = Component.literal((String)String.format("%.2f", Float.valueOf(pEntity.getAmount())));
        float f2 = -font.width((FormattedText)amount) / 2;
        font.drawInBatch((Component)amount, f2, 0.0f, pEntity.getColor(), true, matrix4f, pBuffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        pMatrixStack.popPose();
    }

    public ResourceLocation getTextureLocation(DisplayDamage pEntity) {
        return null;
    }

    protected boolean shouldShowName(DisplayDamage pEntity) {
        return true;
    }

    protected void renderNameTag(DisplayDamage pEntity, Component pDisplayName, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
    }
}

