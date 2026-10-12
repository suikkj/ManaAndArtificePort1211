# _fix28.py - IShearable migration + RecipeHolder (.value()/.id()) nos 18 sites raw RecipeType
import os

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    if old not in t and '\r\n' in t:
        old2 = old.replace('\n', '\r\n')
        if old2 in t:
            old = old2
            new = new.replace('\n', '\r\n')
    if old not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + ']')
    return t.replace(old, new)


S = 'src/main/java/com/mna/'

print('== ShearHelper ==')
p = S + 'tools/ShearHelper.java'
t = load(p)
t = rep(t, 'import net.neoforged.neoforge.common.IForgeShearable;', 'import net.neoforged.neoforge.common.IShearable;', 'sh-import')
t = rep(t, 'public static boolean canBlockBeSheared(Level world, BlockState state, BlockPos pos) {',
        'public static boolean canBlockBeSheared(Level world, Player player, BlockState state, BlockPos pos) {', 'sh-cbb')
t = rep(t, '((IForgeShearable)state.getBlock()).isShearable(new ItemStack((ItemLike)Items.SHEARS), world, pos)',
        '((IShearable)state.getBlock()).isShearable(player, new ItemStack((ItemLike)Items.SHEARS), world, pos)', 'sh-cbb2')
t = rep(t, '''    private static List<ItemStack> shearIForgeShearable(IForgeShearable target, Player player, BlockPos pos, ItemStack heldItem, Level world) {
        List<Object> output = new ArrayList<ItemStack>();
        if (target.isShearable(heldItem, world, pos)) {
            int enchantmentLevel = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, heldItem);
            output = target.onSheared(player, heldItem, world, pos, enchantmentLevel);
        }
        return output;
    }''',
        '''    private static List<ItemStack> shearIForgeShearable(IShearable target, Player player, BlockPos pos, ItemStack heldItem, Level world) {
        List<ItemStack> output = new ArrayList<ItemStack>();
        if (target.isShearable(player, heldItem, world, pos)) {
            output = target.onSheared(player, heldItem, world, pos);
        }
        return output;
    }''', 'sh-body')
t = t.replace('IForgeShearable', 'IShearable')
save(p, t)

print('== ConstructShear ==')
p = S + 'entities/constructs/ai/ConstructShear.java'
t = load(p)
t = rep(t, 'import net.neoforged.neoforge.common.IForgeShearable;', 'import net.neoforged.neoforge.common.IShearable;', 'cs-import')
t = rep(t, 'e instanceof IForgeShearable && ((IForgeShearable)e).isShearable(this.shearsStack, c.level(), e.blockPosition())',
        'e instanceof IShearable && ((IShearable)e).isShearable(this.createFakePlayer(), this.shearsStack, c.level(), e.blockPosition())', 'cs-line136')
t = rep(t, 'ShearHelper.canBlockBeSheared(this.construct.asEntity().level(), state, pos)',
        'ShearHelper.canBlockBeSheared(this.construct.asEntity().level(), this.createFakePlayer(), state, pos)', 'cs-line222')
save(p, t)

print('== RecipeUtil / MagiciansWorkbenchTile ==')
p = S + 'tools/RecipeUtil.java'
t = load(p)
t = rep(t, 'public static Optional<CraftingRecipe> lookupCraftingRecipe(Level world, ItemStack[] items) {',
        'public static Optional<net.minecraft.world.item.crafting.RecipeHolder<CraftingRecipe>> lookupCraftingRecipe(Level world, ItemStack[] items) {', 'ru-sig')
save(p, t)
p = S + 'blocks/tileentities/wizard_lab/MagiciansWorkbenchTile.java'
t = load(p)
t = rep(t, 'resolvedRecipe.getId()', 'resolvedRecipe.id()', 'mw-id')
save(p, t)

print('== ProgressionEventHandler / PlayerProgression ==')
p = S + 'progression/ProgressionEventHandler.java'
t = load(p)
t = rep(t, 'event.getAdvancement().getId()', 'event.getAdvancement().id()', 'peh-1')
t = rep(t, 'adv.getId()', 'adv.id()', 'peh-2')
save(p, t)
p = S + 'capabilities/playerdata/progression/PlayerProgression.java'
t = load(p)
t = rep(t, 'inst.removeModifier(modifier.getId());', 'inst.removeModifier(modifier.id());', 'pp-96')
save(p, t)

COLL = '.stream().map(r -> r.value()).collect(java.util.stream.Collectors.toList())'

print('== RunicAnvilBlock ==')
p = S + 'blocks/runeforging/RunicAnvilBlock.java'
t = load(p)
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.RUNEFORGING_TYPE.get()).stream().filter(r -> r.getOutputResource().toString().equals(BuiltInRegistries.ITEM.getKey(rune.getItem()).toString())).findFirst()',
        'getAllRecipesFor(RecipeInit.RUNEFORGING_TYPE.get()).stream().filter(r -> r.value().getOutputResource().toString().equals(BuiltInRegistries.ITEM.getKey(rune.getItem()).toString())).map(r -> r.value()).findFirst()', 'ra-178')
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.RUNESCRIBING_TYPE.get()).stream().filter(r -> r.getOutputResource().toString().equals(((RuneforgingRecipe)forgeRecipe.get()).getPatternResource().toString())).findFirst()',
        'getAllRecipesFor(RecipeInit.RUNESCRIBING_TYPE.get()).stream().filter(r -> r.value().getOutputResource().toString().equals(((RuneforgingRecipe)forgeRecipe.get()).getPatternResource().toString())).map(r -> r.value()).findFirst()', 'ra-182')
save(p, t)

print('== ManaweaveCacheTile ==')
p = S + 'blocks/tileentities/ManaweaveCacheTile.java'
t = load(p)
t = rep(t, 'return this.getLevel().getRecipeManager().getAllRecipesFor((RecipeType)RecipeInit.MANAWEAVING_PATTERN_TYPE.get()).stream().filter(r -> r.getTier() <= this.tier).map(r -> r.getId()).collect(Collectors.toList());',
        'return this.getLevel().getRecipeManager().getAllRecipesFor(RecipeInit.MANAWEAVING_PATTERN_TYPE.get()).stream().filter(r -> r.value().getTier() <= this.tier).map(r -> r.id()).collect(Collectors.toList());', 'mwc-219')
t = rep(t, 'List effects = this.getLevel().getRecipeManager().getAllRecipesFor((RecipeType)RecipeInit.MANAWEAVE_CACHE_EFFECT_TYPE.get()).stream().filter(r -> r.getTier() <= this.tier).collect(Collectors.toList());',
        'List effects = this.getLevel().getRecipeManager().getAllRecipesFor(RecipeInit.MANAWEAVE_CACHE_EFFECT_TYPE.get()).stream().filter(r -> r.value().getTier() <= this.tier).map(r -> r.value()).collect(Collectors.toList());', 'mwc-223')
save(p, t)

print('== EldrinFumeTile / Broker ==')
p = S + 'blocks/tileentities/wizard_lab/EldrinFumeTile.java'
t = load(p)
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.FUME_FILTER_TYPE.get()).stream().filter(r -> r.matches(ContainerTools.createTemporaryContainer(stack), level)).findFirst()',
        'getAllRecipesFor(RecipeInit.FUME_FILTER_TYPE.get()).stream().filter(r -> r.value().matches(ContainerTools.createTemporaryContainer(stack), level)).map(r -> r.value()).findFirst()', 'ef-77')
save(p, t)
p = S + 'entities/faction/Broker.java'
t = load(p)
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.MANAWEAVING_RECIPE_TYPE.get()).stream().filter(r -> r.getResultItem().getItem() == output).findFirst()',
        'getAllRecipesFor(RecipeInit.MANAWEAVING_RECIPE_TYPE.get()).stream().filter(r -> r.value().getResultItem().getItem() == output).map(r -> r.value()).findFirst()', 'br-293')
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.ELDRIN_ALTAR_TYPE.get()).stream().filter(r -> r.getResultItem().getItem() == output).findFirst()',
        'getAllRecipesFor(RecipeInit.ELDRIN_ALTAR_TYPE.get()).stream().filter(r -> r.value().getResultItem().getItem() == output).map(r -> r.value()).findFirst()', 'br-297')
save(p, t)

print('== GuiCantrips / ModifierRadialSelect / ItemModifierBook ==')
p = S + 'gui/item/GuiCantrips.java'
t = load(p)
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.MANAWEAVING_PATTERN_TYPE.get()).stream().forEach(p -> {',
        'getAllRecipesFor(RecipeInit.MANAWEAVING_PATTERN_TYPE.get()).stream().forEach(p -> {', 'gc-80a')
t = rep(t, 'if (p.getTier() <= this.progression.getTier()) {\n                this.playerKnownPatterns.add((ManaweavingPattern)p);',
        'if (p.value().getTier() <= this.progression.getTier()) {\n                this.playerKnownPatterns.add(p.value());', 'gc-80b')
save(p, t)
p = S + 'gui/radial/ModifierRadialSelect.java'
t = load(p)
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.MODIFIER_TYPE.get()).stream().filter(m -> m.getTier() <= tier).map(m -> (Modifier)m.getComponent())',
        'getAllRecipesFor(RecipeInit.MODIFIER_TYPE.get()).stream().filter(m -> m.value().getTier() <= tier).map(m -> m.value().getComponent())', 'mrs-54')
save(p, t)
p = S + 'items/sorcery/ItemModifierBook.java'
t = load(p)
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.MODIFIER_TYPE.get()).stream().filter(m -> m.getComponent() == modifier).findFirst()',
        'getAllRecipesFor(RecipeInit.MODIFIER_TYPE.get()).stream().filter(m -> m.value().getComponent() == modifier).map(m -> m.value()).findFirst()', 'imb-66')
save(p, t)

print('== RandomManaweaveRecipe / RandomRunescribeRecipe ==')
p = S + 'loot/RandomManaweaveRecipe.java'
t = load(p)
t = rep(t, 'List patterns = context.getLevel().getRecipeManager().getAllRecipesFor((RecipeType)RecipeInit.MANAWEAVING_PATTERN_TYPE.get());',
        'List<ManaweavingPattern> patterns = context.getLevel().getRecipeManager().getAllRecipesFor(RecipeInit.MANAWEAVING_PATTERN_TYPE.get())' + COLL + ';', 'rm-48')
save(p, t)
p = S + 'loot/RandomRunescribeRecipe.java'
t = load(p)
t = rep(t, 'List patterns = context.getLevel().getRecipeManager().getAllRecipesFor((RecipeType)RecipeInit.RUNESCRIBING_TYPE.get());',
        'List<RunescribingRecipe> patterns = context.getLevel().getRecipeManager().getAllRecipesFor(RecipeInit.RUNESCRIBING_TYPE.get())' + COLL + ';', 'rr-48')
save(p, t)

print('== ManaweavingPatternHelper ==')
p = S + 'recipes/manaweaving/ManaweavingPatternHelper.java'
t = load(p)
t = rep(t, 'List mwpList;', 'List<ManaweavingPattern> mwpList;', 'mph-decl')
t = rep(t, 'world.getRecipeManager().getRecipesFor((RecipeType)RecipeInit.MANAWEAVING_PATTERN_TYPE.get(), (Container)ContainerTools.createTemporaryContainer(ItemStack.EMPTY), world)',
        'world.getRecipeManager().getRecipesFor(RecipeInit.MANAWEAVING_PATTERN_TYPE.get(), (Container)ContainerTools.createTemporaryContainer(ItemStack.EMPTY), world)' + COLL, 'mph-sites')
save(p, t)

print('== ProgressionCondition ==')
p = S + 'recipes/progression/ProgressionCondition.java'
t = load(p)
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.PROGRESSION_TYPE.get()).stream().filter(r -> r.getId().equals(rLoc)).findFirst()',
        'getAllRecipesFor(RecipeInit.PROGRESSION_TYPE.get()).stream().filter(r -> r.id().equals(rLoc)).map(r -> r.value()).findFirst()', 'pc-98')
t = rep(t, 'getAllRecipesFor((RecipeType)RecipeInit.PROGRESSION_TYPE.get()).stream().filter(r -> r.getTier() == tier && !except.contains(r.getId())).collect(Collectors.toList())',
        'getAllRecipesFor(RecipeInit.PROGRESSION_TYPE.get()).stream().filter(r -> r.value().getTier() == tier && !except.contains(r.id())).map(r -> r.value()).collect(Collectors.toList())', 'pc-106')
save(p, t)

print('== RitualRecipeHelper ==')
p = S + 'recipes/rituals/RitualRecipeHelper.java'
t = load(p)
t = rep(t, 'getRecipesFor((RecipeType)RecipeInit.RITUAL_TYPE.get(), null, world).stream().filter(r -> r.getId().toString().equals(rLoc.toString())).findFirst()',
        'getRecipesFor(RecipeInit.RITUAL_TYPE.get(), null, world).stream().filter(r -> r.id().toString().equals(rLoc.toString())).map(r -> r.value()).findFirst()', 'rrh-26')
t = rep(t, 'List collection = world.getRecipeManager().getRecipesFor((RecipeType)RecipeInit.RITUAL_TYPE.get(), null, world);',
        'List collection = world.getRecipeManager().getRecipesFor(RecipeInit.RITUAL_TYPE.get(), null, world)' + COLL + ';', 'rrh-31')
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
