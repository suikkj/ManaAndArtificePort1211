 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.effects.harmful.EffectIcarianFlight;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SetIcarianDataMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetIcarianDataMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("set_icarian_data_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SetIcarianDataMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SetIcarianDataMessage.encode(v, b), SetIcarianDataMessage::decode);

    @Override
    public CustomPacketPayload.Type<SetIcarianDataMessage> type() { return TYPE; }

    private int entityID;
    private Vec3 vel;

    public SetIcarianDataMessage(int entityID, Vec3 vel) {
        this.entityID = entityID;
        this.vel = vel;
        this.messageIsValid = true;
    }

    public SetIcarianDataMessage() {
        this.messageIsValid = false;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public static SetIcarianDataMessage decode(FriendlyByteBuf buf) {
        SetIcarianDataMessage msg = new SetIcarianDataMessage();
        try {
            msg.entityID = buf.readInt();
            msg.vel = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading CloudstepJumpMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SetIcarianDataMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
        buf.writeDouble(msg.vel.x);
        buf.writeDouble(msg.vel.y);
        buf.writeDouble(msg.vel.z);
    }

    @Override
    public void Handle(Level world, Player player) {
        Entity entity = world.getEntity(this.entityID);
        if (entity != null && entity instanceof LivingEntity) {
            if (this.vel.x == 0.0 && this.vel.y == 0.0 && this.vel.z == 0.0) {
                EffectIcarianFlight.clearIcarianData((LivingEntity)entity);
            } else {
                EffectIcarianFlight.writeIcarianData((LivingEntity)entity, this.vel);
            }
        }
    }
}

