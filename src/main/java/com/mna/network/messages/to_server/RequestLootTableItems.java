 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.BaseServerMessage;
import com.mna.tools.loot.LootDrop;
import com.mna.tools.loot.LootTableCache;
import com.mna.tools.loot.LootTableHelper;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RequestLootTableItems
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestLootTableItems> TYPE = new CustomPacketPayload.Type<>(RLoc.create("request_loot_table_items"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RequestLootTableItems> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RequestLootTableItems.encode(v, b), RequestLootTableItems::decode);

    @Override
    public CustomPacketPayload.Type<RequestLootTableItems> type() { return TYPE; }

    ResourceLocation lootTableID;

    public RequestLootTableItems(ResourceLocation lootTableID) {
        this.lootTableID = lootTableID;
        this.messageIsValid = true;
    }

    public RequestLootTableItems() {
        this.messageIsValid = false;
    }

    public ResourceLocation getLootTableID() {
        return this.lootTableID;
    }

    public static RequestLootTableItems decode(FriendlyByteBuf buf) {
        RequestLootTableItems msg = new RequestLootTableItems();
        try {
            msg.lootTableID = buf.readResourceLocation();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RequestLootTableItems: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(RequestLootTableItems msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.getLootTableID());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        List<LootDrop> lootItems = LootTableCache.getLoot(this.getLootTableID());
        if (lootItems == null) {
            lootItems = LootTableHelper.toDrops(player.serverLevel(), this.getLootTableID());
        }
        if (lootItems != null) {
            ServerMessageDispatcher.sendRespondLootTableItems(player, this.getLootTableID(), lootItems);
        }
    }
}

