package com.mna.enchantments.framework;

import com.mna.api.enchantments.MAEnchantmentBridge;
import com.mna.items.sorcery.MagicStaff;
import net.minecraft.world.item.Item;

/**
 * [PORT 1.21.1] {@code EnchantmentCategory} foi REMOVIDO (as categorias agora sao
 * HolderSet de itens/tags). Esta classe preserva os predicados antigos como checagens
 * booleanas de item.
 */
public class EnchantmentEnumExtender {

    /** Equivalente ao antigo StaffOrWeapon: staves do MNA ou armas. */
    public static boolean staffOrWeapon(Item i) {
        return i instanceof MagicStaff || MAEnchantmentBridge.isWeapon(i);
    }

    /** Equivalente ao antigo STAVES: apenas staves do MNA. */
    public static boolean staves(Item i) {
        return i instanceof MagicStaff;
    }
}
