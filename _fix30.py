# _fix30.py - receitas: CraftingContainer -> CraftingInput (14 arquivos), getRecipeFor call sites,
#             AMRecipeBase getResultItem, SingleRecipeInput para receitas vanilla
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    if old not in t and '\r\n' in t:
        old2 = old.replace('\n', '\r\n')
        if old2 in t:
            old, new = old2, new.replace('\n', '\r\n')
    if old not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + ']')
    return t.replace(old, new)


S = 'src/main/java/com/mna/'
CI = 'net.minecraft.world.item.crafting.CraftingInput'
SR = 'new net.minecraft.world.item.crafting.SingleRecipeInput(inv.getItem(0))'
RH = '(net.minecraft.world.item.crafting.RecipeHolder)'

print('== AMRecipeBase ==')
p = S + 'recipes/AMRecipeBase.java'
t = load(p)
t = rep(t, '''    public ItemStack getResultItem() {
        return this.getResultItem();
    }''',
        '''    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider provider) {
        return this.getResultItem();
    }

    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }''', 'amb-result')
save(p, t)

print('== 14 receitas: CraftingContainer -> CraftingInput ==')
recipes = ['arcanefurnace/ArcaneFurnaceRecipe', 'crush/CrushingRecipe', 'eldrin/EldrinAltarRecipe', 'eldrin/FumeFilterRecipe',
           'ItemAndPatternRecipe', 'manaweaving/ManaweaveCacheEffect', 'manaweaving/ManaweavingPattern', 'manaweaving/ManaweavingRecipe',
           'manaweaving/TransmutationRecipe', 'multiblock/MultiblockDefinition', 'progression/ProgressionCondition',
           'rituals/RitualRecipe', 'runeforging/RuneforgingRecipe', 'runeforging/RunescribingRecipe']
for r in recipes:
    p = S + 'recipes/' + r + '.java'
    t = load(p)
    o = t
    t = t.replace('matches(CraftingContainer', 'matches(' + CI)
    t = t.replace('assemble(CraftingContainer', 'assemble(' + CI)
    t = t.replace('inv.getContainerSize()', 'inv.size()')
    t = t.replace('inv.getWidth()', 'inv.width()')
    t = t.replace('inv.getHeight()', 'inv.height()')
    if t != o:
        if 'import ' + CI + ';' not in t and 'CraftingInput' not in t.split('public class')[0]:
            t = t.replace('package com.mna.recipes', 'import ' + CI + ';\npackage com.mna.recipes', 1) if False else t
            # inserir import depois da linha package
            lines = t.split('\n')
            for i, ln in enumerate(lines):
                if ln.startswith('package '):
                    lines.insert(i + 1, 'import ' + CI + ';')
                    break
            t = '\n'.join(lines)
        save(p, t)
        print('  ok [' + r + ']')
    else:
        print('  -- [' + r + '] (sem mudanca)')

print('== EldrinFumeTile matches ==')
p = S + 'blocks/tileentities/wizard_lab/EldrinFumeTile.java'
t = load(p)
t = rep(t, 'r.value().matches(ContainerTools.createTemporaryContainer(stack), level)',
        'r.value().matches(ContainerTools.createTemporaryContainer(stack).asCraftInput(), level)', 'ef-match')
save(p, t)

print('== RuneForgeTile ==')
p = S + 'blocks/tileentities/RuneForgeTile.java'
t = load(p)
t = rep(t, 'getRecipeFor((RecipeType)RecipeInit.ARCANE_FURNACE_TYPE.get(), (Container)inv, this.level).orElse(null)',
        'getRecipeFor(RecipeInit.ARCANE_FURNACE_TYPE.get(), inv.asCraftInput(), this.level).map(h -> h.value()).orElse(null)', 'rf-108')
t = t.replace('getRecipeFor(RecipeType.CAMPFIRE_COOKING, (Container)inv, this.level)', 'getRecipeFor(RecipeType.CAMPFIRE_COOKING, ' + SR + ', this.level)')
t = t.replace('getRecipeFor(RecipeType.SMELTING, (Container)inv, this.level)', 'getRecipeFor(RecipeType.SMELTING, ' + SR + ', this.level)')
t = t.replace('getRecipeFor(RecipeType.BLASTING, (Container)inv, this.level)', 'getRecipeFor(RecipeType.BLASTING, ' + SR + ', this.level)')
t = re.sub(r'\(\((CampfireCookingRecipe|SmeltingRecipe|BlastingRecipe)\)(\w+)\.get\(\)\)', r'((\1)' + RH + r'\2.get()).value())', t)
t = t.replace('.assemble((Container)inv, this.level.registryAccess())', '.assemble(' + SR + ', this.level.registryAccess())')
save(p, t)

print('== outros getRecipeFor ==')
p = S + 'blocks/tileentities/EldrinAltarTile.java'
t = load(p)
t = rep(t, 'getRecipeFor((RecipeType)RecipeInit.ELDRIN_ALTAR_TYPE.get(), (Container)craftinginventory, this.level)',
        'getRecipeFor(RecipeInit.ELDRIN_ALTAR_TYPE.get(), craftinginventory.asCraftInput(), this.level).map(h -> h.value()).orElse(null)', 'ea-556')
save(p, t)
p = S + 'blocks/tileentities/ManaweavingAltarTile.java'
t = load(p)
t = rep(t, 'getRecipeFor((RecipeType)RecipeInit.MANAWEAVING_RECIPE_TYPE.get(), (Container)this.createCraftingInventory(), this.level)',
        'getRecipeFor(RecipeInit.MANAWEAVING_RECIPE_TYPE.get(), this.createCraftingInventory().asCraftInput(), this.level).map(h -> h.value()).orElse(null)', 'ma-274')
save(p, t)
p = S + 'blocks/tileentities/RunicAnvilTile.java'
t = load(p)
t = rep(t, 'getRecipeFor((RecipeType)RecipeInit.RUNEFORGING_TYPE.get(), (Container)inv, this.level).orElse(null)',
        'getRecipeFor(RecipeInit.RUNEFORGING_TYPE.get(), inv.asCraftInput(), this.level).map(h -> h.value()).orElse(null)', 'ra-173')
save(p, t)
p = S + 'blocks/tileentities/wizard_lab/RunescribingTableTile.java'
t = load(p)
t = rep(t, '(RunescribingRecipe)this.getLevel().getRecipeManager().getRecipeFor((RecipeType)RecipeInit.RUNESCRIBING_TYPE.get(), (Container)ContainerTools.createTemporaryContainer(patternStack), this.level).orElse(null)',
        'this.getLevel().getRecipeManager().getRecipeFor(RecipeInit.RUNESCRIBING_TYPE.get(), ContainerTools.createTemporaryContainer(patternStack).asCraftInput(), this.level).map(h -> h.value()).orElse(null)', 'rt-197')
save(p, t)
p = S + 'entities/constructs/ai/ConstructCrush.java'
t = load(p)
t = rep(t, 'getRecipeFor((RecipeType)RecipeInit.CRUSHING_TYPE.get(), (Container)inv, c.level()).orElse(null)',
        'getRecipeFor(RecipeInit.CRUSHING_TYPE.get(), inv.asCraftInput(), c.level()).map(h -> h.value()).orElse(null)', 'cc-105')
save(p, t)
p = S + 'tools/RecipeUtil.java'
t = load(p)
t = rep(t, 'getRecipeFor(RecipeType.CRAFTING, (Container)inventory, world)',
        'getRecipeFor(RecipeType.CRAFTING, inventory.asCraftInput(), world)', 'ru-35')
save(p, t)

print('== ComponentCrucible ==')
p = S + 'spells/components/ComponentCrucible.java'
t = load(p)
t = rep(t, 'Optional furnace_recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, (Container)inv, level);',
        'Optional furnace_recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, ' + SR + ', level);', 'cc-96a')
t = rep(t, 'if (furnace_recipe.isPresent() && !(output = ((SmeltingRecipe)furnace_recipe.get()).assemble((Container)inv, level.registryAccess())).isEmpty()) {',
        'if (furnace_recipe.isPresent() && !(output = ((SmeltingRecipe)' + RH + 'furnace_recipe.get()).value()).assemble(' + SR + ', level.registryAccess())).isEmpty()) {', 'cc-96b')
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
