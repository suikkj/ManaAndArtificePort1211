 
package com.mna.entities.utility;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DisplayReagents
extends Entity {
    public static final int DISPLAY_SWITCH_FREQUENCY = 20;
    private static final int MAX_AGE = 200;
    private static final EntityDataAccessor<CompoundTag> RESOURCE_LOCATIONS = SynchedEntityData.defineId(DisplayReagents.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final String NBT_ENTITY_AGE = "entity_age";
    private static final String NBT_LOCATIONS_COUNT = "locations_count";
    private static final String NBT_LOCATIONS_PREFIX = "location_";
    private int age = 0;

    public DisplayReagents(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public void tick() {
        this.setAge(this.getAge() + 1);
        if (this.getAge() >= 200) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void setResourceLocations(List<ResourceLocation> list) {
        CompoundTag data = new CompoundTag();
        this.writeNBT(data, list);
        this.entityData.set(RESOURCE_LOCATIONS, data);
    }

    public List<ResourceLocation> getResourceLocations() {
        CompoundTag data = (CompoundTag)this.entityData.get(RESOURCE_LOCATIONS);
        return this.readNBT(data);
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(RESOURCE_LOCATIONS, new CompoundTag());
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains(NBT_ENTITY_AGE)) {
            this.setAge(compound.getInt(NBT_ENTITY_AGE));
        }
        this.setResourceLocations(this.readNBT(compound));
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt(NBT_ENTITY_AGE, this.getAge());
        List<ResourceLocation> locations = this.getResourceLocations();
        this.writeNBT(compound, locations);
    }

    private List<ResourceLocation> readNBT(CompoundTag compound) {
        ArrayList<ResourceLocation> locations = new ArrayList<ResourceLocation>();
        if (!compound.contains(NBT_LOCATIONS_COUNT)) {
            return locations;
        }
        int count = compound.getInt(NBT_LOCATIONS_COUNT);
        for (int i = 0; i < count; ++i) {
            if (!compound.contains(NBT_LOCATIONS_PREFIX + i)) continue;
            locations.add(ResourceLocation.parse(compound.getString(NBT_LOCATIONS_PREFIX + i)));
        }
        return locations;
    }

    private void writeNBT(CompoundTag compound, List<ResourceLocation> locations) {
        compound.putInt(NBT_LOCATIONS_COUNT, locations.size());
        for (int i = 0; i < locations.size(); ++i) {
            compound.putString(NBT_LOCATIONS_PREFIX + i, locations.get(i).toString());
        }
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}


