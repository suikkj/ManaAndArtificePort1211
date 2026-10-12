 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.gui.containers.providers.NamedSpellCustomization;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public class OpenSpellRenameMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenSpellRenameMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("open_spell_rename_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, OpenSpellRenameMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> OpenSpellRenameMessage.encode(v, b), OpenSpellRenameMessage::decode);

    @Override
    public CustomPacketPayload.Type<OpenSpellRenameMessage> type() { return TYPE; }

    public OpenSpellRenameMessage() {
        this.messageIsValid = true;
    }

    public static OpenSpellRenameMessage decode(FriendlyByteBuf buf) {
        return new OpenSpellRenameMessage();
    }

    public static void encode(OpenSpellRenameMessage msg, FriendlyByteBuf buf) {
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ((ServerPlayer)player).openMenu((MenuProvider)new NamedSpellCustomization());
    }
}


