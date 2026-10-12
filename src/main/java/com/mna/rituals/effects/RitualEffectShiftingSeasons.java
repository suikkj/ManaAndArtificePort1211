 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.factions.Factions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mna.api.tools.MNACap;

public class RitualEffectShiftingSeasons
extends RitualEffect {
    public RitualEffectShiftingSeasons(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        IPlayerProgression progression = MNACap.get((IPlayerProgression)context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return Component.literal((String)"Progression could not be found...this is a problem.");
        }
        if (progression.getTier() > 2 && progression.getAlliedFaction() != Factions.FEY) {
            return Component.translatable((String)"ritual.mna.shifting_seasons.wrong_faction");
        }
        return null;
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        boolean winter;
        if (context.getCaster() == null || context.getCaster().getUUID() == null) {
            return false;
        }
        IPlayerProgression progression = MNACap.get((IPlayerProgression)context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        IPlayerMagic magic = MNACap.get((IPlayerMagic)context.getCaster(), PlayerMagicProvider.MAGIC).orElse(null);
        if (progression == null || magic == null) {
            context.getCaster().sendSystemMessage((Component)Component.literal((String)"Progression or Magic capabilities are missing.  Aborting ritual to prevent a crash.  You should save and quit, something is wrong."));
            return false;
        }
        if (progression != null && progression.hasAlliedFaction() && progression.getAlliedFaction() != Factions.FEY) {
            context.getCaster().sendSystemMessage((Component)Component.translatable((String)"ritual.mna.shifting_seasons.wrong_faction"));
            return false;
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
        magic.setCastingResourceType(Factions.FEY.getCastingResource(context.getCaster()));
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 60;
    }
}

