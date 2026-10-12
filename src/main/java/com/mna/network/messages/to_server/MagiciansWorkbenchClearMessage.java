 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.gui.containers.block.ContainerMagiciansWorkbench;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MagiciansWorkbenchClearMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MagiciansWorkbenchClearMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("magicians_workbench_clear_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, MagiciansWorkbenchClearMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> MagiciansWorkbenchClearMessage.encode(v, b), MagiciansWorkbenchClearMessage::decode);

    @Override
    public CustomPacketPayload.Type<MagiciansWorkbenchClearMessage> type() { return TYPE; }

    private boolean second;

    public MagiciansWorkbenchClearMessage(boolean second) {
        this.second = second;
        this.messageIsValid = true;
    }

    public MagiciansWorkbenchClearMessage() {
        this.messageIsValid = false;
    }

    public boolean isSecond() {
        return this.second;
    }

    public static MagiciansWorkbenchClearMessage decode(FriendlyByteBuf buf) {
        MagiciansWorkbenchClearMessage msg = new MagiciansWorkbenchClearMessage();
        try {
            msg.second = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading EnderDiscIndexSetMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(MagiciansWorkbenchClearMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isSecond());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu;
        if (player.containerMenu != null && (abstractContainerMenu = player.containerMenu) instanceof ContainerMagiciansWorkbench) {
            ContainerMagiciansWorkbench workbenchContainer = (ContainerMagiciansWorkbench)abstractContainerMenu;
            workbenchContainer.tryClearGrid(this.isSecond());
        }
    }
}

