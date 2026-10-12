 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.BaseServerMessage;
import com.mna.recipes.multiblock.MultiblockDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class MultiblockSyncRequestMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MultiblockSyncRequestMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("multiblock_sync_request_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, MultiblockSyncRequestMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> MultiblockSyncRequestMessage.encode(v, b), MultiblockSyncRequestMessage::decode);

    @Override
    public CustomPacketPayload.Type<MultiblockSyncRequestMessage> type() { return TYPE; }

    private int entityID;
    private ResourceLocation recipeID;

    private MultiblockSyncRequestMessage() {
        this.messageIsValid = false;
    }

    public MultiblockSyncRequestMessage(int entityID, ResourceLocation recipe) {
        this();
        this.entityID = entityID;
        this.recipeID = recipe;
        this.messageIsValid = true;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public ResourceLocation getRecipe() {
        return this.recipeID;
    }

    public static MultiblockSyncRequestMessage decode(FriendlyByteBuf buf) {
        MultiblockSyncRequestMessage msg = new MultiblockSyncRequestMessage();
        try {
            msg.entityID = buf.readInt();
            msg.recipeID = buf.readResourceLocation();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MultiblockSyncRequestMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(MultiblockSyncRequestMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
        buf.writeResourceLocation(msg.getRecipe());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        Entity entity = player.level().getEntity(this.getEntityID());
        if (entity != null && entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            entity.level().getRecipeManager().byKey(this.getRecipe()).ifPresent(r -> {
                if (r.value() instanceof MultiblockDefinition) {
                    MultiblockDefinition mbd = (MultiblockDefinition)r.value();
                    ServerMessageDispatcher.sendStructureSyncMessage(mbd, serverPlayer);
                }
            });
        }
    }
}

