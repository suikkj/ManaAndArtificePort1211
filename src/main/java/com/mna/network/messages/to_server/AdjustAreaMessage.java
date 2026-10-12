 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.items.IAreaItem;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class AdjustAreaMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdjustAreaMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("adjust_area_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, AdjustAreaMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> AdjustAreaMessage.encode(v, b), AdjustAreaMessage::decode);

    @Override
    public CustomPacketPayload.Type<AdjustAreaMessage> type() { return TYPE; }

    private Vector3f direction;
    private boolean grow;

    public AdjustAreaMessage(Vector3f direction, boolean grow) {
        this.direction = direction;
        this.grow = grow;
        this.messageIsValid = true;
    }

    public AdjustAreaMessage() {
        this.messageIsValid = false;
    }

    public static AdjustAreaMessage decode(FriendlyByteBuf buf) {
        AdjustAreaMessage msg = new AdjustAreaMessage();
        try {
            msg.direction = buf.readVector3f();
            msg.grow = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading AdjustAreaMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(AdjustAreaMessage msg, FriendlyByteBuf buf) {
        buf.writeVector3f(msg.direction);
        buf.writeBoolean(msg.grow);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        Item item = held.getItem();
        if (item instanceof IAreaItem) {
            IAreaItem areaItem = (IAreaItem)item;
            areaItem.adjust(held, this.direction, this.grow);
        }
    }
}

