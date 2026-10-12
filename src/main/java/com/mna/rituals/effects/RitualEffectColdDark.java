 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.CoffinBlock;
import com.mna.blocks.tileentities.CoffinTile;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.boss.WitherLich;
import com.mna.factions.Factions;
import com.mna.tools.StructureUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class RitualEffectColdDark
extends RitualEffect {
    public RitualEffectColdDark(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        IPlayerProgression progression = MNACap.get((IPlayerProgression)context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return Component.translatable((String)"Progression could not be found...this is a problem.");
        }
        WitherLich tempBoss = new WitherLich(context.getLevel(), Vec3.atBottomCenterOf((Vec3i)context.getCenter()));
        if (!StructureUtils.isPointInStructure((ServerLevel)context.getLevel(), context.getCenter(), tempBoss.getArenaStructureID(), tempBoss.getArenaStructureSegment())) {
            if (progression.getTierProgress(context.getLevel()) < 1.0f) {
                return Component.translatable((String)"ritual.mna.progression.not_ready");
            }
            if (progression != null && progression.hasAlliedFaction() && progression.getAlliedFaction() != Factions.UNDEAD) {
                return Component.translatable((String)"event.mna.faction_ritual_failed");
            }
        }
        return null;
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        if (context.getCaster() == null || context.getCaster().getUUID() == null) {
            return false;
        }
        BlockPos pos = context.getCenter();
        WitherLich lich = new WitherLich(context.getLevel());
        if (StructureUtils.isPointInStructure((ServerLevel)context.getLevel(), context.getCenter(), lich.getArenaStructureID(), lich.getArenaStructureSegment())) {
            lich.setPos((float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f);
            context.getLevel().addFreshEntity((Entity)lich);
            return true;
        }
        BlockPos top_pos = context.getCenter();
        BlockPos other_pos = null;
        Direction coffin_facing = null;
        if (context.getLevel().isEmptyBlock(top_pos.north())) {
            other_pos = top_pos.north();
            coffin_facing = Direction.NORTH;
        } else if (context.getLevel().isEmptyBlock(top_pos.east())) {
            other_pos = top_pos.east();
            coffin_facing = Direction.EAST;
        } else if (context.getLevel().isEmptyBlock(top_pos.south())) {
            other_pos = top_pos.south();
            coffin_facing = Direction.SOUTH;
        } else if (context.getLevel().isEmptyBlock(top_pos.west())) {
            other_pos = top_pos.west();
            coffin_facing = Direction.WEST;
        } else {
            other_pos = top_pos.north();
            coffin_facing = Direction.NORTH;
        }
        if (other_pos == null || coffin_facing == null) {
            return false;
        }
        context.getLevel().setBlockAndUpdate(top_pos, (BlockState)((BlockState)((CoffinBlock)(BlockInit.COFFIN.get())).defaultBlockState().setValue((Property)BedBlock.PART, (Comparable)BedPart.FOOT)).setValue((Property)BedBlock.FACING, (Comparable)coffin_facing));
        context.getLevel().setBlockAndUpdate(other_pos, (BlockState)((BlockState)((CoffinBlock)(BlockInit.COFFIN.get())).defaultBlockState().setValue((Property)BedBlock.PART, (Comparable)BedPart.HEAD)).setValue((Property)BedBlock.FACING, (Comparable)coffin_facing));
        BlockEntity te = context.getLevel().getBlockEntity(other_pos);
        if (te == null || !(te instanceof CoffinTile)) {
            return false;
        }
        ((CoffinTile)te).setRitualPlayer(context.getCaster());
        context.getCaster().sendSystemMessage((Component)Component.translatable((String)"event.mna.cold_dark_prompt").withStyle(ChatFormatting.AQUA));
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 100;
    }
}

