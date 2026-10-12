 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CloudstepJumpMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CloudstepJumpMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("cloudstep_jump_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, CloudstepJumpMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> CloudstepJumpMessage.encode(v, b), CloudstepJumpMessage::decode);

    @Override
    public CustomPacketPayload.Type<CloudstepJumpMessage> type() { return TYPE; }

    private int entityID;

    public CloudstepJumpMessage(int entityID) {
        this.entityID = entityID;
        this.messageIsValid = true;
    }

    public CloudstepJumpMessage() {
        this.messageIsValid = false;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public static CloudstepJumpMessage decode(FriendlyByteBuf buf) {
        CloudstepJumpMessage msg = new CloudstepJumpMessage();
        try {
            msg.entityID = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading CloudstepJumpMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(CloudstepJumpMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
    }

    @Override
    public void Handle(Level world, Player player) {
        Entity e = world.getEntity(this.getEntityID());
        if (e != null && e instanceof LivingEntity) {
            for (int i = 0; i < 20; ++i) {
                Vec3 pos = e.position();
                e.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_ORBIT.get())).setScale(0.2f).setColor(10, 10, 10), pos.x, pos.y, pos.z, 0.25, (double)0.05f, 0.5);
            }
        }
    }
}

