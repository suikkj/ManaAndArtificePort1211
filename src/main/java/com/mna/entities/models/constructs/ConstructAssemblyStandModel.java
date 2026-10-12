/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterables
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.HumanoidArm
 */
package com.mna.entities.models.constructs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mna.entities.constructs.ConstructAssemblyStand;
import com.mna.entities.models.constructs.ConstructAssemblyStandModelBase;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ConstructAssemblyStandModel
extends ConstructAssemblyStandModelBase {
    private final ModelPart rightBodyStick;
    private final ModelPart leftBodyStick;
    private final ModelPart shoulderStick;
    private final ModelPart basePlate;

    public ConstructAssemblyStandModel(ModelPart p_170356_) {
        super(p_170356_);
        this.rightBodyStick = p_170356_.getChild("right_body_stick");
        this.leftBodyStick = p_170356_.getChild("left_body_stick");
        this.shoulderStick = p_170356_.getChild("shoulder_stick");
        this.basePlate = p_170356_.getChild("base_plate");
        this.hat.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh((CubeDeformation)CubeDeformation.NONE, (float)0.0f);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, -7.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.offset((float)0.0f, (float)1.0f, (float)0.0f));
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0f, 0.0f, -1.5f, 12.0f, 3.0f, 3.0f), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset((float)-5.0f, (float)2.0f, (float)0.0f));
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0.0f, -2.0f, -1.0f, 2.0f, 12.0f, 2.0f), PartPose.offset((float)5.0f, (float)2.0f, (float)0.0f));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 11.0f, 2.0f), PartPose.offset((float)-1.9f, (float)12.0f, (float)0.0f));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0f, 0.0f, -1.0f, 2.0f, 11.0f, 2.0f), PartPose.offset((float)1.9f, (float)12.0f, (float)0.0f));
        partdefinition.addOrReplaceChild("right_body_stick", CubeListBuilder.create().texOffs(16, 0).addBox(-3.0f, 3.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_body_stick", CubeListBuilder.create().texOffs(48, 16).addBox(1.0f, 3.0f, -1.0f, 2.0f, 7.0f, 2.0f), PartPose.ZERO);
        partdefinition.addOrReplaceChild("shoulder_stick", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0f, 10.0f, -1.0f, 8.0f, 2.0f, 2.0f), PartPose.ZERO);
        partdefinition.addOrReplaceChild("base_plate", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0f, 11.0f, -6.0f, 12.0f, 1.0f, 12.0f), PartPose.offset((float)0.0f, (float)12.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
    }

    public void prepareMobModel(ConstructAssemblyStand p_102172_, float p_102173_, float p_102174_, float p_102175_) {
        this.basePlate.xRot = 0.0f;
        this.basePlate.yRot = (float)Math.PI / 180 * -Mth.rotLerp((float)p_102175_, (float)p_102172_.yRotO, (float)p_102172_.getYRot());
        this.basePlate.zRot = 0.0f;
    }

    @Override
    public void setupAnim(ConstructAssemblyStand p_102177_, float p_102178_, float p_102179_, float p_102180_, float p_102181_, float p_102182_) {
        super.setupAnim(p_102177_, p_102178_, p_102179_, p_102180_, p_102181_, p_102182_);
        this.leftArm.visible = false;
        this.rightArm.visible = false;
        this.basePlate.visible = true;
        this.rightBodyStick.xRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getX();
        this.rightBodyStick.yRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getY();
        this.rightBodyStick.zRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getZ();
        this.leftBodyStick.xRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getX();
        this.leftBodyStick.yRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getY();
        this.leftBodyStick.zRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getZ();
        this.shoulderStick.xRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getX();
        this.shoulderStick.yRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getY();
        this.shoulderStick.zRot = (float)Math.PI / 180 * p_102177_.getBodyPose().getZ();
    }

    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat((Iterable)super.bodyParts(), (Iterable)ImmutableList.of(this.rightBodyStick, this.leftBodyStick, this.shoulderStick, this.basePlate));
    }

    public void translateToHand(HumanoidArm p_102157_, PoseStack p_102158_) {
        ModelPart modelpart = this.getArm(p_102157_);
        boolean flag = modelpart.visible;
        modelpart.visible = true;
        super.translateToHand(p_102157_, p_102158_);
        modelpart.visible = flag;
    }
}

