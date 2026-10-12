 
package com.mna.gui.containers.providers;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.item.ContainerRoteBook;
import com.mna.items.sorcery.BookOfRote;
import com.mna.items.sorcery.SpellBook;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import com.mna.api.tools.MNACap;

public class NamedRoteBook
implements MenuProvider {
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        java.util.Optional magicContainer;
        ItemStack curStack = player.getMainHandItem();
        int curIndex = 0;
        if (curStack.getItem() instanceof BookOfRote) {
            curIndex = SpellBook.getActiveSpellSlot(curStack);
        }
        if ((magicContainer = MNACap.get(player, PlayerMagicProvider.MAGIC)).isPresent()) {
            return new ContainerRoteBook(i, inventory, ((IPlayerMagic)magicContainer.get()).getRoteInventory(), curIndex);
        }
        return new ContainerRoteBook(i, inventory, null);
    }

    public Component getDisplayName() {
        return Component.literal((String)"");
    }
}


