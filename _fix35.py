# _fix35.py - new BossMonster.CastSpellAtTargetGoal -> new CastSpellAtTargetGoal (raw outer),
#             stack=stack.set -> statement, setPotionContents helper, NeoForge.EVENT_BUS.post cast, EffectDisjunction
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    if old not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + ']')
    return t.replace(old, new)


S = 'src/main/java/com/mna/'

print('== MNAItemData.setPotionContents ==')
p = S + 'api/tools/MNAItemData.java'
t = load(p)
if 'setPotionContents' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** [PORT 1.21.1] ItemStack.set(...) retorna o valor ANTERIOR; em expressoes, usar este helper. */
    public static ItemStack setPotionContents(ItemStack stack, net.minecraft.world.item.alchemy.PotionContents contents) {
        stack.set(DataComponents.POTION_CONTENTS, contents);
        return stack;
    }
''' + t[idx:]
    save(p, t)
    print('  ok')


def find_matching(s, start):
    """start = indice do '('; retorna indice do ')' correspondente."""
    depth = 0
    for i in range(start, len(s)):
        c = s[i]
        if c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return i
    return -1


print('== transform: new ItemStack(...).set(POTION_CONTENTS, ARG) em expressao ==')
total = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        while True:
            m = re.search(r'new ItemStack\(', t)
            found = False
            pos = 0
            while True:
                m = re.search(r'new ItemStack\(', t[pos:])
                if not m:
                    break
                st = pos + m.start()
                open_p = st + len('new ItemStack')
                close_p = find_matching(t, open_p)
                after = t[close_p + 1:close_p + 1 + 6 + 42]
                if t[close_p + 1:].startswith('.set(DataComponents.POTION_CONTENTS, '):
                    set_open = close_p + 1 + len('.set(DataComponents.POTION_CONTENTS, ')
                    arg_close = find_matching(t, close_p + 1 + 4)
                    receiver = t[st:close_p + 1]
                    arg = t[set_open:arg_close]
                    t = t[:st] + 'com.mna.api.tools.MNAItemData.setPotionContents(' + receiver + ', ' + arg + ')' + t[arg_close + 1:]
                    found = True
                    pos = st + 20
                else:
                    pos = close_p + 1
            if not found:
                break
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print('== globais ==')
total = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = t.replace('new BossMonster.CastSpellAtTargetGoal(', 'new CastSpellAtTargetGoal(')
        t = re.sub(r'(\w+) = \1\.set\((DataComponents\.[A-Z_]+), ([^;]+)\);', r'\1.set(\2, \3);', t)
        t = t.replace('NeoForge.EVENT_BUS.post((Event)', 'NeoForge.EVENT_BUS.post(')
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print('== EffectDisjunction ==')
p = S + 'effects/harmful/EffectDisjunction.java'
t = load(p)
t = rep(t, 'Map chants = EnchantmentUtils.enchantmentMap((ItemStack)stack);',
        'java.util.Map<net.minecraft.core.Holder<Enchantment>, Integer> chants = EnchantmentUtils.enchantmentMap((ItemStack)stack);', 'ed-map')
t = rep(t, 'List<Enchantment> keys = Arrays.asList(chants.keySet().toArray(new Enchantment[0]));',
        'List<net.minecraft.core.Holder<Enchantment>> keys = new ArrayList<net.minecraft.core.Holder<Enchantment>>(chants.keySet());', 'ed-keys')
t = rep(t, 'for (Enchantment e : keys) {', 'for (net.minecraft.core.Holder<Enchantment> e : keys) {', 'ed-loop')
t = rep(t, 'if (GeneralConfigValues.DisjunctionBlacklist.contains(String.valueOf(com.mna.api.enchantments.MNAEnchantmentRegistry.idOf(e)))) continue;',
        'if (GeneralConfigValues.DisjunctionBlacklist.contains(String.valueOf(com.mna.api.enchantments.MNAEnchantmentRegistry.idOf(e.value())))) continue;', 'ed-idof')
t = rep(t, 'EnchantmentHelper.setEnchantments((Map)chants, (ItemStack)stack);',
        'EnchantmentHelper.setEnchantments(chants, (ItemStack)stack);', 'ed-set')
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
