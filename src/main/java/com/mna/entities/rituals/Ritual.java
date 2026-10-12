 
package com.mna.entities.rituals;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.api.recipes.IRitualRecipe;
import com.mna.api.rituals.IRitualReagent;
import com.mna.api.rituals.RitualBlockPos;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.sound.SFX;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.blocks.tileentities.ChalkRuneTile;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.manaweaving.Manaweave;
import com.mna.events.EventDispatcher;
import com.mna.network.ServerMessageDispatcher;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import com.mna.recipes.rituals.RitualRecipe;
import com.mna.rituals.RitualBlockPosComparator;
import com.mna.rituals.RitualReagent;
import com.mna.rituals.contexts.RitualContext;
import com.mna.rituals.contexts.RitualReagentReplaceContext;
import com.mna.sound.EntityAliveLoopingSound;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import com.mna.api.tools.MNACap;
import com.mna.api.tools.MNAItemData;

public class Ritual
extends Entity {
    private static final EntityDataAccessor<CompoundTag> BLOCK_LOCATIONS = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<CompoundTag> VALID_LOCATIONS = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<CompoundTag> COLLECTED_REAGENTS = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Byte> RITUAL_STATE = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> SHUTDOWN_AGE = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> RITUAL_NAME = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> CASTER_UUID = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> REQUESTED_PATTERN = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> CURRENT_ENTITY = SynchedEntityData.defineId(Ritual.class, (EntityDataSerializer)EntityDataSerializers.INT);
    public static final byte MODE_ALL = 0;
    public static final byte MODE_INDEXED = 1;
    public static final byte MODE_INDEXED_DISPLAY = 2;
    public static final int REAGENT_ADVANCE_RATE = 20;
    public static final int RADIANT_ADVANCE_RATE = 10;
    public static final int BEAM_ADVANCE_RATE = 3;
    public static final float SPEED_NORMAL = 1.0f;
    public static float SPEED_FAST = 0.1f;
    private int age = 0;
    private int stageTicks = 0;
    final RitualBlockPosComparator comp;
    private NonNullList<Pair<BlockPos, ItemStack>> collectedReagents;
    private NonNullList<ResourceLocation> collectedPatterns;
    private NonNullList<ResourceLocation> requiredPatterns;
    private RitualState prevState;
    private RitualState curState;
    private boolean forceCollectAllReagents = false;
    private Entity __patternEntity;
    private int patternConsumeTicks = 0;
    private int dispatchTries = 0;
    private NonNullList<RitualEffect> __cachedRitualHandlers;
    private RitualRecipe __cachedCurrentRitual;
    private boolean __dynamicReagentsCached = false;
    public long worldTimeAtReagentCollectStart = 0L;
    private ItemStack cachedDynamicStack = ItemStack.EMPTY;
    private boolean dynamicItemChanged = false;

    public Ritual(EntityType<? extends Ritual> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.setInvulnerable(true);
        this.comp = new RitualBlockPosComparator();
        this.collectedReagents = NonNullList.create();
        this.collectedPatterns = NonNullList.create();
        this.requiredPatterns = NonNullList.create();
        NeoForge.EVENT_BUS.register(this);
    }

    private void runStateLogic() {
        try {
            switch (this.curState) {
                case POWERING_UP: {
                    this.logic_statePoweringUp();
                    break;
                }
                case COLLECTING_REAGENTS: {
                    this.logic_stateGatheringReagents();
                    break;
                }
                case COLLECTING_PATTERNS: {
                    this.logic_stateGatheringPatterns();
                    break;
                }
                case COLLAPSING: {
                    this.logic_collapsing();
                    break;
                }
                case PROCESSING_RITUAL: {
                    this.logic_processRitual();
                    break;
                }
                case COMPLETING: {
                    this.logic_complete();
                    break;
                }
            }
        }
        catch (Throwable t) {
            try {
                Player p = this.getCaster();
                if (p != null) {
                    p.sendSystemMessage((Component)Component.translatable((String)"ritual.mna.fatal_error"));
                }
            }
            catch (Throwable subt) {
                ManaAndArtifice.LOGGER.error("Tried to send a message to the player performing the ritual about the following error, but it failed.");
                ManaAndArtifice.LOGGER.error(subt);
            }
            ManaAndArtifice.LOGGER.error("A critical error has been caught during a ritual.  The ritual was terminated to preserve stability.");
            ManaAndArtifice.LOGGER.error(t);
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private void logic_statePoweringUp() {
        if (this.getAge() % 5 == 0) {
            this.checkForExplosion(true);
        }
        if (this.getStageTicks() >= this.getPowerupTime()) {
            this.setState(RitualState.COLLECTING_REAGENTS);
        }
    }

    private void logic_stateGatheringReagents() {
        if (this.getAge() % 5 == 0) {
            this.checkForExplosion(false);
        }
        if (this.getStageTicks() == 10) {
            NonNullList<RitualBlockPos> locations = this.getRitualData((byte)1);
            for (RitualBlockPos rbp : locations) {
                BlockEntity te = this.level().getBlockEntity(rbp.getBlockPos());
                if (te == null || !(te instanceof ChalkRuneTile)) continue;
                ChalkRuneTile teCr = (ChalkRuneTile)te;
                if (rbp.isPresent() && !rbp.getReagent().isEmpty()) {
                    if (teCr.MatchesReagent(rbp.getReagent().getResourceLocation())) {
                        if (rbp.getReagent().shouldConsumeReagent() || rbp.getReagent().isManualReturn() || this.forceCollectAllReagents) {
                            ItemStack stack = teCr.getItem(0).copy();
                            teCr.clearStack();
                            this.collectedReagents.add(new Pair(rbp.getBlockPos(), stack));
                            BlockState state = this.level().getBlockState(rbp.getBlockPos());
                            this.level().sendBlockUpdated(rbp.getBlockPos(), state, state, 2);
                        } else {
                            this.collectedReagents.add(new Pair(rbp.getBlockPos(), teCr.getItem(0).copy()));
                        }
                    } else {
                        this.collectedReagents.add(new Pair(rbp.getBlockPos(), new ItemStack((ItemLike)Items.AIR)));
                    }
                }
                teCr.setReadOnly(true);
            }
            if (!this.level().isClientSide()) {
                ListTag list = this.writeCollectedReagents();
                CompoundTag compound = new CompoundTag();
                compound.put("data", (Tag)list);
                this.entityData.set(COLLECTED_REAGENTS, compound);
                ServerMessageDispatcher.sendRitualReagentData(compound, this.getId(), (ServerLevel)this.level(), this.blockPosition());
            }
        }
        if (this.getStageTicks() >= 40) {
            this.setState(RitualState.COLLECTING_PATTERNS);
        }
    }

    private void logic_stateGatheringPatterns() {
        if (this.getAge() % 5 == 0) {
            this.checkForExplosion(false);
        }
        if (this.requiredPatterns.size() == 0) {
            this.setState(RitualState.COLLAPSING);
            return;
        }
        ManaweavingPattern pattern = this.getRequestedPattern();
        if (pattern == null) {
            this.setRequestedPattern(((ResourceLocation)this.requiredPatterns.get(0)).toString());
            return;
        }
        if (this.getCurrentEntityTarget() == null) {
            if (this.getAge() % 10 == 0) {
                this.findPatternEntity();
            }
        } else {
            ++this.patternConsumeTicks;
            if (this.patternConsumeTicks >= 20) {
                this.getCurrentEntityTarget().remove(Entity.RemovalReason.DISCARDED);
                this.setRequestedPattern("");
                this.patternConsumeTicks = 0;
                this.collectedPatterns.add(((ResourceLocation)this.requiredPatterns.get(0)));
                this.requiredPatterns.remove(0);
            }
        }
    }

    private void logic_collapsing() {
        if (this.getStageTicks() >= 20) {
            this.setState(RitualState.PROCESSING_RITUAL);
        }
    }

    private void logic_processRitual() {
        Player ritualCaster = this.getCaster();
        if (ritualCaster != null || this.dispatchTries >= 10) {
            if (ritualCaster == null) {
                ManaAndArtifice.LOGGER.error("Failed to parse UUID for ritual caster.  Giving up and completing the ritual anyway, but some effects may not apply!");
            }
            for (RitualEffect eff : this.getHandlers()) {
                eff.onRitualCompleted(new RitualContext(this.getCaster(), this));
            }
        } else {
            ++this.dispatchTries;
            return;
        }
        int shutdownTime = 0;
        for (RitualEffect eff : this.getHandlers()) {
            int time = eff.getRitualCompleteDelay(new RitualContext(this.getCaster(), this));
            if (time <= shutdownTime) continue;
            shutdownTime = time;
        }
        if (shutdownTime < 0) {
            shutdownTime = 0;
        }
        this.setShutdownAge(shutdownTime);
        this.setState(RitualState.COMPLETING);
    }

    private void logic_complete() {
        if (this.isAlive() && this.getStageTicks() > this.getShutdownAge()) {
            NonNullList<RitualBlockPos> positions = this.getRitualData((byte)0);
            for (int i = 0; i < positions.size(); ++i) {
                ChalkRuneTile tecr;
                BlockState runeState = this.level().getBlockState(((RitualBlockPos)positions.get(i)).getBlockPos());
                if (runeState.getBlock() != BlockInit.CHALK_RUNE.get() || (tecr = (ChalkRuneTile)this.level().getBlockEntity(((RitualBlockPos)positions.get(i)).getBlockPos())) == null) continue;
                if (((Boolean)runeState.getValue((Property)ChalkRuneBlock.METAL)).booleanValue()) {
                    tecr.setReadOnly(false);
                    this.level().setBlockAndUpdate(((RitualBlockPos)positions.get(i)).getBlockPos(), (BlockState)runeState.setValue(ChalkRuneBlock.ACTIVATED, Boolean.valueOf(false)));
                    continue;
                }
                if (!tecr.isGhostItem()) {
                    Containers.dropContents((Level)this.level(), (BlockPos)((RitualBlockPos)positions.get(i)).getBlockPos(), (Container)tecr);
                }
                this.level().setBlockAndUpdate(((RitualBlockPos)positions.get(i)).getBlockPos(), Blocks.AIR.defaultBlockState());
            }
            if (this.getCurrentRitual() != null && this.getCaster() != null) {
                EventDispatcher.DispatchRitualComplete(this.getCurrentRitual(), this.getHandlers(), this.blockPosition(), this.collectedReagents.stream().map(e -> (ItemStack)e.getSecond()).collect(Collectors.toList()), this.getCaster());
                MNACap.get(this.getCaster(), PlayerProgressionProvider.PROGRESSION).ifPresent(p -> MNACap.get(this.getCaster(), PlayerMagicProvider.MAGIC).ifPresent(m -> m.addMagicXP(10, this.getCaster(), (IPlayerProgression)p)));
                this.executeCommand(this.getCurrentRitual());
                if (this.getCaster() instanceof ServerPlayer) {
                    CustomAdvancementTriggers.PERFORM_RITUAL.trigger((ServerPlayer)this.getCaster(), this.getCurrentRitual().getId(), this.getCurrentRitual().getTier());
                }
            }
            MNACap.get(this.level().getChunkAt(this.blockPosition()), ChunkMagicProvider.MAGIC).ifPresent(cm -> cm.addResidualMagic(this.__cachedCurrentRitual != null ? (float)(this.__cachedCurrentRitual.getTier() * 100) : 100.0f));
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void tick() {
        this.prevState = this.curState;
        this.curState = this.getState();
        this.setStageTicks(this.getStageTicks() + 1);
        if (this.curState == RitualState.GUIDING_REAGENT_PLACEMENT) {
            if (!this.level().isClientSide() && !this.__dynamicReagentsCached && this.stageTicks % 20 == 0) {
                this.checkRitualIntegrity(true);
            }
            return;
        }
        this.setAge(this.getAge() + 1);
        if (this.prevState != this.curState) {
            this.setStageTicks(0);
        }
        if (this.level().isClientSide()) {
            this.SpawnParticles();
            this.PlaySounds();
        } else {
            this.runStateLogic();
        }
    }

    public boolean confirmRitualReagents() {
        if (this.getState() != RitualState.GUIDING_REAGENT_PLACEMENT) {
            return false;
        }
        boolean canStart = true;
        ArrayList<BlockPos> validLocations = new ArrayList<BlockPos>();
        NonNullList<RitualBlockPos> ritualData = this.getRitualData((byte)1, true);
        if (this.dynamicItemChanged) {
            this.cancelRitual(true);
            return false;
        }
        for (RitualBlockPos pos : ritualData) {
            if (!pos.isPresent() || pos.getReagent().isOptional() || !this.level().isLoaded(pos.getBlockPos())) continue;
            ChalkRuneTile te = (ChalkRuneTile)this.level().getBlockEntity(pos.getBlockPos());
            if (te == null) {
                return false;
            }
            boolean match = te.MatchesReagent(pos.getReagent().getResourceLocation());
            if (match) {
                validLocations.add(pos.getBlockPos());
            }
            canStart &= match;
        }
        this.setValidReagentLocations(validLocations);
        if (canStart) {
            this.setState(RitualState.POWERING_UP);
        }
        return canStart;
    }

    @Nullable
    public IRitualReagent getReagentForPosition(BlockPos pos) {
        if (this.getState() != RitualState.GUIDING_REAGENT_PLACEMENT) {
            return null;
        }
        Optional<RitualBlockPos> dataPos = this.getRitualData((byte)1).stream().filter(rbp -> rbp.getBlockPos().equals(pos)).findFirst();
        if (dataPos.isPresent() && this.level().isLoaded(pos)) {
            ChalkRuneTile te = (ChalkRuneTile)this.level().getBlockEntity(dataPos.get().getBlockPos());
            if (te == null) {
                return null;
            }
            return dataPos.get().getReagent();
        }
        return null;
    }

    public boolean checkForExplosion(boolean checkReagents) {
        if (this.getState() == RitualState.COMPLETING || this.getState() == RitualState.PROCESSING_RITUAL) {
            return false;
        }
        if (!this.isAlive()) {
            return false;
        }
        boolean integrityCheck = this.checkRitualIntegrity(checkReagents);
        if (!integrityCheck) {
            this.cancelRitual(true);
            if (this.getState() != RitualState.GUIDING_REAGENT_PLACEMENT) {
                float radius = this.__cachedCurrentRitual == null ? 2.5f : (float)this.__cachedCurrentRitual.getLowerBound();
                this.level().explode(null, this.getX(), this.getY(), this.getZ(), radius, this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
                this.setState(RitualState.DEAD);
            }
            return false;
        }
        return true;
    }

    private boolean checkRitualIntegrity(boolean checkReagents) {
        NonNullList<RitualBlockPos> locations = this.getRitualData((byte)0);
        for (int i = 0; i < locations.size(); ++i) {
            ChalkRuneTile te;
            if (!this.level().isLoaded(((RitualBlockPos)locations.get(i)).getBlockPos()) || this.level().getBlockState(((RitualBlockPos)locations.get(i)).getBlockPos()).getBlock() != ((Block)BlockInit.CHALK_RUNE.get()).defaultBlockState().getBlock()) {
                return false;
            }
            if (!checkReagents || ((RitualBlockPos)locations.get(i)).getIndex() < 0 || !((RitualBlockPos)locations.get(i)).isPresent() || ((RitualBlockPos)locations.get(i)).getReagent().isOptional() || (te = (ChalkRuneTile)this.level().getBlockEntity(((RitualBlockPos)locations.get(i)).getBlockPos())).MatchesReagent(((RitualBlockPos)locations.get(i)).getReagent().getResourceLocation())) continue;
            return false;
        }
        return true;
    }

    private void findPatternEntity() {
        Optional<Manaweave> patternEntity = this.level().getEntities((Entity)this, this.getBoundingBox().inflate(10.0), entity -> {
            if (!(entity instanceof Manaweave)) {
                return false;
            }
            Manaweave mw = (Manaweave)(entity);
            ArrayList<IManaweavePattern> p = mw.getPatterns();
            if (p.size() != 1) {
                return false;
            }
            return p.get(0).getRegistryId().toString().equals(this.getRequestedPattern().getId().toString());
        }).stream().map(e -> (Manaweave)(e)).findFirst();
        if (patternEntity != null && patternEntity.isPresent()) {
            this.setCurrentEntityTarget(patternEntity.get());
            patternEntity.get().setMerging(true);
        }
    }

    public boolean cancelRitual() {
        return this.cancelRitual(false);
    }

    private boolean cancelRitual(boolean ignoreState) {
        if (!this.level().isClientSide() && (ignoreState || this.getState() == RitualState.GUIDING_REAGENT_PLACEMENT)) {
            NonNullList<RitualBlockPos> locations = this.getRitualData((byte)0);
            for (RitualBlockPos pos : locations) {
                BlockEntity te = this.level().getBlockEntity(pos.getBlockPos());
                if (te == null || !(te instanceof ChalkRuneTile)) continue;
                if (this.level().getBlockState(pos.getBlockPos()).getBlock() == BlockInit.CHALK_RUNE.get()) {
                    ((ChalkRuneTile)te).setReadOnly(false);
                    this.level().setBlockAndUpdate(pos.getBlockPos(), (BlockState)this.level().getBlockState(pos.getBlockPos()).setValue(ChalkRuneBlock.ACTIVATED, Boolean.valueOf(false)));
                    continue;
                }
                this.level().setBlockAndUpdate(pos.getBlockPos(), Blocks.AIR.defaultBlockState());
            }
            this.remove(Entity.RemovalReason.DISCARDED);
            return true;
        }
        return false;
    }

    public void executeCommand(IRitualRecipe recipe) {
        if (!recipe.hasCommand()) {
            return;
        }
        MinecraftServer minecraftserver = this.level().getServer();
        if (minecraftserver.isCommandBlockEnabled()) {
            try {
                CommandSourceStack css = new CommandSourceStack((CommandSource)this, this.position(), this.getRotationVector(), this.level() instanceof ServerLevel ? (ServerLevel)this.level() : null, GeneralConfigValues.RitualPermissionLevel, this.getName().getString(), this.getDisplayName(), this.level().getServer(), (Entity)this.getCaster());
                minecraftserver.getCommands().performPrefixedCommand(css, recipe.getCommand());
            }
            catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable((Throwable)throwable, (String)"Executing ritual command");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Command to be executed");
                crashreportcategory.setDetail("Command", recipe.getCommand());
                throw new ReportedException(crashreport);
            }
        }
    }

    private void PlaySounds() {
        if (this.curState == RitualState.POWERING_UP && this.stageTicks == 0) {
            this.PlaySound(SFX.Spell.Cast.ARCANE);
            if (this.getHandlers().size() > 0) {
                RitualEffect effect = (RitualEffect)this.getHandlers().get((int)(Math.random() * (double)this.getHandlers().size()));
                this.PlayLoopingSound(effect.getLoopSound(new RitualContext(this.getCaster(), this)));
            } else {
                this.PlayLoopingSound(SFX.Loops.ENDER);
            }
        }
        if (this.curState.ordinal() >= RitualState.POWERING_UP.ordinal() && this.curState != RitualState.COMPLETING) {
            int beamAge;
            int points;
            int radiant_ticks = (int)Math.ceil(10.0f * this.getSpeed());
            int maxPos = this.age / radiant_ticks;
            if (maxPos < (points = this.getRitualData((byte)2).size()) + 1 && this.age % radiant_ticks == 0) {
                this.PlaySound(SFX.Ritual.Effects.RITUAL_POINT_APPEAR);
            }
            if ((beamAge = this.age - radiant_ticks * points) == 5) {
                this.PlaySound(SFX.Spell.Cast.ENDER);
            }
        }
    }

    private void PlaySound(SoundEvent soundID) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), soundID, SoundSource.PLAYERS, 1.0f, 1.0f, false);
    }

    @OnlyIn(value=Dist.CLIENT)
    private void PlayLoopingSound(SoundEvent soundID) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new EntityAliveLoopingSound(soundID, this));
    }

    private void SpawnParticles() {
        boolean useDefault = false;
        RitualContext context = new RitualContext(this.getCaster(), this);
        for (RitualEffect effect : this.getHandlers()) {
            useDefault |= !effect.spawnRitualParticles(context);
        }
        RitualState curState = this.getState();
        NonNullList<RitualBlockPos> positions = this.getRitualData((byte)1);
        for (int i = 0; i < positions.size(); ++i) {
            BlockPos cur = ((RitualBlockPos)positions.get(i)).getBlockPos();
            if (useDefault && curState.ordinal() > RitualState.POWERING_UP.ordinal()) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_VELOCITY.get())), (double)cur.getX() + Math.random(), (double)cur.getY(), (double)cur.getZ() + Math.random(), 0.0, Math.random() * (double)0.05f + (double)0.05f, 0.0);
            }
            if (curState == RitualState.COLLECTING_PATTERNS && this.getCurrentEntityTarget() != null) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_LERP_POINT.get())), -0.5 + this.getCurrentEntityTarget().getX() + Math.random(), -0.5 + this.getCurrentEntityTarget().getY() + Math.random(), -0.5 + this.getCurrentEntityTarget().getZ() + Math.random(), this.getX(), this.getY() + 1.0, this.getZ());
            }
            if (this.getStageTicks() != this.getShutdownAge()) continue;
            for (int j = 0; j < 10; ++j) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_RANDOM.get())), (double)cur.getX() + Math.random(), (double)cur.getY(), (double)cur.getZ() + Math.random(), (double)0.1f, (double)0.1f, (double)0.1f);
            }
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(BLOCK_LOCATIONS, new CompoundTag());
        builder.define(RITUAL_STATE, ((byte)RitualState.GUIDING_REAGENT_PLACEMENT.ordinal()));
        builder.define(RITUAL_NAME, "");
        builder.define(SHUTDOWN_AGE, -1);
        builder.define(SPEED, Float.valueOf(1.0f));
        builder.define(CASTER_UUID, "");
        builder.define(REQUESTED_PATTERN, "");
        builder.define(CURRENT_ENTITY, -1);
        builder.define(VALID_LOCATIONS, new CompoundTag());
        builder.define(COLLECTED_REAGENTS, new CompoundTag());
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (this.level().isClientSide() && key == COLLECTED_REAGENTS) {
            ListTag data = ((CompoundTag)this.entityData.get(COLLECTED_REAGENTS)).getList("data", 10);
            this.readCollectedReagents(data);
        } else if (this.level().isClientSide() && key == RITUAL_STATE && this.getState() == RitualState.COLLECTING_REAGENTS) {
            this.worldTimeAtReagentCollectStart = this.level().getGameTime();
        }
        super.onSyncedDataUpdated(key);
    }

    private void WriteBlockPosToNBT(String key, RitualBlockPos pos, CompoundTag nbt) {
        CompoundTag subNBT = new CompoundTag();
        subNBT.putInt("idx", pos.getIndex());
        subNBT.putInt("d_idx", pos.getDisplayIndex());
        subNBT.putInt("x", pos.getBlockPos().getX());
        subNBT.putInt("y", pos.getBlockPos().getY());
        subNBT.putInt("z", pos.getBlockPos().getZ());
        if (pos.isPresent()) {
            ((RitualReagent)pos.getReagent()).writeToNBT(subNBT);
        }
        nbt.put(key, (Tag)subNBT);
    }

    private RitualBlockPos ReadBlockPosFromNBT(String key, CompoundTag nbt) {
        if (!nbt.contains(key)) {
            return null;
        }
        CompoundTag subNBT = nbt.getCompound(key);
        if (!(subNBT.contains("x") && subNBT.contains("y") && subNBT.contains("z") && subNBT.contains("idx") && subNBT.contains("d_idx"))) {
            return null;
        }
        return new RitualBlockPos(subNBT.getInt("idx"), subNBT.getInt("d_idx"), new BlockPos(subNBT.getInt("x"), subNBT.getInt("y"), subNBT.getInt("z")), RitualReagent.fromNBT(subNBT));
    }

    public void readCollectedReagents(ListTag data) {
        this.collectedReagents.clear();
        for (int i = 0; i < data.size(); ++i) {
            CompoundTag entry = data.getCompound(i);
            if (entry == null || !entry.contains("pos") || !entry.contains("stack")) continue;
            BlockPos pos = BlockPos.of((long)entry.getLong("pos"));
            ItemStack stack = ItemStack.parseOptional(this.level().registryAccess(), (CompoundTag)entry.getCompound("stack"));
            if (pos == null || stack == null) continue;
            this.collectedReagents.add(new Pair(pos, stack));
        }
        this.worldTimeAtReagentCollectStart = this.level().getGameTime();
        if (!this.level().isClientSide()) {
            ListTag list = this.writeCollectedReagents();
            CompoundTag compound = new CompoundTag();
            compound.put("data", (Tag)list);
            this.entityData.set(COLLECTED_REAGENTS, compound);
        }
    }

    private ListTag writeCollectedReagents() {
        ListTag collected_reagents = new ListTag();
        for (Pair e : this.collectedReagents) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", ((BlockPos)e.getFirst()).asLong());
            entry.put("stack", (Tag)MNAItemData.save((ItemStack)e.getSecond(), new CompoundTag()));
            collected_reagents.add(entry);
        }
        return collected_reagents;
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("ritual_data")) {
            int i;
            int count;
            CompoundTag ritualData = compound.getCompound("ritual_data");
            if (ritualData.contains("shutdown_age")) {
                this.entityData.set(SHUTDOWN_AGE, ritualData.getInt("shutdown_age"));
            }
            if (ritualData.contains("ritual_name")) {
                this.entityData.set(RITUAL_NAME, ritualData.getString("ritual_name"));
            }
            if (ritualData.contains("state")) {
                this.entityData.set(RITUAL_STATE, ritualData.getByte("state"));
            }
            if (ritualData.contains("speed")) {
                this.entityData.set(SPEED, Float.valueOf(ritualData.getFloat("speed")));
            }
            if (ritualData.contains("caster")) {
                this.entityData.set(CASTER_UUID, ritualData.getString("caster"));
            }
            if (ritualData.contains("block_locations")) {
                this.entityData.set(BLOCK_LOCATIONS, ((CompoundTag)ritualData.get("block_locations")));
            }
            if (ritualData.contains("valid_locations")) {
                this.entityData.set(VALID_LOCATIONS, ((CompoundTag)ritualData.get("valid_locations")));
            }
            if (ritualData.contains("__dynamicReagentsCached")) {
                this.__dynamicReagentsCached = ritualData.getBoolean("__dynamicReagentsCached");
            }
            if (ritualData.contains("collected_reagents", 9)) {
                this.readCollectedReagents(ritualData.getList("collected_reagents", 10));
            } else {
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            if (ritualData.contains("forceCollectAllReagents")) {
                this.forceCollectAllReagents = ritualData.getBoolean("forceCollectAllReagents");
            }
            if (ritualData.contains("dynamicSourceCache")) {
                this.cachedDynamicStack = ItemStack.parseOptional(this.level().registryAccess(), (CompoundTag)((CompoundTag)ritualData.get("dynamicSourceCache")));
            }
            if (ritualData.contains("collected_patterns")) {
                CompoundTag collected_patterns = ritualData.getCompound("collected_patterns");
                count = collected_patterns.getInt("count");
                for (i = 0; i < count; ++i) {
                    this.collectedPatterns.add(ResourceLocation.parse(collected_patterns.getString("pattern_" + i)));
                }
            }
            if (ritualData.contains("required_patterns")) {
                CompoundTag required_patterns = ritualData.getCompound("required_patterns");
                count = required_patterns.getInt("count");
                for (i = 0; i < count; ++i) {
                    this.requiredPatterns.add(ResourceLocation.parse(required_patterns.getString("pattern_" + i)));
                }
            }
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        CompoundTag ritualData = new CompoundTag();
        ritualData.putInt("shutdown_age", ((Integer)this.entityData.get(SHUTDOWN_AGE)).intValue());
        ritualData.putString("ritual_name", (String)this.entityData.get(RITUAL_NAME));
        ritualData.putString("caster", (String)this.entityData.get(CASTER_UUID));
        ritualData.putByte("state", ((Byte)this.entityData.get(RITUAL_STATE)).byteValue());
        ritualData.putFloat("speed", ((Float)this.entityData.get(SPEED)).floatValue());
        ritualData.put("block_locations", (Tag)this.entityData.get(BLOCK_LOCATIONS));
        ritualData.put("valid_locations", (Tag)this.entityData.get(VALID_LOCATIONS));
        ritualData.putBoolean("forceCollectAllReagents", this.forceCollectAllReagents);
        ritualData.putBoolean("__dynamicReagentsCached", this.__dynamicReagentsCached);
        CompoundTag __dyn = new CompoundTag();
        com.mna.api.tools.MNAItemData.save(this.cachedDynamicStack, __dyn);
        ritualData.put("dynamicSourceCache", __dyn);
        ritualData.put("collected_reagents", (Tag)this.writeCollectedReagents());
        CompoundTag collected_patterns = new CompoundTag();
        collected_patterns.putInt("count", this.collectedPatterns.size());
        for (int i = 0; i < this.collectedPatterns.size(); ++i) {
            collected_patterns.putString("pattern_" + i, ((ResourceLocation)this.collectedPatterns.get(i)).toString());
        }
        ritualData.put("collected_patterns", (Tag)collected_patterns);
        CompoundTag required_patterns = new CompoundTag();
        required_patterns.putInt("count", this.requiredPatterns.size());
        for (int i = 0; i < this.requiredPatterns.size(); ++i) {
            required_patterns.putString("pattern_" + i, ((ResourceLocation)this.requiredPatterns.get(i)).toString());
        }
        ritualData.put("required_patterns", (Tag)required_patterns);
        compound.put("ritual_data", (Tag)ritualData);
    }

    public ArrayList<BlockPos> getValidReagentLocations() {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        CompoundTag nbt = (CompoundTag)this.entityData.get(VALID_LOCATIONS);
        if (!nbt.contains("count")) {
            return positions;
        }
        int count = nbt.getInt("count");
        for (int i = 0; i < count; ++i) {
            if (nbt.contains("block_pos_" + i)) {
                CompoundTag blockPos = nbt.getCompound("block_pos_" + i);
                BlockPos pos = new BlockPos(blockPos.getInt("x"), blockPos.getInt("y"), blockPos.getInt("z"));
                positions.add(pos);
                continue;
            }
            ManaAndArtifice.LOGGER.error("Missing key block_pos_" + i + " in valid reagent locations sync data.");
        }
        return positions;
    }

    private void setValidReagentLocations(ArrayList<BlockPos> locations) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("count", locations.size());
        for (int i = 0; i < locations.size(); ++i) {
            CompoundTag blockPos = new CompoundTag();
            BlockPos pos = locations.get(i);
            blockPos.putInt("x", pos.getX());
            blockPos.putInt("y", pos.getY());
            blockPos.putInt("z", pos.getZ());
            nbt.put("block_pos_" + i, (Tag)blockPos);
        }
        this.entityData.set(VALID_LOCATIONS, nbt);
    }

    private void setCurrentEntityTarget(Entity e) {
        this.entityData.set(CURRENT_ENTITY, e.getId());
    }

    private Entity getCurrentEntityTarget() {
        if (this.__patternEntity == null || !this.__patternEntity.isAlive()) {
            this.patternConsumeTicks = 0;
            this.__patternEntity = this.level().getEntity(((Integer)this.entityData.get(CURRENT_ENTITY)).intValue());
        }
        return this.__patternEntity;
    }

    public void setRequiredPatterns(Collection<ResourceLocation> patterns) {
        if (patterns == this.requiredPatterns) {
            return;
        }
        this.requiredPatterns.addAll(patterns);
    }

    public void setRitualBlockLocations(NonNullList<RitualBlockPos> locations) {
        CompoundTag blockLocations = new CompoundTag();
        blockLocations.putInt("count", locations.size());
        for (int i = 0; i < locations.size(); ++i) {
            this.WriteBlockPosToNBT("blockPos" + i, (RitualBlockPos)locations.get(i), blockLocations);
        }
        this.entityData.set(BLOCK_LOCATIONS, blockLocations);
    }

    public NonNullList<RitualBlockPos> getRitualData(byte mode) {
        return this.getRitualData(mode, false);
    }

    public NonNullList<RitualBlockPos> getRitualData(byte mode, boolean checkDynamicChange) {
        NonNullList positions = NonNullList.create();
        CompoundTag blockLocations = (CompoundTag)this.entityData.get(BLOCK_LOCATIONS);
        if (!blockLocations.contains("count")) {
            return positions;
        }
        RitualBlockPos dynamicSupplier = null;
        boolean foundDynamics = false;
        int count = blockLocations.getInt("count");
        for (int i = 0; i < count; ++i) {
            RitualBlockPos pos = this.ReadBlockPosFromNBT("blockPos" + i, blockLocations);
            if (pos == null) {
                return NonNullList.create();
            }
            if (mode == 1 && pos.getIndex() < 0 || mode == 2 && pos.getDisplayIndex() < 0) continue;
            if (pos.getReagent() != null) {
                if (pos.getReagent().isDynamic()) {
                    foundDynamics = true;
                }
                if (pos.getReagent().isDynamicSource()) {
                    dynamicSupplier = pos;
                }
            }
            positions.add(pos);
        }
        if (mode == 1 || mode == 0) {
            this.comp.setCompareIndex();
        } else if (mode == 2) {
            this.comp.setCompareDisplay();
        }
        positions.sort((Comparator)this.comp);
        if (!this.level().isClientSide()) {
            ItemStack dynamicStack;
            BlockEntity te;
            if (!this.__dynamicReagentsCached) {
                if (foundDynamics) {
                    if (dynamicSupplier == null) {
                        ManaAndArtifice.LOGGER.error("Ritual specifies dynamic reagents/patterns but no supplier is found!");
                        positions.clear();
                    } else {
                        BlockEntity te2 = this.level().getBlockEntity(dynamicSupplier.getBlockPos());
                        if (te2 != null && te2 instanceof ChalkRuneTile) {
                            ItemStack dynamicStack2;
                            boolean modified = false;
                            this.cachedDynamicStack = dynamicStack2 = ((ChalkRuneTile)te2).getDisplayedItem().copy();
                            for (RitualEffect eff : this.getHandlers()) {
                                modified |= eff.getDynamicReagents(dynamicStack2, new RitualReagentReplaceContext(this.getCaster(), this, (NonNullList<RitualBlockPos>)positions));
                            }
                            if (modified) {
                                this.setRitualBlockLocations((NonNullList<RitualBlockPos>)positions);
                                this.__dynamicReagentsCached = true;
                            }
                        }
                    }
                } else {
                    this.__dynamicReagentsCached = true;
                }
            } else if (foundDynamics && checkDynamicChange && (te = this.level().getBlockEntity(dynamicSupplier.getBlockPos())) != null && te instanceof ChalkRuneTile && !ItemStack.matches((ItemStack)this.cachedDynamicStack, (ItemStack)(dynamicStack = ((ChalkRuneTile)te).getDisplayedItem().copy()))) {
                this.dynamicItemChanged = true;
            }
        }
        return positions;
    }

    private ResourceLocation getRitualName() {
        return ResourceLocation.parse((String)this.entityData.get(RITUAL_NAME));
    }

    public void setRitualName(ResourceLocation name) {
        this.entityData.set(RITUAL_NAME, name.toString());
        this.__cachedCurrentRitual = RitualRecipe.find(this.level(), name);
        if (this.__cachedCurrentRitual != null) {
            ArrayList<ResourceLocation> patterns = new ArrayList<ResourceLocation>();
            for (String s : this.__cachedCurrentRitual.getManaweavePatterns()) {
                patterns.add(ResourceLocation.parse(s));
            }
            this.setRequiredPatterns(patterns);
        }
    }

    public RitualRecipe getCurrentRitual() {
        if (this.__cachedCurrentRitual == null) {
            this.__cachedCurrentRitual = RitualRecipe.find(this.level(), this.getRitualName());
        }
        return this.__cachedCurrentRitual;
    }

    public RitualState getState() {
        return RitualState.values()[(Byte)this.entityData.get(RITUAL_STATE)];
    }

    private void setState(RitualState state) {
        this.entityData.set(RITUAL_STATE, ((byte)state.ordinal()));
        if (state == RitualState.COLLECTING_REAGENTS) {
            this.worldTimeAtReagentCollectStart = this.level().getGameTime();
        }
    }

    private int getShutdownAge() {
        return (Integer)this.entityData.get(SHUTDOWN_AGE);
    }

    private void setShutdownAge(int shutdown_age) {
        this.entityData.set(SHUTDOWN_AGE, shutdown_age);
    }

    public float getSpeed() {
        return ((Float)this.entityData.get(SPEED)).floatValue();
    }

    public void setSpeed(float speed) {
        speed = Mth.clamp((float)speed, (float)0.1f, (float)2.0f);
        this.entityData.set(SPEED, Float.valueOf(speed));
    }

    public void preConsumePatterns(List<ResourceLocation> patterns) {
        this.requiredPatterns.removeAll(patterns);
    }

    public void setCasterUUID(UUID casterUUID) {
        if (casterUUID != null) {
            this.entityData.set(CASTER_UUID, casterUUID.toString());
        } else {
            ManaAndArtifice.LOGGER.error("Received null UUID for ritual caster.  Some effects may not apply!");
        }
    }

    public UUID getCasterUUID() {
        try {
            UUID uUID = UUID.fromString((String)this.entityData.get(CASTER_UUID));
            return uUID;
        }
        catch (Exception exception) {
        }
        catch (Throwable throwable) {
            throw throwable;
        }
        return null;
    }

    @Nullable
    private Player getCaster() {
        if (this.getCasterUUID() == null) {
            return null;
        }
        return this.level().getPlayerByUUID(this.getCasterUUID());
    }

    private int getPowerupTime() {
        return (int)Math.ceil((float)(this.getRitualData((byte)2).size() * 13 + 20) * this.getSpeed());
    }

    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(3.0);
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getStageTicks() {
        return this.stageTicks;
    }

    public void setStageTicks(int stageTicks) {
        this.stageTicks = stageTicks;
    }

    public void setRequestedPattern(String pattern) {
        this.entityData.set(REQUESTED_PATTERN, pattern);
    }

    public ManaweavingPattern getRequestedPattern() {
        String patternID = (String)this.entityData.get(REQUESTED_PATTERN);
        return ManaweavingPatternHelper.GetManaweavingRecipe(this.level(), ResourceLocation.parse(patternID));
    }

    public void setForceConsumeReagents(boolean force) {
        this.forceCollectAllReagents = force;
    }

    private NonNullList<RitualEffect> getHandlers() {
        if (this.__cachedRitualHandlers == null) {
            this.__cachedRitualHandlers = NonNullList.create();
            Registries.RitualEffect.getValues().forEach(eff -> {
                if (eff.handlesRitual(this.getRitualName())) {
                    this.__cachedRitualHandlers.add(eff);
                }
            });
        }
        return this.__cachedRitualHandlers;
    }

    public NonNullList<ItemStack> getCollectedReagents() {
        NonNullList reagents = NonNullList.create();
        reagents.addAll((Collection)this.collectedReagents.stream().map(e -> (ItemStack)e.getSecond()).collect(Collectors.toList()));
        return reagents;
    }

    public NonNullList<Pair<BlockPos, ItemStack>> getCollectedReagentsByLocation() {
        return this.collectedReagents;
    }

    public NonNullList<ResourceLocation> getCollectedPatterns() {
        NonNullList patterns = NonNullList.create();
        patterns.addAll(this.collectedPatterns);
        return patterns;
    }

    public NonNullList<ResourceLocation> getRequiredPatterns() {
        return this.requiredPatterns;
    }

    public void baseTick() {
    }

    public boolean isPickable() {
        return false;
    }

    public boolean isAttackable() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public void checkDespawn() {
    }

    public boolean canUpdate() {
        return true;
    }

    public static enum RitualState {
        GUIDING_REAGENT_PLACEMENT,
        POWERING_UP,
        COLLECTING_REAGENTS,
        COLLECTING_PATTERNS,
        COLLAPSING,
        PROCESSING_RITUAL,
        COMPLETING,
        DEAD;

    }
}


