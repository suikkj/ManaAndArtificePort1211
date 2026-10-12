# _fix13.py - efeitos (MobEffect hooks), handleUpdateTag, Enchantments curto, RecipeHolder sites
import os
import re

FAILS = []
ROOT = 'src/main/java/com/mna/'


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


def extract_body(t, sig):
    """Retorna (start_idx_of_sig, end_idx_apos_fechar_brace) do metodo com assinatura sig."""
    k = t.find(sig)
    if k < 0:
        return None
    j = t.find('{', k)
    depth = 0
    i = j
    while i < len(t):
        c = t[i]
        if c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                return (k, i + 1, t[j + 1:i])
        i += 1
    return None


def migrate_effect_file(p):
    t = load(p)
    if 'void addAttributeModifiers(' not in t and 'void removeAttributeModifiers(' not in t:
        return False
    o = t
    sep = '\r\n' if '\r\n' in t else '\n'
    need_hook = False

    for kind, hookname in (('addAttributeModifiers', 'onEffectAdded'), ('removeAttributeModifiers', 'onEffectRemoved')):
        sig = re.search(r'public void ' + kind + r'\(LivingEntity (\w+), AttributeMap (\w+), int (\w+)\) \{', t)
        if not sig:
            continue
        e, m, a = sig.group(1), sig.group(2), sig.group(3)
        r = extract_body(t, sig.group(0))
        if not r:
            print('  body nao achado em', p)
            FAILS.append(p + ':' + kind)
            continue
        k, end, body = r
        # remover a linha do super
        sup_pat = re.compile(r'\s*super\.' + kind + r'\(\s*' + e + r'\s*,\s*' + m + r'\s*,\s*' + a + r'\s*\);\r?\n')
        m2 = sup_pat.search(body)
        if not m2:
            print('  super nao achado em', p, kind)
            FAILS.append(p + ':' + kind + ':super')
            continue
        rest = body[:m2.start()] + body[m2.end():]
        rest = rest.strip('\r\n')
        # metodo novo com assinatura 1.21.1
        if kind == 'addAttributeModifiers':
            newsig = 'public void addAttributeModifiers(AttributeMap ' + m + ', int ' + a + ') {'
            supcall = 'super.addAttributeModifiers(' + m + ', ' + a + ');'
        else:
            newsig = 'public void removeAttributeModifiers(AttributeMap ' + m + ') {'
            supcall = 'super.removeAttributeModifiers(' + m + ');'
        new_method = newsig + sep + '        ' + supcall + sep + '    }'
        t = t[:k] + new_method + t[end:]
        # hook com o resto
        if rest.strip():
            need_hook = True
            if kind == 'addAttributeModifiers':
                hook_sig = 'public void onEffectAdded(LivingEntity ' + e + ', int ' + a + ') {'
            else:
                hook_sig = 'public void onEffectRemoved(LivingEntity ' + e + ', int ' + a + ') {'
            hook = (hook_sig + sep + '        AttributeMap ' + m + ' = ' + e + '.getAttributes();' + sep +
                    sep.join('    ' + l if l.strip() else l for l in rest.split(sep)) + sep + '    }')
            # inserir antes do metodo que acabamos de escrever
            anchor = newsig
            kk = t.find(anchor)
            t = t[:kk] + hook + sep + sep + '    ' + t[kk:]

    if need_hook and 'IMNAEffectHook' not in t:
        mdecl = re.search(r'(public (?:abstract )?class \w+ extends [\w.<>, ?]+)( implements [\w.<>, ?]+)? \{', t)
        if mdecl:
            if mdecl.group(2):
                t = t[:mdecl.start()] + mdecl.group(1) + mdecl.group(2) + ', com.mna.api.effects.IMNAEffectHook {' + t[mdecl.end():]
            else:
                t = t[:mdecl.start()] + mdecl.group(1) + ' implements com.mna.api.effects.IMNAEffectHook {' + t[mdecl.end():]
        else:
            print('  decl nao achada em', p)
            FAILS.append(p + ':decl')
    if t != o:
        save(p, t)
        return True
    return False


print('== efeitos ==')
n = 0
for dp, _, fns in os.walk('src/main/java/com/mna/effects'):
    for f in fns:
        if f.endswith('.java'):
            if migrate_effect_file(os.path.join(dp, f)):
                n += 1
print('  arquivos migrados:', n)

# ============================================================
# handleUpdateTag
# ============================================================
print('== handleUpdateTag ==')
n = 0
for p in all_java():
    t = load(p)
    o = t
    t = re.sub(r'super\.handleUpdateTag\((\w+)\);', r'super.handleUpdateTag(\1, com.mna.api.tools.MNAItemData.provider());', t)
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

# ============================================================
# Enchantments (vanilla) short-name
# ============================================================
print('== Enchantments curto ==')
n = 0
for p in all_java():
    t = load(p)
    if 'import net.minecraft.world.item.enchantment.Enchantments;' not in t:
        continue
    o = t
    t = t.replace('Enchantments.FLAMING_ARROWS', 'Enchantments.FLAME')
    t = re.sub(r'([\w.()]+)\.getEnchantmentLevel\((Enchantments\.\w+)\)',
               r'EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(\2), \1)', t)
    t = re.sub(r'EnchantmentHelper\.getEnchantmentLevel\((Enchantments\.\w+), ([\w.]+|\([\w.]+\)[\w.]+)\)',
               r'EnchantmentHelper.getEnchantmentLevel((\2).level().registryAccess().holderOrThrow(\1), \2)', t)
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

# ============================================================
# ItemEldritchOrb
# ============================================================
print('== ItemEldritchOrb ==')
p = ROOT + 'items/artifice/curio/ItemEldritchOrb.java'
t = load(p)
t = t.replace('Map enchantments = EnchantmentUtils.enchantmentMap((ItemStack)book);',
              'Map<Enchantment, Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)book);')
t = re.sub(r'this\.allowedEnchantments\.add\((Enchantments\.\w+)\);',
           r'this.allowedEnchantments.add(net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(\1).value());', t)
t = re.sub(r'e\.getKey\(\) == (Enchantments\.\w+)',
           r'e.getKey() == net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(\1).value()', t)
t = re.sub(r'enchants\.containsKey\((Enchantments\.\w+)\)',
           r'enchants.containsKey(net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(\1).value())', t)
t = re.sub(r'enchants\.get\((Enchantments\.\w+)\)',
           r'enchants.get(net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(\1).value())', t)
save(p, t)
print('  ok')

# ============================================================
# RecipeHolder sites
# ============================================================
print('== RecipeHolder ==')
p = ROOT + 'api/items/ITieredItem.java'
t = load(p)
t = t.replace('List recipes = world.getRecipeManager().getRecipes().parallelStream()',
              'List<net.minecraft.world.item.crafting.RecipeHolder<?>> recipes = world.getRecipeManager().getRecipes().parallelStream()')
t = t.replace('r.getResultItem(world.registryAccess())', 'r.value().getResultItem(world.registryAccess())')
t = t.replace('if (a instanceof IMARecipe) {', 'if (a.value() instanceof IMARecipe) {')
t = t.replace('IMARecipe aR = (IMARecipe)a;', 'IMARecipe aR = (IMARecipe)a.value();')
t = t.replace('if (b instanceof IMARecipe) {', 'if (b.value() instanceof IMARecipe) {')
t = t.replace('IMARecipe bR = (IMARecipe)b;', 'IMARecipe bR = (IMARecipe)b.value();')
t = t.replace('if (r instanceof IMARecipe) {', 'if (r.value() instanceof IMARecipe) {')
t = t.replace('IMARecipe mR = (IMARecipe)r;', 'IMARecipe mR = (IMARecipe)r.value();')
save(p, t)

p = ROOT + 'blocks/tileentities/RunicAnvilTile.java'
t = load(p)
t = t.replace('filter(r -> r.getType() == RecipeInit.MANAWEAVING_RECIPE_TYPE.get() && ((ManaweavingRecipe)r).matches(enchantments)).map(r -> (ManaweavingRecipe)r)',
              'filter(r -> r.value().getType() == RecipeInit.MANAWEAVING_RECIPE_TYPE.get() && ((ManaweavingRecipe)r.value()).matches(enchantments)).map(r -> (ManaweavingRecipe)r.value())')
save(p, t)

p = ROOT + 'api/blocks/tile/MultiblockTile.java'
t = load(p)
t = t.replace('if (r instanceof IMultiblockDefinition) {', 'if (r.value() instanceof IMultiblockDefinition) {')
t = t.replace('this._cachedMultiblockRecipe = (IMultiblockDefinition)r;', 'this._cachedMultiblockRecipe = (IMultiblockDefinition)r.value();')
save(p, t)

p = ROOT + 'blocks/tileentities/EldrinAltarTile.java'
t = load(p)
t = t.replace('if (r instanceof EldrinAltarRecipe) {', 'if (r.value() instanceof EldrinAltarRecipe) {')
t = t.replace('this.__cachedRecipe = (EldrinAltarRecipe)r;', 'this.__cachedRecipe = (EldrinAltarRecipe)r.value();')
save(p, t)

p = ROOT + 'gui/widgets/SpellPartList.java'
t = load(p)
t = t.replace('filter(r -> r.getId().equals(p.getRegistryName()))', 'filter(r -> r.id().equals(p.getRegistryName()))')
save(p, t)

p = ROOT + 'tools/debugging/SpellBalanceCalculator.java'
t = load(p)
t = t.replace('filter(r -> r.getType() == RecipeInit.SHAPE_TYPE.get()).map(r -> (ShapeRecipe)r).filter(r -> r.getTier() <= tier).map(r -> (Shape)Registries.Shape.getValue(r.getId()))',
              'filter(r -> r.value().getType() == RecipeInit.SHAPE_TYPE.get()).map(r -> (ShapeRecipe)r.value()).filter(r -> r.getTier() <= tier).map(r -> (Shape)Registries.Shape.getValue(r.id()))')
t = t.replace('filter(r -> r.getType() == RecipeInit.COMPONENT_TYPE.get()).map(r -> (ComponentRecipe)r).filter(r -> r.getTier() <= tier).map(r -> (SpellEffect)Registries.SpellEffect.getValue(r.getId()))',
              'filter(r -> r.value().getType() == RecipeInit.COMPONENT_TYPE.get()).map(r -> (ComponentRecipe)r.value()).filter(r -> r.getTier() <= tier).map(r -> (SpellEffect)Registries.SpellEffect.getValue(r.id()))')
save(p, t)
print('  ok')

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
