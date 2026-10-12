 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.BaseServerMessage;
import com.mna.tools.math.MathUtils;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

public class RequestWardDataMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestWardDataMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("request_ward_data_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RequestWardDataMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RequestWardDataMessage.encode(v, b), RequestWardDataMessage::decode);

    @Override
    public CustomPacketPayload.Type<RequestWardDataMessage> type() { return TYPE; }

    public static final int MAX_SIZE = 7;
    AABB bounds;

    public RequestWardDataMessage(AABB bounds) {
        if (bounds.getXsize() > 7.0 || bounds.getYsize() > 7.0 || bounds.getZsize() > 7.0) {
            throw new InvalidParameterException("RequestWardDataMessage: Requested bounds may be no larger than 7 blocks on any axis");
        }
        this.bounds = bounds;
        this.messageIsValid = true;
    }

    public RequestWardDataMessage() {
        this.messageIsValid = false;
    }

    public static RequestWardDataMessage decode(FriendlyByteBuf buf) {
        RequestWardDataMessage msg = new RequestWardDataMessage();
        try {
            BlockPos min = buf.readBlockPos();
            BlockPos max = buf.readBlockPos();
            AABB bounds = MathUtils.createInclusiveBB(min, max);
            if (bounds.getXsize() > 7.0 || bounds.getYsize() > 7.0 || bounds.getZsize() > 7.0) {
                ManaAndArtifice.LOGGER.error("RequestWardDataMessage: Requested bounds may be no larger than 7 blocks on any axis");
                return msg;
            }
            msg.bounds = bounds;
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RequestWardDataMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(RequestWardDataMessage msg, FriendlyByteBuf buf) {
        BlockPos min = BlockPos.containing((double)msg.bounds.minX, (double)msg.bounds.minY, (double)msg.bounds.minZ);
        BlockPos max = BlockPos.containing((double)msg.bounds.maxX, (double)msg.bounds.maxY, (double)msg.bounds.maxZ);
        buf.writeBlockPos(min);
        buf.writeBlockPos(max);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        if (this.bounds == null) {
            return;
        }
        HashMap<ChunkPos, IChunkMagic> chunkMagics = new HashMap<ChunkPos, IChunkMagic>();
        ArrayList<Long> ward_positions = new ArrayList<Long>();
        for (int x = (int)this.bounds.minX; x <= (int)this.bounds.maxX; ++x) {
            for (int y = (int)this.bounds.minY; y <= (int)this.bounds.maxY; ++y) {
                for (int z = (int)this.bounds.minZ; z <= (int)this.bounds.maxZ; ++z) {
                    IChunkMagic chunkMagic;
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.isLoaded(pos) || (chunkMagic = chunkMagics.computeIfAbsent(new ChunkPos(pos), chunkPos -> {
                        LevelChunk chunkAccess = world.getChunkAt(pos);
                        return (IChunkMagic)chunkAccess.getData(ChunkMagicProvider.MAGIC.get());
                    })) == null || !chunkMagic.isWarded(pos.asLong())) continue;
                    ward_positions.add(pos.asLong());
                }
            }
        }
        long[] arr = ward_positions.stream().mapToLong(i -> i).toArray();
        ServerMessageDispatcher.sendRenderWard(player, arr);
    }
}

