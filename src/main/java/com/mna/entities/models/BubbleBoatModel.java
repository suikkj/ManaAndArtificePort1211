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
import com.mna.entities.constructs.BubbleBoat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class BubbleBoatModel<T extends BubbleBoat>
extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(RLoc.create("bubbleboatmodel"), "main");
    private final ModelPart bone;

    public BubbleBoatModel(ModelPart root) {
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(38, 0).addBox(-7.876f, -6.0f, -6.0f, 1.0f, 1.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(38, 0).addBox(7.124f, -6.0f, -6.0f, 1.0f, 1.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(38, 0).addBox(-7.876f, 5.0f, -6.0f, 1.0f, 1.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(38, 0).addBox(7.124f, 5.0f, -6.0f, 1.0f, 1.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(38, 17).addBox(5.124f, -8.0f, -6.0f, 1.0f, 1.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(38, 17).addBox(5.124f, 7.0f, -6.0f, 1.0f, 1.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(34, 49).addBox(6.124f, 6.0f, -7.0f, 1.0f, 1.0f, 14.0f, new CubeDeformation(0.0f)).texOffs(0, 51).addBox(6.124f, -6.0f, -7.0f, 1.0f, 12.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 51).addBox(6.124f, -6.0f, 6.0f, 1.0f, 12.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 51).addBox(-6.876f, -6.0f, -7.0f, 1.0f, 12.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 51).addBox(-6.876f, -6.0f, 6.0f, 1.0f, 12.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(34, 49).addBox(-6.876f, 6.0f, -7.0f, 1.0f, 1.0f, 14.0f, new CubeDeformation(0.0f)).texOffs(34, 49).addBox(6.124f, -7.0f, -7.0f, 1.0f, 1.0f, 14.0f, new CubeDeformation(0.0f)).texOffs(34, 49).addBox(-6.876f, -7.0f, -7.0f, 1.0f, 1.0f, 14.0f, new CubeDeformation(0.0f)).texOffs(37, 44).addBox(-5.876f, -7.0f, 6.0f, 12.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(37, 44).addBox(-5.876f, 6.0f, 6.0f, 12.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(37, 44).addBox(-5.876f, -7.0f, -7.0f, 12.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(37, 44).addBox(-5.876f, 6.0f, -7.0f, 12.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(38, 17).addBox(-5.876f, -8.0f, -6.0f, 1.0f, 1.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(38, 17).addBox(-5.876f, 7.0f, -6.0f, 1.0f, 1.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-4.876f, -8.0f, -6.0f, 10.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(38, 0).addBox(-5.876f, -6.0f, -8.0f, 12.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(38, 0).addBox(-5.876f, -6.0f, 7.0f, 12.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(38, 0).addBox(-5.876f, 5.0f, -8.0f, 12.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(38, 0).addBox(-5.876f, 5.0f, 7.0f, 12.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(5, 53).addBox(5.124f, -5.0f, -8.0f, 1.0f, 10.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(5, 53).addBox(5.124f, -5.0f, 7.0f, 1.0f, 10.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(5, 53).addBox(-5.876f, -5.0f, -8.0f, 1.0f, 10.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(5, 53).addBox(-5.876f, -5.0f, 7.0f, 1.0f, 10.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-4.876f, 7.0f, -6.0f, 10.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-4.876f, -8.0f, 5.0f, 10.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-4.876f, 7.0f, 5.0f, 10.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(5, 53).addBox(-7.876f, -5.0f, -6.0f, 1.0f, 10.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(5, 53).addBox(7.124f, -5.0f, -6.0f, 1.0f, 10.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(5, 53).addBox(-7.876f, -5.0f, 5.0f, 1.0f, 10.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(5, 53).addBox(7.124f, -5.0f, 5.0f, 1.0f, 10.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)16.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)64);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.bone.zRot = ((BubbleBoat)(entity)).getBoatRotation(limbSwing);
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.bone.render(poseStack, buffer, packedLight, packedOverlay);
    }
}

