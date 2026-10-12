/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 */
package com.mna.api.entities;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public interface ISummonHelper {
    public boolean checkIsSummon(Entity var1);

    @Nullable
    public LivingEntity resolveSummoner(LivingEntity var1);

    public boolean makeSummon(Mob var1, LivingEntity var2, int var3);

    public boolean makeSummon(Mob var1, LivingEntity var2, boolean var3, int var4);

    public List<Mob> getAllSummons(LivingEntity var1);

    public boolean isEntityFriendly(Entity var1, LivingEntity var2);

    public int getSummonCap(LivingEntity var1);

    public int getEmberCap(LivingEntity var1);

    public void setSummonBonus(LivingEntity var1, int var2);
}

