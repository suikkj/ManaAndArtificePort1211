package com.mna.api.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * [PORT 1.21.1] {@code net.neoforged.neoforge.common.TierSortingRegistry} foi
 * REMOVIDO (era um registry custom de tiers ordenados). No 1.21.1 os tiers sao
 * fixos (vanilla), entao a ordem e derivada da tag {@link BlockTags#INCORRECT_FOR_XXX_TOOL}
 * + ordem canonica de mineracao.
 *
 * Uso no MNA: ordenar {@code ConstructMaterial} por poder do tier equivalente.
 */
public final class MNAtierSorting {
    /** Ordem canonica de poder dos tiers vanilla. */
    private static final List<Tier> ORDER = new ArrayList<>(Arrays.asList(
            Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE
    ));

    private MNAtierSorting() {}

    /** Equivalente ao antigo {@code getSortedTiers()}. */
    public static List<Tier> getSortedTiers() {
        return new ArrayList<>(ORDER);
    }

    public static int indexOf(Tier tier) {
        int i = ORDER.indexOf(tier);
        return i < 0 ? 0 : i;
    }

    /** Equivalente ao antigo {@code isTierSorted(tier)}. */
    public static boolean isTierSorted(Tier tier) {
        return ORDER.contains(tier);
    }

    /** Equivalente ao antigo {@code isCorrectTierForDrops(tier, state)}. */
    public static boolean isCorrectTierForDrops(Tier tier, net.minecraft.world.level.block.state.BlockState state) {
        return state.is(tier.getIncorrectBlocksForDrops());
    }
}
