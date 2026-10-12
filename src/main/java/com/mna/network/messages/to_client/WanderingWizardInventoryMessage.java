 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.gui.containers.entity.ContainerWanderingWizard;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public class WanderingWizardInventoryMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WanderingWizardInventoryMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("wandering_wizard_inventory_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, WanderingWizardInventoryMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> WanderingWizardInventoryMessage.encode(v, b), WanderingWizardInventoryMessage::decode);

    @Override
    public CustomPacketPayload.Type<WanderingWizardInventoryMessage> type() { return TYPE; }

    private int containerId;
    private MerchantOffers offers;
    private int level;
    private int xp;
    private boolean hasXPBar;
    private boolean canRestock;
    private boolean isFinalized;

    public WanderingWizardInventoryMessage() {
        this.messageIsValid = false;
    }

    public WanderingWizardInventoryMessage(int id, MerchantOffers offersIn, int levelIn, int xpIn, boolean hasXPBar, boolean canRestock, boolean isFinal) {
        this.containerId = id;
        this.offers = offersIn;
        this.level = levelIn;
        this.xp = xpIn;
        this.hasXPBar = hasXPBar;
        this.canRestock = canRestock;
        this.isFinalized = isFinal;
        this.messageIsValid = true;
    }

    public static WanderingWizardInventoryMessage decode(FriendlyByteBuf buf) {
        WanderingWizardInventoryMessage msg = new WanderingWizardInventoryMessage();
        try {
            msg.containerId = buf.readVarInt();
            msg.offers = MerchantOffers.createFromStream((FriendlyByteBuf)buf);
            msg.level = buf.readVarInt();
            msg.xp = buf.readVarInt();
            msg.hasXPBar = buf.readBoolean();
            msg.canRestock = buf.readBoolean();
            msg.isFinalized = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading WanderingWizardInventoryMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(WanderingWizardInventoryMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.containerId);
        message.offers.writeToStream(buf);
        buf.writeVarInt(message.level);
        buf.writeVarInt(message.xp);
        buf.writeBoolean(message.hasXPBar);
        buf.writeBoolean(message.canRestock);
        buf.writeBoolean(message.isFinalized);
    }

    @Override
    public void Handle(Level world, Player player) {
        AbstractContainerMenu container = player.containerMenu;
        if (this.containerId == container.containerId && container instanceof ContainerWanderingWizard) {
            ContainerWanderingWizard wwContainer = (ContainerWanderingWizard)container;
            wwContainer.setClientSideOffers(new MerchantOffers(this.offers.createTag()));
            wwContainer.setXp(this.xp);
            wwContainer.setFinalized(this.isFinalized);
        }
    }
}

