 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.mna.api.tools.MNACap;

public class UIModifierPress
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UIModifierPress> TYPE = new CustomPacketPayload.Type<>(RLoc.create("u_i_modifier_press"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, UIModifierPress> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> UIModifierPress.encode(v, b), UIModifierPress::decode);

    @Override
    public CustomPacketPayload.Type<UIModifierPress> type() { return TYPE; }

    boolean pressed;

    public UIModifierPress(boolean pressed) {
        this.pressed = pressed;
        this.messageIsValid = true;
    }

    public UIModifierPress() {
        this.messageIsValid = false;
    }

    public boolean getPressed() {
        return this.pressed;
    }

    public static UIModifierPress decode(FriendlyByteBuf buf) {
        UIModifierPress msg = new UIModifierPress();
        try {
            msg.pressed = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading UIModifierPress: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(UIModifierPress msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.getPressed());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.setModifierPressed(this.getPressed()));
    }
}

