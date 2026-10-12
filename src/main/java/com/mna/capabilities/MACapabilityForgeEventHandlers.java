 
package com.mna.capabilities;

import com.mna.Registries;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.IFaction;
import com.mna.api.tools.RLoc;
import com.mna.blocks.artifice.WardingCandleBlock;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.capabilities.entity.MAPFXProvider;
import com.mna.capabilities.particles.ParticleAuraProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.faction.util.FactionRaid;
import com.mna.items.artifice.charms.ContingencyCharm;
import com.mna.network.ServerMessageDispatcher;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.mna.api.tools.MNACap;
import com.mna.api.events.RunicAnvilShouldActivateEvent.Result;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class MACapabilityForgeEventHandlers {
    public static final ResourceLocation PLAYERMAGIC_CAP = RLoc.create("magic");
    public static final ResourceLocation WORLDMAGIC_CAP = RLoc.create("worldmagic");
    public static final ResourceLocation CHUNKMAGIC_CAP = RLoc.create("chunkmagic");
    public static final ResourceLocation PROGRESSION_CAP = RLoc.create("progression");
    public static final ResourceLocation ROTE_SPELLS_CAP = RLoc.create("rote_spells");
    public static final ResourceLocation AURAS_CAP = RLoc.create("auras");
    public static final ResourceLocation PFX_CAP = RLoc.create("pfx_capability");

    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(PFX_CAP, (ICapabilityProvider)new MAPFXProvider());
        }
        if (!(event.getObject() instanceof Player)) {
            return;
        }
        event.addCapability(PLAYERMAGIC_CAP, (ICapabilityProvider)new PlayerMagicProvider());
        event.addCapability(PROGRESSION_CAP, (ICapabilityProvider)new PlayerProgressionProvider());
        event.addCapability(ROTE_SPELLS_CAP, (ICapabilityProvider)new PlayerRoteSpellsProvider());
        event.addCapability(AURAS_CAP, (ICapabilityProvider)new ParticleAuraProvider());
    }

    @SubscribeEvent
    public static void attachWorldCapability(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(WORLDMAGIC_CAP, (ICapabilityProvider)new WorldMagicProvider(((Level)event.getObject()).dimension() == Level.OVERWORLD));
    }

    @SubscribeEvent
    public static void attachChunkCapability(AttachCapabilitiesEvent<LevelChunk> event) {
        if (!(event.getObject() instanceof LevelChunk)) {
            return;
        }
        event.addCapability(CHUNKMAGIC_CAP, (ICapabilityProvider)new ChunkMagicProvider());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player original = event.getOriginal();
        original.reviveCaps();
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(mana -> {
            MNACap.get(original, PlayerMagicProvider.MAGIC).ifPresent(oldMana -> mana.copyFrom((IPlayerMagic)oldMana));
            mana.getCastingResource().setAmount(mana.getCastingResource().getMaxAmount());
        });
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(progression -> MNACap.get(original, PlayerProgressionProvider.PROGRESSION).ifPresent(oldProgression -> progression.copyFrom((IPlayerProgression)oldProgression, player)));
        MNACap.get(player, PlayerRoteSpellsProvider.ROTE).ifPresent(rote -> MNACap.get(original, PlayerRoteSpellsProvider.ROTE).ifPresent(oldRote -> rote.copyFrom((IPlayerRoteSpells)oldRote)));
        MNACap.get(player, ParticleAuraProvider.AURA).ifPresent(aura -> MNACap.get(original, ParticleAuraProvider.AURA).ifPresent(oldAura -> aura.load(oldAura.save())));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        MNACap.get(event.getEntity(), PlayerMagicProvider.MAGIC).ifPresent(magic -> magic.forceSync());
        MNACap.get(event.getEntity(), PlayerProgressionProvider.PROGRESSION).ifPresent(progression -> progression.setDirty());
        MNACap.get(event.getEntity(), PlayerRoteSpellsProvider.ROTE).ifPresent(rote -> rote.setDirty());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        MACapabilityForgeEventHandlers.tickMagic(event);
        MACapabilityForgeEventHandlers.tickRaids(event);
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent event) {
        MNACap.get((LivingEntity)event.getEntity(), MAPFXProvider.MAPFX).ifPresent(pfx -> pfx.sync((LivingEntity)event.getEntity()));
    }

    private static void tickMagic(TickEvent.PlayerTickEvent event) {
        IPlayerMagic magic = (IPlayerMagic)event.getEntity().getData(PlayerMagicProvider.MAGIC.get());
        if (magic != null) {
            magic.tick(event.getEntity());
        }
        if (event.side == LogicalSide.SERVER) {
            if (magic != null && magic.needsSync()) {
                ServerMessageDispatcher.sendMagicSyncMessage((ServerPlayer)event.getEntity());
                magic.clearSyncFlags();
            }
            MNACap.get(event.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                if (p.needsSync()) {
                    ServerMessageDispatcher.sendProgressionSyncMessage((ServerPlayer)event.getEntity());
                    p.clearSyncFlag();
                }
            });
            MNACap.get(event.player, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
                if (r.isDirty()) {
                    ServerMessageDispatcher.sendRoteSyncMessage((ServerPlayer)event.getEntity());
                    r.clearDirty();
                }
            });
        }
    }

    private static void tickRaids(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            if (event.getEntity().level().getServer().getWorldData().getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            if (event.getEntity().level().getGameTime() % 100L != 0L) {
                return;
            }
            MNACap.get(event.player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                p.tickClassicRaids(event.getEntity());
                if (p.canBeRaided(event.getEntity())) {
                    int hRadius = 20;
                    int vRadius = 3;
                    BlockPos baseline = event.getEntity().blockPosition();
                    boolean force = p.hasForceRaid();
                    int loops = force ? 100 : GeneralConfigValues.RaidSpawnAttempts;
                    for (int i = 0; i < loops; ++i) {
                        int anger;
                        int light;
                        List<IFaction> potentialRaiders;
                        BlockPos attemptedLocation = baseline.offset((int)((double)(-hRadius) + Math.random() * (double)hRadius * 2.0), (int)((double)(-vRadius) + Math.random() * (double)vRadius * 2.0), (int)((double)(-hRadius) + Math.random() * (double)hRadius * 2.0));
                        while (attemptedLocation.getY() > 5 && !event.getEntity().level().getBlockState(attemptedLocation).canOcclude()) {
                            attemptedLocation = attemptedLocation.below();
                        }
                        while (attemptedLocation.getY() < event.getEntity().level().getMaxBuildHeight() && !event.getEntity().level().isEmptyBlock(attemptedLocation)) {
                            attemptedLocation = attemptedLocation.above();
                        }
                        BlockPos attemptedLocUp = attemptedLocation.above();
                        if (!force && (event.getEntity().distanceToSqr(new Vec3((double)attemptedLocation.getX(), (double)attemptedLocation.getY(), (double)attemptedLocation.getZ())) < 25.0 || (MNACap.get((IWorldMagic)event.getEntity().level(), WorldMagicProvider.MAGIC).get()).isWithinWardingCandle(attemptedLocation)) || !event.getEntity().level().isEmptyBlock(attemptedLocation) || !event.getEntity().level().isEmptyBlock(attemptedLocUp) || event.getEntity().level().isEmptyBlock(attemptedLocation.below())) continue;
                        ClipContext context = new ClipContext(event.getEntity().getEyePosition(0.0f), new Vec3((double)attemptedLocUp.getX() + 0.5, (double)attemptedLocUp.getY(), (double)attemptedLocUp.getZ() + 0.5), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, (net.minecraft.world.entity.Entity)null);
                        BlockHitResult result = event.getEntity().level().clip(context);
                        if (result.getType() != HitResult.Type.MISS) continue;
                        BlockState spawnBlock = event.getEntity().level().getBlockState(attemptedLocation.below());
                        if (!force && !spawnBlock.isValidSpawn((BlockGetter)event.getEntity().level(), attemptedLocation.below(), (EntityType)EntityInit.FACTION_RAID_ENTITY.get())) {
                            return;
                        }
                        List<IFaction> list = potentialRaiders = force ? Arrays.asList(p.getForceRaid()) : Registries.Factions.getValues().stream().filter(f -> p.canBeRaided((IFaction)f, event.getEntity())).collect(Collectors.toList());
                        if (potentialRaiders.size() == 0) {
                            return;
                        }
                        IFaction raidingFaction = potentialRaiders.get(event.getEntity().level().getRandom().nextInt(potentialRaiders.size()));
                        if (raidingFaction == null) {
                            return;
                        }
                        if (!force && (light = event.getEntity().level().isThundering() ? event.getEntity().level().getMaxLocalRawBrightness(attemptedLocation, 10) : event.getEntity().level().getMaxLocalRawBrightness(attemptedLocation)) > 7 + (anger = (int)Math.floor(p.getRaidChance(raidingFaction)))) {
                            return;
                        }
                        if (event.player instanceof ServerPlayer) {
                            ContingencyCharm.CheckAndConsumeCharmCharge((ServerPlayer)event.player, ContingencyCharm.ContingencyEvent.FACTION_RAID);
                        }
                        FactionRaid fre = new FactionRaid(event.getEntity().level(), event.player, p.getRelativeRaidStrength(raidingFaction, event.getEntity()));
                        fre.setFaction(raidingFaction);
                        fre.setPos((double)attemptedLocUp.getX() + 0.5, attemptedLocUp.getY(), (double)attemptedLocUp.getZ() + 0.5);
                        event.getEntity().level().addFreshEntity((Entity)fre);
                        p.setRaidChance(raidingFaction, 0.0);
                        if (force) {
                            p.clearForceRaid();
                        }
                        return;
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onCheckSpawnEvent(MobSpawnEvent.SpawnPlacementCheck event) {
        List<MobSpawnType> nonBlockedSpawnTypes = Arrays.asList(MobSpawnType.BREEDING, MobSpawnType.BUCKET, MobSpawnType.CHUNK_GENERATION, MobSpawnType.COMMAND, MobSpawnType.CONVERSION, MobSpawnType.DISPENSER, MobSpawnType.MOB_SUMMONED, MobSpawnType.SPAWN_EGG, MobSpawnType.TRIGGERED, MobSpawnType.SPAWNER);
        if (nonBlockedSpawnTypes.contains(event.getSpawnType())) {
            return;
        }
        if (event.getLevel() instanceof Level) {
            MNACap.get((Level)event.getLevel(), WorldMagicProvider.MAGIC).ifPresent(w -> {
                if (WardingCandleBlock.shouldEntityBeBlocked(event.getEntityType()) && w.isWithinWardingCandle(event.getPos())) {
                    event.setResult(Result.DENY);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        if (event == null || event.getEntity() == null || event.getEntity().level() == null) {
            return;
        }
        if (com.mna.api.tools.MATools.canChangeDimensions(event.getEntity())) {
            MNACap.get(event.getEntity().level(), WorldMagicProvider.MAGIC).ifPresent(w -> {
                if (w.isWithinWardingCandle(event.getEntity().blockPosition()) && !GeneralConfigValues.WardingCandleWhitelist.contains(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString())) {
                    event.setResult(Result.DENY);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel) {
            ServerLevel sw = (ServerLevel)event.getLevel();
            MNACap.get(sw, WorldMagicProvider.MAGIC).ifPresent(m -> {
                BlockPos chunkCenter = new BlockPos((event.getChunk().getPos().getMinBlockX() + event.getChunk().getPos().getMaxBlockX()) / 2, 0, (event.getChunk().getPos().getMinBlockZ() + event.getChunk().getPos().getMaxBlockZ()) / 2);
                m.getWellspringRegistry().addRandomNode((Level)sw, chunkCenter);
            });
        }
    }
}


