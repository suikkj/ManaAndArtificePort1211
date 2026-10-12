 
package com.mna.gui.containers.item;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.items.inventory.SpellInventory;
import com.mna.api.spells.SpellValidityResult;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IDamageComponent;
import com.mna.api.spells.base.IModifiable;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.containers.ContainerInit;
import com.mna.items.base.IBagItem;
import com.mna.items.sorcery.ItemSpell;
import com.mna.items.sorcery.SpellBook;
import com.mna.network.ClientMessageDispatcher;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;

public class ContainerRoteBook
extends AbstractContainerMenu {
    public SpellInventory roteBook;
    private SpellRecipe currentSpellRecipe = null;
    private int index = 0;

    public ContainerRoteBook(int id, Inventory playerInv, FriendlyByteBuf buffer) {
        this(id, playerInv, ((IPlayerMagic)playerInv.player.getData(PlayerMagicProvider.MAGIC.get())).getRoteInventory(), SpellBook.getActiveSpellSlot(playerInv.player.getMainHandItem()));
    }

    public ContainerRoteBook(int id, Inventory playerInv, SpellInventory roteBook, int curIndex) {
        super((MenuType)ContainerInit.ROTE_BOOK.get(), id);
        this.roteBook = roteBook;
        this.changeIndex(curIndex);
    }

    public boolean stillValid(@Nonnull Player player) {
        ItemStack held = player.getMainHandItem();
        return held.getItem() instanceof IBagItem;
    }

    @Nullable
    public ModifiedSpellPart<Shape> getShape() {
        if (this.currentSpellRecipe == null) {
            return null;
        }
        return this.currentSpellRecipe.getShape();
    }

    public void setShape(Shape shape) {
        if (this.currentSpellRecipe != null) {
            this.currentSpellRecipe.setShape(shape);
        }
    }

    public void addComponent(SpellEffect component) {
        if (this.currentSpellRecipe != null) {
            this.currentSpellRecipe.addComponent(component);
        }
    }

    public void removeComponent(int index) {
        if (this.currentSpellRecipe != null) {
            this.currentSpellRecipe.removeComponent(index);
        }
    }

    public float getManaCost(Player player) {
        if (this.currentSpellRecipe == null) {
            return 0.0f;
        }
        this.currentSpellRecipe.calculateManaCost();
        SpellCaster.applyAdjusters(ItemStack.EMPTY, (LivingEntity)player, InteractionHand.MAIN_HAND, false, this.currentSpellRecipe, SpellCastStage.SPELLCRAFTING_MANA_COST_ESTIMATE);
        return this.currentSpellRecipe.isChanneled() ? this.currentSpellRecipe.getManaCost() * 20.0f : this.currentSpellRecipe.getManaCost();
    }

    public float getComplexity(Player player) {
        if (this.currentSpellRecipe == null) {
            return 0.0f;
        }
        this.currentSpellRecipe.calculateComplexity();
        return this.currentSpellRecipe.getComplexity();
    }

    public void increaseAttribute(Player player, IModifiable<?> part, Attribute attribute, Level world, boolean isShiftDown) {
        int count = isShiftDown ? 5 : 1;
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        float bonus = 0.0f;
        if (progression != null && progression.getAlliedFaction() != null) {
            bonus = progression.getAlliedFaction().getMaxModifierBonus(attribute);
        }
        for (int i = 0; i < count; ++i) {
            int index;
            if (part instanceof Shape) {
                this.currentSpellRecipe.changeShapeAttributeValue(attribute, ((ModifiedSpellPart)this.currentSpellRecipe.getShape()).stepUp(attribute, bonus));
                continue;
            }
            if (!(part instanceof SpellEffect) || (index = this.currentSpellRecipe.findComponent((SpellEffect)part)) <= -1) continue;
            this.currentSpellRecipe.changeComponentAttributeValue(index, attribute, ((ModifiedSpellPart)this.currentSpellRecipe.getComponent(index)).stepUp(attribute, bonus));
        }
    }

    public void decreaseAttribute(Player player, IModifiable<?> part, Attribute attribute, Level world, boolean isShiftDown) {
        int count = isShiftDown ? 5 : 1;
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        float bonus = 0.0f;
        if (progression != null && progression.getAlliedFaction() != null) {
            bonus = progression.getAlliedFaction().getMinModifierBonus(attribute);
        }
        for (int i = 0; i < count; ++i) {
            int index;
            if (part instanceof Shape) {
                this.currentSpellRecipe.changeShapeAttributeValue(attribute, ((ModifiedSpellPart)this.currentSpellRecipe.getShape()).stepDown(attribute, bonus));
                continue;
            }
            if (!(part instanceof SpellEffect) || (index = this.currentSpellRecipe.findComponent((SpellEffect)part)) <= -1) continue;
            this.currentSpellRecipe.changeComponentAttributeValue(index, attribute, ((ModifiedSpellPart)this.currentSpellRecipe.getComponent(index)).stepDown(attribute, bonus));
        }
    }

    @Nullable
    public ModifiedSpellPart<SpellEffect> getComponent(int index) {
        if (this.currentSpellRecipe == null) {
            return null;
        }
        return this.currentSpellRecipe.getComponent(index);
    }

    public void changeIndex(int newIndex) {
        if (newIndex < 0 || newIndex >= this.roteBook.getContainerSize()) {
            return;
        }
        this.copySpellChangesToInventory();
        this.index = newIndex;
        ItemStack stack = this.roteBook.getItem(newIndex);
        this.currentSpellRecipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        this.currentSpellRecipe.setMysterious(false);
    }

    public int getSize() {
        return this.roteBook.getContainerSize();
    }

    public void copySpellChangesToInventory() {
        if (this.currentSpellRecipe != null) {
            this.currentSpellRecipe.writeToNBT(this.roteBook.getItem(this.index).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            ClientMessageDispatcher.sendSpellBookSlotChange(this.index, InteractionHand.MAIN_HAND, null);
        }
    }

    public int getActiveIndex() {
        return this.index;
    }

    public void setName(String name) {
        this.roteBook.getItem(this.index).set(DataComponents.ITEM_NAME, (Component)Component.literal((String)name));
    }

    public void setIconIndex(int index) {
        ItemSpell.setCustomIcon(this.roteBook.getItem(this.index), index);
    }

    public int getIconIndex() {
        return ItemSpell.getCustomIcon(this.roteBook.getItem(this.index));
    }

    public int getCurColor() {
        return this.currentSpellRecipe == null ? -1 : this.currentSpellRecipe.getParticleColorOverride();
    }

    public void setCurColor(int color) {
        if (this.currentSpellRecipe != null) {
            this.currentSpellRecipe.setParticleColorOverride(color);
        }
    }

    public ItemStack getStack(int index) {
        return this.roteBook.getItem(index);
    }

    public String getName() {
        return this.roteBook.getItem(this.index).getHoverName().getString();
    }

    public int countNonDelayedDamageComponents() {
        MutableInt count = new MutableInt(0);
        this.currentSpellRecipe.iterateComponents(c -> {
            if (c.getPart() instanceof IDamageComponent && c.getValue(Attribute.DELAY) == 0.0f) {
                count.increment();
            }
        });
        return count.getValue();
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    public SpellValidityResult calculateCraftFeedback(Player player, IPlayerProgression progression) {
        return this.currentSpellRecipe.isCraftable(player.level(), progression, player.isCreative(), false, false);
    }
}

