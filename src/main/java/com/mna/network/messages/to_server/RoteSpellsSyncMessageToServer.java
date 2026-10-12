 
package com.mna.network.messages.to_server;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.items.inventory.SpellInventory;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.gui.containers.item.ContainerRoteBook;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.BookOfRote;
import com.mna.items.sorcery.SpellBook;
import com.mna.network.messages.BaseServerMessage;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class RoteSpellsSyncMessageToServer
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RoteSpellsSyncMessageToServer> TYPE = new CustomPacketPayload.Type<>(RLoc.create("rote_spells_sync_message_to_server"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RoteSpellsSyncMessageToServer> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RoteSpellsSyncMessageToServer.encode(v, b), RoteSpellsSyncMessageToServer::decode);

    @Override
    public CustomPacketPayload.Type<RoteSpellsSyncMessageToServer> type() { return TYPE; }

    private NonNullList<ItemStack> _roteInventory = NonNullList.create();

    public RoteSpellsSyncMessageToServer() {
        this.messageIsValid = false;
    }

    public RoteSpellsSyncMessageToServer(SpellInventory roteInventory) {
        this();
        for (int i = 0; i < roteInventory.getContainerSize(); ++i) {
            this._roteInventory.add(roteInventory.getItem(i).copy());
        }
        this.messageIsValid = true;
    }

    public final NonNullList<ItemStack> getInventory() {
        return this._roteInventory;
    }

    public static final RoteSpellsSyncMessageToServer decode(FriendlyByteBuf buf) {
        RoteSpellsSyncMessageToServer msg = new RoteSpellsSyncMessageToServer();
        try {
            int count = buf.readInt();
            for (int i = 0; i < count; ++i) {
                msg._roteInventory.add(buf.readItem());
            }
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RoteSpellsSyncMessageToServer: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(RoteSpellsSyncMessageToServer msg, FriendlyByteBuf buf) {
        buf.writeInt(msg._roteInventory.size());
        for (int i = 0; i < msg._roteInventory.size(); ++i) {
            buf.writeItem((ItemStack)msg._roteInventory.get(i));
        }
    }

    public static final RoteSpellsSyncMessageToServer fromRoteBookContainer(ContainerRoteBook container) {
        return new RoteSpellsSyncMessageToServer(container.roteBook);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            MutableInt playerTier = new MutableInt(0);
            MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
            for (int i = 0; i < this.getInventory().size(); ++i) {
                ItemStack roteStack = (ItemStack)this.getInventory().get(i);
                SpellRecipe spell = SpellRecipe.fromNBT(roteStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                if (!spell.isValid()) {
                    m.getRoteInventory().setItem(i, new ItemStack((ItemLike)ItemInit.SPELL.get()));
                    continue;
                }
                m.getRoteInventory().setItem(i, roteStack);
            }
            ItemStack spellStack = player.getInventory().getItem(player.getInventory().selected);
            if (spellStack.getItem() instanceof BookOfRote) {
                int slot = SpellBook.getActiveSpellSlot(spellStack);
                SpellBook.setSlot((Player)player, spellStack, slot, InteractionHand.MAIN_HAND, null, false);
            }
            m.setSyncRote();
            m.forceSync();
        });
    }
}

