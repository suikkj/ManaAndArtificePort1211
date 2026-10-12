 
package com.mna.capabilities.playerdata.magic;

import com.mna.api.cantrips.ICantrip;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.api.capabilities.IPlayerCantrip;
import com.mna.api.capabilities.IPlayerCantrips;
import com.mna.cantrips.Cantrip;
import com.mna.cantrips.CantripRegistry;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.capabilities.playerdata.magic.PlayerCantrip;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.inventory.InventoryCantrips;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class PlayerCantrips
implements IPlayerCantrips {
    private List<IPlayerCantrip> cantrips = new ArrayList<IPlayerCantrip>();
    private boolean _needsSync = true;

    public PlayerCantrips() {
        CantripRegistry.INSTANCE.getCantrips().forEach(c -> this.cantrips.add(new PlayerCantrip((ICantrip)c)));
    }

    @Override
    @Nullable
    public ICantrip matchAndCastCantrip(Player player, InteractionHand hand, List<Recipe<?>> recipes) {
        MutableInt playerTier = new MutableInt(1);
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> playerTier.setValue(p.getTier()));
        ArrayList<ManaweavingPattern> patterns = new ArrayList<ManaweavingPattern>();
        for (Recipe<?> recipe : recipes) {
            if (!(recipe instanceof ManaweavingPattern)) continue;
            patterns.add((ManaweavingPattern)recipe);
        }
        for (IPlayerCantrip cantrip : this.cantrips) {
            Optional<ICantrip> registeredCantrip;
            if (!cantrip.matches(patterns) || (registeredCantrip = CantripRegistry.INSTANCE.getCantrip(cantrip.getCantripID())) == null || !registeredCantrip.isPresent() || registeredCantrip.get().getTier() > playerTier.getValue()) continue;
            Cantrip resolvedCantrip = (Cantrip)registeredCantrip.get();
            IChunkMagic chunkMagic = MNACap.get((IChunkMagic)player.level().getChunkAt(player.blockPosition()), ChunkMagicProvider.MAGIC).orElse(null);
            if (chunkMagic != null) {
                chunkMagic.addResidualMagic(resolvedCantrip.getTier() * 5);
            }
            resolvedCantrip.callEffector(player, hand);
            return registeredCantrip.get();
        }
        return null;
    }

    @Override
    public CompoundTag writeToNBT(boolean patternsOnly) {
        CompoundTag nbt = new CompoundTag();
        this.cantrips.forEach(c -> nbt.put(c.getCantripID().toString(), (Tag)c.writeToNBT()));
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        this.cantrips.forEach(c -> {
            if (nbt.contains(c.getCantripID().toString())) {
                c.readFromNBT((CompoundTag)nbt.get(c.getCantripID().toString()));
            }
        });
    }

    @Override
    public void setPattern(ResourceLocation cantripID, List<ResourceLocation> manaweavingPatterns) {
        this.cantrips.stream().filter(c -> c.getCantripID().equals(cantripID)).findFirst().ifPresent(c -> {
            c.setPatterns(manaweavingPatterns);
            this._needsSync = true;
        });
    }

    @Override
    public Optional<IPlayerCantrip> getCantrip(ResourceLocation cantripID) {
        return this.cantrips.stream().filter(c -> c.getCantripID().equals(cantripID)).findFirst();
    }

    @Override
    public List<IPlayerCantrip> getCantrips() {
        return this.cantrips;
    }

    @Override
    public Container getAsInventory() {
        return new InventoryCantrips(this);
    }

    @Override
    public boolean needsSync() {
        return this._needsSync;
    }

    @Override
    public void clearSync() {
        this._needsSync = false;
    }

    @Override
    public void setNeedsSync() {
        this._needsSync = true;
    }
}

