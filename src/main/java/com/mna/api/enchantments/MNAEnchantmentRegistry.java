package com.mna.api.enchantments;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * [PORT 1.21.1] Enchantment virou um registry DATA-DRIVEN: nao existe mais
 * {@code BuiltInRegistries.ENCHANTMENT}. O acesso se da via o registryAccess do server
 * ativo. Em contextos sem server (client early init) os metodos devolvem null/vazio.
 */
public final class MNAEnchantmentRegistry {
    private MNAEnchantmentRegistry() {
    }

    /** Registry de enchantments do server ativo, ou null (client/sem server). */
    public static Registry<Enchantment> currentOrNull() {
        net.minecraft.server.MinecraftServer srv = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (srv == null) {
            return null;
        }
        return srv.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
    }

    public static Optional<Registry<Enchantment>> current() {
        return Optional.ofNullable(currentOrNull());
    }

    public static Holder<Enchantment> holder(ResourceKey<Enchantment> key) {
        Registry<Enchantment> reg = currentOrNull();
        return reg == null || key == null ? null : reg.getHolder(key).orElse(null);
    }

    public static Holder<Enchantment> holder(ResourceLocation id) {
        return id == null ? null : holder(ResourceKey.create(Registries.ENCHANTMENT, id));
    }

    public static Enchantment get(ResourceKey<Enchantment> key) {
        Holder<Enchantment> h = holder(key);
        return h == null ? null : h.value();
    }

    public static Enchantment get(ResourceLocation id) {
        Holder<Enchantment> h = holder(id);
        return h == null ? null : h.value();
    }

    public static ResourceKey<Enchantment> keyOf(Enchantment enchantment) {
        Registry<Enchantment> reg = currentOrNull();
        return reg == null || enchantment == null ? null : reg.getResourceKey(enchantment).orElse(null);
    }

    public static ResourceLocation idOf(Enchantment enchantment) {
        ResourceKey<Enchantment> k = keyOf(enchantment);
        return k == null ? null : k.location();
    }
}
