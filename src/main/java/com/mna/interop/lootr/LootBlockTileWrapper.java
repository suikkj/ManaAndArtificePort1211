package com.mna.interop.lootr;

import com.mna.interop.lootr.ILootrBridge;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

/**
 * Stub livre de Lootr (primeiro port/teste). A integração real com o mod Lootr
 * deve ser restaurada depois, nesta classe.
 */
public class LootBlockTileWrapper<T extends RandomizableContainerBlockEntity> {
    private T tile;

    public LootBlockTileWrapper(T tile) {
        this.tile = tile;
    }

    public T getTile() {
        return this.tile;
    }
}
