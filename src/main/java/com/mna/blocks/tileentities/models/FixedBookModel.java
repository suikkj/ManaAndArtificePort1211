/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.util.Mth
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mna.blocks.tileentities.models;

import com.mna.api.tools.RLoc;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class FixedBookModel
extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(RLoc.create("sentry_book"), "main");
    private final ModelPart root;
    private final ModelPart leftLid;
    private final ModelPart rightLid;
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart flipPage1;
    private final ModelPart flipPage2;

    public FixedBookModel(ModelPart p_170475_) {
        super(RenderType::entitySolid);
        this.root = p_170475_;
        this.leftLid = p_170475_.getChild("left_lid");
        this.rightLid = p_170475_.getChild("right_lid");
        this.leftPages = p_170475_.getChild("left_pages");
        this.rightPages = p_170475_.getChild("right_pages");
        this.flipPage1 = p_170475_.getChild("flip_page1");
        this.flipPage2 = p_170475_.getChild("flip_page2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), PartPose.offset((float)0.0f, (float)0.0f, (float)-1.0f));
        partdefinition.addOrReplaceChild("right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0f, -5.0f, -0.005f, 6.0f, 10.0f, 0.005f), PartPose.offset((float)0.0f, (float)0.0f, (float)1.0f));
        partdefinition.addOrReplaceChild("seam", CubeListBuilder.create().texOffs(12, 0).addBox(-1.005f, -5.005f, -0.005f, 2.005f, 10.005f, 0.005f), PartPose.rotation((float)0.0f, (float)1.5707964f, (float)0.0f));
        partdefinition.addOrReplaceChild("left_pages", CubeListBuilder.create().texOffs(0, 10).addBox(0.0f, -4.0f, -0.99f, 5.0f, 8.0f, 1.0f), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_pages", CubeListBuilder.create().texOffs(12, 10).addBox(0.0f, -4.0f, -0.01f, 5.0f, 8.0f, 1.0f), PartPose.ZERO);
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(24, 10).addBox(0.0f, -4.0f, 0.0f, 5.0f, 8.0f, 0.005f);
        partdefinition.addOrReplaceChild("flip_page1", cubelistbuilder, PartPose.ZERO);
        partdefinition.addOrReplaceChild("flip_page2", cubelistbuilder, PartPose.ZERO);
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)64, (int)32);
    }

    public void renderToBuffer(PoseStack p_102298_, VertexConsumer p_102299_, int p_102300_, int p_102301_, float p_102302_, float p_102303_, float p_102304_, float p_102305_) {
        this.render(p_102298_, p_102299_, p_102300_, p_102301_, p_102302_, p_102303_, p_102304_, p_102305_);
    }

    public void render(PoseStack p_102317_, VertexConsumer p_102318_, int p_102319_, int p_102320_, float p_102321_, float p_102322_, float p_102323_, float p_102324_) {
        this.root.render(p_102317_, p_102318_, p_102319_, p_102320_, p_102321_, p_102322_, p_102323_, p_102324_);
    }

    public void setupAnim(float p_102293_, float p_102294_, float p_102295_, float p_102296_) {
        float f = (Mth.sin((float)(p_102293_ * 0.02f)) * 0.1f + 1.25f) * p_102296_;
        this.leftLid.yRot = (float)Math.PI + f;
        this.rightLid.yRot = -f;
        this.leftPages.yRot = f;
        this.rightPages.yRot = -f;
        this.flipPage1.yRot = f - f * 2.0f * p_102294_;
        this.flipPage2.yRot = f - f * 2.0f * p_102295_;
        this.leftPages.x = Mth.sin((float)f);
        this.rightPages.x = Mth.sin((float)f);
        this.flipPage1.x = Mth.sin((float)f);
        this.flipPage2.x = Mth.sin((float)f);
    }
}


