 
package com.mna.entities.renderers.boss;

import com.mna.capabilities.entity.MAPFX;
import com.mna.capabilities.entity.MAPFXProvider;
import com.mna.entities.boss.FaerieQueen;
import com.mna.entities.models.boss.FaerieQueenModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import com.mna.api.tools.MNACap;

public class FaerieQueenRenderer
extends MAGeckoRenderer<FaerieQueen> {
    public FaerieQueenRenderer(EntityRendererProvider.Context context) {
        super(context, new FaerieQueenModel());
    }

    @Override
    public void render(FaerieQueen entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
        int[] nArray;
        int[] nArray2;
        if (entity.isWinter()) {
            int[] nArray3 = new int[3];
            nArray3[0] = 148;
            nArray3[1] = 242;
            nArray2 = nArray3;
            nArray3[2] = 244;
        } else {
            int[] nArray4 = new int[3];
            nArray4[0] = 128;
            nArray4[1] = 102;
            nArray2 = nArray4;
            nArray4[2] = 25;
        }
        int[] innerColor = nArray2;
        if (entity.isWinter()) {
            int[] nArray5 = new int[3];
            nArray5[0] = 236;
            nArray5[1] = 255;
            nArray = nArray5;
            nArray5[2] = 253;
        } else {
            int[] nArray6 = new int[3];
            nArray6[0] = 253;
            nArray6[1] = 94;
            nArray = nArray6;
            nArray6[2] = 83;
        }
        int[] outerColor = nArray;
        float size = 0.25f;
        if (entity.faerieDeathTime > 0) {
            size = (float)((double)size - 0.01 * (double)entity.faerieDeathTime);
        }
        if (size > -0.035f) {
            stack.pushPose();
            stack.translate(0.0, 1.2, 0.0);
            WorldRenderUtils.renderRadiant((Entity)entity, stack, bufferIn, innerColor, outerColor, 64, size);
            stack.popPose();
        }
        stack.mulPose(Axis.YP.rotationDegrees(180.0f));
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
    }

    public boolean shouldRender(FaerieQueen pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        java.util.Optional<MAPFX> pfxCap = MNACap.get(pLivingEntity, MAPFXProvider.MAPFX);
        if (pfxCap.isPresent() && pfxCap.get().getFlag((LivingEntity)pLivingEntity, MAPFX.Flag.MIST_FORM)) {
            return false;
        }
        return super.shouldRender(pLivingEntity, pCamera, pCamX, pCamY, pCamZ);
    }
}


