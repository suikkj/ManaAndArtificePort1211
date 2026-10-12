 
package com.mna.gui;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.affinity.Affinity;
import com.mna.api.cantrips.ICantrip;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.resource.ICastingResourceGuiProvider;
import com.mna.api.config.ClientConfigValues;
import com.mna.api.guidebook.RecipeRendererBase;
import com.mna.api.items.ChargeableItem;
import com.mna.api.items.inventory.ISpellBookInventory;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.cantrips.CantripRegistry;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceGuiRegistry;
import com.mna.effects.EffectInit;
import com.mna.entities.constructs.animated.Construct;
import com.mna.gui.GuiTextures;
import com.mna.gui.widgets.entity.ConstructDiagnosticsDisplay;
import com.mna.items.sorcery.SpellBook;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.Matrix4f;
import top.theillusivec4.curios.api.CuriosApi;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.mna.api.tools.MNACap;

public class HUDOverlayRenderer {
    private Minecraft mc = Minecraft.getInstance();
    private RecipeRendererBase pinnedRecipe;
    private ConstructDiagnosticsDisplay pinnedDiagnostics = new ConstructDiagnosticsDisplay(0, 0, null);
    private int cantripTimer = 0;
    private int cantripTimerMax = 1;
    private int unlockTimer = 0;
    private int unlockTimerMax = 1;
    private boolean showTimerBar = true;
    private String castingCantrip = null;
    private float cantripAlpha = 0.0f;
    public boolean isRenderingSpellBookChords = false;
    private float newEntryUnlockedTextAlpha = 0.0f;
    protected int screenWidth;
    protected int screenHeight;
    public static HUDOverlayRenderer instance;
    private static final int NUMBER_WIDTH = 6;
    private static final int NUMBER_HEIGHT = 10;

    @SubscribeEvent(receiveCanceled=true, priority=EventPriority.LOWEST)
    @OnlyIn(value=Dist.CLIENT)
    public static void onRenderGameOverlay(net.neoforged.neoforge.client.event.RenderGuiEvent.Pre event) {
        LocalPlayer entityPlayerSP = HUDOverlayRenderer.instance.mc.player;
        HUDOverlayRenderer.instance.screenWidth = HUDOverlayRenderer.instance.mc.getWindow().getGuiScaledWidth();
        HUDOverlayRenderer.instance.screenHeight = HUDOverlayRenderer.instance.mc.getWindow().getGuiScaledHeight();
        if (entityPlayerSP == null) {
        }
        if (!entityPlayerSP.isAlive()) {
            instance.setPinnedRecipe(null);
            instance.setTrackedConstruct(null);
        }
        if (entityPlayerSP.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIND_VISION.get())) || entityPlayerSP.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()))) {
            MutableComponent prompt = Component.translatable((String)"gui.mna.doublejumptoexitprompt");
            int x = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
            int n = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();
            Objects.requireNonNull(HUDOverlayRenderer.instance.mc.font);
            int y = n - 9 - 5;
            event.getGuiGraphics().drawCenteredString(HUDOverlayRenderer.instance.mc.font, (Component)prompt, x, y, ChatFormatting.GOLD.getColor().intValue());
            event.setCanceled(true);
        }
        if (instance.renderColdDarkFade(net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth(), net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight(), event.getGuiGraphics()) || instance.renderTeleportHud(net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth(), net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight(), event.getGuiGraphics())) {
            event.setCanceled(true);
            Minecraft.getInstance().setScreen(null);
        }
    }

    @SubscribeEvent
    @OnlyIn(value=Dist.CLIENT)
    public static void onClientTick(ClientTickEvent event) {
        if (HUDOverlayRenderer.instance.cantripTimer > 0) {
            --HUDOverlayRenderer.instance.cantripTimer;
            if (HUDOverlayRenderer.instance.cantripTimer <= 10) {
                HUDOverlayRenderer.instance.cantripAlpha -= 0.1f;
            } else if (HUDOverlayRenderer.instance.cantripTimer >= HUDOverlayRenderer.instance.cantripTimerMax - 10) {
                HUDOverlayRenderer.instance.cantripAlpha += 0.1f;
            }
            if (HUDOverlayRenderer.instance.cantripTimer == 0) {
                HUDOverlayRenderer.instance.castingCantrip = null;
            }
        }
        if (HUDOverlayRenderer.instance.unlockTimer > 0) {
            --HUDOverlayRenderer.instance.unlockTimer;
            if (HUDOverlayRenderer.instance.unlockTimer <= 10) {
                HUDOverlayRenderer.instance.newEntryUnlockedTextAlpha -= 0.1f;
            } else if (HUDOverlayRenderer.instance.unlockTimer >= HUDOverlayRenderer.instance.unlockTimerMax - 10) {
                HUDOverlayRenderer.instance.newEntryUnlockedTextAlpha += 0.1f;
            }
        }
    }

    public void renderHUD(GuiGraphics pGuiGraphics, int screenWidth, int screenHeight, float partialTicks) {
        LocalPlayer player = this.mc.player;
        Font fr = this.mc.font;
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        if (magic == null || !magic.isMagicUnlocked()) {
        }
        Pair<Integer, Integer> coord = this.getHudCoordinates(screenWidth, screenHeight);
        int yPos = (Integer)coord.getSecond();
        int xPos = (Integer)coord.getFirst();
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)GuiTextures.Hud.BARS);
        this.renderManaBar(pGuiGraphics, xPos, yPos, magic, (Player)this.mc.player, partialTicks);
        if (ClientConfigValues.HudAffinity) {
            this.renderAffinity(pGuiGraphics, magic, fr, screenWidth, screenHeight);
        }
    }

    private Pair<Integer, Integer> getHudCoordinates(int screenWidth, int screenHeight) {
        int UIWidth = 170;
        int UIHeight = 35;
        switch (ClientConfigValues.HudPosition) {
            case BottomCenter: {
                return new Pair((screenWidth / 2 - UIWidth / 2), (screenHeight - UIHeight - 45));
            }
            case BottomLeft: {
                return new Pair(10, (screenHeight - UIHeight));
            }
            case BottomRight: {
                return new Pair((screenWidth - UIWidth), (screenHeight - UIHeight));
            }
            case MiddleLeft: {
                if (this.pinnedRecipe != null) {
                    return new Pair(-10, (screenHeight / 2 + (int)((float)this.pinnedRecipe.getHeight() * this.getPinSizeScale()) / 2));
                }
                return new Pair(-10, (screenHeight / 2 - UIHeight / 2));
            }
            case MiddleRight: {
                return new Pair((screenWidth - UIWidth), (screenHeight / 2 - UIHeight / 2));
            }
            case TopCenter: {
                return new Pair((screenWidth / 2 - UIWidth / 2), 1);
            }
            case TopRight: {
                return new Pair((screenWidth - UIWidth), 1);
            }
        }
        return new Pair(-10, 1);
    }

    private float getPinSizeScale() {
        return ClientConfigValues.PinnedRecipeScale == ClientConfigValues.PinnedRecipeSize.Small ? 0.35f : (ClientConfigValues.PinnedRecipeScale == ClientConfigValues.PinnedRecipeSize.Medium ? 0.55f : 0.75f);
    }

    public void renderPinnedRecipe(GuiGraphics pGuiGraphics, int screenWidth, int screenHeight) {
        if (this.pinnedRecipe != null && this.mc.player != null) {
            float scale = this.getPinSizeScale();
            float yPos = (float)(screenHeight / 2) - (float)this.pinnedRecipe.getHeight() * scale / 2.0f;
            this.pinnedRecipe.setScale(scale);
            this.pinnedRecipe.setX(0);
            this.pinnedRecipe.setY((int)yPos);
            this.pinnedRecipe.render(pGuiGraphics, 0, 0, 0.0f);
            RenderSystem.disableDepthTest();
        }
    }

    public void renderPinnedDiagnostics(GuiGraphics pGuiGraphics, int screenWidth, int screenHeight) {
        if (this.pinnedDiagnostics.isValid() && this.mc.player != null) {
            float yPos = screenHeight / 2 - this.pinnedDiagnostics.getHeight() / 2;
            this.pinnedDiagnostics.setX(0);
            this.pinnedDiagnostics.setY((int)yPos);
            this.pinnedDiagnostics.render(pGuiGraphics, 0, 0, 0.0f);
            RenderSystem.disableDepthTest();
        }
    }

    private void renderManaBar(GuiGraphics pGuiGraphics, int xPos, int yPos, IPlayerMagic magic, Player player, float partialTicks) {
        if (magic.getCastingResource().getMaxAmount() <= 0.0f) {
        }
        ICastingResourceGuiProvider provider = CastingResourceGuiRegistry.Instance.getGuiProvider(magic.getCastingResource().getRegistryName());
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate((float)xPos, (float)yPos, 0.0f);
        xPos = 14;
        yPos = 6;
        pGuiGraphics.fill(xPos + provider.getFillStartX(), yPos + provider.getFillStartY(), xPos + provider.getFillStartX() + provider.getFillWidth(), yPos + provider.getFillStartY() + provider.getFillHeight(), 0x7F000000);
        int scaledManaBarWidth = (int)(magic.getCastingResource().getAmount() / magic.getCastingResource().getMaxAmount() * (float)provider.getFillWidth());
        pGuiGraphics.pose().pushPose();
        if (scaledManaBarWidth > 0) {
            float manaCost;
            pGuiGraphics.fill(xPos + provider.getFillStartX(), yPos + provider.getFillStartY(), xPos + provider.getFillStartX() + scaledManaBarWidth, yPos + provider.getFillStartY() + provider.getFillHeight(), provider.getBarColor());
            SpellRecipe mainHand = null;
            SpellRecipe offHand = null;
            mainHand = player.getMainHandItem().getItem() instanceof SpellBook ? SpellRecipe.fromNBT(((SpellBook)player.getMainHandItem().getItem()).getSpellCompound(player.getMainHandItem(), player)) : SpellRecipe.fromNBT(player.getMainHandItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            offHand = player.getOffhandItem().getItem() instanceof SpellBook ? SpellRecipe.fromNBT(((SpellBook)player.getOffhandItem().getItem()).getSpellCompound(player.getOffhandItem(), player)) : SpellRecipe.fromNBT(player.getOffhandItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            if (mainHand != null && mainHand.isValid()) {
                SpellCaster.applyAdjusters(player.getMainHandItem(), (LivingEntity)player, InteractionHand.MAIN_HAND, false, mainHand, SpellCastStage.CALCULATING_MANA_COST);
            }
            if (mainHand != null && offHand.isValid()) {
                SpellCaster.applyAdjusters(player.getOffhandItem(), (LivingEntity)player, InteractionHand.OFF_HAND, false, offHand, SpellCastStage.CALCULATING_MANA_COST);
            }
            if ((manaCost = mainHand.getManaCost() + offHand.getManaCost()) > 0.0f) {
                float manaCostPct = MathUtils.clamp01(manaCost / magic.getCastingResource().getMaxAmount());
                int color = provider.getBarManaCostEstimateColor();
                if (manaCost > magic.getCastingResource().getAmount()) {
                    color = 0xFF0000;
                }
                int blitWidth = (int)((float)provider.getFrameWidth() * manaCostPct);
                int startX = Math.max(xPos + provider.getFillStartX() + scaledManaBarWidth - blitWidth, xPos + provider.getFillStartX());
                pGuiGraphics.fill(startX, yPos + provider.getFillStartY(), startX + blitWidth, yPos + provider.getFillStartY() + provider.getFillHeight(), color);
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            }
        }
        RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        int m = (int)((float)magic.getMagicXP() / (float)magic.getXPForLevel(magic.getMagicLevel() + 1) * (float)provider.getFillWidth());
        pGuiGraphics.fill(xPos + provider.getXPBarOffsetX(), yPos + provider.getXPBarOffsetY(), xPos + provider.getXPBarOffsetX() + m, yPos + provider.getXPBarOffsetY() + provider.getXPBarHeight(), provider.getXPBarColor());
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)provider.getTexture());
        pGuiGraphics.blit(GuiTextures.Hud.BARS, xPos, yPos, provider.getFrameU(), provider.getFrameV(), provider.getFrameWidth(), provider.getFrameHeight());
        int lcs = this.getLowChargeItemStatus();
        if (lcs != 0) {
            int delta = (provider.getFrameHeight() - provider.getFillHeight()) / 2 + provider.getLowChargeOffsetY();
            int v = lcs == 2 ? 18 : 0;
            pGuiGraphics.blit(GuiTextures.Hud.BARS, xPos + provider.getFrameWidth() + 2, yPos + delta, 20, 18, 230.0f, (float)v, 20, 18, 256, 256);
        }
        int digitOffset = 0;
        ArrayList<Integer> digits = new ArrayList<Integer>();
        for (int l = magic.getMagicLevel(); l > 0; l /= 10) {
            int digit = l % 10;
            digits.add(digit);
        }
        int levelXOffset = digits.size() == 1 ? 4 : 0;
        int offsetY = 0;
        switch (ClientConfigValues.HudPosition) {
            case BottomCenter: 
            case BottomLeft: 
            case BottomRight: {
                offsetY = -30;
            }
        }
        for (int i = digits.size() - 1; i >= 0; --i) {
            int u = 250;
            int v = 10 * (Integer)digits.get(i);
            pGuiGraphics.blit(GuiTextures.Hud.BARS, xPos + provider.getLevelDisplayX() + digitOffset + levelXOffset, yPos + provider.getLevelDisplayY() + offsetY, u, v, 6, 10);
            digitOffset += 7;
        }
        ItemStack deco = provider.getBadgeItem();
        if (!deco.isEmpty()) {
            pGuiGraphics.renderItem(deco, provider.getBadgeItemOffsetX(), provider.getBadgeItemOffsetY());
        }
        pGuiGraphics.pose().popPose();
        Font fr = this.mc.font;
        String s = String.format("%.2f / %.2f", Float.valueOf(magic.getCastingResource().getAmount()), Float.valueOf(magic.getCastingResource().getMaxAmount()));
        int textWidth = fr.width(s);
        int textX = xPos + provider.getResourceNumericOffsetX() + provider.getFillWidth() / 2 - textWidth / 2;
        pGuiGraphics.drawString(fr, s, textX, yPos + provider.getResourceNumericOffsetY(), provider.getResourceNumericTextColor(), false);
        if (this.newEntryUnlockedTextAlpha > 0.0f) {
            s = Component.translatable((String)"gui.mna.codex_entry_unlocked").getString();
            textWidth = fr.width(s);
            textX = xPos + provider.getLevelDisplayX() + digitOffset + levelXOffset + 4;
            int textY = yPos + provider.getLevelDisplayY() + offsetY + (offsetY < 0 ? 2 : 4);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Objects.requireNonNull(fr);
            pGuiGraphics.fill(textX - 3, textY - 3, textX + textWidth + 2, textY + 9 + 2, 0 | (int)(255.0f * this.newEntryUnlockedTextAlpha / 2.0f) << 24);
            pGuiGraphics.drawString(fr, s, textX, textY, 0xE7D7BE | (int)(255.0f * this.newEntryUnlockedTextAlpha) << 24, false);
        }
        pGuiGraphics.pose().popPose();
    }

    private int getLowChargeItemStatus() {
        MutableFloat lowestPct = new MutableFloat(1.0f);
        this.mc.player.getInventory().armor.stream().forEach(is -> lowestPct.setValue(Math.min(lowestPct.getValue().floatValue(), this.getChargePct((ItemStack)is))));
        this.mc.player.getInventory().items.stream().forEach(is -> lowestPct.setValue(Math.min(lowestPct.getValue().floatValue(), this.getChargePct((ItemStack)is))));
        java.util.Optional<Object> lazyCurios = CuriosApi.getCuriosHelper().getEquippedCurios((LivingEntity)this.mc.player);
        if (lazyCurios.isPresent()) {
            IItemHandlerModifiable curios = (IItemHandlerModifiable)lazyCurios.get();
            for (int i = 0; i < curios.getSlots(); ++i) {
                ItemStack is2 = curios.getStackInSlot(i);
                lowestPct.setValue(Math.min(lowestPct.getValue().floatValue(), this.getChargePct(is2)));
            }
        }
        if (lowestPct.getValue().floatValue() == 0.0f) {
            return 2;
        }
        if (lowestPct.getValue().floatValue() < 0.1f) {
            return 1;
        }
        return 0;
    }

    private float getChargePct(ItemStack is) {
        if (is.getItem() instanceof ChargeableItem) {
            ChargeableItem ci = (ChargeableItem)is.getItem();
            return ci.getMana(is) / ci.getMaxMana();
        }
        return 1.0f;
    }

    void renderAffinity(GuiGraphics pGuiGraphics, IPlayerMagic magic, Font fr, int screenWidth, int screenHeight) {
        boolean above;
        Minecraft mc = Minecraft.getInstance();
        if (fr == null) {
            fr = mc.font;
        }
        if (magic == null) {
            LocalPlayer p = mc.player;
            if (p == null) {
            }
            java.util.Optional<com.mna.api.capabilities.IPlayerMagic> cap = MNACap.get(p, PlayerMagicProvider.MAGIC);
            if (!cap.isPresent()) {
            }
            magic = (IPlayerMagic)cap.get();
        }
        List affs = magic.getSortedAffinityDepths().entrySet().stream().filter(e -> e.getKey() == ((Affinity)((e.getKey()))).getShiftAffinity()).limit(3L).collect(Collectors.toList());
        Pair<Integer, Integer> coords = instance.getHudCoordinates(screenWidth, screenHeight);
        int x = (Integer)coords.getFirst() + 35;
        int y = (Integer)coords.getSecond() + 32;
        boolean bl = above = ClientConfigValues.HudPosition == ClientConfigValues.HudPos.BottomCenter || ClientConfigValues.HudPosition == ClientConfigValues.HudPos.BottomLeft || ClientConfigValues.HudPosition == ClientConfigValues.HudPos.BottomRight;
        if (above) {
            y = (Integer)coords.getSecond() - 9;
        }
        if (HUDOverlayRenderer.instance.newEntryUnlockedTextAlpha > 0.0f) {
            Objects.requireNonNull(fr);
            int amt = 9 + 6;
            y = above ? (y -= amt) : (y += amt);
        }
        for (Map.Entry aff : affs) {
            ItemStack affStack = GuiTextures.affinityIcons.get(aff.getKey());
            if (affStack.isEmpty()) continue;
            pGuiGraphics.renderItem(affStack, x, y);
            String s = String.format("%.1f%%", aff.getValue());
            pGuiGraphics.drawCenteredString(fr, s, x + 16 + fr.width(s) / 2, y + 4, 0xE0E0E0);
            x += 45;
        }
    }

    public void setPinnedRecipe(RecipeRendererBase renderer) {
        this.pinnedRecipe = renderer;
    }

    public void setTrackedConstruct(Construct construct) {
        this.pinnedDiagnostics.setConstruct(construct);
    }

    public Construct getTrackedConstruct() {
        return this.pinnedDiagnostics.getConstruct();
    }

    public void setCastingCantrip(String cantrip, int timer) {
        this.castingCantrip = cantrip;
        if (timer == 0) {
            this.cantripTimer = 60;
            this.showTimerBar = false;
        } else {
            this.cantripTimer = timer;
            this.cantripTimerMax = Math.max(timer, 1);
        }
        this.cantripAlpha = 0.0f;
    }

    public void showCodexEntriesUnlocked() {
        this.newEntryUnlockedTextAlpha = 0.0f;
        this.unlockTimer = 200;
        this.unlockTimerMax = 200;
    }

    public void renderCantripTimer(GuiGraphics pGuiGraphics, int screenWidth, int screenHeight, float partialTicks) {
        if (this.castingCantrip == null) {
        }
        Optional<ICantrip> cantrip = CantripRegistry.INSTANCE.getCantrip(ResourceLocation.parse(this.castingCantrip));
        if (cantrip == null || !cantrip.isPresent()) {
        }
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        int x = screenWidth / 2 - GuiTextures.Cantrip_Icon_Size.x / 2;
        int y = screenHeight / 2 - GuiTextures.Cantrip_Icon_Size.y;
        float alphaF = MathUtils.clamp01(this.cantripAlpha);
        int alpha = (int)(255.0f * alphaF);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)alphaF);
        pGuiGraphics.blit(cantrip.get().getIcon(), x, y, 0, 0.0f, 0.0f, GuiTextures.Cantrip_Icon_Size.x, GuiTextures.Cantrip_Icon_Size.y, GuiTextures.Cantrip_Icon_Size.x, GuiTextures.Cantrip_Icon_Size.y);
        if (this.showTimerBar) {
            float pct = ((float)this.cantripTimer + partialTicks) / (float)this.cantripTimerMax;
            this.renderTimer(pGuiGraphics, pct, alpha, x, y += GuiTextures.Cantrip_Icon_Size.y + 10, GuiTextures.Cantrip_Icon_Size.x, 0, 0, 255);
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void renderCenteredTimer(GuiGraphics pGuiGraphics, float pct, int alpha, int screenWidth, int screenHeight, int width, int r, int g, int b) {
        this.renderTimer(pGuiGraphics, pct, alpha, screenWidth / 2 - width / 2, screenHeight / 2 + 10, width, r, g, b);
    }

    private void renderTimer(GuiGraphics pGuiGraphics, float pct, int alpha, int x, int y, int width, int r, int g, int b) {
        pGuiGraphics.fill(x, y, (int)((float)x + (float)width * pct), y + 3, FastColor.ARGB32.setColor((int)255, (int)r, (int)g, (int)b));
    }

    protected boolean renderColdDarkFade(int width, int height, GuiGraphics pGuiGraphics) {
        if (this.mc.player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.COLD_DARK.get()))) {
            int delay = 50;
            int fadeTime = 100;
            int duration = this.mc.player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.COLD_DARK.get())).getDuration();
            if (duration < delay + fadeTime) {
                RenderSystem.disableDepthTest();
                float opacity = (float)(fadeTime - (duration - delay)) / (float)fadeTime;
                if (opacity > 1.0f) {
                    opacity = 1.0f;
                }
                int color = (int)(255.0f * opacity) << 24 | 0x101020;
                pGuiGraphics.fill(0, 0, width, height, color);
                RenderSystem.enableDepthTest();
            }
            return true;
        }
        return false;
    }

    protected boolean renderTeleportHud(int width, int height, GuiGraphics pGuiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        IPlayerMagic playerMagic = (IPlayerMagic)mc.player.getData(PlayerMagicProvider.MAGIC.get());
        if (playerMagic == null || playerMagic.getTeleportElapsedTicks() == 0) {
            return false;
        }
        long gameTime = mc.player.level().getGameTime();
        int transitionTicks = playerMagic.getTeleportElapsedTicks();
        int transitionDuration = playerMagic.getTeleportTotalTicks();
        int endFadeTicks = Math.max(transitionDuration - 40, transitionDuration);
        float warpPct = (float)transitionTicks / (float)transitionDuration;
        float portalAlpha = (float)Math.max(transitionTicks - (transitionDuration - endFadeTicks), 0) / (float)endFadeTicks;
        long angle = gameTime * 10L;
        float size = (float)Math.max(width, height) * warpPct;
        RenderSystem.disableDepthTest();
        int color = (int)(89.25 * (double)warpPct) << 24;
        pGuiGraphics.fill(0, 0, width, height, color);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate((float)(width / 2), (float)(height / 2), 0.0f);
        pGuiGraphics.pose().mulPose(Axis.ZN.rotationDegrees((float)angle));
        float pX1 = -size;
        float pY1 = -size;
        float pX2 = size;
        float pY2 = size;
        float pMinU = 0.0f;
        float pMaxU = 1.0f;
        float pMinV = 0.0f;
        float pMaxV = 1.0f;
        float pBlitOffset = 0.0f;
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)GuiTextures.Overlay.TELEPORTING);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)((1.0f - portalAlpha) * 0.5f));
        Matrix4f matrix4f = pGuiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix4f, pX1, pY1, pBlitOffset).setUv(pMinU, pMinV);
        bufferbuilder.addVertex(matrix4f, pX1, pY2, pBlitOffset).setUv(pMinU, pMaxV);
        bufferbuilder.addVertex(matrix4f, pX2, pY2, pBlitOffset).setUv(pMaxU, pMaxV);
        bufferbuilder.addVertex(matrix4f, pX2, pY1, pBlitOffset).setUv(pMaxU, pMinV);
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        pGuiGraphics.pose().popPose();
        return true;
    }

    protected void renderTextureOverlay(ResourceLocation pTextureLocation, float pAlpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)pAlpha);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)pTextureLocation);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(0.0f, (float)this.screenHeight, -90.0f).setUv(0.0f, 1.0f);
        bufferbuilder.addVertex((float)this.screenWidth, (float)this.screenHeight, -90.0f).setUv(1.0f, 1.0f);
        bufferbuilder.addVertex((float)this.screenWidth, 0.0f, -90.0f).setUv(1.0f, 0.0f);
        bufferbuilder.addVertex(0.0f, 0.0f, -90.0f).setUv(0.0f, 0.0f);
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    protected void renderSpellBookChordsHud(Player player, GuiGraphics pGuiGraphics) {
        int i;
        if (!this.isRenderingSpellBookChords) {
        }
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        if (magic == null) {
        }
        ItemStack mhStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack ohStack = player.getItemInHand(InteractionHand.OFF_HAND);
        ISpellBookInventory bookInv = null;
        if (mhStack.getItem() instanceof SpellBook) {
            bookInv = ((SpellBook)mhStack.getItem()).getInventory(mhStack, magic);
        } else if (ohStack.getItem() instanceof SpellBook) {
            bookInv = ((SpellBook)ohStack.getItem()).getInventory(ohStack, magic);
        }
        if (bookInv == null) {
        }
        ItemStack[] spells = bookInv.getActiveSpells();
        if (spells.length == 0) {
        }
        int loops = Math.min(spells.length, 9);
        int xStep = loops * 4;
        int width = xStep * loops;
        int xStart = (this.screenWidth - width) / 2;
        int xEnd = xStart + width;
        int yStart = 32;
        int height = 24;
        int solidColor = FastColor.ARGB32.setColor((int)255, (int)42, (int)42, (int)42);
        pGuiGraphics.fillGradient(xStart, yStart, xEnd, yStart + height, solidColor, FastColor.ARGB32.setColor((int)0, (int)42, (int)42, (int)42));
        int xCur = xStart;
        for (i = 0; i < 10; ++i) {
            pGuiGraphics.fill(xCur, yStart, xCur + 2, yStart + height, solidColor);
            xCur += xStep;
        }
        xCur = xStart;
        for (i = 0; i < loops; ++i) {
            pGuiGraphics.renderItem(spells[i], xCur + xStep / 2 - 8, yStart + height / 2 - 8);
            xCur += xStep;
        }
        xCur = xStart;
        for (i = 1; i < 10; ++i) {
            Font font = this.mc.font;
            MutableComponent mutableComponent = Component.literal((String)String.format("%d", i));
            int n = xCur + xStep / 2 + 8;
            Objects.requireNonNull(this.mc.font);
            pGuiGraphics.drawString(font, (Component)mutableComponent, n, yStart + height - 9, 0xFFFFFF);
            xCur += xStep;
        }
    }

    public RecipeRendererBase getPinnedrecipe() {
        return this.pinnedRecipe;
    }

    public ConstructDiagnosticsDisplay getConstructDisplay() {
        return this.pinnedDiagnostics;
    }
}


