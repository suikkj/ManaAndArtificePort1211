/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.apibridge;

import com.mna.api.faction.IFaction;
import com.mna.api.faction.IRaidHelper;
import com.mna.entities.faction.util.FactionRaid;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class FactionRaidHelper
implements IRaidHelper {
    @Override
    public boolean spawnRaidAt(Player target, IFaction faction, int strength, Vec3 position, boolean friendly, MobEffectInstance ... effects) {
        if (target.level().isClientSide()) {
            return false;
        }
        FactionRaid fre = new FactionRaid(target.level(), target, strength);
        fre.setFaction(faction);
        fre.setPos(position.x, position.y, position.z);
        if (friendly) {
            fre.setProtective(target);
        }
        if (effects != null && effects.length > 0) {
            fre.setAdditionalEffects(effects);
        }
        target.level().addFreshEntity((Entity)fre);
        return true;
    }
}

