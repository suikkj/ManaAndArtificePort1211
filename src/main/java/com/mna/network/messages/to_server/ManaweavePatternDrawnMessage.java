 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.blocks.tileentities.ManaweavingAltarTile;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.manaweaving.Manaweave;
import com.mna.events.EventDispatcher;
import com.mna.items.manaweaving.ItemManaweaverWand;
import com.mna.network.messages.BaseServerMessage;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class ManaweavePatternDrawnMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ManaweavePatternDrawnMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("manaweave_pattern_drawn_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ManaweavePatternDrawnMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> ManaweavePatternDrawnMessage.encode(v, b), ManaweavePatternDrawnMessage::decode);

    @Override
    public CustomPacketPayload.Type<ManaweavePatternDrawnMessage> type() { return TYPE; }

    private ResourceLocation patternID;
    private Vec3 position;
    private InteractionHand hand;
    private int ticksDrawn;

    public ManaweavePatternDrawnMessage(ResourceLocation patternID, Vec3 position, InteractionHand hand, int ticksDrawn) {
        this.patternID = patternID;
        this.position = position;
        this.hand = hand;
        this.ticksDrawn = ticksDrawn;
        this.messageIsValid = true;
    }

    public ManaweavePatternDrawnMessage() {
        this.messageIsValid = false;
    }

    public ResourceLocation getPatternID() {
        return this.patternID;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public int getTicksDrawn() {
        return this.ticksDrawn;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public static ManaweavePatternDrawnMessage decode(FriendlyByteBuf buf) {
        ManaweavePatternDrawnMessage msg = new ManaweavePatternDrawnMessage();
        try {
            msg.patternID = buf.readResourceLocation();
            msg.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            msg.hand = InteractionHand.values()[buf.readInt()];
            msg.ticksDrawn = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ManaweavePatternDrawnMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(ManaweavePatternDrawnMessage msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.getPatternID());
        buf.writeDouble(msg.getPosition().x);
        buf.writeDouble(msg.getPosition().y);
        buf.writeDouble(msg.getPosition().z);
        buf.writeInt(msg.getHand().ordinal());
        buf.writeInt(msg.getTicksDrawn());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        BlockEntity worldTile;
        BlockPos messagePos = BlockPos.containing((Position)this.getPosition());
        if (!player.level().isLoaded(messagePos)) {
            return;
        }
        Recipe pattern = player.level().getRecipeManager().byKey(this.getPatternID()).orElse(null);
        if (!(pattern instanceof IManaweavePattern)) {
            return;
        }
        if (!EventDispatcher.DispatchManaweavePatternDrawn((IManaweavePattern)pattern, (LivingEntity)player)) {
            return;
        }
        BlockPos storedPos = ItemManaweaverWand.getStoredBlockPos(player.getItemInHand(this.getHand()));
        ManaweavingAltarTile tile = null;
        if (storedPos != null && storedPos.distSqr((Vec3i)messagePos) <= 64.0 && (worldTile = player.level().getBlockEntity(storedPos)) != null && worldTile instanceof ManaweavingAltarTile) {
            tile = (ManaweavingAltarTile)worldTile;
        }
        if (tile == null) {
            Entity weave = ((EntityType)EntityInit.MANAWEAVE_ENTITY.get()).create(player.level());
            if (weave != null) {
                weave.moveTo(this.getPosition().x, this.getPosition().y, this.getPosition().z, 0.0f, 0.0f);
                if (weave instanceof Manaweave) {
                    Manaweave manaweave = (Manaweave)weave;
                    manaweave.setPattern(this.getPatternID());
                    manaweave.setCaster((LivingEntity)player, this.getHand());
                    manaweave.setManuallyDrawn();
                    manaweave.setManaRefunded(this.getTicksDrawn());
                }
                player.level().addFreshEntity(weave);
            }
        } else {
            tile.pushPattern((ManaweavingPattern)pattern, (Player)player);
        }
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.addMagicXP(3, (Player)player, (IPlayerProgression)p)));
        player.getMainHandItem().hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)player.level(), player, i -> {});
    }
}

