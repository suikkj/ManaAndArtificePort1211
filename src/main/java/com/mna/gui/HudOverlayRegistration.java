 
package com.mna.gui;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.config.ClientConfigValues;
import com.mna.api.items.IShowHud;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.gui.GuiTextures;
import com.mna.gui.HUDOverlayRenderer;
import com.mna.items.artifice.SpectralElytra;
import com.mna.items.manaweaving.ItemManaweaverWand;
import com.mna.tools.math.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(value={Dist.CLIENT}, modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class HudOverlayRegistration {
    @OnlyIn(value=Dist.CLIENT)
    @SubscribeEvent
    public static void onRegistrGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAboveAll(com.mna.api.tools.RLoc.create("mna_overlays"), (gui, deltaTracker) -> {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
            Minecraft mc = Minecraft.getInstance();
            if (mc.player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get()))) {
                HUDOverlayRenderer.instance.renderTextureOverlay(GuiTextures.Overlay.MIST_FORM, 0.8f);
            }
        });
        event.registerAboveAll(com.mna.api.tools.RLoc.create("mna_main_hud"), (gui, deltaTracker) -> {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
            if (ClientConfigValues.ShowHudMode == ClientConfigValues.HudMode.AlwaysShow || HudOverlayRegistration.shouldConditionalShow()) {
                HUDOverlayRenderer.instance.renderHUD(gui, gui.guiWidth(), gui.guiHeight(), deltaTracker.getGameTimeDeltaPartialTick(false));
            }
        });
        event.registerAboveAll(com.mna.api.tools.RLoc.create("mna_pinned_recipe"), (gui, deltaTracker) -> {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
            HUDOverlayRenderer.instance.renderPinnedRecipe(gui, gui.guiWidth(), gui.guiHeight());
            HUDOverlayRenderer.instance.renderPinnedDiagnostics(gui, gui.guiWidth(), gui.guiHeight());
        });
        event.registerAboveAll(com.mna.api.tools.RLoc.create("mna_cantrip_timer"), (gui, deltaTracker) -> {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
            HUDOverlayRenderer.instance.renderCantripTimer(gui, gui.guiWidth(), gui.guiHeight(), deltaTracker.getGameTimeDeltaPartialTick(false));
        });
        event.registerAboveAll(com.mna.api.tools.RLoc.create("mna_manaweave_timer"), (gui, deltaTracker) -> {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player.getUseItem().getItem() instanceof ItemManaweaverWand && ItemManaweaverWand.getStoredPattern((Level)mc.level, player.getUseItem()) != null) {
                IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
                float ticksNeeded = ItemManaweaverWand.getAutoweaveTicks((LivingEntity)player);
                float pct = MathUtils.clamp01((float)player.getTicksUsingItem() / ticksNeeded);
                if (pct < 1.0f) {
                    int[] color = new int[]{50, 0, 128};
                    if (progression != null && progression.getAlliedFaction() != null) {
                        color = progression.getAlliedFaction().getManaweaveRGB();
                    }
                    if (color == null) {
                        color = new int[]{50, 0, 128};
                    }
                    HUDOverlayRenderer.instance.renderCenteredTimer(gui, pct, 255, gui.guiWidth(), gui.guiHeight(), 50, color[0], color[1], color[2]);
                } else {
                    HUDOverlayRenderer.instance.renderCenteredTimer(gui, pct, 255, gui.guiWidth(), gui.guiHeight(), 50, 0, 255, 0);
                }
            }
        });
        event.registerAboveAll(com.mna.api.tools.RLoc.create("mna_spellbook_chord_hud"), (gui, deltaTracker) -> {
            com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            HUDOverlayRenderer.instance.renderSpellBookChordsHud((Player)player, gui);
        });
    }

    private static boolean shouldConditionalShow() {
        if (ClientConfigValues.ShowHudMode != ClientConfigValues.HudMode.ConditionalShow) {
            return false;
        }
        Player entityPlayer = ManaAndArtifice.instance.proxy.getClientPlayer();
        if (entityPlayer == null) {
            return false;
        }
        return entityPlayer.getMainHandItem().getItem() instanceof IShowHud || entityPlayer.getOffhandItem().getItem() instanceof IShowHud || entityPlayer.isFallFlying() && entityPlayer.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SpectralElytra;
    }
}


