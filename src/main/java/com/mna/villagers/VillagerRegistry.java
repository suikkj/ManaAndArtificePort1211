/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.ai.village.poi.PoiType
 *  net.minecraft.world.entity.npc.VillagerProfession
 *  net.minecraft.world.entity.npc.VillagerTrades$EnchantBookForEmeralds
 *  net.minecraft.world.entity.npc.VillagerTrades$ItemListing
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.event.village.VillagerTradesEvent
 *  net.minecraftforge.event.village.WandererTradesEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.NeoForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.mna.villagers;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.google.common.collect.ImmutableSet;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.blocks.BlockInit;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.ItemTornJournalPage;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.villagers.RandomTradeBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class VillagerRegistry {
    public static DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, "mna");
    public static DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, "mna");
    public static final DeferredHolder<PoiType, ? extends PoiType> SPELLMONGER_POI = POI_TYPES.register("spellmonger_poi", () -> new PoiType((Set)ImmutableSet.copyOf((Collection)((Block)BlockInit.RUNEFORGE.get()).getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, ? extends PoiType> RITUALIST_POI = POI_TYPES.register("ritualist_poi", () -> new PoiType((Set)ImmutableSet.copyOf((Collection)((Block)BlockInit.RUNESCRIBING_TABLE.get()).getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<PoiType, ? extends PoiType> MANAWEAVER_POI = POI_TYPES.register("manaweaver_poi", () -> new PoiType((Set)ImmutableSet.copyOf((Collection)((Block)BlockInit.MANAWEAVING_ALTAR.get()).getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<VillagerProfession, ? extends VillagerProfession> SPELLMONGER = PROFESSIONS.register("spellmonger", () -> new VillagerProfession("spellmonger", x -> x.value() == SPELLMONGER_POI.get(), x -> x.value() == SPELLMONGER_POI.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_SHEPHERD));
    public static final DeferredHolder<VillagerProfession, ? extends VillagerProfession> RITUALIST = PROFESSIONS.register("ritualist", () -> new VillagerProfession("ritualist", x -> x.value() == RITUALIST_POI.get(), x -> x.value() == RITUALIST_POI.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_SHEPHERD));
    public static final DeferredHolder<VillagerProfession, ? extends VillagerProfession> MANAWEAVER = PROFESSIONS.register("manaweaver", () -> new VillagerProfession("manaweaver_poi", x -> x.value() == MANAWEAVER_POI.get(), x -> x.value() == MANAWEAVER_POI.get(), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_SHEPHERD));

    @EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
    static class VillagerTradesHandler {
        VillagerTradesHandler() {
        }

        @SubscribeEvent
        public static void registerTrades(VillagerTradesEvent event) {
            if (!GeneralConfigValues.DisableVillagerRegistration) {
                if (event.getType() == SPELLMONGER.get()) {
                    VillagerTradesHandler.addSpellmongerTrades(event);
                } else if (event.getType() == RITUALIST.get()) {
                    VillagerTradesHandler.addRunesmithTrades(event);
                } else if (event.getType() == MANAWEAVER.get()) {
                    VillagerTradesHandler.addManaweaverTrades(event);
                }
            }
            if (event.getType() == VillagerProfession.LIBRARIAN) {
                if (GeneralConfigValues.ModifyVillagerTrades) {
                    VillagerTradesHandler.removeLibrarianTrades(event);
                    ManaAndArtifice.LOGGER.info("Mana And Artifice >> Applied villager librarian balance changes.");
                } else {
                    ManaAndArtifice.LOGGER.info("Mana And Artifice >> Skipped villager librarian balance changes as per config.");
                }
            }
        }

        private static void addSpellmongerTrades(VillagerTradesEvent event) {
            ((List)event.getTrades().get(1)).add(new RandomTradeBuilder(8, 10, 0.05f).setPrice((Item)ItemInit.VINTEUM_INGOT.get(), 5, 5).setForSale((Item)ItemInit.GUIDE_BOOK.get(), 1, 1).build());
            ((List)event.getTrades().get(5)).add(new RandomTradeBuilder(1, 10, 0.05f).setPrice((Item)ItemInit.CHIMERITE_GEM.get(), 3, 8).setForSale((Item)ItemInit.MANA_CRYSTAL_FRAGMENT.get(), 1, 1).build());
            VillagerTradesHandler.addRecipeTrades(event, 1, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.ARCANIST_INK.get()));
            VillagerTradesHandler.addRecipeTrades(event, 2, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.HERBALIST_POUCH.get(), (Item)ItemInit.SPELL_BOOK.get(), (Item)ItemInit.BED_CHARM.get(), (Item)ItemInit.FALL_CHARM.get(), (Item)ItemInit.BURN_CHARM.get(), (Item)ItemInit.DROWN_CHARM.get(), (Item)ItemInit.VINTEUM_DUST.get()));
            VillagerTradesHandler.addRecipeTrades(event, 3, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.HERBALIST_POUCH.get(), (Item)ItemInit.SPELL_BOOK.get(), (Item)ItemInit.BED_CHARM.get(), (Item)ItemInit.FALL_CHARM.get(), (Item)ItemInit.BURN_CHARM.get(), (Item)ItemInit.DROWN_CHARM.get(), (Item)ItemInit.VINTEUM_DUST.get()));
            VillagerTradesHandler.addRecipeTrades(event, 4, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.WITHERBONE.get(), (Item)ItemInit.LIVING_FLAME.get(), (Item)ItemInit.IRONBARK.get()));
            VillagerTradesHandler.addRecipeTrades(event, 5, 1, 1, 1, 32, 64, r -> {
                ItemStack stack = new ItemStack((ItemLike)ItemInit.SPELL.get());
                SpellRecipe recipe = new SpellRecipe();
                recipe.setShape(Shapes.RUNE);
                Registries.SpellEffect.getValues().stream().skip((long)((double)Registries.SpellEffect.getValues().size() * Math.random())).findFirst().ifPresent(c -> {
                    recipe.addComponent((SpellEffect)c);
                    recipe.writeToNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                });
                return recipe.isValid() ? stack : ItemStack.EMPTY;
            });
            System.out.println("Registered Spellmonger Trades");
        }

        private static void addRunesmithTrades(VillagerTradesEvent event) {
            VillagerTradesHandler.addRecipeTrades(event, 1, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.CLAY_RUNE_PLATE.get(), (Item)ItemInit.WIZARD_CHALK.get()));
            VillagerTradesHandler.addRecipeTrades(event, 1, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.RUNE_MARKING.get()));
            VillagerTradesHandler.addRecipeTrades(event, 2, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.MUNDANE_AMULET.get(), (Item)ItemInit.MUNDANE_BRACELET.get(), (Item)ItemInit.MUNDANE_RING.get(), (Item)ItemInit.INSCRIPTIONIST_POUCH.get()));
            VillagerTradesHandler.addRecipeTrades(event, 3, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.CONSTRUCT_RUNE_ROD.get(), (Item)ItemInit.CONSTRUCT_RUNE_CLAW.get(), (Item)ItemInit.CONSTRUCT_RUNE_HAMMER.get(), (Item)ItemInit.CONSTRUCT_RUNE_AXE.get()));
            VillagerTradesHandler.addRecipeTrades(event, 4, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.CONSTRUCT_RUNE_TORSO.get(), (Item)ItemInit.CONSTRUCT_RUNE_HIPS.get(), (Item)ItemInit.CONSTRUCT_RUNE_HEAD.get()));
            ((List)event.getTrades().get(5)).add(new RandomTradeBuilder(1, 10, 0.05f).setPrice((Item)ItemInit.RUNESMITH_HAMMER.get(), 1, 1).setPrice2((Item)ItemInit.TRANSMUTED_SILVER.get(), 3, 8).setForSale(r -> {
                ItemStack stack = new ItemStack((ItemLike)ItemInit.RUNESMITH_HAMMER.get());
                HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
                if (r.nextBoolean()) {
                    enchants.put((Enchantment)EnchantmentInit.MANA_REPAIR.get(), 1);
                } else {
                    enchants.put(Enchantments.UNBREAKING, 3 + r.nextInt(3));
                }
                EnchantmentHelper.setEnchantments(enchants, (ItemStack)stack);
                return stack;
            }).build());
            ((List)event.getTrades().get(5)).add(new RandomTradeBuilder(1, 10, 0.05f).setPrice((Item)ItemInit.RUNESMITH_CHISEL.get(), 1, 1).setPrice2((Item)ItemInit.TRANSMUTED_SILVER.get(), 3, 8).setForSale(r -> {
                ItemStack stack = new ItemStack((ItemLike)ItemInit.RUNESMITH_CHISEL.get());
                HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
                if (r.nextBoolean()) {
                    enchants.put((Enchantment)EnchantmentInit.MANA_REPAIR.get(), 1);
                } else {
                    enchants.put(Enchantments.UNBREAKING, 3 + r.nextInt(3));
                }
                EnchantmentHelper.setEnchantments(enchants, (ItemStack)stack);
                return stack;
            }).build());
            System.out.println("Registered Runesmith Trades");
        }

        private static void addManaweaverTrades(VillagerTradesEvent event) {
            ((List)event.getTrades().get(1)).add(new RandomTradeBuilder(8, 1, 0.05f).setPrice((Item)ItemInit.TRANSMUTED_SILVER.get(), 6, 12).setForSale(BlockItem.byBlock((Block)((Block)BlockInit.ARCANE_SANDSTONE.get())), 8, 32).build());
            ((List)event.getTrades().get(1)).add(new RandomTradeBuilder(8, 1, 0.05f).setPrice((Item)ItemInit.TRANSMUTED_SILVER.get(), 6, 12).setForSale(BlockItem.byBlock((Block)((Block)BlockInit.ARCANE_STONE.get())), 8, 32).build());
            VillagerTradesHandler.addRecipeTrades(event, 1, 2, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.RITUAL_FOCUS_MINOR.get(), (Item)ItemInit.MANAWEAVER_WAND.get(), BlockItem.byBlock((Block)((Block)BlockInit.ARCANE_STONE.get())), BlockItem.byBlock((Block)((Block)BlockInit.ARCANE_SANDSTONE.get()))));
            VillagerTradesHandler.addRecipeTrades(event, 2, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.WEAVERS_POUCH.get(), (Item)ItemInit.INFUSED_SILK.get(), (Item)ItemInit.ANIMUS_DUST.get()));
            VillagerTradesHandler.addRecipeTrades(event, 3, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.WEAVERS_POUCH.get(), (Item)ItemInit.INFUSED_SILK.get(), (Item)ItemInit.ANIMUS_DUST.get(), (Item)ItemInit.RITUAL_FOCUS_LESSER.get()));
            VillagerTradesHandler.addRecipeTrades(event, 4, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.RUNIC_SILK.get(), (Item)ItemInit.SORCEROUS_SEWING_SET.get()));
            VillagerTradesHandler.addRecipeTrades(event, 5, 1, r -> VillagerTradesHandler.pickRandom(r, (Item)ItemInit.MANA_TEA.get(), (Item)ItemInit.WELLSPRING_DOWSING_ROD.get()));
            System.out.println("Registered Manaweaver Trades");
        }

        private static void addRecipeTrades(VillagerTradesEvent event, int level, int numRolls, Function<Random, ItemStack> generator) {
            VillagerTradesHandler.addRecipeTrades(event, level, numRolls, 8, 10, 6, 14, generator);
        }

        private static void addRecipeTrades(VillagerTradesEvent event, int level, int numRolls, int maxTrades, int xp, int minSalePrice, int maxSalePrice, Function<Random, ItemStack> generator) {
            for (int r = 0; r < numRolls; ++r) {
                ((List)event.getTrades().get(level)).add(new RandomTradeBuilder(maxTrades, xp, 0.05f).setPrice((Item)ItemInit.TRANSMUTED_SILVER.get(), minSalePrice, maxSalePrice).setForSale(generator).build());
            }
        }

        private static void removeLibrarianTrades(VillagerTradesEvent event) {
            event.getTrades().forEach((i, t) -> {
                if (i > 2) {
                    return;
                }
                ArrayList<VillagerTrades.ItemListing> toRemove = new ArrayList<VillagerTrades.ItemListing>();
                for (VillagerTrades.ItemListing trade : t) {
                    if (!(trade instanceof VillagerTrades.EnchantBookForEmeralds)) continue;
                    toRemove.add(trade);
                }
                t.removeAll(toRemove);
            });
        }

        @SubscribeEvent
        public static void registerTrades(WandererTradesEvent event) {
            event.getGenericTrades().add(new RandomTradeBuilder(4, 10, 0.05f).setEmeraldPrice(1).setForSale((Item)ItemInit.HEALING_POULTICE.get(), 3, 5).build());
            event.getGenericTrades().add(new RandomTradeBuilder(4, 10, 0.05f).setEmeraldPrice(1).setForSale((Item)ItemInit.INFUSED_SILK.get(), 3, 5).build());
            event.getGenericTrades().add(new RandomTradeBuilder(4, 10, 0.05f).setEmeraldPrice(1).setForSale((Item)ItemInit.MANA_TEA.get(), 3, 5).build());
            event.getGenericTrades().add(new RandomTradeBuilder(4, 10, 0.05f).setEmeraldPrice(1).setForSale((Item)ItemInit.HERBALIST_POUCH.get(), 1, 1).build());
            event.getRareTrades().add(new RandomTradeBuilder(1, 10, 0.05f).setEmeraldPrice(1).setForSale((Item)ItemInit.BED_CHARM.get(), 1, 1).build());
            event.getRareTrades().add(new RandomTradeBuilder(4, 10, 0.05f).setEmeraldPrice(1).setForSale((Item)ItemInit.FALL_CHARM.get(), 1, 1).build());
            event.getRareTrades().add(new RandomTradeBuilder(4, 10, 0.05f).setEmeraldPrice(3, 5).setForSale(r -> ItemTornJournalPage.getRandomPage(RandomSource.create())).build());
            event.getRareTrades().add(new RandomTradeBuilder(8, 10, 0.05f).setPrice((Item)ItemInit.VINTEUM_INGOT.get(), 5, 5).setForSale((Item)ItemInit.GUIDE_BOOK.get(), 1, 1).build());
            System.out.println("Registered Wandering Trader Trades");
        }

        public static ItemStack pickRandom(Random random, int minQty, int maxQty, Item ... items) {
            if (items.length == 0) {
                return ItemStack.EMPTY;
            }
            Item item = items[(int)(random.nextDouble() * (double)items.length)];
            ItemStack stack = new ItemStack((ItemLike)item);
            stack.setCount(random.nextInt(minQty, maxQty));
            return stack;
        }

        public static ItemStack pickRandom(Random random, Item ... items) {
            if (items.length == 0) {
                return ItemStack.EMPTY;
            }
            Item item = items[(int)(random.nextDouble() * (double)items.length)];
            return new ItemStack((ItemLike)item);
        }
    }
}

