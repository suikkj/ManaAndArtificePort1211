 
package com.mna.gui.radial;

import com.google.common.collect.Lists;
import com.mna.KeybindInit;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.spells.parts.Modifier;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.events.ClientEventHandler;
import com.mna.gui.radial.GenericRadialMenu;
import com.mna.gui.radial.components.BlitRadialMenuItem;
import com.mna.gui.radial.components.IRadialMenuHost;
import com.mna.gui.radial.components.RadialMenuItem;
import com.mna.items.ItemInit;
import com.mna.network.ClientMessageDispatcher;
import com.mna.recipes.RecipeInit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import com.mna.api.tools.MNACap;

@EventBusSubscriber(value={Dist.CLIENT})
public class ModifierRadialSelect
extends Screen {
    private ItemStack stackEquipped;
    private List<Modifier> knownModifiers;
    private boolean needsRecheckStacks = true;
    private final List<BlitRadialMenuItem> cachedMenuItems = Lists.newArrayList();
    private final GenericRadialMenu menu;
    private Minecraft mc;
    private final boolean offhand;

    public ModifierRadialSelect(boolean offhand) {
        super((Component)Component.literal((String)"RADIAL MENU"));
        this.offhand = offhand;
        this.mc = Minecraft.getInstance();
        this.knownModifiers = new ArrayList<Modifier>();
        this.stackEquipped = this.getHandItem();
        if (this.stackEquipped.getItem() == ItemInit.MODIFIER_BOOK.get()) {
            int tier = ((IPlayerProgression)MNACap.get(this.mc.player, PlayerProgressionProvider.PROGRESSION).get()).getTier();
            this.knownModifiers.addAll(this.mc.player.level().getRecipeManager().getAllRecipesFor(RecipeInit.MODIFIER_TYPE.get()).stream().filter(m -> m.value().getTier() <= tier).map(m -> m.value().getComponent()).collect(Collectors.toList()));
        } else {
            this.onClose();
        }
        this.menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost(){

            @Override
            public void renderTooltip(GuiGraphics pGuiGraphics, ItemStack stack, int mouseX, int mouseY) {
            }

            @Override
            public void renderTooltip(GuiGraphics pGuiGraphics, Component text, int mouseX, int mouseY) {
            }

            @Override
            public Screen getScreen() {
                return ModifierRadialSelect.this;
            }

            @Override
            public Font getFontRenderer() {
                return ModifierRadialSelect.this.font;
            }
        }){

            @Override
            public void onClickOutside() {
                this.close();
            }
        };
    }

    private ItemStack getHandItem() {
        return this.offhand ? this.mc.player.getOffhandItem() : this.mc.player.getMainHandItem();
    }

    @SubscribeEvent
    public static void overlayEvent(CustomizeGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ModifierRadialSelect) {
            event.setCanceled(true);
        }
    }

    public void removed() {
        super.removed();
        ClientEventHandler.wipeOpen();
    }

    public void tick() {
        super.tick();
        this.menu.tick();
        if (this.menu.isClosed()) {
            Minecraft.getInstance().setScreen(null);
            ClientEventHandler.wipeOpen();
        }
        if (!this.menu.isReady() || this.knownModifiers == null || this.knownModifiers.size() == 0) {
            return;
        }
        ItemStack inHand = this.getHandItem();
        if (inHand.getItem() != ItemInit.MODIFIER_BOOK.get()) {
            this.knownModifiers.clear();
        }
        if (this.knownModifiers.size() == 0) {
            Minecraft.getInstance().setScreen(null);
        } else if (!ClientEventHandler.isKeyDown((KeyMapping)KeybindInit.RadialMenuOpen.get())) {
            this.processClick(false);
        }
    }

    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        this.processClick(true);
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    protected void processClick(boolean triggeredByMouse) {
        this.menu.clickItem();
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.knownModifiers == null || this.knownModifiers.size() == 0) {
            return;
        }
        ItemStack inHand = this.getHandItem();
        if (inHand.getItem() != ItemInit.MODIFIER_BOOK.get()) {
            return;
        }
        if (this.needsRecheckStacks) {
            this.cachedMenuItems.clear();
            for (int i = 0; i < this.knownModifiers.size(); ++i) {
                final Modifier m = this.knownModifiers.get(i);
                BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, i, m.getGuiIcon(), 0, 0, 16, 16, 16, 16, (Component)Component.translatable((String)m.getRegistryName().toString())){

                    @Override
                    public boolean onClick() {
                        ClientMessageDispatcher.sendSelectedModifierMessage(m, ModifierRadialSelect.this.offhand);
                        ModifierRadialSelect.this.menu.close();
                        return true;
                    }
                };
                item.setVisible(true);
                this.cachedMenuItems.add(item);
            }
            this.menu.clear();
            this.menu.addAll(this.cachedMenuItems);
            this.needsRecheckStacks = false;
        }
        if (this.cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)) {
            this.menu.setCentralText((Component)Component.translatable((String)"gui.mna.spellbook.empty"));
        } else {
            this.menu.setCentralText(null);
        }
        this.menu.draw(pGuiGraphics, partialTicks, mouseX, mouseY);
    }

    public boolean isPauseScreen() {
        return false;
    }
}


