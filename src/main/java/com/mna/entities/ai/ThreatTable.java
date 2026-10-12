/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  org.apache.commons.lang3.mutable.MutableFloat
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package com.mna.entities.ai;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class ThreatTable {
    private static final int THREAT_STEAL_THRESHOLD = 10;
    private Map<Integer, Float> threat = Maps.newHashMap();
    private Predicate<Entity> threatAddPredicate = null;

    public ThreatTable() {
        this(null);
    }

    public ThreatTable(Predicate<Entity> threatAdd) {
        this.threatAddPredicate = threatAdd;
    }

    public void addThreat(LivingEntity attacker, float amount, LivingEntity currentTarget) {
        if (attacker == null || this.threatAddPredicate != null && !this.threatAddPredicate.test((Entity)attacker)) {
            return;
        }
        float newThreat = this.threat.getOrDefault(attacker.getId(), Float.valueOf(0.0f)).floatValue() + amount;
        this.threat.put(attacker.getId(), Float.valueOf(newThreat));
    }

    public void initializeThreat(LivingEntity attacker) {
        if (attacker == null) {
            return;
        }
        if (!this.threat.containsKey(attacker.getId())) {
            this.threat.put(attacker.getId(), Float.valueOf(1.0f));
        }
    }

    public boolean shouldSwitchTarget(LivingEntity currentTarget, LivingEntity potentialTarget) {
        if (currentTarget == null || !currentTarget.isAlive()) {
            return true;
        }
        if (potentialTarget == null || !potentialTarget.isAlive()) {
            return false;
        }
        if (currentTarget.getId() == potentialTarget.getId()) {
            return false;
        }
        float currentThreat = this.threat.getOrDefault(currentTarget.getId(), Float.valueOf(0.0f)).floatValue();
        float targetThreat = this.threat.getOrDefault(potentialTarget.getId(), Float.valueOf(0.0f)).floatValue();
        return targetThreat >= currentThreat + 10.0f;
    }

    public int getRandomThreatEntry(Level world, LivingEntity owner, double maxTargetDist, @Nullable Predicate<LivingEntity> selectionPredicate) {
        ArrayList allKeys = new ArrayList();
        ArrayList<Integer> toRemove = new ArrayList<Integer>();
        double maxDistSquared = maxTargetDist * maxTargetDist;
        this.threat.keySet().forEach((Integer entityID) -> {
            Entity e = world.getEntity(entityID.intValue());
            if (e == null || !e.isAlive() || !(e instanceof LivingEntity) || selectionPredicate != null && !selectionPredicate.test((LivingEntity)e)) {
                toRemove.add((Integer)entityID);
                return;
            }
            if (e.position().distanceToSqr(owner.position()) < maxDistSquared) {
                allKeys.add(entityID);
            }
        });
        toRemove.forEach((Integer i) -> this.threat.remove(i));
        if (allKeys.size() == 0) {
            return -1;
        }
        return (Integer)allKeys.get((int)(Math.random() * (double)allKeys.size()));
    }

    @Nullable
    private Pair<Integer, Float> getHighestThreatTarget() {
        if (this.threat.size() == 0) {
            return null;
        }
        MutableInt highestID = new MutableInt(-1);
        MutableFloat highestThreat = new MutableFloat(-1.0f);
        this.threat.forEach((Integer k, Float v) -> {
            if (highestID.getValue() == -1 || highestThreat.getValue().floatValue() < v.floatValue()) {
                highestID.setValue((Number)k);
                highestThreat.setValue((Number)v);
            }
        });
        return new Pair(highestID.getValue(), highestThreat.getValue());
    }

    public void forEach(BiConsumer<Integer, Float> callback) {
        this.threat.forEach(callback);
    }

    public boolean isOn(LivingEntity e) {
        return this.threat.containsKey(e.getId());
    }

    public int size() {
        return this.threat.size();
    }

    public ArrayList<Player> players(Level world) {
        ArrayList<Player> players = new ArrayList<Player>();
        this.threat.forEach((Integer entityID, Float threat) -> {
            Entity e = world.getEntity(entityID.intValue());
            if (e != null && e.isAlive() && e instanceof Player) {
                players.add((Player)e);
            }
        });
        return players;
    }

    public void remove(LivingEntity target) {
        if (target != null) {
            this.threat.remove(target.getId());
        }
    }
}

