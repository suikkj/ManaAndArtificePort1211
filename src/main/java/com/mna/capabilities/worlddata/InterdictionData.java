 
package com.mna.capabilities.worlddata;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.blocks.PlayerOwnershipRecord;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class InterdictionData {
    private final PlayerOwnershipRecord owner;
    private final AABB bounds;
    private final boolean in;
    private final boolean affect_allied_factions;

    public InterdictionData(PlayerOwnershipRecord owner, AABB bounds, boolean in, boolean affect_allied_factions) {
        this.owner = owner;
        this.bounds = bounds;
        this.in = in;
        this.affect_allied_factions = affect_allied_factions;
    }

    public boolean getDirection() {
        return this.in;
    }

    public boolean isAlly(LivingEntity other) {
        if (this.owner == null) {
            return false;
        }
        this.owner.refresh(other.level());
        if (other instanceof IFactionEnemy) {
            IFactionEnemy factionMob = (IFactionEnemy)other;
            if (this.owner.getFaction() != null && factionMob.getFaction() == this.owner.getFaction()) {
                return true;
            }
        }
        if (other instanceof Player) {
            IPlayerProgression progression;
            Player player = (Player)other;
            if (this.owner.getPlayerProfileID().equals(player.getGameProfile().getId())) {
                return true;
            }
            if (this.owner.getTeam() != null && player.getTeam() != null && this.owner.getTeam() == player.getTeam().getName()) {
                return true;
            }
            if (!this.affect_allied_factions && (progression = (IPlayerProgression)MNACap.get(player, ManaAndArtificeMod.getProgressionCapability()).orElse(null)) != null) {
                IFaction ownerFaction = this.owner.getFaction();
                IFaction playerFaction = progression.getAlliedFaction();
                if (ownerFaction != null && playerFaction != null && ownerFaction.isAlliedTo(playerFaction)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean includes(Vec3 pos) {
        return this.bounds.contains(pos);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("owner", (Tag)this.owner.save(null));
        tag.putDouble("aabbcx", this.bounds.getCenter().x);
        tag.putDouble("aabbcy", this.bounds.getCenter().y);
        tag.putDouble("aabbcz", this.bounds.getCenter().z);
        tag.putDouble("aabbsx", this.bounds.getXsize());
        tag.putDouble("aabbsy", this.bounds.getYsize());
        tag.putDouble("aabbsz", this.bounds.getZsize());
        tag.putBoolean("in", this.in);
        tag.putBoolean("faction", this.affect_allied_factions);
        return tag;
    }

    public static InterdictionData of(CompoundTag tag) {
        PlayerOwnershipRecord rec = PlayerOwnershipRecord.of(tag.getCompound("owner"));
        AABB bounds = AABB.ofSize((Vec3)new Vec3(tag.getDouble("aabbcx"), tag.getDouble("aabbcy"), tag.getDouble("aabbcz")), (double)tag.getDouble("aabbsx"), (double)tag.getDouble("aabbsy"), (double)tag.getDouble("aabbsz"));
        return new InterdictionData(rec, bounds, tag.getBoolean("in"), tag.getBoolean("faction"));
    }
}

