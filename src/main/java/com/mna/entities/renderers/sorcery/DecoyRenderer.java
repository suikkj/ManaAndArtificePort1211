 
package com.mna.entities.renderers.sorcery;

import com.mna.entities.sorcery.EntityDecoy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class DecoyRenderer
extends LivingEntityRenderer<EntityDecoy, PlayerModel<EntityDecoy>> {
    private static final ResourceLocation DEFAULT = ResourceLocation.parse("");
    private PlayerModel<EntityDecoy> slim = null;
    private PlayerModel<EntityDecoy> normal = null;

    public DecoyRenderer(EntityRendererProvider.Context context) {
        super(context, (EntityModel)new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.normal = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    public void render(EntityDecoy pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        String s;
        Player owner = pEntity.getPlayer();
        this.model = this.normal;
        if (owner != null && (s = ((AbstractClientPlayer)owner).getModelName()) == "slim") {
            this.model = this.slim;
        }
        super.render((LivingEntity)pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    public ResourceLocation getTextureLocation(EntityDecoy entity) {
        Player player;
        if (entity.getPlayer() != null && (player = entity.getPlayer()) instanceof AbstractClientPlayer) {
            AbstractClientPlayer player2 = (AbstractClientPlayer)player;
            return player2.getSkinTextureLocation();
        }
        return DEFAULT;
    }

    protected boolean shouldShowName(EntityDecoy entity) {
        return true;
    }

    protected void renderNameTag(EntityDecoy entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.getPlayer() != null) {
            super.renderNameTag((Entity)entityIn, entityIn.getPlayer().getDisplayName(), matrixStackIn, bufferIn, packedLightIn);
        }
    }
}

