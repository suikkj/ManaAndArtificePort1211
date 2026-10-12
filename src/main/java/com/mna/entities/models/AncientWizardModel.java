/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 */
package com.mna.entities.models;

import com.mna.api.tools.RLoc;
import com.mna.entities.LivingUtilityEntity;
import com.mna.entities.models.ModelPositionData;
import com.mna.entities.rituals.AncientCouncil;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class AncientWizardModel<T extends AncientCouncil>
extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(RLoc.create("ancientwizardmodel"), "main");
    private final ModelPart main;
    private final ModelPart right_arm;
    private final ModelPart right_elbow;
    private final ModelPart left_arm;
    private final ModelPart left_elbow;
    private final ModelPart head;
    private static ModelPositionData neutral;
    private static ModelPositionData imbue_stage_1;
    private static ModelPositionData imbue_stage_2;
    private static ModelPositionData imbue_stage_3;
    private static HashMap<String, ModelPositionData> animationMap;

    public AncientWizardModel(ModelPart root) {
        this.main = root.getChild("main");
        this.right_arm = this.main.getChild("right_arm");
        this.left_arm = this.main.getChild("left_arm");
        this.head = this.main.getChild("head");
        this.right_elbow = this.right_arm.getChild("right_elbow");
        this.left_elbow = this.left_arm.getChild("left_elbow");
        this.initPoses();
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(48, 22).addBox(-4.0f, -12.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(48, 22).addBox(0.0f, -12.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(0, 33).addBox(-4.0f, -24.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)24.0f, (float)0.0f));
        PartDefinition right_arm = main.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 0).addBox(-4.0f, -0.025f, -2.0f, 4.0f, 7.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-4.0f, (float)-24.0f, (float)0.0f, (float)-0.6981f, (float)0.0f, (float)0.0f));
        right_arm.addOrReplaceChild("right_elbow", CubeListBuilder.create().texOffs(48, 11).addBox(-2.0f, -1.025f, -1.95f, 4.0f, 7.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-1.95f, (float)6.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-1.0472f));
        PartDefinition left_arm = main.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 0).addBox(0.0f, -0.025f, -2.0f, 4.0f, 7.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)4.0f, (float)-24.0f, (float)0.0f, (float)-0.6981f, (float)0.0f, (float)0.0f));
        left_arm.addOrReplaceChild("left_elbow", CubeListBuilder.create().texOffs(48, 11).addBox(-2.075f, -1.025f, -1.95f, 4.0f, 7.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)2.05f, (float)6.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)1.0472f));
        main.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -12.0f, -2.0f, 1.0f, 24.0f, 5.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(4.0f, -12.0f, -2.0f, 1.0f, 24.0f, 5.0f, new CubeDeformation(0.0f)).texOffs(30, 0).addBox(-4.0f, -12.0f, 2.0f, 8.0f, 24.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(30, 0).addBox(-5.05f, -12.0f, -2.45f, 4.0f, 12.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(22, 0).addBox(-5.05f, 0.0f, -2.45f, 3.0f, 4.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(31, 13).addBox(-5.05f, 4.0f, -2.45f, 2.0f, 7.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(22, 0).mirror().addBox(1.95f, 0.0f, -2.45f, 3.0f, 4.0f, 1.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(31, 13).mirror().addBox(2.95f, 4.0f, -2.45f, 2.0f, 7.0f, 1.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(30, 0).mirror().addBox(0.95f, -12.0f, -2.45f, 4.0f, 12.0f, 1.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offset((float)0.0f, (float)-12.0f, (float)0.0f));
        main.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 52).addBox(-3.0f, -6.0f, -3.0f, 6.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)).texOffs(0, 49).addBox(-4.0f, -6.95f, -4.0f, 8.0f, 7.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)-24.0f, (float)0.0f));
        main.addOrReplaceChild("gem", CubeListBuilder.create().texOffs(15, 8).addBox(-1.0f, -1.0f, -0.5f, 2.0f, 2.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(15, 12).addBox(-0.5f, -0.5f, -0.55f, 1.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)-21.95f, (float)-1.675f, (float)0.0f, (float)0.0f, (float)0.7854f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
    }

    private void initPoses() {
        animationMap = new HashMap();
        neutral = new ModelPositionData();
        neutral.addPositionDegrees(this.head);
        neutral.addPositionDegrees(this.right_arm);
        neutral.addPositionDegrees(this.right_elbow);
        neutral.addPositionDegrees(this.left_arm);
        neutral.addPositionDegrees(this.left_elbow);
        animationMap.put("neutral", neutral);
        imbue_stage_1 = new ModelPositionData();
        imbue_stage_1.addPositionDegrees(this.head, new Vector3(-30.0, 0.0, 0.0));
        imbue_stage_1.addPositionDegrees(this.right_arm, new Vector3(40.0, -45.0, 0.0));
        imbue_stage_1.addPositionDegrees(this.right_elbow, new Vector3(0.0, 0.0, 17.5));
        imbue_stage_1.addPositionDegrees(this.left_arm, new Vector3(40.0, 45.0, 0.0));
        imbue_stage_1.addPositionDegrees(this.left_elbow, new Vector3(0.0, 0.0, -17.5));
        animationMap.put("imbue_stage_1", imbue_stage_1);
        imbue_stage_2 = new ModelPositionData();
        imbue_stage_2.addPositionDegrees(this.head, new Vector3(30.0, 0.0, 0.0));
        imbue_stage_2.addPositionDegrees(this.right_arm, new Vector3(135.0, 5.0, 44.0));
        imbue_stage_2.addPositionDegrees(this.right_elbow, new Vector3(0.0, 0.0, 35.0));
        imbue_stage_2.addPositionDegrees(this.left_arm, new Vector3(125.0, -5.0, -44.0));
        imbue_stage_2.addPositionDegrees(this.left_elbow, new Vector3(0.0, 0.0, -35.0));
        animationMap.put("imbue_stage_2", imbue_stage_2);
        imbue_stage_3 = new ModelPositionData();
        imbue_stage_3.addPositionDegrees(this.head, new Vector3(30.0, 0.0, 0.0));
        imbue_stage_3.addPositionDegrees(this.right_arm, new Vector3(95.0, 47.0, 80.0));
        imbue_stage_3.addPositionDegrees(this.right_elbow, new Vector3(0.0, 0.0, 15.0));
        imbue_stage_3.addPositionDegrees(this.left_arm, new Vector3(95.0, -47.0, -80.0));
        imbue_stage_3.addPositionDegrees(this.left_elbow, new Vector3(0.0, 0.0, -15.0));
        animationMap.put("imbue_stage_3", imbue_stage_3);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        int lastChange = ((LivingUtilityEntity)(entity)).getLastAnimChangeTimer();
        if (lastChange == 0) {
            animationMap.forEach((s, d) -> d.startLerp());
        } else {
            if (lastChange <= 20) {
                float neutral_return_timer = (float)lastChange / 20.0f;
                neutral.lerpRotations(neutral_return_timer);
            }
            float anim_pct = ((LivingUtilityEntity)(entity)).getAnimationPct(0.0f);
            String current_anim = ((LivingUtilityEntity)(entity)).getCurrentAnimation();
            if (animationMap.containsKey(current_anim)) {
                animationMap.get(current_anim).lerpRotations(anim_pct);
            }
        }
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.main.render(poseStack, buffer, packedLight, packedOverlay);
    }
}

