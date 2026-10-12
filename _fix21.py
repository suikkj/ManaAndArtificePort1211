# _fix21.py - TooltipContext .level() em call-sites + ClipContext null ambiguo
import os
import re

FAILS = []
ENT = '(net.minecraft.world.entity.Entity)null'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def all_java():
    out = []
    for dp, _, fns in os.walk('src/main/java'):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


print('== TooltipContext -> .level() ==')
total = 0
for p in all_java():
    t = load(p)
    o = t
    # sites especificos (nomes de var distintos)
    t = t.replace('recipe.addItemTooltip(pStack, pLevel, pTooltipComponents, player);',
                  'recipe.addItemTooltip(pStack, pLevel.level(), pTooltipComponents, player);')
    t = t.replace('recipe.addItemTooltip(stack, worldIn, tooltip, player);',
                  'recipe.addItemTooltip(stack, worldIn.level(), tooltip, player);')
    t = t.replace('this.addContentsDescription(stack, world, tooltip);',
                  'this.addContentsDescription(stack, world.level(), tooltip);')
    t = t.replace('.addItemTooltip(stack, world, lines, ManaAndArtifice.instance.proxy.getClientPlayer());',
                  '.addItemTooltip(stack, world.level(), lines, ManaAndArtifice.instance.proxy.getClientPlayer());')
    t = t.replace('IPhylacteryItem.super.appendTooltip(stack, worldIn, tooltip, flagIn);',
                  'IPhylacteryItem.super.appendTooltip(stack, worldIn.level(), tooltip, flagIn);')
    t = t.replace('ItemManaweaverWand.getStoredPattern(worldIn, stack)', 'ItemManaweaverWand.getStoredPattern(worldIn.level(), stack)')
    t = t.replace('this.getRecipe(stack, worldIn)', 'this.getRecipe(stack, worldIn.level())')
    t = t.replace('PractitionersPouch.getRitual(worldIn, stack)', 'PractitionersPouch.getRitual(worldIn.level(), stack)')
    t = t.replace('recipe.addItemTooltip(stack, worldIn, tooltip, (Player)mc.player);',
                  'recipe.addItemTooltip(stack, worldIn.level(), tooltip, (Player)mc.player);')
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total)

print('== ClipContext null ==')
total = 0
for p in all_java():
    t = load(p)
    o = t
    t, n = re.subn(r'(ClipContext\.Fluid\.\w+, )null\)', r'\1' + ENT + ')', t)
    if n:
        save(p, t)
        total += n
print('  sites:', total)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
