 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import com.mna.api.tools.MNACap;

public class PlayerJumpMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerJumpMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("player_jump_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PlayerJumpMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> PlayerJumpMessage.encode(v, b), PlayerJumpMessage::decode);

    @Override
    public CustomPacketPayload.Type<PlayerJumpMessage> type() { return TYPE; }

    public PlayerJumpMessage() {
        this.messageIsValid = true;
    }

    public static final PlayerJumpMessage decode(FriendlyByteBuf buf) {
        PlayerJumpMessage msg = new PlayerJumpMessage();
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(PlayerJumpMessage message, FriendlyByteBuf buf) {
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        if (player.isInWater()) {
            return;
        }
        int enchLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.CLOUDSTEP, player.getItemBySlot(EquipmentSlot.FEET));
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (m.getAirJumps() < enchLevel) {
                m.incrementAirJumps((Player)player);
                player.jumpFromGround();
                player.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)player));
                ServerMessageDispatcher.sendCloudstepJumpMessage(player);
            }
        });
    }
}

