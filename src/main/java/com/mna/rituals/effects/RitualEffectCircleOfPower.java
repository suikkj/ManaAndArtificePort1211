 
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.CircleOfPowerBlock;
import com.mna.blocks.tileentities.SanctumTile;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.mna.api.tools.MNACap;

public class RitualEffectCircleOfPower
extends RitualEffect {
    public RitualEffectCircleOfPower(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        if (context.getCaster() == null) {
            return false;
        }
        MNACap.get(context.getCaster(), PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            BlockState state = context.getLevel().getBlockState(context.getCenter().below());
            if (state.getBlock() != Blocks.BEDROCK && context.getLevel().getBlockEntity(context.getCenter().below()) == null) {
                context.getLevel().setBlockAndUpdate(context.getCenter().below(), ((CircleOfPowerBlock)BlockInit.CIRCLE_OF_POWER.get()).defaultBlockState());
                BlockEntity te = context.getLevel().getBlockEntity(context.getCenter().below());
                if (te != null && te instanceof SanctumTile) {
                    ((SanctumTile)te).setFaction(p.getAlliedFaction());
                }
            }
        });
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }
}

