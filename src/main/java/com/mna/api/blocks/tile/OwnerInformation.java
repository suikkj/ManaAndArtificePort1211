/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.scores.Team
 */
package com.mna.api.blocks.tile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

public class OwnerInformation {
    private UUID owner = null;
    private String _cachedTeamName = null;
    private Player _cachedOwner = null;
    private Team _cachedTeam = null;

    public void setOwner(Player player) {
        if (player.getGameProfile() != null && player.getGameProfile().getId() != null) {
            this._cachedOwner = player;
            this.owner = player.getGameProfile().getId();
            this._cachedTeam = player.getTeam();
            this._cachedTeamName = player.getTeam() != null ? player.getTeam().getName() : "";
        }
    }

    @Nullable
    public Player getOwner(Level level) {
        if (!(this.owner == null || this._cachedOwner != null && this._cachedOwner.isAlive())) {
            this._cachedOwner = level.getPlayerByUUID(this.owner);
        }
        return this._cachedOwner;
    }

    @Nullable
    public Team getOwnerTeam(Level level) {
        Player owner = this.getOwner(level);
        if (owner != null) {
            this._cachedTeam = owner.getTeam();
        } else if (!(this._cachedTeamName == null || this._cachedTeam != null && this._cachedTeam.getName().equals(this._cachedTeamName))) {
            this._cachedTeam = level.getScoreboard().getPlayerTeam(this._cachedTeamName);
        }
        return this._cachedTeam;
    }

    public boolean isFriendlyTo(LivingEntity living) {
        if (living == this.getOwner(living.level())) {
            return true;
        }
        Team team = this.getOwnerTeam(living.level());
        return team != null && living.isAlliedTo(team);
    }

    public void save(CompoundTag tag) {
        CompoundTag ownerInfo = new CompoundTag();
        if (this.owner != null) {
            ownerInfo.putString("owner_uuid", this.owner.toString());
        }
        if (this._cachedTeamName != null) {
            ownerInfo.putString("owner_team", this._cachedTeamName);
        }
        tag.put("owner_info", (Tag)ownerInfo);
    }

    public void load(CompoundTag tag) {
        if (tag.contains("owner_info")) {
            CompoundTag ownerInfo = tag.getCompound("owner_info");
            if (ownerInfo.contains("owner_uuid")) {
                try {
                    this.owner = UUID.fromString(ownerInfo.getString("owner_uuid"));
                }
                catch (Throwable t) {
                    this.owner = null;
                }
            }
            if (ownerInfo.contains("owner_team")) {
                this._cachedTeamName = ownerInfo.getString("owner_team");
            }
        }
    }

    @Nullable
    public UUID getOwnerId() {
        return this.owner;
    }
}

