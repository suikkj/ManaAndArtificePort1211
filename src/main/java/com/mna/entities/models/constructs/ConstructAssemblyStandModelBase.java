/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 */
package com.mna.entities.models.constructs;

import com.mna.entities.constructs.ConstructAssemblyStand;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ConstructAssemblyStandModelBase
extends HumanoidModel<ConstructAssemblyStand> {
    public ConstructAssemblyStandModelBase(ModelPart p_170346_) {
        super(p_170346_);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation p_170348_) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh((CubeDeformation)p_170348_, (float)0.0f);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, p_170348_), PartPose.offset((float)0.0f, (float)1.0f, (float)0.0f));
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, p_170348_.extend(0.5f)), PartPose.offset((float)0.0f, (float)1.0f, (float)0.0f));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, p_170348_), PartPose.offset((float)-1.9f, (float)11.0f, (float)0.0f));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, p_170348_), PartPose.offset((float)1.9f, (float)11.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)32);
    }

    public void setupAnim(ConstructAssemblyStand p_102131_, float p_102132_, float p_102133_, float p_102134_, float p_102135_, float p_102136_) {
        this.head.xRot = (float)Math.PI / 180 * p_102131_.getHeadPose().getX();
        this.head.yRot = (float)Math.PI / 180 * p_102131_.getHeadPose().getY();
        this.head.zRot = (float)Math.PI / 180 * p_102131_.getHeadPose().getZ();
        this.body.xRot = (float)Math.PI / 180 * p_102131_.getBodyPose().getX();
        this.body.yRot = (float)Math.PI / 180 * p_102131_.getBodyPose().getY();
        this.body.zRot = (float)Math.PI / 180 * p_102131_.getBodyPose().getZ();
        this.leftArm.xRot = (float)Math.PI / 180 * p_102131_.getLeftArmPose().getX();
        this.leftArm.yRot = (float)Math.PI / 180 * p_102131_.getLeftArmPose().getY();
        this.leftArm.zRot = (float)Math.PI / 180 * p_102131_.getLeftArmPose().getZ();
        this.rightArm.xRot = (float)Math.PI / 180 * p_102131_.getRightArmPose().getX();
        this.rightArm.yRot = (float)Math.PI / 180 * p_102131_.getRightArmPose().getY();
        this.rightArm.zRot = (float)Math.PI / 180 * p_102131_.getRightArmPose().getZ();
        this.leftLeg.xRot = (float)Math.PI / 180 * p_102131_.getLeftLegPose().getX();
        this.leftLeg.yRot = (float)Math.PI / 180 * p_102131_.getLeftLegPose().getY();
        this.leftLeg.zRot = (float)Math.PI / 180 * p_102131_.getLeftLegPose().getZ();
        this.rightLeg.xRot = (float)Math.PI / 180 * p_102131_.getRightLegPose().getX();
        this.rightLeg.yRot = (float)Math.PI / 180 * p_102131_.getRightLegPose().getY();
        this.rightLeg.zRot = (float)Math.PI / 180 * p_102131_.getRightLegPose().getZ();
        this.hat.copyFrom(this.head);
    }
}

