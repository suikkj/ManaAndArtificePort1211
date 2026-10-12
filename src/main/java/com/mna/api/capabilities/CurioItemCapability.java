package com.mna.api.capabilities;

import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Compat 1.21.1 (Curios 9.x): a interface {@code ICurio} e a capability
 * {@code CuriosCapability.ITEM} foram substituidas por {@link ICurioItem}.
 */
public class CurioItemCapability {
    /** Cria o provider de curios para um item (API 1.21.1 / Curios 9.x). */
    public static ICapabilityProvider<net.minecraft.world.item.ItemStack, Void, ICurioItem> createProvider(ICurioItem curio) {
        return (stack, ctx) -> curio;
    }
}


