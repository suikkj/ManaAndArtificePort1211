 
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.blocks.tileentities.ChalkRuneTile;
import com.mna.entities.utility.PresentItem;
import com.mna.items.ItemInit;
import com.mna.tools.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.mutable.MutableBoolean;
import net.minecraft.core.registries.BuiltInRegistries;

public class RitualEffectHole
extends RitualEffect {
    protected static final int radius = 1;

    public RitualEffectHole(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        int delay = 10;
        BlockPos pos = context.getCenter().below();
        while (pos.getY() > context.getLevel().getMinBuildHeight() && context.getLevel().getBlockState(pos).getBlock() != Blocks.BEDROCK) {
            DelayedEventQueue.pushEvent(context.getLevel(), new TimedDelayedEvent<DelayData>("hole", delay, new DelayData(context.getCaster(), pos, context.getLevel()), this::breakBlocks));
            delay += 10;
            pos = pos.below();
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

    private void breakBlocks(String identifier, DelayData data) {
        if (data.caster == null || data.world == null || !data.world.isLoaded(data.center)) {
            return;
        }
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                BlockPos target = data.center.offset(i, 0, j);
                if (!data.world.isLoaded(target) || data.world.getBlockEntity(target) != null) continue;
                BlockUtils.destroyBlock((LivingEntity)data.caster, data.world, target, false, (Tier)Tiers.IRON);
            }
        }
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }

    protected class DelayData {
        public Player caster;
        public BlockPos center;
        public Level world;

        public DelayData(Player caster, BlockPos pos, Level world) {
            this.caster = caster;
            this.center = pos;
            this.world = world;
        }
    }
}


