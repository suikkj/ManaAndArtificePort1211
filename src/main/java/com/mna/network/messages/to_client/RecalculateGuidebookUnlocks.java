 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.guide.GuideBookEntries;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class RecalculateGuidebookUnlocks
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RecalculateGuidebookUnlocks> TYPE = new CustomPacketPayload.Type<>(RLoc.create("recalculate_guidebook_unlocks"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RecalculateGuidebookUnlocks> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RecalculateGuidebookUnlocks.encode(v, b), RecalculateGuidebookUnlocks::decode);

    @Override
    public CustomPacketPayload.Type<RecalculateGuidebookUnlocks> type() { return TYPE; }

    private String advancementID;

    public RecalculateGuidebookUnlocks(String advancementID) {
        this.advancementID = advancementID;
        this.messageIsValid = true;
    }

    public RecalculateGuidebookUnlocks() {
        this.messageIsValid = false;
    }

    public String getAdvancementID() {
        return this.advancementID;
    }

    public static RecalculateGuidebookUnlocks decode(FriendlyByteBuf buf) {
        RecalculateGuidebookUnlocks msg = new RecalculateGuidebookUnlocks();
        try {
            msg.advancementID = buf.readUtf();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RecalculateGuidebookUnlocks: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(RecalculateGuidebookUnlocks msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.getAdvancementID());
    }

    @Override
    public void Handle(Level world, Player player) {
        GuideBookEntries.INSTANCE.updateUnlockedEntries(ResourceLocation.parse(this.getAdvancementID()));
    }
}

