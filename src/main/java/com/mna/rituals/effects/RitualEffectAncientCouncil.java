 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.CouncilWarden;
import com.mna.entities.rituals.AncientCouncil;
import com.mna.factions.Factions;
import com.mna.tools.StructureUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class RitualEffectAncientCouncil
extends RitualEffect {
    public RitualEffectAncientCouncil(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        IPlayerProgression progression = MNACap.get((IPlayerProgression)context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return Component.literal((String)"Progression could not be found...this is a problem.");
        }
        CouncilWarden tempBoss = new CouncilWarden(context.getLevel(), Vec3.atBottomCenterOf((Vec3i)context.getCenter()));
        if (!StructureUtils.isPointInStructure((ServerLevel)context.getLevel(), context.getCenter(), tempBoss.getArenaStructureID(), tempBoss.getArenaStructureSegment())) {
            if (progression.getTierProgress(context.getLevel()) < 1.0f) {
                return Component.translatable((String)"ritual.mna.progression.not_ready");
            }
            if (progression != null && progression.hasAlliedFaction() && progression.getAlliedFaction() != Factions.COUNCIL) {
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
        CouncilWarden warden = new CouncilWarden(context.getLevel(), Vec3.atBottomCenterOf((Vec3i)context.getCenter()));
        if (StructureUtils.isPointInStructure((ServerLevel)context.getLevel(), context.getCenter(), warden.getArenaStructureID(), warden.getArenaStructureSegment())) {
            context.getLevel().addFreshEntity((Entity)warden);
        } else {
            AncientCouncil e = (AncientCouncil)((EntityType)EntityInit.ANCIENT_COUNCIL.get()).spawn((ServerLevel)context.getLevel(), null, context.getCaster(), context.getCenter(), MobSpawnType.EVENT, false, false);
            e.setCasterUUID(context.getCaster().getUUID());
        }
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 100;
    }
}

