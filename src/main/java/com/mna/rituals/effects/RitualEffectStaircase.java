 
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.blocks.tileentities.ChalkRuneTile;
import com.mna.entities.utility.PresentItem;
import com.mna.items.ItemInit;
import com.mna.rituals.effects.RitualEffectHole;
import com.mna.tools.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.mutable.MutableBoolean;
import net.minecraft.core.registries.BuiltInRegistries;

public class RitualEffectStaircase
extends RitualEffectHole {
    public RitualEffectStaircase(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        int delay = 10;
        BlockPos pos = context.getCenter().below();
        Direction dir = context.getRecipe().getMatchedDirection(context.getLevel(), context.getCenter());
        if (dir == Direction.SOUTH || dir == Direction.NORTH) {
            dir = dir.getOpposite();
        }
        while (pos.getY() > context.getLevel().getMinBuildHeight() && context.getLevel().getBlockState(pos).getBlock() != Blocks.BEDROCK) {
            DelayedEventQueue.pushEvent(context.getLevel(), new TimedDelayedEvent<RitualEffectHole.DelayData>("staircase", delay, new RitualEffectHole.DelayData(context.getCaster(), pos, context.getLevel()), this::breakBlocks));
            delay += 10;
            pos = pos.below().offset(dir.getNormal());
        }
        MutableBoolean drop_book = new MutableBoolean(true);
        context.getAllPositions().forEach(c -> {
            ChalkRuneTile crt;
            BlockEntity be;
            if (c.isPresent() && c.getReagent() != null && c.getReagent().getResourceLocation().equals(BuiltInRegistries.ITEM.getKey(((Item)ItemInit.FLAT_LANDS_BOOK.get()))) && (be = context.getLevel().getBlockEntity(c.getBlockPos())) instanceof ChalkRuneTile && (crt = (ChalkRuneTile)be).isGhostItem()) {
                drop_book.setFalse();
            }
        });
        if (drop_book.booleanValue()) {
            PresentItem epi = new PresentItem(context.getLevel(), context.getCenter().getX(), context.getCenter().getY() + 1, context.getCenter().getZ());
            epi.setItem(new ItemStack((ItemLike)ItemInit.FLAT_LANDS_BOOK.get()));
            epi.setDeltaMovement(0.0, 0.0, 0.0);
            context.getLevel().addFreshEntity((Entity)epi);
        }
        return true;
    }

    private void breakBlocks(String identifier, RitualEffectHole.DelayData data) {
        if (data.caster == null || data.world == null || !data.world.isLoaded(data.center)) {
            return;
        }
        for (int i = -1; i <= 1; ++i) {
            for (int j = 0; j <= 2; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    BlockPos target = data.center.offset(i, j, k);
                    if (!data.world.isLoaded(target) || data.world.getBlockEntity(target) != null) continue;
                    BlockUtils.destroyBlock((LivingEntity)data.caster, data.world, target, false, (Tier)Tiers.IRON);
                }
            }
        }
    }
}


