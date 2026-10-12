/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.HumanoidModel$ArmPose
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.player.PlayerModelPart
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.extensions.common.IClientItemExtensions
 *  net.minecraftforge.fluids.FluidType
 */
package com.mna.entities.renderers.sorcery;

import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.entities.summon.Simulacrum;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.FluidType;

public class SimulacrumRenderer
extends LivingEntityRenderer<Simulacrum, PlayerModel<Simulacrum>> {
    private static final ResourceLocation DEFAULT = RLoc.create("textures/entity/simulacrum.png");
    private PlayerModel<Simulacrum> slim = null;
    private PlayerModel<Simulacrum> normal = null;
    private int pass = 0;

    public SimulacrumRenderer(EntityRendererProvider.Context context) {
        super(context, (EntityModel)new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.normal = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false);
        this.slim = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    public void render(Simulacrum pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        String s;
        Player owner = pEntity.getOwner();
        this.model = this.normal;
        if (owner != null && (s = ((AbstractClientPlayer)owner).getModelName()) == "slim") {
            this.model = this.slim;
        }
        if (owner instanceof AbstractClientPlayer) {
            AbstractClientPlayer acp = (AbstractClientPlayer)owner;
            this.setModelProperties(acp);
        }
        this.pass = 0;
        pPoseStack.pushPose();
        super.render((LivingEntity)pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        pPoseStack.popPose();
        this.pass = 1;
        pPoseStack.pushPose();
        super.render((LivingEntity)pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        pPoseStack.popPose();
        if (owner == ManaAndArtifice.instance.proxy.getClientPlayer()) {
            this.renderManaBar(pEntity, pEntityYaw, pPoseStack, pBuffer);
        }
    }

    private void renderManaBar(Simulacrum pEntity, float pEntityYaw, PoseStack pPoseStack, MultiBufferSource pBuffer) {
        float pct = pEntity.getManaPct();
        int color = Mth.hsvToRgb((float)(pct / 3.0f), (float)1.0f, (float)1.0f) | 0xFF000000;
        int r = FastColor.ARGB32.red((int)color) / 3 * 2;
        int g = FastColor.ARGB32.green((int)color) / 3 * 2;
        int b = FastColor.ARGB32.blue((int)color) / 3 * 2;
        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-pEntityYaw));
        pPoseStack.translate(0.0, 2.5, 0.0);
        pPoseStack.scale(1.5f, 0.05f, 1.0f);
        WorldRenderUtils.renderProgressBar(pPoseStack, pBuffer, pct, new int[]{r, g, b}, 255);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        WorldRenderUtils.renderProgressBar(pPoseStack, pBuffer, 1.0f - pct, new int[]{0, 0, 0}, 255);
        pPoseStack.popPose();
    }

    public ResourceLocation getTextureLocation(Simulacrum entity) {
        if (this.pass == 0) {
            Player player;
            if (entity.getOwner() != null && (player = entity.getOwner()) instanceof AbstractClientPlayer) {
                AbstractClientPlayer player2 = (AbstractClientPlayer)player;
                return player2.getSkinTextureLocation();
            }
            return DEFAULT;
        }
        return DEFAULT;
    }

    protected boolean shouldShowName(Simulacrum entity) {
        return false;
    }

    protected void renderNameTag(Simulacrum entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
    }

    public Vec3 getRenderOffset(Simulacrum pEntity, float pPartialTicks) {
        return pEntity.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset((Entity)pEntity, pPartialTicks);
    }

    protected void scale(AbstractClientPlayer pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        pPoseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }

    protected void setupRotations(Simulacrum pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        Player owner = pEntityLiving.getOwner();
        if (owner instanceof AbstractClientPlayer) {
            AbstractClientPlayer acp = (AbstractClientPlayer)owner;
            float f = pEntityLiving.getSwimAmount(pPartialTicks);
            if (pEntityLiving.isFallFlying()) {
                super.setupRotations((LivingEntity)pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
                float f1 = (float)pEntityLiving.getFallFlyingTicks() + pPartialTicks;
                float f2 = Mth.clamp((float)(f1 * f1 / 100.0f), (float)0.0f, (float)1.0f);
                if (!pEntityLiving.isAutoSpinAttack()) {
                    pPoseStack.mulPose(Axis.XP.rotationDegrees(f2 * (-90.0f - pEntityLiving.getXRot())));
                }
                Vec3 vec3 = pEntityLiving.getViewVector(pPartialTicks);
                Vec3 vec31 = acp.getDeltaMovementLerped(pPartialTicks);
                double d0 = vec31.horizontalDistanceSqr();
                double d1 = vec3.horizontalDistanceSqr();
                if (d0 > 0.0 && d1 > 0.0) {
                    double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
                    double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
                    pPoseStack.mulPose(Axis.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
                }
            } else if (f > 0.0f) {
                super.setupRotations((LivingEntity)pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
                float f3 = pEntityLiving.isInWater() || pEntityLiving.isInFluidType((fluidType, height) -> pEntityLiving.canSwimInFluidType((FluidType)fluidType)) ? -90.0f - pEntityLiving.getXRot() : -90.0f;
                float f4 = Mth.lerp((float)f, (float)0.0f, (float)f3);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(f4));
                if (pEntityLiving.isVisuallySwimming()) {
                    pPoseStack.translate(0.0f, -1.0f, 0.3f);
                }
            } else {
                super.setupRotations((LivingEntity)pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
            }
        }
    }

    private void setModelProperties(AbstractClientPlayer pClientPlayer) {
        PlayerModel playermodel = (PlayerModel)this.getModel();
        if (pClientPlayer.isSpectator()) {
            playermodel.setAllVisible(false);
            playermodel.head.visible = true;
            playermodel.hat.visible = true;
        } else {
            playermodel.setAllVisible(true);
            playermodel.hat.visible = pClientPlayer.isModelPartShown(PlayerModelPart.HAT);
            playermodel.jacket.visible = pClientPlayer.isModelPartShown(PlayerModelPart.JACKET);
            playermodel.leftPants.visible = pClientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            playermodel.rightPants.visible = pClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            playermodel.leftSleeve.visible = pClientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            playermodel.rightSleeve.visible = pClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            playermodel.crouching = pClientPlayer.isCrouching();
            HumanoidModel.ArmPose humanoidmodel$armpose = SimulacrumRenderer.getArmPose(pClientPlayer, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose humanoidmodel$armpose1 = SimulacrumRenderer.getArmPose(pClientPlayer, InteractionHand.OFF_HAND);
            if (humanoidmodel$armpose.isTwoHanded()) {
                HumanoidModel.ArmPose armPose = humanoidmodel$armpose1 = pClientPlayer.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }
            if (pClientPlayer.getMainArm() == HumanoidArm.RIGHT) {
                playermodel.rightArmPose = humanoidmodel$armpose;
                playermodel.leftArmPose = humanoidmodel$armpose1;
            } else {
                playermodel.rightArmPose = humanoidmodel$armpose1;
                playermodel.leftArmPose = humanoidmodel$armpose;
            }
        }
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer pPlayer, InteractionHand pHand) {
        HumanoidModel.ArmPose forgeArmPose;
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (pPlayer.getUsedItemHand() == pHand && pPlayer.getUseItemRemainingTicks() > 0) {
            UseAnim useanim = itemstack.getUseAnimation();
            if (useanim == UseAnim.BLOCK) {
                return HumanoidModel.ArmPose.BLOCK;
            }
            if (useanim == UseAnim.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            if (useanim == UseAnim.SPEAR) {
                return HumanoidModel.ArmPose.THROW_SPEAR;
            }
            if (useanim == UseAnim.CROSSBOW && pHand == pPlayer.getUsedItemHand()) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (useanim == UseAnim.SPYGLASS) {
                return HumanoidModel.ArmPose.SPYGLASS;
            }
            if (useanim == UseAnim.TOOT_HORN) {
                return HumanoidModel.ArmPose.TOOT_HORN;
            }
            if (useanim == UseAnim.BRUSH) {
                return HumanoidModel.ArmPose.BRUSH;
            }
        } else if (!pPlayer.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged((ItemStack)itemstack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        if ((forgeArmPose = IClientItemExtensions.of((ItemStack)itemstack).getArmPose((LivingEntity)pPlayer, pHand, itemstack)) != null) {
            return forgeArmPose;
        }
        return HumanoidModel.ArmPose.ITEM;
    }
}


