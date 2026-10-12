/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.ArmedModel
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 */
package com.mna.entities.renderers.player;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.items.sorcery.Bangle;
import com.mna.items.sorcery.ItemSpell;
import com.mna.items.sorcery.MagicStaff;
import com.mna.items.sorcery.SpellBook;
import com.mna.spells.SpellAdjusters;
import com.mna.spells.crafting.SpellRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class HandParticleLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    public HandParticleLayer(RenderLayerParent<T, M> rendererIn) {
        super(rendererIn);
    }

    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack leftHandItem;
        if (entitylivingbaseIn.isInvisible()) {
            return;
        }
        boolean playerIsRightHanded = entitylivingbaseIn.getMainArm() == HumanoidArm.RIGHT;
        boolean itemIsInUse = entitylivingbaseIn.getUseItemRemainingTicks() > 0;
        InteractionHand activeHand = entitylivingbaseIn.getUsedItemHand();
        ItemStack rightHandItem = playerIsRightHanded ? entitylivingbaseIn.getOffhandItem() : entitylivingbaseIn.getMainHandItem();
        ItemStack itemStack = leftHandItem = playerIsRightHanded ? entitylivingbaseIn.getMainHandItem() : entitylivingbaseIn.getOffhandItem();
        if (!rightHandItem.isEmpty() || !leftHandItem.isEmpty()) {
            matrixStackIn.pushPose();
            if (this.getParentModel().young) {
                matrixStackIn.translate(0.0f, 0.75f, 0.0f);
                matrixStackIn.scale(0.5f, 0.5f, 0.5f);
            }
            if (!itemIsInUse || playerIsRightHanded && activeHand == InteractionHand.OFF_HAND || !playerIsRightHanded && activeHand == InteractionHand.MAIN_HAND) {
                this.renderHandParticle((LivingEntity)entitylivingbaseIn, leftHandItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStackIn, bufferIn, packedLightIn);
            }
            if (!itemIsInUse || !playerIsRightHanded && activeHand == InteractionHand.OFF_HAND || playerIsRightHanded && activeHand == InteractionHand.MAIN_HAND) {
                this.renderHandParticle((LivingEntity)entitylivingbaseIn, rightHandItem, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStackIn, bufferIn, packedLightIn);
            }
            matrixStackIn.popPose();
        }
    }

    private void renderHandParticle(LivingEntity living, ItemStack stack, ItemDisplayContext ItemDisplayContext2, HumanoidArm side, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        if (!stack.isEmpty() && stack.getItem() instanceof ItemSpell) {
            PoseStack identity = new PoseStack();
            identity.setIdentity();
            identity.mulPose(Axis.YP.rotationDegrees(-living.yBodyRot));
            identity.mulPose(Axis.XP.rotationDegrees(180.0f));
            identity.translate(0.0, -1.35, 0.0);
            ((ArmedModel)this.getParentModel()).translateToHand(side, identity);
            identity.mulPose(Axis.XP.rotationDegrees(-90.0f));
            identity.mulPose(Axis.YP.rotationDegrees(180.0f));
            boolean leftHand = side == HumanoidArm.LEFT;
            identity.translate((float)(leftHand ? -1 : 1) / 16.0f, 0.125f, -0.625f);
            CompoundTag nbt = ((ItemSpell)stack.getItem()).getSpellCompound(stack, living instanceof Player ? (Player)living : null);
            SpellRecipe recipe = SpellRecipe.fromNBT(nbt);
            if (stack.getItem() instanceof MagicStaff && !(stack.getItem() instanceof Bangle)) {
                identity.translate(0.0, 1.5, 0.0);
            } else if (stack.getItem() instanceof SpellBook && ((SpellBook)stack.getItem()).renderBookModel) {
                identity.translate(0.0, 0.5, -0.55);
            }
            SpellAdjustingContext adjustingContext = new SpellAdjustingContext(stack, recipe, living, leftHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, false, SpellCastStage.CASTING);
            if (living instanceof Player && SpellAdjusters.checkHellfireStaff(adjustingContext)) {
                SpellAdjusters.modifyHellfireStaff(adjustingContext);
            }
            Affinity[] affs = recipe.getAffinity().keySet().toArray(new Affinity[0]);
            this.spawnParticleFromMatrix(affs[(int)(Math.random() * (double)affs.length)], recipe, identity, living, ItemDisplayContext2);
        }
    }

    private void spawnParticleFromMatrix(Affinity affinity, SpellRecipe recipe, PoseStack matrixStackIn, LivingEntity entitylivingbaseIn, ItemDisplayContext type) {
        Vec3 playerPos = entitylivingbaseIn.position();
        Matrix4f curMatrix = matrixStackIn.last().pose();
        Vec3 particlePos = playerPos.add(new Vec3((double)curMatrix.m30(), (double)curMatrix.m31(), (double)curMatrix.m32()));
        switch (affinity) {
            case ARCANE: {
                Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
                Vec3 offset = new Vec3(entitylivingbaseIn.level().random.nextGaussian(), entitylivingbaseIn.level().random.nextGaussian(), entitylivingbaseIn.level().random.nextGaussian()).normalize().scale((double)0.3f);
                origin = origin.add(offset);
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_LERP.get())), (Entity)entitylivingbaseIn), origin.x, origin.y, origin.z, particlePos.x, particlePos.y, particlePos.z);
                break;
            }
            case EARTH: {
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())), (Entity)entitylivingbaseIn), particlePos.x, particlePos.y, particlePos.z, (double)-0.005f + Math.random() * (double)0.01f, (double)0.03f, (double)-0.005f + Math.random() * (double)0.01f);
                break;
            }
            case ENDER: {
                Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
                Vec3 offset = new Vec3(entitylivingbaseIn.level().random.nextGaussian(), entitylivingbaseIn.level().random.nextGaussian(), entitylivingbaseIn.level().random.nextGaussian()).normalize().scale((double)0.3f);
                origin = origin.add(offset);
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (Entity)entitylivingbaseIn), origin.x, origin.y, origin.z, particlePos.x, particlePos.y, particlePos.z);
                break;
            }
            case FIRE: {
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), (Entity)entitylivingbaseIn), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
                break;
            }
            case HELLFIRE: {
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), (Entity)entitylivingbaseIn), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
                break;
            }
            case LIGHTNING: {
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), (Entity)entitylivingbaseIn), particlePos.x, particlePos.y, particlePos.z, particlePos.x - (double)0.4f + Math.random() * (double)0.8f, particlePos.y - (double)0.4f + Math.random() * (double)0.8f, particlePos.z - (double)0.4f + Math.random() * (double)0.8f);
                break;
            }
            case WATER: {
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())), (Entity)entitylivingbaseIn), particlePos.x, particlePos.y, particlePos.z, -0.05 * Math.random() * 0.1, Math.random() * 0.05, -0.05 * Math.random() * 0.1);
                break;
            }
            case BLOOD: {
                if (!(Math.random() < (double)0.1f)) break;
                Vec3 offset = new Vec3(entitylivingbaseIn.level().getRandom().nextGaussian(), 1.0, entitylivingbaseIn.level().getRandom().nextGaussian()).normalize().scale((double)0.075f);
                particlePos = particlePos.add(offset);
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DROPLET.get())).setColor(91, 15, 14).setGravity(0.01f).setPhysics(true), (Entity)entitylivingbaseIn), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
                break;
            }
            case ICE: {
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FROST.get())), (Entity)entitylivingbaseIn), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
                break;
            }
            case WIND: {
                entitylivingbaseIn.level().addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.05f).setColor(10, 10, 10), (Entity)entitylivingbaseIn), particlePos.x, particlePos.y, particlePos.z, (double)0.3f, (double)0.01f, (double)0.05f);
                break;
            }
        }
    }
}

