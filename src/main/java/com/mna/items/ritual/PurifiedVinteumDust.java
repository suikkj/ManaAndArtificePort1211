 
package com.mna.items.ritual;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.items.TieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.rituals.RitualBlockPos;
import com.mna.api.rituals.RitualEffect;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.Ritual;
import com.mna.recipes.rituals.RitualRecipe;
import com.mna.rituals.MatchedRitual;
import com.mna.rituals.contexts.RitualCheckContext;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class PurifiedVinteumDust
extends TieredItem {
    public PurifiedVinteumDust(Item.Properties properties) {
        super(new Item.Properties());
    }

    public InteractionResult useOn(UseOnContext context) {
        IPlayerMagic magic = MNACap.get((IPlayerMagic)context.getPlayer(), PlayerMagicProvider.MAGIC).orElse(null);
        IPlayerProgression progression = MNACap.get((IPlayerProgression)context.getPlayer(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (!magic.isMagicUnlocked()) {
            return InteractionResult.FAIL;
        }
        if (!context.getLevel().isClientSide && context.getPlayer().isShiftKeyDown() && context.getLevel().getBlockState(context.getClickedPos()).getBlock() == BlockInit.CHALK_RUNE.get()) {
            this.commandRitualEntity(context, (NonNullList<RitualBlockPos>)(NonNullList)NonNullList.create());
            return InteractionResult.SUCCESS;
        }
        MatchedRitual ritualMatch = RitualRecipe.matchAnyInWorld(context.getClickedPos(), context.getLevel());
        if (ritualMatch != null) {
            if (context.getLevel().isClientSide) {
                this.spawnClientParticles(context);
            } else {
                Component cantStartReason;
                RitualEffect matchedEffect = Registries.RitualEffect.getValues().stream().filter(r -> r.matchRitual(ritualMatch.getRitual().getId())).findFirst().orElse(null);
                if (matchedEffect == null && !ritualMatch.getRitual().hasCommand()) {
                    context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:ritual-no-handler"));
                    return InteractionResult.FAIL;
                }
                if (ritualMatch.getRitual().getTier() > progression.getTier()) {
                    context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:ritual-start-tier-fail"));
                    return InteractionResult.FAIL;
                }
                if (matchedEffect != null && (!context.getPlayer().isCreative() || matchedEffect.applyStartCheckInCreative()) && (cantStartReason = matchedEffect.canRitualStart(new RitualCheckContext(context.getPlayer(), (ServerLevel)context.getLevel(), ritualMatch.getRitual(), context.getClickedPos(), this.getRitualAbove(context)))) != null) {
                    context.getPlayer().sendSystemMessage(cantStartReason);
                    return InteractionResult.FAIL;
                }
                this.updateBlockStates(context, ritualMatch.getPositions(), true);
                RitualInteractResult result = this.commandRitualEntity(context, ritualMatch.getPositions());
                if (result == RitualInteractResult.SPAWN) {
                    this.spawnRitualEntity(context, ritualMatch.getRitual(), ritualMatch.getPositions());
                } else if (result == RitualInteractResult.STARTED && !context.getPlayer().isShiftKeyDown()) {
                    context.getItemInHand().shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void spawnClientParticles(UseOnContext context) {
        for (int i = 0; i < 50; ++i) {
            context.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_RANDOM.get())), (double)((float)context.getClickedPos().getX() + context.getLevel().getRandom().nextFloat() * 10.0f - 5.0f), (double)((float)context.getClickedPos().getY() + context.getLevel().getRandom().nextFloat()), (double)((float)context.getClickedPos().getZ() + context.getLevel().getRandom().nextFloat() * 10.0f - 5.0f), 0.0, 0.0, 0.0);
        }
    }

    void updateBlockStates(UseOnContext context, NonNullList<RitualBlockPos> matchedPositions, boolean activated) {
        for (RitualBlockPos position : matchedPositions) {
            BlockState state = context.getLevel().getBlockState(position.getBlockPos());
            BlockState newState = (BlockState)state.setValue(ChalkRuneBlock.ACTIVATED, Boolean.valueOf(activated));
            context.getLevel().setBlockAndUpdate(position.getBlockPos(), newState);
            context.getLevel().sendBlockUpdated(position.getBlockPos(), state, newState, 2);
        }
    }

    private Ritual getRitualAbove(UseOnContext context) {
        List entities = context.getLevel().getEntities((Entity)context.getPlayer(), new AABB(context.getClickedPos()).inflate(2.0), e -> e.getType() == EntityInit.RITUAL_ENTITY.get());
        Entity ritualEntity = null;
        if (entities != null && entities.size() == 1 && (ritualEntity = (Entity)entities.get(0)) instanceof Ritual) {
            return (Ritual)ritualEntity;
        }
        return null;
    }

    RitualInteractResult commandRitualEntity(UseOnContext context, NonNullList<RitualBlockPos> matchedPositions) {
        Ritual ritualEntity = this.getRitualAbove(context);
        if (ritualEntity != null) {
            if (context.getPlayer().isCrouching()) {
                if (ritualEntity.cancelRitual()) {
                    return RitualInteractResult.CANCELED;
                }
            } else if (ritualEntity.confirmRitualReagents()) {
                return RitualInteractResult.STARTED;
            }
            return RitualInteractResult.NO_ACTION;
        }
        return RitualInteractResult.SPAWN;
    }

    boolean spawnRitualEntity(UseOnContext context, RitualRecipe ritual, NonNullList<RitualBlockPos> matchedPositions) {
        if (context.getPlayer() == null) {
            ManaAndArtifice.LOGGER.error("Received a null player reference when trying to spawn a ritual.  This shouln't happen and something has messed with vanilla code!  Aborting.");
            return false;
        }
        if (context.getPlayer().getUUID() == null) {
            ManaAndArtifice.LOGGER.error("Unable to resolve UUID for a player.  This shouln't happen and something has messed with vanilla code!  Aborting.");
            return false;
        }
        MutableBoolean success = new MutableBoolean(false);
        MNACap.get(context.getPlayer(), PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            float speed = Math.max(1.0f - 0.2f * (float)(p.getTier() - ritual.getTier()), 0.2f);
            Ritual ritualEntity = new Ritual((EntityType<? extends Ritual>)((EntityType)EntityInit.RITUAL_ENTITY.get()), context.getLevel());
            BlockPos ritualSpawnPos = context.getClickedPos();
            ritualEntity.setPos((float)ritualSpawnPos.getX() + 0.5f, ritualSpawnPos.getY(), (float)ritualSpawnPos.getZ() + 0.5f);
            ritualEntity.setRitualBlockLocations(matchedPositions);
            ritualEntity.setRitualName(ritual.getId());
            ritualEntity.setSpeed(speed);
            ritualEntity.setCasterUUID(context.getPlayer().getUUID());
            context.getLevel().addFreshEntity((Entity)ritualEntity);
            success.setTrue();
        });
        return success.getValue();
    }

    static enum RitualInteractResult {
        SPAWN,
        UPDATED,
        STARTED,
        CANCELED,
        NO_ACTION;

    }
}

