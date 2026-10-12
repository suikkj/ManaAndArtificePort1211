 
package com.mna.gui.radial;

import com.google.common.collect.Lists;
import com.mna.KeybindInit;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.inventory.ISpellBookInventory;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.events.ClientEventHandler;
import com.mna.gui.radial.GenericRadialMenu;
import com.mna.gui.radial.components.IRadialMenuHost;
import com.mna.gui.radial.components.ItemStackRadialMenuItem;
import com.mna.gui.radial.components.RadialMenuItem;
import com.mna.interop.CuriosInterop;
import com.mna.items.sorcery.SpellBook;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import top.theillusivec4.curios.api.SlotContext;

@EventBusSubscriber(value={Dist.CLIENT})
public class SpellRadialSelect
extends Screen {
    private ItemStack stackEquipped;
    private ISpellBookInventory inventory;
    private boolean needsRecheckStacks = true;
    private final List<ItemStackRadialMenuItem> cachedMenuItems = Lists.newArrayList();
    private final GenericRadialMenu menu;
    private final InteractionHand hand;
    private final SlotContext curioSlotContext;

    public SpellRadialSelect(InteractionHand hand, SlotContext curioSlot) {
        super((Component)Component.literal((String)"RADIAL MENU"));
        this.hand = hand;
        this.curioSlotContext = curioSlot;
        this.minecraft = Minecraft.getInstance();
        this.stackEquipped = this.getTargetItem();
        if (this.stackEquipped.getItem() instanceof SpellBook) {
            this.inventory = ((SpellBook)this.stackEquipped.getItem()).getInventory(this.stackEquipped, (IPlayerMagic)this.minecraft.player.getData(PlayerMagicProvider.MAGIC.get()));
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
                return SpellRadialSelect.this;
            }

            @Override
            public Font getFontRenderer() {
                return SpellRadialSelect.this.font;
            }
        }){

            @Override
            public void onClickOutside() {
                this.close();
            }
        };
        this.menu.radiusOffset = 35.0f;
    }

    private ItemStack getTargetItem() {
        return this.curioSlotContext != null ? CuriosInterop.getCurioFromSlot((LivingEntity)this.minecraft.player, this.curioSlotContext) : this.minecraft.player.getItemInHand(this.hand);
    }

    @SubscribeEvent
    public static void overlayEvent(CustomizeGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof SpellRadialSelect) {
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
        if (!this.menu.isReady() || this.inventory == null) {
            return;
        }
        ItemStack inHand = this.getTargetItem();
        if (!(inHand.getItem() instanceof SpellBook)) {
            this.inventory = null;
        } else {
            ItemStack stack = inHand;
            if (stack.getCount() <= 0) {
                this.inventory = null;
                this.stackEquipped = null;
            } else if (this.stackEquipped != stack) {
                this.menu.close();
            }
        }
        if (this.inventory == null) {
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
        if (this.inventory == null) {
            return;
        }
        ItemStack inHand = this.getTargetItem();
        if (!(inHand.getItem() instanceof SpellBook)) {
            return;
        }
        if (this.needsRecheckStacks) {
            this.cachedMenuItems.clear();
            ItemStack[] activeSpells = this.inventory.getActiveSpells();
            int i = 0;
            while (i < activeSpells.length) {
                ItemStack inSlot = activeSpells[i];
                int index = i++;
                LocalPlayer player = this.minecraft.player;
                ItemStackRadialMenuItem item = new ItemStackRadialMenuItem(this.menu, inSlot, (Component)Component.translatable((String)"gui.mna.spellbook.empty"), (Player)player, inHand, index){
                    final   Player val$player;
                    final   ItemStack val$inHand;
                    final   int val$index;
                    {
                        this.val$player = player;
                        this.val$inHand = itemStack;
                        this.val$index = n;
                        super(owner, stack, altText);
                    }

                    @Override
                    public boolean onClick() {
                        SpellBook.setSlot(this.val$player, this.val$inHand, this.val$index, SpellRadialSelect.this.hand, SpellRadialSelect.this.curioSlotContext, true);
                        SpellRadialSelect.this.menu.close();
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


