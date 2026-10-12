 
package com.mna.api.items;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.faction.IFaction;
import net.minecraft.world.entity.player.Player;
import com.mna.api.tools.MNACap;

public interface IFactionSpecific {
    default public IFaction getFaction() {
        return null;
    }

    default public float getMinIre() {
        return 0.0f;
    }

    default public float getMaxIre() {
        return 0.005f;
    }

    default public void usedByPlayer(Player player) {
        if (player.level().isClientSide() || this.getFaction() == null) {
            return;
        }
        MNACap.get(player, ManaAndArtificeMod.getProgressionCapability()).ifPresent(p -> {
            IFaction playerFaction = p.getAlliedFaction();
            if (playerFaction != null && playerFaction != this.getFaction() && !this.getFaction().isAlliedTo(playerFaction)) {
                p.incrementFactionAggro(this.getFaction(), this.getMinIre(), this.getMaxIre());
            }
        });
    }
}

