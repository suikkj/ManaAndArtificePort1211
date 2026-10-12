 
package com.mna.gui.radial;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.google.common.collect.Lists;
import com.mna.KeybindInit;
import com.mna.events.ClientEventHandler;
import com.mna.gui.radial.GenericRadialMenu;
import com.mna.gui.radial.components.IRadialMenuHost;
import com.mna.gui.radial.components.ItemStackRadialMenuItem;
import com.mna.gui.radial.components.RadialMenuItem;
import com.mna.items.ItemInit;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.network.ClientMessageDispatcher;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(value={Dist.CLIENT})
public class RitualKitRadialSelect
extends Screen {
    private ItemStack stackEquipped;
    private boolean needsRecheckStacks = true;
    private final List<RadialMenuItem> cachedMenuItems = Lists.newArrayList();
    private final GenericRadialMenu menu;
    private boolean forceclose = false;
    private Minecraft mc = Minecraft.getInstance();
    private final boolean offhand;

    public RitualKitRadialSelect(boolean offhand) {
        super((Component)Component.literal((String)"RADIAL MENU"));
        this.offhand = offhand;
        this.stackEquipped = this.getHandItem();
        if (!(this.stackEquipped.getItem() instanceof PractitionersPouch)) {
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
                return RitualKitRadialSelect.this;
            }

            @Override
            public Font getFontRenderer() {
                return RitualKitRadialSelect.this.font;
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
    public static void overlayEvent(net.neoforged.neoforge.client.event.RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof RitualKitRadialSelect) {
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
        if (!this.menu.isReady()) {
            return;
        }
        ItemStack inHand = this.getHandItem();
        if (!(inHand.getItem() instanceof PractitionersPouch)) {
            this.forceclose = true;
        } else {
            ItemStack stack = inHand;
            if (stack.getCount() <= 0) {
                this.stackEquipped = null;
            } else if (this.stackEquipped != stack) {
                this.menu.close();
            }
        }
        if (this.forceclose) {
            Minecraft.getInstance().setScreen(null);
        } else if (!ClientEventHandler.isKeyDown((KeyMapping)KeybindInit.RadialMenuOpen.get())) {
            this.processClick(false);
            this.forceclose = true;
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
        if (this.needsRecheckStacks) {
            this.cachedMenuItems.clear();
            for (int i = 0; i < 8; ++i) {
                final int index = i;
                String translationKey = PractitionersPouch.getRitualRLoc(this.stackEquipped, i);
                MutableComponent menuDisplayItem = null;
                ItemStack stack = ItemStack.EMPTY;
                if (translationKey == "") {
                    menuDisplayItem = Component.translatable((String)"item.mna.ritual_kit.no_ritual_stored");
                    stack = new ItemStack((ItemLike)this.stackEquipped.getItem());
                    stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("hideTier", true);
                } else {
                    stack = new ItemStack((ItemLike)ItemInit.RUNE_PATTERN_RITUAL_METAL.get());
                    stack.set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)translationKey));
                    stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("hideTier", true);
                }
                ItemStackRadialMenuItem item = new ItemStackRadialMenuItem(this.menu, stack, (Component)menuDisplayItem){

                    @Override
                    public boolean onClick() {
                        ClientMessageDispatcher.sendRitualKitIndexChange(index, RitualKitRadialSelect.this.offhand);
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
            this.menu.setCentralText((Component)Component.translatable((String)"item.mna.ritual_kit"));
        } else {
            this.menu.setCentralText(null);
        }
        this.menu.draw(pGuiGraphics, partialTicks, mouseX, mouseY);
    }

    public boolean isPauseScreen() {
        return false;
    }
}


