 
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

public class MagiciansWorkbenchRecipeSetMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MagiciansWorkbenchRecipeSetMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("magicians_workbench_recipe_set_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, MagiciansWorkbenchRecipeSetMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> MagiciansWorkbenchRecipeSetMessage.encode(v, b), MagiciansWorkbenchRecipeSetMessage::decode);

    @Override
    public CustomPacketPayload.Type<MagiciansWorkbenchRecipeSetMessage> type() { return TYPE; }

    private int recipeIndex;

    public MagiciansWorkbenchRecipeSetMessage(int recipeIndex) {
        this.recipeIndex = recipeIndex;
        this.messageIsValid = true;
    }

    public MagiciansWorkbenchRecipeSetMessage() {
        this.messageIsValid = false;
    }

    public int getIndex() {
        return this.recipeIndex;
    }

    public static MagiciansWorkbenchRecipeSetMessage decode(FriendlyByteBuf buf) {
        MagiciansWorkbenchRecipeSetMessage msg = new MagiciansWorkbenchRecipeSetMessage();
        try {
            msg.recipeIndex = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading EnderDiscIndexSetMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(MagiciansWorkbenchRecipeSetMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getIndex());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu;
        if (player.containerMenu != null && (abstractContainerMenu = player.containerMenu) instanceof ContainerMagiciansWorkbench) {
            ContainerMagiciansWorkbench workbenchContainer = (ContainerMagiciansWorkbench)abstractContainerMenu;
            workbenchContainer.moveRecipeToCraftingGrid(this.getIndex());
        }
    }
}

