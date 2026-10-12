 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.effects.EffectHelper;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class EndControlEffectEarlyMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EndControlEffectEarlyMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("end_control_effect_early_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, EndControlEffectEarlyMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> EndControlEffectEarlyMessage.encode(v, b), EndControlEffectEarlyMessage::decode);

    @Override
    public CustomPacketPayload.Type<EndControlEffectEarlyMessage> type() { return TYPE; }

    public EndControlEffectEarlyMessage() {
        this.messageIsValid = true;
    }

    public static final EndControlEffectEarlyMessage decode(FriendlyByteBuf buf) {
        return new EndControlEffectEarlyMessage();
    }

    public static void encode(EndControlEffectEarlyMessage message, FriendlyByteBuf buf) {
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        EffectHelper.removeDoubleTapEvents((Player)player);
    }
}

