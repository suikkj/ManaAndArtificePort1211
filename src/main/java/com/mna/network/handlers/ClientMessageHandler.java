package com.mna.network.handlers;

import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientMessageHandler {
    public static void HandleClientMessage(BaseClientMessage message, IPayloadContext ctx) {
        if (!message.isMessageValid()) {
            ManaAndArtifice.LOGGER.error(message.getClass().getName() + " was invalid: " + message);
            return;
        }
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                message.Handle(mc.level, mc.player);
            }
        });
    }
}
