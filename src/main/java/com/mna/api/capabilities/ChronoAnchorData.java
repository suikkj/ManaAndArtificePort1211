 
package com.mna.api.capabilities;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ChronoAnchorData {
    private float health;
    private float mana;
    private int hunger;
    private BlockPos position;
    private ResourceKey<Level> dimension;
    private boolean isValid = false;

    public float getHealth() {
        return this.health;
    }

    public float getMana() {
        return this.mana;
    }

    public int getHunger() {
        return this.hunger;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    protected void setHealth(float health) {
        this.health = health;
    }

    protected void setMana(float mana) {
        this.mana = mana;
    }

    protected void setHunger(int hunger) {
        this.hunger = hunger;
    }

    protected void setPosition(BlockPos position, ResourceKey<Level> dimension) {
        this.position = position;
        this.dimension = dimension;
    }

    public boolean canRevert(Player player) {
        return this.dimension != null && this.dimension.compareTo(player.level().dimension()) == 0;
    }

    public void fromPlayer(Player player) {
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        this.setHealth(player.getHealth());
        this.setMana(magic == null ? 0.0f : magic.getCastingResource().getAmount());
        this.setPosition(player.blockPosition(), (ResourceKey<Level>)player.level().dimension());
        this.setHunger(player.getFoodData().getFoodLevel());
        this.isValid = true;
    }

    public void revert(Player player) {
        if (!this.isValid) {
            return;
        }
        if (!this.canRevert(player)) {
            return;
        }
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        if (magic != null) {
            magic.getCastingResource().setAmount(this.getMana());
        }
        player.setHealth(this.getHealth());
        player.getFoodData().setFoodLevel(this.getHunger());
        player.teleportTo((double)this.getPosition().getX(), (double)this.getPosition().getY(), (double)this.getPosition().getZ());
        player.fallDistance = 0.0f;
        this.isValid = false;
    }

    public void writeToNBT(CompoundTag compound) {
        if (!this.isValid) {
            return;
        }
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("health", this.getHealth());
        nbt.putFloat("mana", this.getMana());
        nbt.putInt("hunger", this.getHunger());
        nbt.putInt("x", this.getPosition().getX());
        nbt.putInt("y", this.getPosition().getY());
        nbt.putInt("z", this.getPosition().getZ());
        nbt.putString("dimension_key_value", this.dimension.location().toString());
        compound.put("chrono_anchor_data", (Tag)nbt);
    }

    public void readFromNBT(CompoundTag compound) {
        CompoundTag nbt;
        if (compound.contains("chrono_anchor_data") && (nbt = compound.getCompound("chrono_anchor_data")).contains("health") && nbt.contains("mana") && nbt.contains("hunger") && nbt.contains("x") && nbt.contains("y") && nbt.contains("z") && nbt.contains("dimension_key_value")) {
            this.setHealth(nbt.getFloat("health"));
            this.setMana(nbt.getFloat("mana"));
            this.setHunger(nbt.getInt("hunger"));
            BlockPos position = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
            ResourceKey dimension = ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)ResourceLocation.parse(nbt.getString("dimension_key_value")));
            this.setPosition(position, (ResourceKey<Level>)dimension);
            this.isValid = true;
        }
    }
}

