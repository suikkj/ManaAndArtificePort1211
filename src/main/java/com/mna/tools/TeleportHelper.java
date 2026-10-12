 
package com.mna.tools;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import com.mna.api.tools.MNACap;

public class TeleportHelper {
    private static void teleport(Entity entityIn, ServerLevel worldIn, double x, double y, double z, float yaw, float pitch) {
        if (entityIn instanceof ServerPlayer) {
            ServerPlayer spe = (ServerPlayer)entityIn;
            MNACap.get(spe, PlayerMagicProvider.MAGIC).ifPresent(m -> m.delayedTeleportTo(40, new Vec3(x, y, z), new Vec2(yaw, pitch), worldIn));
        } else {
            float wrappedYaw = Mth.wrapDegrees((float)yaw);
            float wrappedPitch = Mth.clamp((float)Mth.wrapDegrees((float)pitch), (float)-90.0f, (float)90.0f);
            if (worldIn == entityIn.level()) {
                entityIn.moveTo(x, y, z, wrappedYaw, wrappedPitch);
                entityIn.setYHeadRot(wrappedYaw);
            } else {
                entityIn.unRide();
                Entity entity = entityIn.getType().create((Level)worldIn);
                if (entity == null) {
                    return;
                }
                entity.restoreFrom(entityIn);
                entity.moveTo(x, y, z, wrappedYaw, wrappedPitch);
                entity.setYHeadRot(wrappedYaw);
                worldIn.addDuringTeleport(entity);
                entityIn.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            }
        }
        if (!(entityIn instanceof LivingEntity) || !((LivingEntity)entityIn).isFallFlying()) {
            entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            entityIn.setOnGround(true);
        }
        if (entityIn instanceof PathfinderMob) {
            ((PathfinderMob)entityIn).getNavigation().stop();
        }
    }

    public static void performDelayedTeleport(ServerPlayer spe, Vec3 location, Vec2 rotation, ServerLevel destination, IPlayerMagic magic) {
        EnumSet<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
        set.add(RelativeMovement.X_ROT);
        set.add(RelativeMovement.Y_ROT);
        ChunkPos chunkPos = new ChunkPos(BlockPos.containing((Position)location));
        destination.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, spe.getId());
        spe.stopRiding();
        if (spe.isSleeping()) {
            spe.stopSleepInBed(true, true);
        }
        if (destination == spe.level()) {
            spe.connection.teleport(location.x, location.y, location.z, rotation.x, rotation.y, set);
        } else {
            spe.teleportTo(destination, location.x, location.y, location.z, rotation.x, rotation.y);
        }
        spe.setYHeadRot(rotation.y);
    }

    public static void teleportEntity(Entity entity, ResourceKey<Level> destType, Vec3 destPos) {
        if (entity.level().isClientSide()) {
            return;
        }
        ServerLevel world = entity.getServer().getLevel(destType);
        if (world != null) {
            TeleportHelper.teleport(entity, world, destPos.x(), destPos.y(), destPos.z(), entity.getYRot(), entity.getXRot());
        } else if (entity instanceof Player) {
            ((Player)entity).sendSystemMessage((Component)Component.literal((String)"Error looking up world key. Teleport failed."));
        }
    }

    public static boolean randomTeleport(LivingEntity entity, float maxDist, int tries) {
        do {
            boolean teleported;
            double rX = entity.getRandomX((double)maxDist);
            double rY = entity.getY((double)maxDist);
            double rZ = entity.getRandomZ((double)maxDist);
            BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos(rX, rY, rZ);
            while (targetPos.getY() > 0 && !entity.level().getBlockState((BlockPos)targetPos).blocksMotion()) {
                targetPos.move(Direction.DOWN);
            }
            BlockState blockstate = entity.level().getBlockState((BlockPos)targetPos);
            boolean blocksMotion = blockstate.blocksMotion();
            boolean isLava = blockstate.getFluidState().is(FluidTags.LAVA);
            if (!blocksMotion || isLava || !(teleported = entity.randomTeleport(rX, rY, rZ, true))) continue;
            if (!entity.isSilent()) {
                entity.level().playSound((Player)null, entity.xo, entity.yo, entity.zo, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.0f, 1.0f);
                entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
            return true;
        } while (--tries >= 0);
        return false;
    }

    @Nullable
    public static ServerLevel resolveRegistryKey(ServerLevel world, ResourceKey<Level> dimension) {
        return TeleportHelper.resolveRegistryKey(world, dimension.location());
    }

    @Nullable
    public static ServerLevel resolveRegistryKey(ServerLevel world, ResourceLocation location) {
        if (world.dimension().location().equals(location)) {
            return world;
        }
        MinecraftServer server = world.getServer();
        if (server == null) {
            return null;
        }
        MutableObject targetWorld = new MutableObject(null);
        server.getAllLevels().iterator().forEachRemaining(sw -> {
            if (sw == null) {
                return;
            }
            if (sw.dimension().location().equals(location)) {
                targetWorld.setValue(sw);
            }
        });
        return (ServerLevel)targetWorld.getValue();
    }

    public static Vec3 calculateBlinkPosition(double distance, LivingEntity tgt, Vec3 direction, Level world) {
        direction = direction.normalize();
        Vec3 motion = direction.scale(distance);
        double newX = tgt.getX() + motion.x;
        double newZ = tgt.getZ() + motion.z;
        double newY = tgt.getY() + motion.y;
        if (newY < 1.0) {
            newY = 1.0;
        }
        boolean coordsValid = false;
        while (!coordsValid && distance > 0.0) {
            motion = direction.scale(distance);
            newX = tgt.getX() + motion.x;
            newZ = tgt.getZ() + motion.z;
            newY = tgt.getY() + motion.y;
            if (distance < 0.0) {
                coordsValid = false;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.floor(newX), (int)newY, (int)Math.floor(newZ))) {
                newX = Math.floor(newX) + 0.5;
                newZ = Math.floor(newZ) + 0.5;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.floor(newX), (int)newY, (int)Math.ceil(newZ))) {
                newX = Math.floor(newX) + 0.5;
                newZ = Math.ceil(newZ) + 0.5;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.ceil(newX), (int)newY, (int)Math.floor(newZ))) {
                newX = Math.ceil(newX) + 0.5;
                newZ = Math.floor(newZ) + 0.5;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.ceil(newX), (int)newY, (int)Math.ceil(newZ))) {
                newX = Math.ceil(newX) + 0.5;
                newZ = Math.ceil(newZ) + 0.5;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.floor(newX), (int)newY - 1, (int)Math.floor(newZ))) {
                newX = Math.floor(newX) + 0.5;
                newZ = Math.floor(newZ) + 0.5;
                newY -= 1.0;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.floor(newX), (int)newY - 1, (int)Math.ceil(newZ))) {
                newX = Math.floor(newX) + 0.5;
                newZ = Math.ceil(newZ) + 0.5;
                newY -= 1.0;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.ceil(newX), (int)newY - 1, (int)Math.floor(newZ))) {
                newX = Math.ceil(newX) + 0.5;
                newZ = Math.floor(newZ) + 0.5;
                newY -= 1.0;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.ceil(newX), (int)newY - 1, (int)Math.ceil(newZ))) {
                newX = Math.ceil(newX) + 0.5;
                newZ = Math.ceil(newZ) + 0.5;
                newY -= 1.0;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.floor(newX), (int)newY + 1, (int)Math.floor(newZ))) {
                newX = Math.floor(newX) + 0.5;
                newZ = Math.floor(newZ) + 0.5;
                newY += 1.0;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.floor(newX), (int)newY + 1, (int)Math.ceil(newZ))) {
                newX = Math.floor(newX) + 0.5;
                newZ = Math.ceil(newZ) + 0.5;
                newY += 1.0;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.ceil(newX), (int)newY + 1, (int)Math.floor(newZ))) {
                newX = Math.ceil(newX) + 0.5;
                newZ = Math.floor(newZ) + 0.5;
                newY += 1.0;
                coordsValid = true;
                break;
            }
            if (TeleportHelper.coordsValidForBlink(world, (int)Math.ceil(newX), (int)newY + 1, (int)Math.ceil(newZ))) {
                newX = Math.ceil(newX) + 0.5;
                newZ = Math.ceil(newZ) + 0.5;
                newY += 1.0;
                coordsValid = true;
                break;
            }
            motion = direction.scale(distance -= 1.0);
            newX = tgt.getX() + motion.x;
            newZ = tgt.getZ() + motion.z;
            newY = tgt.getY() + motion.y;
        }
        if (coordsValid) {
            return new Vec3(newX, newY, newZ);
        }
        return null;
    }

    public static boolean coordsValidForBlink(Level world, int x, int y, int z) {
        if (y < world.getMinBuildHeight()) {
            return false;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!world.isLoaded(pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        BlockState up = world.getBlockState(pos.above());
        return !state.canOcclude() && !up.canOcclude();
    }
}

