package com.mna.api.tools;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/** [PORT 1.21.1] Ponte para as capabilities padrao do NeoForge (substitui BlockEntity.getCapability). */
public final class MNACaps {

    private MNACaps() {
    }

    /** Item handler de um BlockEntity (lado opcional). */
    public static Optional<IItemHandler> itemHandler(BlockEntity be, Direction side) {
        if (be == null || be.getLevel() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(Capabilities.ItemHandler.BLOCK.getCapability(be.getLevel(), be.getBlockPos(), null, be, side));
    }
}
