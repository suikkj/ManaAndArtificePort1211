 
package com.mna.api.particles.parameters;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ParticleItemStack {
    private final ItemStack value;

    public ParticleItemStack(ItemStack stack) {
        this.value = stack;
    }

    public ParticleItemStack() {
        this.value = ItemStack.EMPTY;
    }

    public ItemStack value() {
        return this.value;
    }

    public String serialize() {
        return this.value.save(com.mna.api.tools.MNAItemData.provider()).toString();
    }

    @Nullable
    public static ParticleItemStack deserialize(String string) {
        if (string == null) {
            return null;
        }
        try {
            return new ParticleItemStack(ItemStack.EMPTY);
        }
        catch (Exception ex) {
            return null;
        }
    }

    @Nullable
    public static ParticleItemStack deserialize(FriendlyByteBuf packetBuffer) {
        try {
            if (packetBuffer.readBoolean()) {
                return new ParticleItemStack(ItemStack.parseOptional(ManaAndArtifice.instance.proxy.getClientWorld() == null ? net.minecraft.server.MinecraftServer.getServer().registryAccess() : ManaAndArtifice.instance.proxy.getClientWorld().registryAccess(), packetBuffer.readNbt()));
            }
        }
        catch (Exception ex) {
            return null;
        }
        return null;
    }

    public static void serialize(@Nullable ParticleItemStack inst, FriendlyByteBuf packetBuffer) {
        if (inst != null && !inst.value.isEmpty()) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeNbt(inst.value().serializeNBT());
        } else {
            packetBuffer.writeBoolean(false);
        }
    }
}

