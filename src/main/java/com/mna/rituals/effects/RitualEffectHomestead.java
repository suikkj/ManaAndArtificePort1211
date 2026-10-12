 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.blocks.BlockInit;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.items.runes.StoneRune;
import java.util.ArrayList;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;

public class RitualEffectHomestead
extends RitualEffect {
    public RitualEffectHomestead(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        if (context.getCaster() == null) {
            return false;
        }
        java.util.Optional<com.mna.api.capabilities.IWorldMagic> worldMagicContainer = MNACap.get(context.getLevel(), WorldMagicProvider.MAGIC);
        if (worldMagicContainer.isPresent()) {
            IWorldMagic worldMagic = (IWorldMagic)worldMagicContainer.get();
            ArrayList<ResourceLocation> runes = new ArrayList<ResourceLocation>();
            context.getCollectedReagents(i -> i.getItem() instanceof StoneRune || i.getItem() == Items.AIR).forEach(i -> runes.add(BuiltInRegistries.ITEM.getKey(i.getItem())));
            BlockState state = context.getLevel().getBlockState(context.getCenter().below());
            if (state.getBlock() != Blocks.BEDROCK && context.getLevel().getBlockEntity(context.getCenter().below()) == null) {
                if (worldMagic.setRitualTeleportLocation((ServerLevel)context.getLevel(), context.getCenter().below(), runes, Direction.NORTH)) {
                    context.getLevel().setBlockAndUpdate(context.getCenter().below(), ((Block)BlockInit.RITUAL_TELEPORT_DESTINATION.get()).defaultBlockState());
                } else {
                    context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/homestead.duplicate"));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }
}


