 
package com.mna.api.blocks;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PlayerOwnershipRecord {
    private static final String NBT_DISPLAY_NAME = "displayName";
    private static final String NBT_PROFILE_ID = "profileid";
    private static final String NBT_TEAM = "team";
    private static final String NBT_FACTION_ID = "factionid";
    private Player player;
    private UUID playerProfileID;
    private String playerName;
    private String team;
    private IFaction faction;

    @Nullable
    public final Player getPlayer() {
        return this.player;
    }

    public final UUID getPlayerProfileID() {
        return this.playerProfileID;
    }

    public String getDisplayName() {
        return this.playerName;
    }

    @Nullable
    public final String getTeam() {
        return this.team;
    }

    public final IFaction getFaction() {
        return this.faction;
    }

    private PlayerOwnershipRecord(@Nullable Player player) {
        this.player = player;
        if (this.player == null) {
            this.playerProfileID = UUID.randomUUID();
        }
        this.refresh(null);
    }

    private PlayerOwnershipRecord(UUID profileID, String displayName, @Nullable String team, @Nullable ResourceLocation factionID) {
        this.player = null;
        this.playerProfileID = profileID;
        this.team = team;
        this.playerName = displayName;
        this.faction = factionID != null ? (IFaction)ManaAndArtificeMod.getFactionsRegistry().getValue(factionID) : null;
    }

    public static PlayerOwnershipRecord of(Player player) {
        return new PlayerOwnershipRecord(player);
    }

    public static PlayerOwnershipRecord of(@Nullable CompoundTag savedData) {
        UUID profileID = UUID.randomUUID();
        ResourceLocation factionID = null;
        String team = null;
        String playerName = "Unknown";
        if (savedData != null) {
            if (savedData.contains(NBT_PROFILE_ID)) {
                profileID = UUID.fromString(savedData.getString(NBT_PROFILE_ID));
            }
            if (savedData.contains(NBT_FACTION_ID)) {
                factionID = ResourceLocation.parse(savedData.getString(NBT_FACTION_ID));
            }
            if (savedData.contains(NBT_TEAM)) {
                team = savedData.getString(NBT_TEAM);
            }
            if (savedData.contains(NBT_DISPLAY_NAME)) {
                playerName = savedData.getString(NBT_DISPLAY_NAME);
            }
        }
        return new PlayerOwnershipRecord(profileID, playerName, team, factionID);
    }

    public CompoundTag save(@Nullable Level world) {
        if (world != null) {
            this.refresh(world);
        }
        CompoundTag outputTag = new CompoundTag();
        outputTag.putString(NBT_PROFILE_ID, this.playerProfileID.toString());
        outputTag.putString(NBT_DISPLAY_NAME, this.playerName);
        if (this.team != null) {
            outputTag.putString(NBT_TEAM, this.team);
        }
        if (this.faction != null) {
            ResourceLocation factionID = ManaAndArtificeMod.getFactionsRegistry().getKey(this.faction);
            outputTag.putString(NBT_FACTION_ID, factionID.toString());
        }
        return outputTag;
    }

    public void refresh(@Nullable Level world) {
        if (this.player == null && world != null) {
            this.player = world.getPlayerByUUID(this.playerProfileID);
        }
        if (this.player != null) {
            this.playerProfileID = this.player.getGameProfile().getId();
            this.team = this.player.getTeam() != null ? this.player.getTeam().getName() : null;
            this.playerName = this.player.getDisplayName().getString();
            IPlayerProgression progression = (IPlayerProgression)this.player.getData(PlayerProgressionProvider.PROGRESSION.get());
            this.faction = progression == null ? null : progression.getAlliedFaction();
        }
    }

    public boolean permits(Player player, boolean checkFaction, boolean checkTeam) {
        IPlayerProgression prog;
        if (player == null) {
            return false;
        }
        this.refresh(player.level());
        if (this.playerProfileID == null) {
            return false;
        }
        if (this.getPlayerProfileID().equals(player.getGameProfile().getId())) {
            return true;
        }
        if (checkTeam && this.team != null && player.getTeam() != null && player.getTeam().getName().equals(this.team)) {
            return true;
        }
        if (checkFaction && (prog = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get())) != null) {
            IFaction playerFaction = prog.getAlliedFaction();
            if (this.faction != null && playerFaction != null && this.faction.isAlliedTo(playerFaction)) {
                return true;
            }
        }
        return false;
    }
}

