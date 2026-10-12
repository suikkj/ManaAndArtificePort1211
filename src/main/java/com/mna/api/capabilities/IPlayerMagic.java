/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec2
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 */
package com.mna.api.capabilities;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.ChronoAnchorData;
import com.mna.api.capabilities.IPlayerCantrips;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.resource.ICastingResource;
import com.mna.api.items.inventory.SpellInventory;
import com.mna.api.spells.base.ISpellDefinition;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public interface IPlayerMagic {
    public static final int FLAGS_CASTING_RESOURCE = 1;
    public static final int FLAGS_GRIMOIRE = 2;
    public static final int FLAGS_ROTE = 4;

    public void setModifierPressed(boolean var1);

    public boolean isModifierPressed();

    public boolean isMagicUnlocked();

    public void unlockMagic();

    public void copyFrom(IPlayerMagic var1);

    public int getMagicLevel();

    public void setMagicLevel(Player var1, int var2);

    public void magicLevelUp(Player var1, IPlayerProgression var2);

    public int getMagicXP();

    public int getXPForLevel(int var1);

    public void setMagicXP(int var1);

    public void addMagicXP(int var1, Player var2, IPlayerProgression var3);

    public float getAffinityDepth(Affinity var1);

    public Map<Affinity, Float> getSortedAffinityDepths();

    public void setAffinityDepth(Affinity var1, float var2);

    public void shiftAffinity(Player var1, Affinity var2, float var3);

    public ICastingResource getCastingResource();

    public void setCastingResourceType(ResourceLocation var1);

    public int getParticleColorOverride();

    public void setParticleColorOverride(int var1);

    public boolean canRegenerate(Player var1);

    public int getTeleportSalt();

    public void setTeleportSalt(int var1);

    public void resetTeleportSalt();

    public void tick(Player var1);

    public boolean needsSync();

    public void clearSyncFlags();

    public void forceSync();

    public void forceSync(int var1);

    public boolean shouldSyncGrimoire();

    public void setSyncGrimoire();

    public boolean shouldSyncRote();

    public void setSyncRote();

    public int getPortalCooldown();

    public void setPortalCooldown(int var1);

    public boolean getIsTeleporting();

    public void delayedTeleportTo(int var1, Vec3 var2, Vec2 var3, ServerLevel var4);

    public void cancelTeleport();

    public int getTeleportElapsedTicks();

    public int getTeleportTotalTicks();

    public void updateClientsideTeleportData(boolean var1, int var2, int var3);

    public void clearRememberedPoints();

    public void addRememberedPoint(Vector3f var1, Vector3f var2);

    public Vector3f[] getRememberedPoints();

    public Vector3f getAverageLook();

    public Vector3f[] getRememberedLooks();

    public SimpleContainer getRiftInventory();

    public SpellInventory getGrimoireInventory();

    public SpellInventory getRoteInventory();

    @Deprecated(forRemoval=true)
    default public int getAirCastLimit(Player player) {
        return this.getAirCastLimit(player, null);
    }

    public int getAirCastLimit(Player var1, @Nullable ISpellDefinition var2);

    public int getAirCasts();

    @Deprecated(forRemoval=true)
    default public void incrementAirCasts(Player player) {
        this.incrementAirCasts(player, null);
    }

    public void incrementAirCasts(Player var1, @Nullable ISpellDefinition var2);

    public void setAirCasts(int var1);

    public int getAirJumps();

    public void incrementAirJumps(Player var1);

    public void setAirJumps(int var1);

    public ChronoAnchorData getChronoAnchorData();

    public void setNeedsChronoExhaustion();

    public Vec3 getLiftPosition();

    public void setLiftPosition(Vec3 var1);

    public boolean didAllowFlying();

    public void setDidAllowFlying(boolean var1);

    public float getFocusDistance();

    public void setFocusDistance(float var1);

    public void offsetFocusDistance(float var1, float var2);

    public int getEmberCooldown();

    public void setEmberCooldown(int var1);

    public boolean getHadWizardSight();

    public void setHadWizardSight(boolean var1);

    public IPlayerCantrips getCantripData();

    public int bankArmorRepair(int var1, float var2);

    public HashMap<Integer, Float> getBankedArmorRepair();

    public void setBankedArmorRepair(HashMap<Integer, Float> var1);

    public void validate();
}

