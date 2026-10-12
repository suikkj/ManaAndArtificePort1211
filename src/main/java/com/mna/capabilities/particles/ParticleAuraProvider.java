 
package com.mna.capabilities.particles;

import com.mna.ManaAndArtifice;
import com.mna.capabilities.particles.ParticleAura;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import java.util.function.Supplier;

public class ParticleAuraProvider {
    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<com.mna.capabilities.particles.ParticleAura>> AURA = com.mna.capabilities.CapabilitiesInit.ENTITY_PARTICLE_AURA;
    private final ParticleAura holder = new ParticleAura();

    public Tag serializeNBT() {
        ParticleAura instance = (ParticleAura)this.holder;
        return instance.save();
    }

    public void deserializeNBT(Tag nbt) {
        ParticleAura instance = (ParticleAura)this.holder;
        if (nbt instanceof CompoundTag) {
            CompoundTag cnbt = (CompoundTag)nbt;
            instance.load(cnbt);
        } else {
            ManaAndArtifice.LOGGER.error("Aura NBT passed back not an instance of CompoundNBT - save data was NOT loaded!");
        }
    }
}


