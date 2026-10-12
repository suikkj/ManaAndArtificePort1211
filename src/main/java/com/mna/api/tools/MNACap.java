package com.mna.api.tools;

import java.util.Optional;

/**
 * Ponte de compatibilidade para o sistema de Attachments do 1.21.1.
 *
 * No 1.20.1 o mod usava ICapabilityProvider.getCapability(...) que devolvia
 * LazyOptional. Como LazyOptional NAO existe mais no 1.21.1, os call-sites
 * foram migrados para getData(AttachmentType). Este helper preserva o padrao
 * funcional {@code .ifPresent(lam)} sobre o resultado.
 */
public final class MNACap {
    private MNACap() {
    }

    /**
     * Devolve um Optional para permitir o padrao funcional antigo:
     * {@code MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> ...)}
     */
    public static <T> Optional<T> get(Object holder,
                                      java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<T>> type) {
        if (holder instanceof net.minecraft.world.entity.Entity e) {
            if (!e.hasData(type.get())) {
                return Optional.empty();
            }
            return Optional.ofNullable(e.getData(type.get()));
        }
        if (holder instanceof net.minecraft.world.level.block.entity.BlockEntity be) {
            if (!be.hasData(type.get())) {
                return Optional.empty();
            }
            return Optional.ofNullable(be.getData(type.get()));
        }
        if (holder instanceof net.minecraft.world.level.chunk.ChunkAccess ca) {
            if (!ca.hasData(type.get())) {
                return Optional.empty();
            }
            return Optional.ofNullable(ca.getData(type.get()));
        }
        if (holder instanceof net.minecraft.world.level.Level lvl) {
            if (!lvl.hasData(type.get())) {
                return Optional.empty();
            }
            return Optional.ofNullable(lvl.getData(type.get()));
        }
        return Optional.empty();
    }
}
