/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.faction;

import com.mna.api.faction.IFaction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface IRaidHelper {
    public boolean spawnRaidAt(Player var1, IFaction var2, int var3, Vec3 var4, boolean var5, MobEffectInstance ... var6);
}

