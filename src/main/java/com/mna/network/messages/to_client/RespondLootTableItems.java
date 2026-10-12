 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import com.mna.tools.loot.LootDrop;
import com.mna.tools.loot.LootTableCache;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class RespondLootTableItems
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RespondLootTableItems> TYPE = new CustomPacketPayload.Type<>(RLoc.create("respond_loot_table_items"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RespondLootTableItems> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RespondLootTableItems.encode(v, b), RespondLootTableItems::decode);

    @Override
    public CustomPacketPayload.Type<RespondLootTableItems> type() { return TYPE; }

    ResourceLocation lootTableID;
    List<LootDrop> lootDrops;

    public RespondLootTableItems(ResourceLocation lootTableID, List<LootDrop> drops) {
        this.lootTableID = lootTableID;
        this.lootDrops = drops;
        this.messageIsValid = true;
    }

    public RespondLootTableItems() {
        this.messageIsValid = false;
    }

    public ResourceLocation getLootTableID() {
        return this.lootTableID;
    }

    public List<LootDrop> getLootDrops() {
        return this.lootDrops;
    }

    public static RespondLootTableItems decode(FriendlyByteBuf buf) {
        RespondLootTableItems msg = new RespondLootTableItems();
        try {
            msg.lootTableID = buf.readResourceLocation();
            int numDrops = buf.readInt();
            msg.lootDrops = new ArrayList<LootDrop>();
            for (int i = 0; i < numDrops; ++i) {
                msg.lootDrops.add(LootDrop.readFrom(buf));
            }
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RespondLootTableItems: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(RespondLootTableItems msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.getLootTableID());
        buf.writeInt(msg.getLootDrops().size());
        for (int i = 0; i < msg.getLootDrops().size(); ++i) {
            msg.getLootDrops().get(i).writeTo(buf);
        }
    }

    @Override
    public void Handle(Level world, Player player) {
        LootTableCache.cacheLoot(this.getLootTableID(), this.getLootDrops());
    }
}

