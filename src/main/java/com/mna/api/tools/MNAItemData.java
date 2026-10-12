package com.mna.api.tools;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * [PORT 1.21.1] Helpers de acesso ao NBT de um {@link ItemStack}.
 *
 * No 1.20.1 o ItemStack tinha um CompoundTag direto ({@code getTag},
 * {@code setTag}, {@code getOrCreateTagElement}, {@code hasTag}). No 1.21.1 o NBT
 * custom mora dentro do data component {@link DataComponents#CUSTOM_DATA}, e os
 * metodos diretos foram removidos.
 *
 * Regra de ouro: usar estas helpers nos receptores ItemStack. BlockEntity /
 * TileEntity tem {@code getTag()} proprio e NAO deve ser convertido.
 */
public final class MNAItemData {
    private MNAItemData() {}

    /** Equivalente ao antigo {@code stack.getTag()} (null-safe: nunca nulo). */
    public static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /** Equivalente ao antigo {@code stack.getOrCreateTag()} (mutavel). */
    public static CompoundTag createTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return tag;
    }

    /** Equivalente ao antigo {@code stack.getOrCreateTagElement(key)}. */
    public static CompoundTag element(ItemStack stack, String key) {
        CompoundTag tag = createTag(stack);
        if (!tag.contains(key, 10)) {
            tag.put(key, new CompoundTag());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return tag.getCompound(key);
    }

    /** Equivalente ao antigo {@code stack.hasTag(key)}. */
    public static boolean has(ItemStack stack, String key) {
        return tag(stack).contains(key);
    }

    /** Equivalente ao antigo {@code stack.removeTag(key)}. */
    public static void remove(ItemStack stack, String key) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return;
        }
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
        tag.remove(key);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Provider de registries para serializacao (server ativo; fallback vazio). */
    public static net.minecraft.core.HolderLookup.Provider provider() {
        net.minecraft.server.MinecraftServer srv = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        return srv != null ? srv.registryAccess() : net.minecraft.core.RegistryAccess.EMPTY;
    }

    /** [PORT 1.21.1] ItemStack.save agora exige provider. */
    public static CompoundTag save(ItemStack stack, CompoundTag tag) {
        return stack.save(provider(), tag);
    }

    /** [PORT 1.21.1] ItemStack.load agora exige provider. */
    public static void load(ItemStack stack, CompoundTag tag) {
        stack.load(provider(), tag);
    }

    /** [PORT 1.21.1] ContainerHelper.loadAllItems agora exige provider (ultimo arg). */
    public static void loadItems(CompoundTag tag, net.minecraft.core.NonNullList<ItemStack> list) {
        net.minecraft.world.MNAItemData.loadItems(tag, list, provider());
    }

    /** [PORT 1.21.1] ContainerHelper.saveAllItems agora exige provider (ultimo arg). */
    public static CompoundTag saveItems(CompoundTag tag, net.minecraft.core.NonNullList<ItemStack> list) {
        return net.minecraft.world.MNAItemData.saveItems(tag, list, provider());
    }
}
