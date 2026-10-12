 
package com.mna.gui.containers.providers;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.item.ContainerCantrips;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.mna.api.tools.MNACap;

public class NamedCantrips
implements MenuProvider {
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        java.util.Optional<com.mna.api.capabilities.IPlayerMagic> magicContainer = MNACap.get(player, PlayerMagicProvider.MAGIC);
        if (magicContainer.isPresent()) {
            return new ContainerCantrips(i, inventory, ((IPlayerMagic)magicContainer.get()).getCantripData().getAsInventory());
        }
        return new ContainerCantrips(i, inventory, (FriendlyByteBuf)null);
    }

    public Component getDisplayName() {
        return Component.literal((String)"");
    }
}


