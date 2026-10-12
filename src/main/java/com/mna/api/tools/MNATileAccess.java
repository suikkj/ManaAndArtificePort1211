package com.mna.api.tools;

/**
 * [PORT 1.21.1] BlockEntity.loadAdditional virou protected - ponte reflexiva.
 */
public final class MNATileAccess {

    private MNATileAccess() {
    }

    public static void loadAdditional(net.minecraft.world.level.block.entity.BlockEntity be, net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        try {
            java.lang.reflect.Method m = net.minecraft.world.level.block.entity.BlockEntity.class.getDeclaredMethod("loadAdditional", net.minecraft.nbt.CompoundTag.class, net.minecraft.core.HolderLookup.Provider.class);
            m.setAccessible(true);
            m.invoke(be, tag, provider);
        } catch (Throwable t) {
        }
    }
}
