/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
 *  net.minecraftforge.fluids.FluidType
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  software.bernie.geckolib.cache.object.BakedGeoModel
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.util.Color
 *  software.bernie.geckolib.util.RenderUtil
 */
package com.mna.entities.renderers.construct;

import com.mna.ManaAndArtifice;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.tools.RLoc;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.models.constructs.ConstructModel;
import com.mna.entities.models.constructs.modular.ConstructMaterialModel;
import com.mna.entities.models.constructs.modular.ConstructModelRegistry;
import com.mna.entities.renderers.MAGeckoRenderer;
import com.mna.items.constructs.parts.torso.ConstructPartManaTorso;
import com.mna.items.constructs.parts.torso.ConstructPartTankTorso;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.Color;
import software.bernie.geckolib.util.RenderUtil;

public class ConstructRenderer
extends MAGeckoRenderer<Construct> {
    private static final ResourceLocation FISHING_HOOK_TEX = ResourceLocation.parse("textures/entity/fishing_hook.png");
    private static final RenderType BOBBER_RENDER = RenderType.entityCutout((ResourceLocation)FISHING_HOOK_TEX);
    public static final ConstructModel constructModel = new ConstructModel();
    public static ConstructRenderer instance;
    public static final ResourceLocation model_brb_mining;
    public static final ResourceLocation model_brb_adventuring;
    public static final ResourceLocation model_brb_hunting;
    private final ItemRenderer itemRenderer;
    private RenderReferenceStack renderStack = null;
    private float fillPct = 0.0f;
    Matrix4f pos;
    Matrix3f normal;
    Minecraft mc = Minecraft.getInstance();

    public ConstructRenderer(EntityRendererProvider.Context context) {
        super(context, constructModel);
        this.itemRenderer = this.mc.getItemRenderer();
        instance = this;
    }

    @Override
    public void defaultRender(PoseStack poseStack, Construct animatable, MultiBufferSource bufferSource, RenderType renderType, VertexConsumer buffer, float yaw, float partialTick, int packedLight) {
        RenderReferenceStack latest;
        Color renderColor = this.getRenderColor(animatable, partialTick, packedLight);
        float red = renderColor.getRedFloat();
        float green = renderColor.getGreenFloat();
        float blue = renderColor.getBlueFloat();
        float alpha = renderColor.getAlphaFloat();
        int packedOverlay = this.getPackedOverlay(animatable, 0.0f, 0.0f);
        BakedGeoModel model = this.getGeoModel().getBakedModel(this.getGeoModel().getModelResource(animatable));
        this.renderStack = latest = new RenderReferenceStack(animatable.getMainHandItem(), animatable.getOffhandItem(), animatable.getConstructData().getHat(), animatable.getConstructData().getBanner());
        latest.model = (ConstructModel)this.getGeoModel();
        latest.data = animatable.getConstructData();
        latest.model.getBakedModel(latest.model.getModelResource(animatable));
        for (ConstructMaterial matl : latest.data.getComposition()) {
            latest.setVisibilityMatrix(animatable, matl);
            poseStack.pushPose();
            this.renderPass(poseStack, animatable, bufferSource, renderType, buffer, yaw, partialTick, packedLight, model, com.mna.tools.render.MNAGeo.packColour(red, green, blue, alpha), packedOverlay);
            poseStack.popPose();
        }
        animatable.getConstructData().getPart(ConstructSlot.TORSO).ifPresent(p -> {
            if (p instanceof ConstructPartManaTorso) {
                this.fillPct = animatable.getManaPct();
                this.renderChargeBar(packedLight, ((float)animatable.tickCount + partialTick) / 5.0f, bufferSource);
            } else if (p instanceof ConstructPartTankTorso) {
                this.fillPct = (float)animatable.getStoredFluidAmount() / (float)animatable.getTankCapacity(1);
                this.renderFluidBar(bufferSource, packedLight, partialTick, animatable, poseStack);
            }
        });
        this.renderFishingLine(bufferSource, animatable, partialTick, poseStack);
        this.renderHeldItems(bufferSource);
    }

    private void renderPass(PoseStack poseStack, Construct animatable, MultiBufferSource bufferSource, RenderType renderType, VertexConsumer buffer, float yaw, float partialTick, int packedLight, BakedGeoModel model, int colour, int packedOverlay) {
        this.emissivePass = false;
        if (renderType == null) {
            renderType = this.getRenderType(animatable, this.getTextureLocation(animatable), bufferSource, partialTick);
        }
        if (buffer == null) {
            buffer = bufferSource.getBuffer(renderType);
        }
        poseStack.pushPose();
        this.preRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, colour);
        if (this.firePreRenderEvent(poseStack, model, bufferSource, partialTick, packedLight)) {
            this.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, packedLight, packedLight, packedOverlay);
            this.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, colour);
            this.applyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            this.postRender(poseStack, animatable, model, bufferSource, buffer, false, partialTick, packedLight, packedOverlay, colour);
            this.firePostRenderEvent(poseStack, model, bufferSource, partialTick, packedLight);
        }
        poseStack.popPose();
        this.renderFinal(poseStack, animatable, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public void render(Construct entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.solid());
        int away_type = entityIn.getAwayType();
        if (away_type != 0) {
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(entityIn.yaw_rnd));
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(entityIn.pitch_rnd));
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(entityIn.roll_rnd));
            switch (entityIn.getAwayType()) {
                case 1: {
                    ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_brb_mining, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY);
                }
                case 2: {
                    ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_brb_adventuring, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY);
                }
                case 3: {
                    ModelUtils.renderEntityModel(builder, (Level)this.mc.level, model_brb_hunting, matrixStackIn, packedLightIn, OverlayTexture.NO_OVERLAY);
                }
            }
            matrixStackIn.popPose();
        }
        if (entityIn.isRenderDisabled()) {
        }
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void renderChargeBar(int packedLight, float partialTicks, MultiBufferSource bufferSource) {
        if (bufferSource == null || this.pos == null || this.normal == null) {
        }
        float alpha = this.fillPct > 0.2f ? 1.0f : Math.abs((float)Math.sin(partialTicks));
        float[] fullColor = new float[]{0.0f, 1.0f, 0.0f, alpha};
        float[] emptyColor = new float[]{1.0f, 0.0f, 0.0f, alpha};
        float width = 0.028f;
        float min = 0.65f;
        float height = 0.25f;
        VertexConsumer builder = bufferSource.getBuffer(RenderType.endGateway());
        float[] rgba = new float[]{MathUtils.lerpf(emptyColor[0], fullColor[0], this.fillPct), MathUtils.lerpf(emptyColor[1], fullColor[1], this.fillPct), MathUtils.lerpf(emptyColor[2], fullColor[2], this.fillPct), MathUtils.lerpf(emptyColor[3], fullColor[3], this.fillPct)};
        Vector3f nrm = new Vector3f(1.0f, 0.0f, 0.0f);
        nrm.mul((Matrix3fc)this.normal);
        builder.addVertex(this.pos, -width, min, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(0.0f, 0.0f);
        builder.setOverlay(0);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, width, min, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(1.0f, 0.0f);
        builder.setOverlay(0);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, width, min + height * this.fillPct, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(1.0f, 1.0f);
        builder.setOverlay(0);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, -width, min + height * this.fillPct, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(0.0f, 1.0f);
        builder.setOverlay(0);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
    }

    private void renderFluidBar(MultiBufferSource bufferIn, int packedLight, float partialTicks, Construct entity, PoseStack matrixStackIn) {
        if (this.pos == null || this.normal == null || entity == null || entity.getStoredFluid() == null) {
        }
        boolean isBone = entity.getConstructData().getSlotMaterial(ConstructSlot.TORSO) == ConstructMaterial.BONE;
        float width = 0.1f;
        float min = isBone ? 0.7125f : 0.65f;
        float height = isBone ? 0.25f : 0.3125f;
        FluidType attrs = entity.getStoredFluid().getFluidType();
        IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of((FluidType)attrs);
        ResourceLocation fluidTexBase = extension.getStillTexture();
        if (fluidTexBase == null) {
        }
        TextureAtlasSprite sp = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTexBase);
        RenderType liquid = RenderType.armorCutoutNoCull((ResourceLocation)TextureAtlas.LOCATION_BLOCKS);
        VertexConsumer builder = bufferIn.getBuffer(liquid);
        int color = extension.getTintColor();
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color >> 0 & 0xFF) / 255.0f;
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        float[] rgba = new float[]{r, g, b, a};
        Vector3f nrm = new Vector3f(0.0f, 1.0f, 0.0f);
        nrm.mul((Matrix3fc)this.normal);
        float maxV = sp.getV0() + (sp.getV1() - sp.getV0()) * this.fillPct;
        int light = 0xF000F0;
        builder.addVertex(this.pos, -width, min, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(sp.getU0(), sp.getV0());
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, width, min, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(sp.getU1(), sp.getV0());
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, width, min + height * this.fillPct, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(sp.getU1(), maxV);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, -width, min + height * this.fillPct, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(sp.getU0(), maxV);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        float topY = min + height * this.fillPct;
        builder.addVertex(this.pos, -width, topY, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(sp.getU0(), sp.getV0());
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, width, topY, 0.0f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(sp.getU1(), sp.getV0());
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, width, topY, -0.2f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(sp.getU1(), maxV);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
        builder.addVertex(this.pos, -width, topY, -0.2f);
        builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.setUv(sp.getU0(), maxV);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(packedLight);
        builder.setNormal(nrm.x(), nrm.y(), nrm.z());
    }

    private void renderHeldItems(MultiBufferSource bufferIn) {
        ClientLevel level = this.mc.level;
        if (this.renderStack.offHandStack != null) {
            this.itemRenderer.renderStatic(this.renderStack.offHand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, this.renderStack.packedLight, this.renderStack.packedOverlay, this.renderStack.offHandStack, bufferIn, (Level)level, 0);
        }
        if (this.renderStack.mainHandStack != null) {
            this.itemRenderer.renderStatic(this.renderStack.mainHand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, this.renderStack.packedLight, this.renderStack.packedOverlay, this.renderStack.mainHandStack, bufferIn, (Level)level, 0);
        }
        if (this.renderStack.hatStack != null) {
            this.itemRenderer.renderStatic(this.renderStack.hat, ItemDisplayContext.FIXED, this.renderStack.packedLight, this.renderStack.packedOverlay, this.renderStack.hatStack, bufferIn, (Level)level, 0);
        }
        if (this.renderStack.bannerStack != null) {
            this.itemRenderer.renderStatic(this.renderStack.banner, ItemDisplayContext.FIXED, this.renderStack.packedLight, this.renderStack.packedOverlay, this.renderStack.bannerStack, bufferIn, (Level)level, 0);
        }
        this.renderStack.mainHandStack = null;
        this.renderStack.offHandStack = null;
    }

    private void renderFishingLine(MultiBufferSource bufferSource, Construct construct, float partialTicks, PoseStack poseStack) {
        if (!construct.isFishing()) {
        }
        if (poseStack == null || construct.getFishingTarget() == null || !construct.blockPosition().closerThan((Vec3i)construct.getFishingTarget(), 8.0)) {
        }
        VertexConsumer lineStrips = bufferSource.getBuffer(RenderType.lineStrip());
        float bob = (float)(Math.sin(((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTicks) / 20.0f) / 12.0);
        poseStack.pushPose();
        Vec3 forward = construct.getForward().normalize();
        Vec3 side = forward.cross(new Vec3(0.0, 1.0, 0.0)).scale(construct.swingingArm == InteractionHand.OFF_HAND ? 0.175 : -0.275);
        Vec3 sourceOffset = new Vec3(0.0, 1.35, 0.0).add(forward.scale((double)0.94f)).add(side);
        poseStack.translate(sourceOffset.x, sourceOffset.y, sourceOffset.z);
        Vec3 source = construct.position().add(sourceOffset);
        Vec3 target = Vec3.upFromBottomCenterOf((Vec3i)construct.getFishingTarget(), (double)1.0).add(0.0, (double)bob, 0.0);
        Vec3 delta = target.subtract(source);
        PoseStack.Pose lastPose = poseStack.last();
        for (int k = 0; k <= 16; ++k) {
            ConstructRenderer.stringVertex((float)delta.x, (float)delta.y, (float)delta.z, lineStrips, lastPose, ConstructRenderer.fraction(k, 16), ConstructRenderer.fraction(k + 1, 16));
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(sourceOffset.x, sourceOffset.y, sourceOffset.z);
        poseStack.translate(delta.x, delta.y, delta.z);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        VertexConsumer vertexconsumer = bufferSource.getBuffer(BOBBER_RENDER);
        ConstructRenderer.vertex(vertexconsumer, matrix4f, matrix3f, this.renderStack.packedLight, 0.0f, 0, 0, 1);
        ConstructRenderer.vertex(vertexconsumer, matrix4f, matrix3f, this.renderStack.packedLight, 1.0f, 0, 1, 1);
        ConstructRenderer.vertex(vertexconsumer, matrix4f, matrix3f, this.renderStack.packedLight, 1.0f, 1, 1, 0);
        ConstructRenderer.vertex(vertexconsumer, matrix4f, matrix3f, this.renderStack.packedLight, 0.0f, 1, 0, 0);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer bufferBuilder, Matrix4f pose, Matrix3f normal, int packedLight, float x, int p_114717_, int p_114718_, int p_114719_) {
        bufferBuilder.addVertex(pose, x - 0.5f, (float)p_114717_ - 0.5f, 0.0f).setColor(255, 255, 255, 255).setUv((float)p_114718_, (float)p_114719_).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normal, 0.0f, 1.0f, 0.0f);
    }

    private static float fraction(int numerator, int denominator) {
        return (float)numerator / (float)denominator;
    }

    private static void stringVertex(float deltaX, float deltaY, float deltaZ, VertexConsumer vertexBuffer, PoseStack.Pose pose, float pctFrom, float pctTo) {
        float startX = deltaX * pctFrom;
        float startY = deltaY * (pctFrom * pctFrom + pctFrom) * 0.5f + 0.25f;
        float startZ = deltaZ * pctFrom;
        Vec3 normal = new Vec3((double)(deltaX * pctTo - startX), (double)(deltaY * (pctTo * pctTo + pctTo) * 0.5f + 0.25f - startY), (double)(deltaZ * pctTo - startZ)).normalize();
        vertexBuffer.addVertex(pose.pose(), startX, startY, startZ).setColor(187, 191, 191, 255).setNormal(pose.normal(), (float)normal.x, (float)normal.y, (float)normal.z);
    }

    @Override
    public RenderType getRenderType(Construct animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.solid();
    }

    public void renderRecursively(PoseStack stack, Construct animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        Optional<ItemConstructPart> TORSO = animatable.getConstructData().getPart(ConstructSlot.TORSO);
        if (bone.getName().equals("RIGHT_GRAB_POINT") && !this.renderStack.offHand.isEmpty()) {
            Optional<ItemConstructPart> rightArm = animatable.getConstructData().getPart(ConstructSlot.RIGHT_ARM);
            boolean rightHandWickerwood = rightArm.isPresent() && rightArm.get().getMaterial().equals(ConstructMaterial.WICKERWOOD);
            stack.pushPose();
            if (rightHandWickerwood) {
                stack.translate(0.38, 0.2, -0.05);
            } else {
                stack.translate(0.53, 0.23, -0.05);
            }
            stack.scale(0.7f, 0.7f, 0.7f);
            this.renderStack.offHandStack = new PoseStack();
            this.renderStack.offHandStack.poseStack.add(stack.last());
            this.renderStack.packedLight = packedLight;
            this.renderStack.packedOverlay = packedOverlay;
            stack.popPose();
        } else if (bone.getName().equals("LEFT_GRAB_POINT") && !this.renderStack.mainHand.isEmpty()) {
            Optional<ItemConstructPart> leftArm = animatable.getConstructData().getPart(ConstructSlot.LEFT_ARM);
            boolean leftHandWickerwood = leftArm.isPresent() && leftArm.get().getMaterial().equals(ConstructMaterial.WICKERWOOD);
            stack.pushPose();
            if (leftHandWickerwood) {
                stack.translate(-0.38, 0.2, -0.05);
            } else {
                stack.translate(-0.53, 0.23, -0.05);
            }
            stack.scale(0.7f, 0.7f, 0.7f);
            this.renderStack.mainHandStack = new PoseStack();
            this.renderStack.mainHandStack.poseStack.add(stack.last());
            this.renderStack.packedLight = packedLight;
            this.renderStack.packedOverlay = packedOverlay;
            stack.popPose();
        } else if (bone.getName().equals("MANA_TORSO_GAUGE_POS") && TORSO.isPresent() && TORSO.get() instanceof ConstructPartManaTorso) {
            stack.pushPose();
            stack.translate(0.0f, 0.35f, 0.51f);
            if (TORSO.get().getMaterial() == ConstructMaterial.BONE) {
                stack.translate(0.0, 0.0, -0.166);
            }
            this.normal = stack.last().normal();
            this.pos = stack.last().pose();
            stack.popPose();
        } else if (bone.getName().equals("TANK_TORSO_FLUID_POS") && TORSO.isPresent() && TORSO.get() instanceof ConstructPartTankTorso) {
            stack.pushPose();
            stack.translate(0.0f, 0.22f, 0.48f);
            if (TORSO.get().getMaterial() == ConstructMaterial.BONE) {
                stack.translate(0.0, 0.0625, -0.1875);
            }
            this.normal = stack.last().normal();
            this.pos = stack.last().pose();
            stack.popPose();
        }
        if ((bone.getName().equals("RIGHT_GRAB_POINT") || bone.getName().equals("LEFT_GRAB_POINT")) && animatable.isFishing()) {
            if (animatable.handHasCapability(InteractionHand.MAIN_HAND, ConstructCapability.FISH)) {
                stack.pushPose();
                this.renderStack.mainHandStack = new PoseStack();
                this.renderStack.mainHandStack.poseStack.add(stack.last());
                this.renderStack.packedLight = packedLight;
                this.renderStack.packedOverlay = packedOverlay;
                stack.popPose();
            }
            if (animatable.handHasCapability(InteractionHand.OFF_HAND, ConstructCapability.FISH)) {
                stack.pushPose();
                this.renderStack.mainHandStack = new PoseStack();
                this.renderStack.mainHandStack.poseStack.add(stack.last());
                this.renderStack.packedLight = packedLight;
                this.renderStack.packedOverlay = packedOverlay;
                stack.popPose();
            }
        }
        ItemStack hatStack = animatable.getConstructData().getHat();
        if (bone.getName().equals("HAT") && !hatStack.isEmpty()) {
            stack.pushPose();
            stack.translate(0.0f, 1.775f, 0.0f);
            if (hatStack.getItem() instanceof BlockItem) {
                stack.translate(0.0, -0.05, 0.0);
                stack.scale(0.5f, 0.5f, 0.5f);
            } else if (hatStack.getItem() instanceof ItemConstructPart) {
                stack.translate(0.0, 0.0, 0.125);
                stack.scale(0.5f, 0.5f, 0.5f);
            } else {
                stack.translate(0.0, -0.125, 0.0);
                stack.mulPose(Axis.XP.rotationDegrees(90.0f));
                stack.scale(0.5f, 0.5f, 0.5f);
            }
            this.renderStack.hatStack = new PoseStack();
            this.renderStack.hatStack.poseStack.add(stack.last());
            this.renderStack.packedLight = packedLight;
            this.renderStack.packedOverlay = packedOverlay;
            stack.popPose();
        }
        if (bone.getName().equals("BANNER") && !animatable.getConstructData().getBanner().isEmpty()) {
            stack.pushPose();
            stack.translate(0.0f, 1.35f, 0.25f);
            stack.mulPose(Axis.YP.rotationDegrees(180.0f));
            stack.scale(1.5f, 1.5f, 1.5f);
            this.renderStack.bannerStack = new PoseStack();
            this.renderStack.bannerStack.poseStack.add(stack.last());
            this.renderStack.packedLight = packedLight;
            this.renderStack.packedOverlay = packedOverlay;
            stack.popPose();
        }
        ConstructMaterialModel cmm = ConstructModelRegistry.getMaterialModelFor(this.renderStack.matl);
        List<ResourceLocation> bonemodels = cmm.getForBone(bone.getName(), this.renderStack.model, animatable);
        if (!bone.isHidden()) {
            stack.pushPose();
            RenderUtil.translateMatrixToBone((PoseStack)stack, (GeoBone)bone);
            RenderUtil.translateToPivotPoint((PoseStack)stack, (GeoBone)bone);
            RenderUtil.rotateMatrixAroundBone((PoseStack)stack, (GeoBone)bone);
            RenderUtil.scaleMatrixForBone((PoseStack)stack, (GeoBone)bone);
            RenderUtil.translateAwayFromPivotPoint((PoseStack)stack, (GeoBone)bone);
            bonemodels.forEach(model -> {
                stack.pushPose();
                ModelUtils.renderEntityModel(buffer, (Level)this.mc.level, model, stack, packedLight, packedOverlay);
                stack.popPose();
            });
            for (GeoBone childBone : bone.getChildBones()) {
                this.renderRecursively(stack, animatable, childBone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
            }
            stack.popPose();
        }
    }

    public boolean shouldShowName(Construct entity) {
        if (entity.isAway()) {
            return false;
        }
        return super.shouldShowName((Entity)entity);
    }

    protected void renderNameTag(Construct construct, Component name, PoseStack matrixStack, MultiBufferSource buffer, int p_225629_5_) {
        super.renderNameTag((Entity)construct, name, matrixStack, buffer, p_225629_5_);
    }

    static {
        model_brb_mining = RLoc.create("construct/common/brb_mining");
        model_brb_adventuring = RLoc.create("construct/common/brb_adventuring");
        model_brb_hunting = RLoc.create("construct/common/brb_hunting");
    }

    private class RenderReferenceStack {
        public ItemStack mainHand = ItemStack.EMPTY;
        public ItemStack offHand = ItemStack.EMPTY;
        public ItemStack hat = ItemStack.EMPTY;
        public ItemStack banner = ItemStack.EMPTY;
        public ConstructModel model;
        public IConstructConstruction data;
        ConstructMaterial matl;
        private PoseStack mainHandStack = null;
        private PoseStack offHandStack = null;
        private PoseStack hatStack = null;
        private PoseStack bannerStack = null;
        private int packedLight;
        private int packedOverlay;

        public RenderReferenceStack(ItemStack mainHand, ItemStack offHand, ItemStack hat, ItemStack banner) {
            this.mainHand = mainHand;
            this.offHand = offHand;
            this.hat = hat;
            this.banner = banner;
        }

        public void setVisibilityMatrix(Construct entity, ConstructMaterial matl) {
            this.matl = matl;
            if (this.model == null || this.data == null || matl == null || entity == null) {
            }
            this.model.resetMutexVisibility();
            this.model.setActiveMaterial(matl);
            this.model.setOwner(entity.getOwner() != null ? entity.getOwner().getUUID() : null);
            for (ItemConstructPart part : this.data.getPartsForMaterial(matl)) {
                this.model.setMutexVisibility(part.getSlot(), part.getModelTypeMutex());
            }
        }
    }
}


