 
package com.mna.tools.render;

import com.mna.ManaAndArtifice;
import com.mna.api.blocks.tile.IMultiblockDefinition;
import com.mna.tools.DidYouKnowHelper;
import com.mna.tools.RotationUtils;
import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.awt.Color;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(value={Dist.CLIENT})
public class MultiblockRenderer {
    public static boolean hasMultiblock;
    private static IMultiblockDefinition multiblock;
    private static Component name;
    private static BlockPos renderPos;
    private static boolean posLocked;
    private static Rotation facingRotation;
    private static int blocks;
    private static int blocksDone;
    private static boolean syncWait;
    private static int timeComplete;
    private static BlockState lookingState;
    private static BlockPos lookingPos;
    private static MultiBufferSource.BufferSource buffers;
    private static boolean lowestLayerMode;

    public static void setMultiblock(IMultiblockDefinition multiblock, Component name, boolean flip) {
        if (flip && hasMultiblock) {
            hasMultiblock = false;
        } else {
            MultiblockRenderer.multiblock = multiblock;
            MultiblockRenderer.name = name;
            renderPos = null;
            hasMultiblock = multiblock != null;
            posLocked = false;
        }
    }

    public static void toggleLowestLayerMode() {
        lowestLayerMode = !lowestLayerMode;
    }

    @SubscribeEvent
    public static void onRenderHUD(RenderGuiLayerEvent.Post event) {
        if (hasMultiblock) {
            int fullAnimTime;
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Minecraft mc = Minecraft.getInstance();
            int waitTime = 40;
            int fadeOutSpeed = 4;
            float animTime = (float)timeComplete + (timeComplete == 0 ? 0.0f : event.getPartialTick().getGameTimeDeltaPartialTick(false));
            if (animTime > (float)(fullAnimTime = waitTime + 10)) {
                hasMultiblock = false;
                return;
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0f, -Math.max(0.0f, animTime - (float)waitTime) * (float)fadeOutSpeed, 0.0f);
            int x = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
            int y = 16;
            int width = 180;
            int height = 9;
            int left = x - width / 2;
            int top = y + 10;
            if (timeComplete > 0) {
                String s = I18n.get((String)"gui.mna:structure_complete", (Object[])new Object[0]);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0f, Math.min((float)(height + 5), animTime), 0.0f);
                guiGraphics.drawString(mc.font, s, x - mc.font.width(s) / 2, top + height - 10, 65280, true);
                guiGraphics.pose().popPose();
            }
            guiGraphics.fill(left - 1, top - 1, left + width + 1, top + height + 1, -16777216);
            guiGraphics.fillGradient(left, top, left + width, top + height, -10066330, -11184811);
            float pct = MathUtils.clamp01((float)blocksDone / (float)Math.max(1, blocks));
            int progressWidth = (int)((float)width * pct);
            int color = Mth.hsvToRgb((float)(pct / 3.0f), (float)1.0f, (float)1.0f) | 0xFF000000;
            int color2 = new Color(color).darker().getRGB();
            guiGraphics.fillGradient(left, top, left + progressWidth, top + height, color, color2);
            if (mc.options.forceUnicodeFont().get()) {
                guiGraphics.drawString(mc.font, name.getVisualOrderText(), x - mc.font.width((FormattedText)name) / 2, y + 10, 0xFFFFFF, true);
            } else {
                guiGraphics.drawString(mc.font, name.getVisualOrderText(), x - mc.font.width((FormattedText)name) / 2, y + 11, 0xFFFFFF, true);
            }
            if (!posLocked) {
                String s = I18n.get((String)"gui.mna:not_anchored", (Object[])new Object[0]);
                guiGraphics.drawString(mc.font, s, x - mc.font.width(s) / 2, top + height + 8, 0xFFFFFF, true);
            } else {
                if (lookingState != null) {
                    try {
                        Block block = lookingState.getBlock();
                        ItemStack stack = block.getCloneItemStack((net.minecraft.world.level.LevelReader)mc.level, lookingPos, lookingState);
                        if (!stack.isEmpty()) {
                            guiGraphics.drawString(mc.font, stack.getHoverName().getVisualOrderText(), left + 20, top + height + 8, 0xFFFFFF, true);
                            guiGraphics.renderItem(stack, left, top + height + 2);
                        }
                    }
                    catch (Exception block) {
                         
                    }
                }
                if (timeComplete == 0) {
                    color = 0xFFFFFF;
                    int posx = left + width;
                    int posy = top + height + 2;
                    int mult = 1;
                    String progress = blocksDone + "/" + blocks;
                    guiGraphics.drawString(mc.font, progress, posx - mc.font.width(progress) / mult, posy, color, true);
                }
            }
            guiGraphics.pose().popPose();
        }
    }

    @SubscribeEvent
    public static void onWorldRenderLast(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES && hasMultiblock && multiblock != null) {
            MultiblockRenderer.renderMultiblock(ManaAndArtifice.instance.proxy.getClientWorld(), event.getPoseStack());
        }
    }

    public static void anchorTo(BlockPos target, Rotation rot) {
        renderPos = target;
        facingRotation = rot;
        posLocked = true;
        DidYouKnowHelper.CheckAndShowDidYouKnow(ManaAndArtifice.instance.proxy.getClientPlayer(), "helptip.mna.visualized_multiblock");
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (hasMultiblock && !posLocked && event.getEntity() == Minecraft.getInstance().player) {
            MultiblockRenderer.anchorTo(event.getPos().relative(event.getFace()), MultiblockRenderer.getRotation((Entity)event.getEntity()));
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ManaAndArtifice.instance.proxy.getClientWorld() == null) {
            hasMultiblock = false;
        } else if (posLocked && !syncWait && blocks == blocksDone) {
            if (++timeComplete == 16) {
                Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.EXPERIENCE_ORB_PICKUP, (float)1.0f));
            }
            if (timeComplete == 60) {
                MultiblockRenderer.setMultiblock(null, null, false);
            }
        } else {
            timeComplete = 0;
        }
    }

    public static void renderMultiblock(Level world, PoseStack ms) {
        Minecraft mc = Minecraft.getInstance();
        if (!posLocked) {
            facingRotation = MultiblockRenderer.getRotation((Entity)mc.player);
            if (mc.hitResult instanceof BlockHitResult) {
                renderPos = ((BlockHitResult)mc.hitResult).getBlockPos().relative(((BlockHitResult)mc.hitResult).getDirection());
            }
        } else if (renderPos.distToCenterSqr((Position)mc.player.position()) > 4096.0) {
            return;
        }
        if (renderPos == null) {
            return;
        }
        if (multiblock.isSymmetrical()) {
            facingRotation = Rotation.NONE;
        }
        EntityRenderDispatcher erm = mc.getEntityRenderDispatcher();
        double renderPosX = erm.camera.getPosition().x();
        double renderPosY = erm.camera.getPosition().y();
        double renderPosZ = erm.camera.getPosition().z();
        ms.translate(-renderPosX, -renderPosY, -renderPosZ);
        if (buffers == null) {
            buffers = MultiblockRenderer.initBuffers(mc.renderBuffers().bufferSource());
        }
        BlockPos checkPos = null;
        if (mc.hitResult instanceof BlockHitResult) {
            BlockHitResult blockRes = (BlockHitResult)mc.hitResult;
            checkPos = blockRes.getBlockPos().relative(blockRes.getDirection());
        }
        blocks = 0;
        blocksDone = 0;
        lookingState = null;
        lookingPos = checkPos;
        MutableInt lowestY = new MutableInt(Integer.MAX_VALUE);
        HashMap<BlockPos, BlockState> missingBlocks = multiblock.getMissingBlocks(world, MultiblockRenderer.getStartPos(), MultiblockRenderer.getFacingRotation(), true);
        if (missingBlocks != null) {
            blocksDone = multiblock.getBlockCount() - missingBlocks.size();
            blocks = multiblock.getBlockCount();
            float alpha = 0.2f;
            if (lowestLayerMode) {
                missingBlocks.entrySet().stream().forEach(e -> {
                    if (lowestY.getValue() > ((BlockPos)e.getKey()).getY()) {
                        lowestY.setValue(((BlockPos)e.getKey()).getY());
                    }
                });
            }
            missingBlocks.entrySet().forEach(e -> {
                if (!MultiblockRenderer.posLocked() || !lowestLayerMode || ((BlockPos)e.getKey()).getY() == lowestY.getValue().intValue()) {
                    MultiblockRenderer.renderBlock(world, (BlockState)e.getValue(), (BlockPos)e.getKey(), alpha, ms);
                }
            });
            syncWait = false;
        } else {
            syncWait = true;
        }
        buffers.endBatch();
        if (!posLocked) {
            blocksDone = 0;
            blocks = 0;
        }
    }

    public static void renderBlock(Level world, BlockState state, BlockPos pos, float alpha, PoseStack ms) {
        if (pos != null) {
            ms.pushPose();
            ms.translate((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
            if (state.getBlock() == Blocks.AIR) {
                float scale = 0.3f;
                float off = (1.0f - scale) / 2.0f;
                ms.translate(off, off, -off);
                ms.scale(scale, scale, scale);
                state = Blocks.RED_CONCRETE.defaultBlockState();
            }
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, ms, (MultiBufferSource)buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            ms.popPose();
        }
    }

    public static IMultiblockDefinition getMultiblock() {
        return multiblock;
    }

    public static boolean posLocked() {
        return posLocked;
    }

    public static Rotation getFacingRotation() {
        return multiblock.isSymmetrical() ? Rotation.NONE : facingRotation;
    }

    public static BlockPos getStartPos() {
        return renderPos;
    }

    private static Rotation getRotation(Entity entity) {
        return RotationUtils.rotationFromFacing(entity.getDirection());
    }

    private static MultiBufferSource.BufferSource initBuffers(MultiBufferSource.BufferSource original) {
        BufferBuilder fallback = (BufferBuilder)ObfuscationReflectionHelper.getPrivateValue(MultiBufferSource.BufferSource.class, original, (String)"builder");
        Map layerBuffers = (Map)ObfuscationReflectionHelper.getPrivateValue(MultiBufferSource.BufferSource.class, original, (String)"fixedBuffers");
        Object2ObjectLinkedOpenHashMap remapped = new Object2ObjectLinkedOpenHashMap();
        for (Map.Entry e : (java.util.Set<Map.Entry>)layerBuffers.entrySet()) {
            remapped.put(GhostRenderType.remap((RenderType)e.getKey()), (BufferBuilder)e.getValue());
        }
        return new GhostBuffers(new com.mojang.blaze3d.vertex.ByteBufferBuilder(256), (java.util.SequencedMap<RenderType, com.mojang.blaze3d.vertex.ByteBufferBuilder>)(java.util.SequencedMap)remapped);
    }

    static {
        syncWait = true;
        buffers = null;
        lowestLayerMode = true;
    }

    private static class GhostRenderType
    extends RenderType {
        private static Map<RenderType, RenderType> remappedTypes = new IdentityHashMap<RenderType, RenderType>();

        private GhostRenderType(RenderType original) {
            super(String.format("%s_%s_ghost", original.toString(), "mna"), original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), true, () -> {
                original.setupRenderState();
                RenderSystem.disableDepthTest();
                RenderSystem.enableBlend();
                RenderSystem.blendEquation((int)32774);
                RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)0.6f);
            }, () -> {
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
                original.clearRenderState();
            });
        }

        public boolean equals(@Nullable Object other) {
            return this == other;
        }

        public int hashCode() {
            return System.identityHashCode(this);
        }

        public static RenderType remap(RenderType in) {
            if (in instanceof GhostRenderType) {
                return in;
            }
            return remappedTypes.computeIfAbsent(in, GhostRenderType::new);
        }
    }

    private static class GhostBuffers
    extends MultiBufferSource.BufferSource {
        protected GhostBuffers(com.mojang.blaze3d.vertex.ByteBufferBuilder fallback, java.util.SequencedMap<RenderType, com.mojang.blaze3d.vertex.ByteBufferBuilder> layerBuffers) {
            super(fallback, layerBuffers);
        }

        public VertexConsumer getBuffer(RenderType type) {
            return super.getBuffer(GhostRenderType.remap(type));
        }
    }
}

