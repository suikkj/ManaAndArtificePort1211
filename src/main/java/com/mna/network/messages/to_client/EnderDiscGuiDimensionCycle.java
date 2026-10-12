 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.gui.containers.item.ContainerEnderDisc;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

public class EnderDiscGuiDimensionCycle
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EnderDiscGuiDimensionCycle> TYPE = new CustomPacketPayload.Type<>(RLoc.create("ender_disc_gui_dimension_cycle"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, EnderDiscGuiDimensionCycle> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> EnderDiscGuiDimensionCycle.encode(v, b), EnderDiscGuiDimensionCycle::decode);

    @Override
    public CustomPacketPayload.Type<EnderDiscGuiDimensionCycle> type() { return TYPE; }

    private ResourceLocation dimID;

    public EnderDiscGuiDimensionCycle(ResourceLocation dimID) {
        this.dimID = dimID;
        this.messageIsValid = true;
    }

    public EnderDiscGuiDimensionCycle() {
        this.messageIsValid = false;
    }

    public ResourceLocation getDimensionID() {
        return this.dimID;
    }

    public static EnderDiscGuiDimensionCycle decode(FriendlyByteBuf buf) {
        EnderDiscGuiDimensionCycle msg = new EnderDiscGuiDimensionCycle();
        try {
            msg.dimID = buf.readResourceLocation();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading EnderDiscGuiDimensionCycle: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(EnderDiscGuiDimensionCycle msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.getDimensionID());
    }

    @Override
    public void Handle(Level world, Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null && menu instanceof ContainerEnderDisc) {
            ((ContainerEnderDisc)menu).setDimension(this.getDimensionID());
        }
    }
}

