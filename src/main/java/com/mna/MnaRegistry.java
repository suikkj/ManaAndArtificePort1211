package com.mna;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Wrapper sobre {@link Registry} que preserva a superficie de metodos que o
 * M&A esperava de IForgeRegistry (getValues/getRegistryKey/containsValue),
 * removido no NeoForge 1.21.1.
 */
public class MnaRegistry<T> implements Iterable<T> {
    private final Registry<T> registry;

    public MnaRegistry(Registry<T> registry) {
        this.registry = registry;
    }

    public Registry<T> unwrap() {
        return this.registry;
    }

    public T getValue(ResourceLocation key) {
        return this.registry.get(key);
    }

    public ResourceLocation getKey(T value) {
        return this.registry.getKey(value);
    }

    public boolean containsKey(ResourceLocation key) {
        return this.registry.containsKey(key);
    }

    public boolean containsValue(T value) {
        if (value == null) return false;
        for (T v : this.registry) {
            if (value.equals(v)) return true;
        }
        return false;
    }

    public Collection<T> getValues() {
        return this.registry.entrySet().stream().map(Map.Entry::getValue).toList();
    }

    public Set<Map.Entry<ResourceKey<T>, T>> getEntries() {
        return this.registry.entrySet();
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return this.registry.key();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        this.registry.forEach(action);
    }

    @Override
    public Iterator<T> iterator() {
        return this.registry.iterator();
    }
}
