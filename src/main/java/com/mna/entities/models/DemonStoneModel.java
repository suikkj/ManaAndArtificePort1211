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
 *  net.minecraft.world.entity.Entity
 */
package com.mna.entities.models;

import com.mna.api.tools.RLoc;
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
import net.minecraft.world.entity.Entity;

public class DemonStoneModel<T extends Entity>
extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(RLoc.create("demonstonemodel"), "main");
    private final ModelPart base;

    public DemonStoneModel(ModelPart root) {
        this.base = root.getChild("base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -16.0f, -6.0f, 12.0f, 16.0f, 12.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)24.0f, (float)0.0f));
        PartDefinition first_pivot = base.addOrReplaceChild("first_pivot", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -12.1216f, -5.8895f, 10.0f, 14.0f, 10.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)-16.0f, (float)1.0f, (float)-0.2182f, (float)0.0f, (float)0.0f));
        PartDefinition second_pivot = first_pivot.addOrReplaceChild("second_pivot", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -13.1158f, -7.6818f, 8.0f, 14.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)-8.6761f, (float)2.1737f, (float)-0.48f, (float)0.0f, (float)0.0f));
        second_pivot.addOrReplaceChild("third_pivot", CubeListBuilder.create().texOffs(5, 8).addBox(-2.0f, -11.1158f, -7.6818f, 6.0f, 12.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-1.0f, (float)-9.8618f, (float)0.8628f, (float)-0.48f, (float)0.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)32, (int)32);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.base.render(poseStack, buffer, packedLight, packedOverlay);
    }
}

