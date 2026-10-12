 
package com.mna.capabilities.chunkdata;

import com.mna.api.capabilities.IChunkMagic;
import com.mna.tools.BlockUtils;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class ChunkMagic
implements IChunkMagic {
    private float residualMagic = 0.0f;
    private HashSet<Long> knownSuppliers = new HashSet();
    private HashMap<Long, Pair<UUID, ResourceLocation>> wards = new HashMap();

    @Override
    public void addResidualMagic(float amount) {
        this.residualMagic += amount;
    }

    @Override
    public void removeResidualMagic(float amount) {
        this.residualMagic -= amount;
        if (this.residualMagic < 0.0f) {
            this.residualMagic = 0.0f;
        }
    }

    @Override
    public float getResidualMagic() {
        return this.residualMagic;
    }

    @Override
    public void setResidualMagic(float amount) {
        this.residualMagic = amount;
    }

    @Override
    public void pushKnownEldrinSupplier(BlockPos pos) {
        this.knownSuppliers.add(pos.asLong());
    }

    @Override
    public void popKnownEldrinSupplier(BlockPos pos) {
        this.knownSuppliers.remove(pos.asLong());
    }

    @Override
    public List<Long> getKnownEldrinSuppliers() {
        return this.knownSuppliers.stream().collect(Collectors.toList());
    }

    @Override
    public boolean blockInteractionFor(BlockPos pos, @Nullable Player player) {
        Pair ownershipData = this.wards.getOrDefault(pos.asLong(), null);
        if (ownershipData == null) {
            return false;
        }
        if (player == null) {
            return true;
        }
        if (((UUID)ownershipData.getFirst()).equals(player.getGameProfile().getId())) {
            return false;
        }
        if (ownershipData.getSecond() == null) {
            return true;
        }
        ResourceLocation mhStackType = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem());
        ResourceLocation ohStackType = BuiltInRegistries.ITEM.getKey(player.getOffhandItem().getItem());
        return !((ResourceLocation)ownershipData.getSecond()).equals(mhStackType) && !((ResourceLocation)ownershipData.getSecond()).equals(ohStackType);
    }

    @Override
    public boolean wardBlock(BlockPos pos, Player warder, ItemStack wardStack) {
        if (!BlockUtils.canDestroyBlock((LivingEntity)warder, warder.level(), pos, (Tier)Tiers.NETHERITE)) {
            return false;
        }
        if (this.blockInteractionFor(pos, warder)) {
            return false;
        }
        long posKey = pos.asLong();
        if (this.wards.containsKey(posKey)) {
            return false;
        }
        ResourceLocation wardStackType = wardStack.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(wardStack.getItem());
        this.wards.put(posKey, (Pair<UUID, ResourceLocation>)new Pair(warder.getGameProfile().getId(), wardStackType));
        warder.level().getChunk(pos).setUnsaved(true);
        return true;
    }

    @Override
    public boolean removeBlockWard(BlockPos pos, Player warder) {
        if (this.blockInteractionFor(pos, warder)) {
            return false;
        }
        long posKey = pos.asLong();
        if (this.wards.containsKey(posKey)) {
            this.wards.remove(posKey);
            warder.level().getChunk(pos).setUnsaved(true);
            return true;
        }
        return false;
    }

    @Override
    public void removeBlockWard(BlockPos pos) {
        this.wards.remove(pos.asLong());
    }

    @Override
    public HashMap<Long, Pair<UUID, ResourceLocation>> getWards() {
        return this.wards;
    }

    @Override
    public void setWards(HashMap<Long, Pair<UUID, ResourceLocation>> wardData) {
        this.wards = wardData;
    }

    @Override
    public boolean isWarded(long blockPos) {
        return this.wards.containsKey(blockPos);
    }
}


