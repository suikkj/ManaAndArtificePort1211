 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.FaerieQueen;
import com.mna.entities.rituals.FeyLight;
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

public class RitualEffectFaerieCourts
extends RitualEffect {
    public RitualEffectFaerieCourts(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        IPlayerProgression progression = MNACap.get((IPlayerProgression)context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return Component.literal((String)"Progression could not be found...this is a problem.");
        }
        FaerieQueen tempBoss = new FaerieQueen(context.getLevel(), Vec3.atBottomCenterOf((Vec3i)context.getCenter()), true);
        if (!StructureUtils.isPointInStructure((ServerLevel)context.getLevel(), context.getCenter(), tempBoss.getArenaStructureID(), tempBoss.getArenaStructureSegment())) {
            if (progression.getTierProgress(context.getLevel()) < 1.0f) {
                return Component.translatable((String)"ritual.mna.progression.not_ready");
            }
            if (progression != null && progression.hasAlliedFaction() && progression.getAlliedFaction() != Factions.FEY) {
                return Component.translatable((String)"event.mna.faction_ritual_failed");
            }
        }
        return null;
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        boolean winter;
        if (context.getCaster() == null || context.getCaster().getUUID() == null) {
            return false;
        }
        FaerieQueen summerQueen = new FaerieQueen(context.getLevel(), Vec3.atBottomCenterOf((Vec3i)context.getCenter()).add(3.0, 0.0, 0.0), false);
        if (StructureUtils.isPointInStructure((ServerLevel)context.getLevel(), context.getCenter(), summerQueen.getArenaStructureID(), summerQueen.getArenaStructureSegment())) {
            FaerieQueen winterQueen = new FaerieQueen(context.getLevel(), Vec3.atBottomCenterOf((Vec3i)context.getCenter()).add(-3.0, 0.0, 0.0), true);
            summerQueen.setupSpawn();
            winterQueen.setupSpawn();
            context.getLevel().addFreshEntity((Entity)summerQueen);
            context.getLevel().addFreshEntity((Entity)winterQueen);
            return true;
        }
        boolean summer = context.getCollectedReagents(i -> MATags.isItemIn(i.getItem(), MATags.Items.Ritual.SUMMER_FLOWERS)).size() > 0;
        boolean bl = winter = context.getCollectedReagents(i -> MATags.isItemIn(i.getItem(), MATags.Items.Ritual.WINTER_FLOWERS)).size() > 0;
        if (!summer && !winter) {
            context.getCaster().sendSystemMessage((Component)Component.translatable((String)"event.mna.fey_ritual_no_decider"));
            return false;
        }
        if (summer && winter) {
            context.getCaster().sendSystemMessage((Component)Component.translatable((String)"event.mna.fey_ritual_conflicting_decider"));
            return false;
        }
        if (summer && !winter) {
            context.getCaster().getPersistentData().putInt("faction_casting_resource_idx", 0);
        } else {
            context.getCaster().getPersistentData().putInt("faction_casting_resource_idx", 1);
        }
        FeyLight e = (FeyLight)((EntityType)EntityInit.FEY_LIGHT.get()).spawn((ServerLevel)context.getLevel(), null, context.getCaster(), context.getCenter().above(12), MobSpawnType.EVENT, false, false);
        e.setTargetPos(context.getCenter().above(3));
        e.setCasterUUID(context.getCaster().getUUID());
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 140;
    }
}

