 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.DemonLord;
import com.mna.entities.rituals.DemonStone;
import com.mna.factions.Factions;
import com.mna.tools.StructureUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class RitualEffectBurningHells
extends RitualEffect {
    public RitualEffectBurningHells(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        IPlayerProgression progression = MNACap.get((IPlayerProgression)context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return Component.translatable((String)"Progression could not be found...this is a problem.");
        }
        DemonLord tempBoss = new DemonLord(context.getLevel(), Vec3.atBottomCenterOf((Vec3i)context.getCenter()));
        if (!StructureUtils.isPointInStructure((ServerLevel)context.getLevel(), context.getCenter(), tempBoss.getArenaStructureID(), tempBoss.getArenaStructureSegment())) {
            if (progression.getTierProgress(context.getLevel()) < 1.0f) {
                return Component.translatable((String)"ritual.mna.progression.not_ready");
            }
            if (progression != null && progression.hasAlliedFaction() && progression.getAlliedFaction() != Factions.DEMONS) {
                return Component.translatable((String)"event.mna.faction_ritual_failed");
            }
        }
        return null;
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        BlockPos pos = context.getCenter();
        if (context.getCaster() == null || context.getCaster().getUUID() == null) {
            return false;
        }
        DemonStone e = new DemonStone((EntityType)EntityInit.DEMON_STONE.get(), context.getLevel());
        e.moveTo((float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f, 0.0f, 0.0f);
        e.setCasterUUID(context.getCaster().getUUID());
        DemonLord tempDemon = new DemonLord(context.getLevel());
        if (StructureUtils.isPointInStructure((ServerLevel)context.getLevel(), pos, tempDemon.getArenaStructureID(), tempDemon.getArenaStructureSegment())) {
            e.setSummonAsHostile();
        }
        context.getLevel().addFreshEntity((Entity)e);
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 200;
    }
}

