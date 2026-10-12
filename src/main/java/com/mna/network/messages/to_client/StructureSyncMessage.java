 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import com.mna.recipes.multiblock.MultiblockDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class StructureSyncMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StructureSyncMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("structure_sync_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, StructureSyncMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> StructureSyncMessage.encode(v, b), StructureSyncMessage::decode);

    @Override
    public CustomPacketPayload.Type<StructureSyncMessage> type() { return TYPE; }

    private CompoundTag _data;
    private ResourceLocation structureID;

    private StructureSyncMessage(CompoundTag data, ResourceLocation structureID) {
        this._data = data;
        this.structureID = structureID;
        this.messageIsValid = true;
    }

    private StructureSyncMessage() {
        this.messageIsValid = false;
    }

    public static StructureSyncMessage decode(FriendlyByteBuf buf) {
        StructureSyncMessage msg = new StructureSyncMessage();
        try {
            msg._data = buf.readNbt();
            msg.structureID = buf.readResourceLocation();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading StructureSyncMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(StructureSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg._data);
        buf.writeResourceLocation(msg.structureID);
    }

    public static StructureSyncMessage fromMultiblock(MultiblockDefinition multiblock, ServerLevel world) {
        return new StructureSyncMessage(multiblock.serializeCoreBlocks(world), multiblock.getId());
    }

    @Override
    public void Handle(Level world, Player player) {
        world.getRecipeManager().byKey(this.structureID).ifPresent(r -> {
            if (r.value() instanceof MultiblockDefinition) {
                MultiblockDefinition mbd = (MultiblockDefinition)r.value();
                mbd.deserializeCoreBlocks(this._data);
            }
        });
    }
}

