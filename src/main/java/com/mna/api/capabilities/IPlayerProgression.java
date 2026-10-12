/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package com.mna.api.capabilities;

import com.mna.api.capabilities.CodexBreadcrumb;
import com.mna.api.faction.FactionDifficultyStats;
import com.mna.api.faction.IFaction;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IPlayerProgression {
    public static final String[] Tier_Health_Boost_IDs = new String[]{"e13eed85-2800-4e0f-adfe-888cc0bebe6f", "056cb5aa-0992-44fb-8dcc-432ecc160a84", "17fac1ea-b63c-424d-b30f-9721d84eacc7", "befe52c5-fd98-48fa-aaa9-8bfcb91a77ca", "71090d1d-f63a-4f39-aa69-a05599f9b0f3"};

    public void copyFrom(IPlayerProgression var1, Player var2);

    public int getTier();

    default public void setTier(int tier, @Nullable Player player) {
        this.setTier(tier, player, true);
    }

    public void setTier(int var1, @Nullable Player var2, boolean var3);

    public void addTierProgressionComplete(ResourceLocation var1);

    public void setTierProgression(List<ResourceLocation> var1);

    public float getTierProgress(Level var1);

    public List<ResourceLocation> getCompletedProgressionSteps();

    public int getTierMaxComplexity();

    public boolean hasAlliedFaction();

    @Nullable
    public IFaction getAlliedFaction();

    public void setAlliedFaction(@Nullable IFaction var1, @Nullable Player var2);

    public FactionDifficultyStats getFactionDifficultyStats(IFaction var1);

    public int getFactionStanding();

    public void increaseFactionStanding(int var1);

    public void setFactionStanding(int var1);

    public boolean needsSync();

    public void clearSyncFlag();

    public void setDirty();

    public boolean canBeRaided(Player var1);

    public boolean canBeRaided(IFaction var1, Player var2);

    public int getRelativeRaidStrength(IFaction var1, Player var2);

    public double getRaidChance(IFaction var1);

    public void setRaidChance(IFaction var1, double var2);

    public void raidImmediate(IFaction var1);

    public boolean hasForceRaid();

    public void clearForceRaid();

    @Nullable
    public IFaction getForceRaid();

    public void incrementFactionAggro(IFaction var1, float var2, float var3);

    @Nullable
    public CodexBreadcrumb popCodexBreadcrumb();

    @Nullable
    public CodexBreadcrumb peekCodexBreadcrumb();

    public void pushCodexBreadcrumb(CodexBreadcrumb.Type var1, String var2, int var3, String ... var4);

    public void clearCodexEntryHistory();

    public int breadcrumbLength();

    public void tickClassicRaids(Player var1);

    public int getAllyCooldown();

    public void summonRandomAlly(Player var1);

    public void setAllyCooldown(int var1);

    public boolean performedInitialSync();

    public void setHasSynced();

    public boolean isCodexEntryUnlocked(long var1);

    public boolean isCodexEntryRead(long var1);

    public void setCodexEntryUnlocked(long var1);

    public boolean setCodexEntryRead(long var1);

    public void resetCodexUnlocks();

    public CompoundTag saveCodexUnlocks();

    public void loadCodexUnlocks(CompoundTag var1);

    public void loadCodexUnlocks(long[] var1, byte[] var2);

    public int unlockedEntryCount();

    public void setEntriesUnlocked(long[] var1);
}

