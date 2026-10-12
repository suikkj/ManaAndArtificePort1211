 
package com.mna.capabilities.entity;

import com.mna.capabilities.entity.MAPFX;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import java.util.function.Supplier;

public class MAPFXProvider {
    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<com.mna.capabilities.entity.MAPFX>> MAPFX = com.mna.capabilities.CapabilitiesInit.ENTITY_MAPFX;
    private final MAPFX holder = new MAPFX();

    public Tag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("flags", ((MAPFX)this.holder).getFlags());
        return nbt;
    }

    public void deserializeNBT(Tag nbt) {
        CompoundTag cnbt;
        if (nbt instanceof CompoundTag && (cnbt = (CompoundTag)nbt).contains("flags")) {
            ((MAPFX)this.holder).setFlags(cnbt.getLong("flags"));
        }
    }
}


