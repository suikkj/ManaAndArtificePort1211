package com.mna.api.tools;

import java.lang.reflect.Field;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * [PORT 1.21.1] {@code FallingBlockEntity.blockState} virou {@code private}.
 * Acesso por reflexao (ha getter publico para leitura, mas nao setter).
 */
public final class MNAFallingBlocks {
    private static final Field FIELD = field();

    private MNAFallingBlocks() {
    }

    private static Field field() {
        try {
            Field f = FallingBlockEntity.class.getDeclaredField("blockState");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[MNA] campo nao encontrado: FallingBlockEntity.blockState", e);
        }
    }

    public static void setBlockState(FallingBlockEntity entity, BlockState state) {
        try {
            FIELD.set(entity, state);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static BlockState getBlockState(FallingBlockEntity entity) {
        try {
            return (BlockState)FIELD.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
