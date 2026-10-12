 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.entities.rituals.Ritual;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SetRitualCollectedReagentsMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetRitualCollectedReagentsMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("set_ritual_collected_reagents_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SetRitualCollectedReagentsMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SetRitualCollectedReagentsMessage.encode(v, b), SetRitualCollectedReagentsMessage::decode);

    @Override
    public CustomPacketPayload.Type<SetRitualCollectedReagentsMessage> type() { return TYPE; }

    CompoundTag reagentData;
    int entityID;

    public SetRitualCollectedReagentsMessage(CompoundTag data, int entityID) {
        this.reagentData = data;
        this.entityID = entityID;
        this.messageIsValid = true;
    }

    public SetRitualCollectedReagentsMessage() {
        this.messageIsValid = false;
    }

    public static SetRitualCollectedReagentsMessage decode(FriendlyByteBuf buf) {
        SetRitualCollectedReagentsMessage msg = new SetRitualCollectedReagentsMessage();
        try {
            msg.entityID = buf.readInt();
            msg.reagentData = buf.readNbt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ManaweavePatternDrawnMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SetRitualCollectedReagentsMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityID);
        buf.writeNbt(message.reagentData);
    }

    @Override
    public void Handle(Level world, Player player) {
        Entity e = ManaAndArtifice.instance.proxy.getClientWorld().getEntity(this.entityID);
        if (e != null && e instanceof Ritual) {
            ((Ritual)e).readCollectedReagents(this.reagentData.getList("data", 10));
        }
    }
}

