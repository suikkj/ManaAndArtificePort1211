 
package com.mna.capabilities.worlddata;

import com.google.common.collect.ImmutableList;
import com.mna.api.capabilities.IRitualTeleportLocation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class RitualTeleportLocation
implements IRitualTeleportLocation {
    private static final String KEY_POSITION_X = "position_x";
    private static final String KEY_POSITION_Y = "position_y";
    private static final String KEY_POSITION_Z = "position_z";
    private static final String KEY_WORLD_REG = "world_registry_key";
    private static final String KEY_WORLD_REGTYPE = "world_registry_key_regtype";
    private static final String KEY_DIRECTION = "direction";
    private static final String KEY_REAGENTS = "reagents";
    private static final String KEY_REAGENT_COUNT = "reagent_count";
    private static final String KEY_REAGENT_PREFIX = "reagent_";
    private final BlockPos pos;
    private final Direction direction;
    private final ImmutableList<ResourceLocation> reagents;
    private ResourceKey<Level> worldType;

    public RitualTeleportLocation(ResourceKey<Level> worldType, BlockPos position, List<ResourceLocation> reagents, Direction direction) {
        this.pos = position;
        this.reagents = ImmutableList.copyOf(reagents);
        this.direction = direction;
        this.worldType = worldType;
    }

    @Override
    public boolean matches(List<ResourceLocation> reagentList) {
        if (reagentList == null || reagentList.size() != this.reagents.size()) {
            return false;
        }
        for (int i = 0; i < reagentList.size(); ++i) {
            ResourceLocation internal = (ResourceLocation)this.reagents.get(i);
            ResourceLocation external = reagentList.get(i);
            if (external == null) {
                return false;
            }
            if (internal.toString().equals(external.toString())) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        return this.pos.getX() == pos.getX() && this.pos.getY() == pos.getY() && this.pos.getZ() == pos.getZ();
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.putFloat(KEY_DIRECTION, this.direction.toYRot());
        nbt.putInt(KEY_POSITION_X, this.pos.getX());
        nbt.putInt(KEY_POSITION_Y, this.pos.getY());
        nbt.putInt(KEY_POSITION_Z, this.pos.getZ());
        nbt.putInt(KEY_REAGENT_COUNT, this.reagents.size());
        nbt.putString(KEY_WORLD_REG, this.worldType.location().toString());
        nbt.putString(KEY_WORLD_REGTYPE, this.worldType.location().toString());
        CompoundTag reagentData = new CompoundTag();
        for (int i = 0; i < this.reagents.size(); ++i) {
            reagentData.putString(KEY_REAGENT_PREFIX + i, ((ResourceLocation)this.reagents.get(i)).toString());
        }
        nbt.put(KEY_REAGENTS, (Tag)reagentData);
    }

    @Nullable
    public static RitualTeleportLocation fromNBT(CompoundTag nbt) {
        Direction direction;
        if (!nbt.contains(KEY_WORLD_REG) || !nbt.contains(KEY_WORLD_REGTYPE)) {
            return null;
        }
        ResourceLocation regType = ResourceLocation.parse(nbt.getString(KEY_WORLD_REGTYPE));
        ResourceKey key = ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)regType);
        Direction direction2 = direction = nbt.contains(KEY_DIRECTION) ? Direction.fromYRot((double)nbt.getFloat(KEY_DIRECTION)) : Direction.NORTH;
        if (!(nbt.contains(KEY_POSITION_X) && nbt.contains(KEY_POSITION_Y) && nbt.contains(KEY_POSITION_Z))) {
            return null;
        }
        BlockPos position = new BlockPos(nbt.getInt(KEY_POSITION_X), nbt.getInt(KEY_POSITION_Y), nbt.getInt(KEY_POSITION_Z));
        ArrayList<ResourceLocation> reagents = new ArrayList<ResourceLocation>();
        if (nbt.contains(KEY_REAGENT_COUNT) && nbt.contains(KEY_REAGENTS)) {
            int count = nbt.getInt(KEY_REAGENT_COUNT);
            CompoundTag subNBT = nbt.getCompound(KEY_REAGENTS);
            for (int i = 0; i < count; ++i) {
                if (!subNBT.contains(KEY_REAGENT_PREFIX + i)) {
                    return null;
                }
                reagents.add(ResourceLocation.parse(subNBT.getString(KEY_REAGENT_PREFIX + i)));
            }
        } else {
            return null;
        }
        return new RitualTeleportLocation((ResourceKey<Level>)key, position, reagents, direction);
    }

    @Override
    public ImmutableList<ResourceLocation> getReagents() {
        return this.reagents;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public ResourceKey<Level> getWorldType() {
        return this.worldType;
    }

    @Override
    public void tryCorrectWorldKey(ResourceKey<Level> world) {
        if (this.worldType.toString().equals(world.toString())) {
            this.worldType = world;
        }
    }
}

