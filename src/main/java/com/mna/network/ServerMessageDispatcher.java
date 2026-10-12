 
package com.mna.network;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.particles.MAParticleType;
import com.mna.blocks.tileentities.ParticleEmitterTile;
import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import com.mna.capabilities.entity.MAPFXProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.entities.IAnimPacketSync;
import com.mna.network.MAPacketHandler;
import com.mna.network.messages.to_client.AuraSyncMessageToClient;
import com.mna.network.messages.to_client.CantripCastingTimerMessage;
import com.mna.network.messages.to_client.ClientboundPlayerFocusDistanceMessage;
import com.mna.network.messages.to_client.CloudstepJumpMessage;
import com.mna.network.messages.to_client.EnderDiscGuiDimensionCycle;
import com.mna.network.messages.to_client.EntityStateMessage;
import com.mna.network.messages.to_client.ExtendedSlotContainerContentsMessage;
import com.mna.network.messages.to_client.ExtendedSlotContentsMessage;
import com.mna.network.messages.to_client.LodestarLogicS2CMessage;
import com.mna.network.messages.to_client.MAPFXMessage;
import com.mna.network.messages.to_client.MagicSyncMessageToClient;
import com.mna.network.messages.to_client.MindVisionMessage;
import com.mna.network.messages.to_client.PlayerGreaterInvisibilityDisabledMessage;
import com.mna.network.messages.to_client.PossessionMessage;
import com.mna.network.messages.to_client.ProgressionSyncMessageToClient;
import com.mna.network.messages.to_client.RecalculateGuidebookUnlocks;
import com.mna.network.messages.to_client.RenderWardMessage;
import com.mna.network.messages.to_client.RespondLootTableItems;
import com.mna.network.messages.to_client.RoteProgressSyncMessageToClient;
import com.mna.network.messages.to_client.SetIcarianDataMessage;
import com.mna.network.messages.to_client.SetLiftPositionMessage;
import com.mna.network.messages.to_client.SetLiftSpeedMessage;
import com.mna.network.messages.to_client.SetRitualCollectedReagentsMessage;
import com.mna.network.messages.to_client.ShowDidYouKnow;
import com.mna.network.messages.to_client.SpawnParticleEffectMessage;
import com.mna.network.messages.to_client.SpawnParticleMessage;
import com.mna.network.messages.to_client.StructureSyncMessage;
import com.mna.network.messages.to_client.WanderingWizardInventoryMessage;
import com.mna.network.messages.to_client.WellspringPowerNetworkSyncMessage;
import com.mna.network.messages.to_client.WellspringSyncMessage;
import com.mna.network.messages.to_server.InscriptionTableCraftingUpdateMessage;
import com.mna.recipes.multiblock.MultiblockDefinition;
import com.mna.tools.NBTUtilities;
import com.mna.tools.TeleportHelper;
import com.mna.tools.loot.LootDrop;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import com.mna.api.tools.MNACap;

public class ServerMessageDispatcher {
    public static void sendVelocityPacketToAllNearby(Entity entity) {
        for (net.minecraft.server.level.ServerPlayer p : entity.getServer().getPlayerList().getPlayers()) {
            p.connection.send(new ClientboundSetEntityMotionPacket(entity));
        }
    }

    public static void sendMagicSyncMessage(ServerPlayer player) {
        java.util.Optional<PlayerMagicProvider> magic = MNACap.get(player, PlayerMagicProvider.MAGIC);
        if (!magic.isPresent()) {
            return;
        }
        PacketDistributor.sendToPlayer(player, MagicSyncMessageToClient.fromCapability((IPlayerMagic)magic.get()));
    }

    public static void sendEnderDiscGuiDimensionCycle(ServerPlayer player, ResourceLocation dimensionID) {
        PacketDistributor.sendToPlayer(player, new EnderDiscGuiDimensionCycle(dimensionID));
    }

    public static void sendRespondLootTableItems(ServerPlayer player, ResourceLocation lootTableID, List<LootDrop> drops) {
        PacketDistributor.sendToPlayer(player, new RespondLootTableItems(lootTableID, drops));
    }

    public static void sendDidYouKnow(ServerPlayer player, String message) {
        PacketDistributor.sendToPlayer(player, new ShowDidYouKnow(message));
    }

    public static void sendAdvancementUnlocked(ServerPlayer player, net.minecraft.resources.ResourceLocation advancementId) {
        PacketDistributor.sendToPlayer(player, new RecalculateGuidebookUnlocks(advancementId.toString()));
    }

    public static void sendSetIcarianData(LivingEntity living, Vec3 vel) {
        PacketDistributor.sendToPlayersTrackingEntity(living, new SetIcarianDataMessage(living.getId(), vel));
        if (living instanceof ServerPlayer) {
            PacketDistributor.sendToPlayer((ServerPlayer)living, new SetIcarianDataMessage(living.getId(), vel));
        }
    }

    public static void sendStructureSyncMessage(MultiblockDefinition structure, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, StructureSyncMessage.fromMultiblock(structure, (ServerLevel)player.level()));
    }

    public static void sendEntityStateMessage(IAnimPacketSync<?> entity) {
        PacketDistributor.sendToPlayersTrackingEntity((Entity)entity, EntityStateMessage.fromEntity(entity));
    }

    public static void sendEntityStateMessage(IAnimPacketSync<?> entity, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, EntityStateMessage.fromEntity(entity));
    }

    public static void sendSetLiftSpeedMessage(ServerPlayer player, float speed) {
        PacketDistributor.sendToPlayer(player, new SetLiftSpeedMessage(player.getId(), speed));
    }

    public static void sendMAPFXMessage(LivingEntity entity, ServerPlayer player) {
        MNACap.get(entity, MAPFXProvider.MAPFX).ifPresent(p -> PacketDistributor.sendToPlayer(player, new MAPFXMessage(entity.getId(), p.getFlags())));
    }

    public static void sendMAPFXMessage(LivingEntity entity) {
        MNACap.get(entity, MAPFXProvider.MAPFX).ifPresent(p -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new MAPFXMessage(entity.getId(), p.getFlags())));
    }

    public static void sendPlayerFocusDistanceChange(ServerPlayer player, float delta, float maximum) {
        PacketDistributor.sendToPlayer(player, new ClientboundPlayerFocusDistanceMessage(delta, maximum));
    }

    public static void sendCloudstepJumpMessage(ServerPlayer sendingPlayer) {
        MNACap.get(sendingPlayer, MAPFXProvider.MAPFX).ifPresent(p -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(sendingPlayer, new CloudstepJumpMessage(sendingPlayer.getId())));
    }

    public static void sendProgressionSyncMessage(ServerPlayer player) {
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> PacketDistributor.sendToPlayer(player, ProgressionSyncMessageToClient.fromCapability(p)));
    }

    public static void sendRoteSyncMessage(ServerPlayer player) {
        MNACap.get(player, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> PacketDistributor.sendToPlayer(player, RoteProgressSyncMessageToClient.fromCapability(r)));
    }

    public static void sendInscriptionTableCraftingUpdate(InscriptionTableTile te) {
        ServerLevel world = (ServerLevel)te.getLevel();
        BlockPos pos = te.getBlockPos();
        PacketDistributor.sendToPlayersTrackingChunk(world, world.getChunk(pos).getPos(), InscriptionTableCraftingUpdateMessage.fromInscriptionTable(te));
    }

    public static void sendRitualReagentData(CompoundTag ritualReagentData, int entityID, ServerLevel world, BlockPos pos) {
        PacketDistributor.sendToPlayersTrackingChunk(world, world.getChunk(pos).getPos(), new SetRitualCollectedReagentsMessage(ritualReagentData, entityID));
    }

    public static void sendParticleSpawn(double x, double y, double z, double vX, double vY, double vZ, int color, float radius, ResourceKey<Level> dimension, MAParticleType type) {
        SpawnParticleMessage msg = new SpawnParticleMessage(x, y, z, vX, vY, vZ, color, BuiltInRegistries.PARTICLE_TYPE.getKey(type));
        PacketDistributor.sendToPlayersNear(ServerLifecycleHooks.getCurrentServer().getLevel(dimension), null, x, y, z, radius, msg);
    }

    public static void sendParticleEffect(ResourceKey<Level> dimension, float radius, double x, double y, double z, SpawnParticleEffectMessage.ParticleTypes type) {
        ServerMessageDispatcher.sendParticleEffect(dimension, radius, x, y, z, type, null);
    }

    public static void sendParticleEffect(ResourceKey<Level> dimension, float radius, double x, double y, double z, SpawnParticleEffectMessage.ParticleTypes type, CompoundTag meta) {
        SpawnParticleEffectMessage msg = new SpawnParticleEffectMessage(x, y, z, type, meta);
        PacketDistributor.sendToPlayersNear(ServerLifecycleHooks.getCurrentServer().getLevel(dimension), null, x, y, z, radius, msg);
    }

    public static void sendSetLiftPosition(double x, double y, double z, ServerPlayer player) {
        SetLiftPositionMessage msg = new SetLiftPositionMessage(x, y, z);
        PacketDistributor.sendToPlayer(player, msg);
    }

    public static void sendCantripTimerMessage(String id, int ticks, ServerPlayer player) {
        CantripCastingTimerMessage msg = new CantripCastingTimerMessage(id, ticks);
        PacketDistributor.sendToPlayer(player, msg);
    }

    public static void sendWanderingWizardContainerMessage(int containerID, MerchantOffers offers, int level, int xp, boolean hasXPBar, boolean canRestock, boolean isFinal, ServerPlayer player) {
        WanderingWizardInventoryMessage msg = new WanderingWizardInventoryMessage(containerID, offers, level, xp, hasXPBar, canRestock, isFinal);
        PacketDistributor.sendToPlayer(player, msg);
    }

    public static void sendWellspringPowerNetworkSyncMessage(ResourceKey<Level> targetDimension, ServerPlayer player, boolean fullSync) {
        ServerLevel targetWorld = TeleportHelper.resolveRegistryKey((ServerLevel)player.level(), targetDimension);
        ServerMessageDispatcher.sendWellspringPowerNetworkSyncMessage(targetWorld, player, fullSync);
    }

    public static void sendWellspringPowerNetworkSyncMessage(ServerLevel world, ServerPlayer player, boolean fullSync) {
        PacketDistributor.sendToPlayer(player, WellspringPowerNetworkSyncMessage.from(world, player, fullSync));
    }

    public static void sendWellspringSyncMessage(ServerLevel world, ServerPlayer player) {
        ServerMessageDispatcher.sendWellspringSyncMessage(world, player, 64);
    }

    public static void sendWellspringSyncMessage(ServerLevel world, ServerPlayer player, int radius) {
        CompoundTag nbt = new CompoundTag();
        MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(m -> {
            if (m.getWellspringRegistry().writeToNBT(nbt, player.blockPosition(), radius)) {
                PacketDistributor.sendToPlayer(player, new WellspringSyncMessage(nbt));
            }
        });
    }

    public static void sendPlayerMindVisionMessage(ServerPlayer player, Entity entity) {
        PacketDistributor.sendToPlayer(player, new MindVisionMessage(entity != null ? entity.getId() : -1));
    }

    public static void sendPlayerPosessionMessage(ServerPlayer player, Entity entity) {
        PacketDistributor.sendToPlayer(player, new PossessionMessage(entity.getId()));
    }

    public static void sendExtendedItemStack(ServerPlayer player, int screenID, int stateID, int slotID, ItemStack stack) {
        PacketDistributor.sendToPlayer(player, new ExtendedSlotContentsMessage(screenID, stateID, slotID, stack));
    }

    public static void sendExtendedInitialContainer(ServerPlayer player, int screenID, int stateID, NonNullList<ItemStack> items, ItemStack carried) {
        PacketDistributor.sendToPlayer(player, new ExtendedSlotContainerContentsMessage(screenID, stateID, items, carried));
    }

    public static void sendAuraSyncMessage(ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntity(player, AuraSyncMessageToClient.fromPlayer((Player)player));
    }

    public static void sendAuraSyncMessage(ServerPlayer sendTo, ServerPlayer dataFor) {
        PacketDistributor.sendToPlayer(sendTo, AuraSyncMessageToClient.fromPlayer((Player)dataFor));
    }

    public static void sendAuraSyncMessage(ParticleEmitterTile tile) {
        PacketDistributor.sendToPlayersTrackingChunk((net.minecraft.server.level.ServerLevel)tile.getLevel(), tile.getLevel().getChunk(tile.getBlockPos()).getPos(), AuraSyncMessageToClient.fromTile(tile));
    }

    public static void sendLodestarLogicMessage(ServerPlayer sendTo, BlockPos pos, CompoundTag logic) {
        byte[] bytes = NBTUtilities.toBytes(logic);
        int numMessages = (int)Math.ceil((float)bytes.length / 30000.0f);
        for (int i = 0; i < numMessages; ++i) {
            int startIndex = i * 30000;
            int bufferSize = Math.min(30000, bytes.length - startIndex);
            byte[] buf = new byte[bufferSize];
            System.arraycopy(bytes, startIndex, buf, 0, bufferSize);
            PacketDistributor.sendToPlayer(sendTo, new LodestarLogicS2CMessage(pos, i, numMessages, bytes.length, buf));
        }
    }

    public static void sendGreaterInvisibilityDisabled(ServerPlayer disabledFor, int duration) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(disabledFor, new PlayerGreaterInvisibilityDisabledMessage(disabledFor.getId(), duration));
    }

    public static void sendRenderWard(ServerLevel level, long ... positions) {
        if (positions.length == 0) {
            return;
        }
        PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunk(BlockPos.of((long)positions[0])).getPos(), new RenderWardMessage(positions));
    }

    public static void sendRenderWard(ServerPlayer sendTo, long ... positions) {
        if (positions.length == 0) {
            return;
        }
        PacketDistributor.sendToPlayer(sendTo, new RenderWardMessage(positions));
    }
}


