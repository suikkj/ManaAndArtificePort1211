 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.artifice.EnderDisk;
import com.mna.network.messages.BaseServerMessage;
import java.util.ArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class EnderDiscPatternSetMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EnderDiscPatternSetMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("ender_disc_pattern_set_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, EnderDiscPatternSetMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> EnderDiscPatternSetMessage.encode(v, b), EnderDiscPatternSetMessage::decode);

    @Override
    public CustomPacketPayload.Type<EnderDiscPatternSetMessage> type() { return TYPE; }

    private ArrayList<ResourceLocation> patterns;
    private ResourceLocation dimensionID;
    private int patternIndex;
    private String name;

    public EnderDiscPatternSetMessage(ArrayList<ResourceLocation> patterns, ResourceLocation dimensionID, int patternIndex, String name) {
        this.patterns = patterns;
        this.patternIndex = patternIndex;
        this.dimensionID = dimensionID;
        this.name = name;
        this.messageIsValid = true;
    }

    public EnderDiscPatternSetMessage() {
        this.messageIsValid = false;
    }

    public ArrayList<ResourceLocation> getPatterns() {
        return this.patterns;
    }

    public int getIndex() {
        return this.patternIndex;
    }

    public String getName() {
        return this.name;
    }

    public ResourceLocation getDimensionID() {
        return this.dimensionID;
    }

    public static EnderDiscPatternSetMessage decode(FriendlyByteBuf buf) {
        EnderDiscPatternSetMessage msg = new EnderDiscPatternSetMessage();
        try {
            msg.patternIndex = buf.readInt();
            msg.name = buf.readUtf(Short.MAX_VALUE);
            msg.dimensionID = buf.readResourceLocation();
            int count = buf.readInt();
            msg.patterns = new ArrayList();
            for (int i = 0; i < count; ++i) {
                msg.patterns.add(buf.readResourceLocation());
            }
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading EnderDiscPatternSetMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(EnderDiscPatternSetMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getIndex());
        buf.writeUtf(msg.getName());
        buf.writeResourceLocation(msg.getDimensionID());
        buf.writeInt(msg.getPatterns().size());
        for (ResourceLocation rLoc : msg.getPatterns()) {
            buf.writeResourceLocation(rLoc);
        }
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        EnderDisk.setPattern(player.getInventory().getSelected(), this.getPatterns(), this.getDimensionID(), this.getIndex(), this.getName());
    }
}

