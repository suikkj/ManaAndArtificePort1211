 
package com.mna.api.blocks.tile;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.blocks.PotionPylonBlock;
import com.mna.api.blocks.tile.pylon.PylonTileBase;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class PotionEffectPylonTile
extends PylonTileBase {
    private ResourceLocation cachedIcon;

    public PotionEffectPylonTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public PotionEffectPylonTile(BlockPos pPos, BlockState pBlockState) {
        super(ManaAndArtificeMod.getTileHelper().getPotionPylonTileType(), pPos, pBlockState);
    }

    @Override
    protected PotionPylonBlock getBlock() {
        return (PotionPylonBlock)this.getBlockState().getBlock();
    }

    protected MobEffect getMobEffect() {
        return this.getBlock().getEffect();
    }

    @Override
    public ResourceLocation getIcon() {
        if (this.cachedIcon == null) {
            ResourceLocation effectKey = BuiltInRegistries.MOB_EFFECT.getKey(this.getMobEffect());
            this.cachedIcon = effectKey != null ? ResourceLocation.fromNamespaceAndPath(effectKey.getNamespace(), "textures/mob_effect/" + effectKey.getPath() + ".png") : ResourceLocation.parse("");
        }
        return this.cachedIcon;
    }

    @Override
    public int[] getColor() {
        int clrPacked = this.getMobEffect().getColor();
        if (clrPacked != 0) {
            return new int[]{FastColor.ARGB32.red((int)clrPacked), FastColor.ARGB32.green((int)clrPacked), FastColor.ARGB32.blue((int)clrPacked)};
        }
        return super.getColor();
    }

    protected int getAmplifier() {
        return this.getBlock().getAmplifier();
    }

    protected int getDuration() {
        return this.getBlock().getDuration();
    }

    protected int getReapplicationThreshold() {
        return this.getBlock().getReapplicationThreshold();
    }

    protected boolean applyAsAmbient() {
        return true;
    }

    protected boolean effectVisible() {
        return true;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, PotionEffectPylonTile tile) {
        if (tile.getMobEffect() == null) {
            return;
        }
        if (tile.tick()) {
            int n = tile.powerUpTicks;
            Objects.requireNonNull(tile);
            if (n == 60 && level.getGameTime() % 20L == 0L) {
                tile.forAllPlayers(false, p -> {
                    MobEffectInstance existing = p.getEffect(tile.getMobEffect());
                    if (existing == null || existing.endsWithin(tile.getReapplicationThreshold())) {
                        p.addEffect(new MobEffectInstance(tile.getMobEffect(), tile.getDuration(), tile.getAmplifier(), tile.applyAsAmbient(), false, tile.effectVisible()));
                    }
                });
            }
        }
    }
}


