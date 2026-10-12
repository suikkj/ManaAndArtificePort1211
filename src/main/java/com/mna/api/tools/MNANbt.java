package com.mna.api.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * [PORT 1.21.1] Ajudantes de NBT para formatos legados.
 */
public final class MNANbt {
    private MNANbt() {}

    /** Le BlockPos no formato antigo {X,Y,Z} (pre-1.21). */
    public static BlockPos readBlockPosLegacy(CompoundTag tag) {
        if (tag == null || !tag.contains("X")) {
            return null;
        }
        return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }
}
