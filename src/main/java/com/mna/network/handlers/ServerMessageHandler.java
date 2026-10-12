package com.mna.network.handlers;

import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerMessageHandler {
    public static void HandleServerMessage(BaseServerMessage message, IPayloadContext ctx) {
        if (!message.isMessageValid()) {
            ManaAndArtifice.LOGGER.error(message.getClass().getName() + " was invalid: " + message);
            return;
        }
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp && sp.level() instanceof ServerLevel sl) {
                message.Handle(sl, sp);
            } else {
                ManaAndArtifice.LOGGER.error("Player was a ServerPlayer but their level was not a ServerLevel.  M&A packet unable to handle.  Skipping.");
            }
        });
    }
}
