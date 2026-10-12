 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.entities.constructs.animated.Construct;
import com.mna.network.messages.BaseClientMessage;
import com.mna.tools.ParticleConfigurations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpawnParticleEffectMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpawnParticleEffectMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("spawn_particle_effect_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SpawnParticleEffectMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SpawnParticleEffectMessage.encode(v, b), SpawnParticleEffectMessage::decode);

    @Override
    public CustomPacketPayload.Type<SpawnParticleEffectMessage> type() { return TYPE; }

    Vec3 position;
    ParticleTypes particleType;
    CompoundTag particleMeta;

    public SpawnParticleEffectMessage(double x, double y, double z, ParticleTypes type) {
        this(x, y, z, type, null);
    }

    public SpawnParticleEffectMessage(double x, double y, double z, ParticleTypes type, CompoundTag meta) {
        this.position = new Vec3(x, y, z);
        this.particleType = type;
        this.particleMeta = meta;
        this.messageIsValid = true;
    }

    public SpawnParticleEffectMessage() {
        this.messageIsValid = false;
    }

    public static SpawnParticleEffectMessage decode(FriendlyByteBuf buf) {
        SpawnParticleEffectMessage msg = new SpawnParticleEffectMessage();
        try {
            msg.particleType = ParticleTypes.valueOf(ParticleTypes.class, buf.readUtf(Short.MAX_VALUE));
            msg.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            msg.particleMeta = buf.readAnySizeNbt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading SpawnParticleEffectMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SpawnParticleEffectMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.particleType.name());
        buf.writeDouble(message.position.x);
        buf.writeDouble(message.position.y);
        buf.writeDouble(message.position.z);
        buf.writeNbt(message.particleMeta);
    }

    @Override
    public void Handle(Level world, Player player) {
        switch (this.particleType) {
            case MANAWEAVE_CRAFT_COMPLETE: {
                ParticleConfigurations.ArcaneParticleBurst(world, this.position);
                break;
            }
            case ENSORCELLATION_ITEM_PULL: {
                ParticleConfigurations.ItemPullParticle(world, new Vec3(this.position.x, this.position.y, this.position.z), this.particleMeta);
                break;
            }
            case CONSTRUCT_HAMMER_SMASH: {
                Construct.SpawnSmashParticles(world, this.position);
                break;
            }
            case CONSTRUCT_AXE_SMASH: {
                Construct.SpawnCritParticles(world, this.position, this.particleMeta);
            }
        }
    }

    public static enum ParticleTypes {
        MANAWEAVE_CRAFT_COMPLETE,
        ENSORCELLATION_ITEM_PULL,
        CONSTRUCT_HAMMER_SMASH,
        CONSTRUCT_AXE_SMASH;

    }
}

