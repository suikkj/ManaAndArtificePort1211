 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.entities.constructs.animated.Construct;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AnimatedConstructDropItemMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AnimatedConstructDropItemMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("animated_construct_drop_item_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, AnimatedConstructDropItemMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> AnimatedConstructDropItemMessage.encode(v, b), AnimatedConstructDropItemMessage::decode);

    @Override
    public CustomPacketPayload.Type<AnimatedConstructDropItemMessage> type() { return TYPE; }

    private int entityID;
    private int slot;

    private AnimatedConstructDropItemMessage() {
        this.messageIsValid = false;
    }

    public AnimatedConstructDropItemMessage(int entityID, int slot) {
        this();
        this.entityID = entityID;
        this.slot = slot;
        this.messageIsValid = true;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public int getSlot() {
        return this.slot;
    }

    public static AnimatedConstructDropItemMessage decode(FriendlyByteBuf buf) {
        AnimatedConstructDropItemMessage msg = new AnimatedConstructDropItemMessage();
        try {
            msg.entityID = buf.readInt();
            msg.slot = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading AnimatedConstructDropItemMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(AnimatedConstructDropItemMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
        buf.writeInt(msg.slot);
    }

    public static AnimatedConstructDropItemMessage fromConstruct(Construct construct, int slot) {
        AnimatedConstructDropItemMessage msg = new AnimatedConstructDropItemMessage(construct.getId(), slot);
        return msg;
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        Construct eac = (Construct)player.level().getEntity(this.getEntityID());
        if (eac != null && eac.playerCanCommand((Player)player)) {
            ItemStack stack = ItemStack.EMPTY;
            switch (this.getSlot()) {
                case 0: {
                    stack = eac.getMainHandItem();
                    break;
                }
                case 1: {
                    stack = eac.getOffhandItem();
                    break;
                }
                case 2: {
                    stack = eac.getConstructData().getHat();
                    break;
                }
                case 3: {
                    stack = eac.getConstructData().getBanner();
                    break;
                }
                default: {
                    stack = eac.getStackInSlot(this.getSlot() - 4);
                }
            }
            if (!stack.isEmpty()) {
                switch (this.getSlot()) {
                    case 0: {
                        eac.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        break;
                    }
                    case 1: {
                        eac.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                        break;
                    }
                    case 2: {
                        eac.setHat(ItemStack.EMPTY);
                        return;
                    }
                    case 3: {
                        eac.setBanner(ItemStack.EMPTY);
                        return;
                    }
                    default: {
                        eac.setStackInSlot(this.getSlot() - 4, ItemStack.EMPTY);
                    }
                }
                ItemEntity ie = new ItemEntity(eac.level(), eac.getX(), eac.getY(), eac.getZ(), stack);
                ie.setDefaultPickUpDelay();
                eac.level().addFreshEntity((Entity)ie);
            }
        }
    }
}

