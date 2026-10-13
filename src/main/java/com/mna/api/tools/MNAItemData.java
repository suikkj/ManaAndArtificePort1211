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

    /** Equivalente ao antigo {@code com.mna.api.tools.MNAItemData.getOrCreateTagElement(stack, key)}. */
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

    /** [PORT 1.21.1] ItemStack.save agora exige provider E lanca "Cannot encode empty ItemStack";
     *  no 1.20.1 salvar um stack vazio apenas retornava o tag intacto. */
    public static CompoundTag save(ItemStack stack, CompoundTag tag) {
        if (stack == null || stack.isEmpty()) {
            return tag;
        }
        return (CompoundTag)stack.save(provider(), tag);
    }

    /** [PORT 1.21.1] ContainerHelper.loadAllItems agora exige provider (ultimo arg). */
    public static void loadItems(CompoundTag tag, net.minecraft.core.NonNullList<ItemStack> list) {
        net.minecraft.world.ContainerHelper.loadAllItems(tag, list, provider());
    }

    /** [PORT 1.21.1] ContainerHelper.saveAllItems agora exige provider (ultimo arg). */
    public static CompoundTag saveItems(CompoundTag tag, net.minecraft.core.NonNullList<ItemStack> list) {
        return net.minecraft.world.ContainerHelper.saveAllItems(tag, list, provider());
    }

    /** Equivalente ao antigo {@code stack.getOrCreateTagElement(key)} (escritas persistem). */
    public static CompoundTag getOrCreateTagElement(ItemStack stack, String key) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        CompoundTag element = tag.contains(key) ? tag.getCompound(key) : new CompoundTag();
        tag.put(key, element);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return element;
    }

    /** [PORT 1.21.1] ItemStack.set(...) retorna o valor ANTERIOR; em expressoes, usar este helper. */
    public static ItemStack setPotionContents(ItemStack stack, net.minecraft.world.item.alchemy.PotionContents contents) {
        stack.set(DataComponents.POTION_CONTENTS, contents);
        return stack;
    }

    /** [PORT 1.21.1] ItemStack.set(...) retorna o valor ANTERIOR; em expressoes, usar este helper. */
    public static <T> ItemStack setComponent(ItemStack stack, net.minecraft.core.component.DataComponentType<T> type, T value) {
        stack.set(type, value);
        return stack;
    }

    /** [PORT 1.21.1] ItemStack.hurt(int, RandomSource, Player) removido - helper com semantica de quebra. */
    public static boolean hurt(ItemStack stack, int amount, net.minecraft.util.RandomSource random) {
        if (!stack.isDamageableItem()) {
            return false;
        }
        int newDamage = stack.getDamageValue() + amount;
        if (newDamage >= stack.getMaxDamage()) {
            return true;
        }
        stack.setDamageValue(newDamage);
        return false;
    }
}
