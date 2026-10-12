 
package com.mna.entities.renderers.ritual;

import com.mna.ManaAndArtifice;
import com.mna.api.rituals.RitualBlockPos;
import com.mna.entities.rituals.Ritual;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Quaternionf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;

public class RitualRenderer
extends EntityRenderer<Ritual> {
    private final Minecraft mc = Minecraft.getInstance();
    private final ItemRenderer itemRenderer = this.mc.getItemRenderer();
    private static final float FX_Y_OFFSET = 1.2f;
    private int reagentSlotCount = 1;

    public RitualRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(Ritual entity) {
        return null;
    }

    public void render(Ritual entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        boolean guiding_placement = entityIn.getState() == Ritual.RitualState.GUIDING_REAGENT_PLACEMENT;
        NonNullList<RitualBlockPos> rbp = guiding_placement ? entityIn.getRitualData((byte)1) : entityIn.getRitualData((byte)2);
        ArrayList<BlockPos> validPositions = entityIn.getValidReagentLocations();
        float speed = entityIn.getSpeed();
        int radiant_advance_rate = (int)Math.ceil(10.0f * speed);
        int beam_advance_rate = (int)Math.ceil(3.0f * speed);
        int maxPositions = entityIn.getState() == Ritual.RitualState.GUIDING_REAGENT_PLACEMENT ? 256 : entityIn.getAge() / radiant_advance_rate;
        int beamAge = entityIn.getAge() - radiant_advance_rate * rbp.size();
        int beamMaximum = 0;
        int iteratorMaximum = Math.min(maxPositions, rbp.size());
        int lastBeamStartIndex = -1;
        if (iteratorMaximum == rbp.size()) {
            beamMaximum = beamAge / beam_advance_rate;
        }
        this.reagentSlotCount = 1;
        for (int i = 0; i < iteratorMaximum; ++i) {
            if (lastBeamStartIndex == -1) {
                lastBeamStartIndex = i;
            }
            BlockPos curPos = ((RitualBlockPos)rbp.get(i)).getBlockPos();
            BlockPos nextPos = null;
            if (!guiding_placement) {
                if (i == iteratorMaximum - 1) {
                    if (entityIn.getCurrentRitual().getConnectBeam()) {
                        nextPos = ((RitualBlockPos)rbp.get(lastBeamStartIndex)).getBlockPos();
                    }
                } else {
                    int curIndex = ((RitualBlockPos)rbp.get(i)).getDisplayIndex();
                    int nextIndex = ((RitualBlockPos)rbp.get(i + 1)).getDisplayIndex();
                    if (nextIndex == curIndex + 1) {
                        nextPos = ((RitualBlockPos)rbp.get(i + 1)).getBlockPos();
                    } else {
                        if (entityIn.getCurrentRitual().getConnectBeam()) {
                            nextPos = ((RitualBlockPos)rbp.get(lastBeamStartIndex)).getBlockPos();
                        }
                        lastBeamStartIndex = i + 1;
                    }
                }
            }
            double xPosCur = (double)curPos.getX() - entityIn.getX() + 0.5;
            double zPosCur = (double)curPos.getZ() - entityIn.getZ() + 0.5;
            if (entityIn.getState() == Ritual.RitualState.GUIDING_REAGENT_PLACEMENT) {
                int index = i;
                this.renderReagentPlacementGuide(entityIn, (RitualBlockPos)rbp.get(i), matrixStackIn, xPosCur, zPosCur, bufferIn, packedLightIn, partialTicks, validPositions.stream().anyMatch(p -> p.equals(((RitualBlockPos)rbp.get(index)).getBlockPos())));
                continue;
            }
            matrixStackIn.pushPose();
            matrixStackIn.translate(xPosCur, (double)1.2f, zPosCur);
            int alpha = 128;
            if (i == iteratorMaximum - 1 && iteratorMaximum != rbp.size()) {
                float ageTicks = (float)(entityIn.getAge() % radiant_advance_rate) + partialTicks;
                float pct = ageTicks / (float)radiant_advance_rate;
                alpha = (int)(128.0f * pct);
            }
            WorldRenderUtils.renderRadiant(entityIn, matrixStackIn, bufferIn, RitualRenderer.colorsFromLong(entityIn.getCurrentRitual().getInnerColor()), RitualRenderer.colorsFromLong(entityIn.getCurrentRitual().getOuterColor()), alpha, 0.005f);
            if (nextPos != null) {
                double xPosNext = (double)nextPos.getX() - entityIn.getX() + 0.5;
                double zPosNext = (double)nextPos.getZ() - entityIn.getZ() + 0.5;
                if (i < beamMaximum - 1) {
                    WorldRenderUtils.renderBeam(entityIn.level(), partialTicks, matrixStackIn, bufferIn, packedLightIn, new Vec3(xPosCur, (double)1.2f, zPosCur), new Vec3(xPosNext, (double)1.2f, zPosNext), 1.0f, RitualRenderer.colorsFromLong(entityIn.getCurrentRitual().getBeamColor()), MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                } else if (!(i != beamMaximum - 1 || beamMaximum >= rbp.size() && entityIn.getCurrentRitual().getConnectBeam())) {
                    float beamPct = ((float)(beamAge % beam_advance_rate) + partialTicks) / (float)beam_advance_rate;
                    WorldRenderUtils.renderBeam(entityIn.level(), partialTicks, matrixStackIn, bufferIn, packedLightIn, new Vec3(xPosCur, (double)1.2f, zPosCur), new Vec3(xPosNext, (double)1.2f, zPosNext), beamPct, RitualRenderer.colorsFromLong(entityIn.getCurrentRitual().getBeamColor()), MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                }
            }
            matrixStackIn.popPose();
        }
        if (entityIn.getState() == Ritual.RitualState.COLLECTING_PATTERNS) {
            this.renderPatternGuide(entityIn, matrixStackIn, bufferIn);
        }
        this.renderCollectedReagents(entityIn, matrixStackIn, partialTicks, packedLightIn, bufferIn);
        super.render((Entity)entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private void renderCollectedReagents(Ritual entityIn, PoseStack matrixStackIn, float partialTicks, int packedLightIn, MultiBufferSource bufferIn) {
        NonNullList<Pair<BlockPos, ItemStack>> collectedReagents = entityIn.getCollectedReagentsByLocation();
        BlockPos pos = entityIn.blockPosition();
        float scale = 0.5f;
        float collapse = 1.0f;
        if (entityIn.getState() == Ritual.RitualState.COLLAPSING) {
            collapse = 1.0f - (float)entityIn.getStageTicks() / 20.0f;
        } else if (entityIn.getState().ordinal() > Ritual.RitualState.COLLAPSING.ordinal()) {
            return;
        }
        long ticks = ManaAndArtifice.instance.proxy.getGameTicks();
        float up = entityIn.getState() == Ritual.RitualState.COLLECTING_REAGENTS ? Math.min(((float)entityIn.getStageTicks() + partialTicks) / 20.0f, 1.0f) : 1.0f;
        float rotation = 0.0f;
        rotation = entityIn.getState() == Ritual.RitualState.COLLECTING_REAGENTS && entityIn.getStageTicks() <= 20 ? 0.0f : (float)((ticks - entityIn.worldTimeAtReagentCollectStart - 20L) % 360L) + partialTicks;
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.0f, up, 0.0f);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(rotation));
        matrixStackIn.scale(collapse, collapse, collapse);
        for (Pair e : collectedReagents) {
            matrixStackIn.pushPose();
            matrixStackIn.translate((float)(((BlockPos)e.getFirst()).getX() - pos.getX()), (float)(((BlockPos)e.getFirst()).getY() - pos.getY()), (float)(((BlockPos)e.getFirst()).getZ() - pos.getZ()));
            matrixStackIn.scale(scale, scale, scale);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(rotation));
            if (((ItemStack)e.getSecond()).getItem() instanceof BlockItem) {
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(rotation));
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(rotation));
            }
            this.itemRenderer.renderStatic((ItemStack)e.getSecond(), ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();
    }

    private void renderReagentPlacementGuide(Ritual entityIn, RitualBlockPos posData, PoseStack matrixStackIn, double xPosCur, double zPosCur, MultiBufferSource bufferIn, int packedLightIn, float partialTicks, boolean present) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(xPosCur, -0.5, zPosCur);
        if (posData.isPresent() && !posData.getReagent().isDynamic()) {
            Optional<Holder<Item>> rnd;
            HolderSet.Named<Item> tags;
            Item renderItem;
            if (entityIn.getCurrentRitual().getDisplayIndexes()) {
                if (posData.getReagent().isOptional()) {
                    this.renderNameTag(entityIn, (Component)Component.literal((String)("" + this.reagentSlotCount++)), matrixStackIn, bufferIn, packedLightIn);
                } else {
                    this.renderNameTag(entityIn, (Component)Component.literal((String)("" + this.reagentSlotCount++)).withStyle(ChatFormatting.GOLD), matrixStackIn, bufferIn, packedLightIn);
                }
            }
            if ((renderItem = (Item)BuiltInRegistries.ITEM.get(posData.getReagent().getResourceLocation())) == Items.AIR && (tags = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, posData.getReagent().getResourceLocation())).orElse(null)) != null && (rnd = tags.getRandomElement(RandomSource.create((long)432L))).isPresent()) {
                renderItem = rnd.get().value();
            }
            if (renderItem != null) {
                matrixStackIn.translate(0.0f, 1.2f, 0.0f);
                matrixStackIn.scale(0.3f, 0.3f, 0.3f);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees((float)entityIn.getStageTicks() + partialTicks));
                if (!present) {
                    this.itemRenderer.renderStatic(new ItemStack((ItemLike)renderItem), ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, (Level)this.mc.level, 0);
                }
            }
        }
        matrixStackIn.popPose();
    }

    private void renderPatternGuide(Ritual entityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn) {
        Quaternionf cameraRotation = this.entityRenderDispatcher.cameraOrientation();
        Quaternionf patternRotation = new Quaternionf(cameraRotation.x(), cameraRotation.y(), cameraRotation.z(), cameraRotation.w());
        ManaweavingPattern pattern = entityIn.getRequestedPattern();
        if (pattern != null) {
            WorldRenderUtils.renderManaweavePattern(pattern, patternRotation, matrixStackIn, bufferIn, false);
        }
    }

    private static int[] colorsFromLong(long packedColor) {
        return new int[]{(int)(packedColor >> 16 & 0xFFL), (int)(packedColor >> 8 & 0xFFL), (int)(packedColor & 0xFFL)};
    }
}


