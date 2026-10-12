 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.particles.MAParticleType;
import com.mna.network.messages.BaseClientMessage;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class SpawnParticleMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpawnParticleMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("spawn_particle_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SpawnParticleMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SpawnParticleMessage.encode(v, b), SpawnParticleMessage::decode);

    @Override
    public CustomPacketPayload.Type<SpawnParticleMessage> type() { return TYPE; }

    Vector3 position;
    Vector3 speed;
    int color;
    ResourceLocation type;

    public SpawnParticleMessage(double x, double y, double z, double vX, double vY, double vZ, int color, ResourceLocation type) {
        this.position = new Vector3(x, y, z);
        this.speed = new Vector3(vX, vY, vZ);
        this.type = type;
        this.color = color;
        this.messageIsValid = true;
    }

    public SpawnParticleMessage() {
        this.messageIsValid = false;
    }

    public static SpawnParticleMessage decode(FriendlyByteBuf buf) {
        SpawnParticleMessage msg = new SpawnParticleMessage();
        try {
            msg.type = buf.readResourceLocation();
            msg.position = new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            msg.speed = new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            msg.color = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading SpawnParticleMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SpawnParticleMessage message, FriendlyByteBuf buf) {
        buf.writeResourceLocation(message.type);
        buf.writeDouble((double)message.position.x);
        buf.writeDouble((double)message.position.y);
        buf.writeDouble((double)message.position.z);
        buf.writeDouble((double)message.speed.x);
        buf.writeDouble((double)message.speed.y);
        buf.writeDouble((double)message.speed.z);
        buf.writeInt(message.color);
    }

    @Override
    public void Handle(Level world, Player player) {
        ParticleType resolvedType = (ParticleType)BuiltInRegistries.PARTICLE_TYPE.get(this.type);
        if (resolvedType == null || !(resolvedType instanceof MAParticleType)) {
            return;
        }
        MAParticleType particle = new MAParticleType((MAParticleType)resolvedType);
        if (this.color != 0) {
            int clr = this.color;
            float pct = MathUtils.clamp((float)FastColor.ARGB32.alpha((int)clr) / 255.0f, 0.1f, 1.0f);
            particle.setColor((float)FastColor.ARGB32.red((int)clr) * pct, (float)FastColor.ARGB32.green((int)clr) * pct, (float)FastColor.ARGB32.blue((int)clr) * pct);
        }
        world.addParticle((ParticleOptions)particle, (double)this.position.x, (double)this.position.y, (double)this.position.z, (double)this.speed.x, (double)this.speed.y, (double)this.speed.z);
    }
}


