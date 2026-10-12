# _fix18.py - allowedEnchantments -> ResourceKey; RLoc/RitualReagent; contains sites
import os
import re

FAILS = []
MREG = 'com.mna.api.enchantments.MNAEnchantmentRegistry'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = new.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + '] x' + str(t.count(o)))
    return t.replace(o, n)


RK = 'net.minecraft.resources.ResourceKey<Enchantment>'
H = 'net.minecraft.core.Holder<Enchantment>'

print('== RLoc/RitualReagent ==')
p = 'src/main/java/com/mna/api/tools/RLoc.java'
t = load(p)
t = rep(t, 'return new ResourceLocation("mna", path);', 'return ResourceLocation.fromNamespaceAndPath("mna", path);', 'rloc')
save(p, t)
p = 'src/main/java/com/mna/rituals/RitualReagent.java'
t = load(p)
t = rep(t, 'this(new ResourceLocation(namespaceID, path));', 'this(ResourceLocation.fromNamespaceAndPath(namespaceID, path));', 'reagent')
save(p, t)

print('== MagicStaff ==')
p = 'src/main/java/com/mna/items/sorcery/MagicStaff.java'
t = load(p)
t = rep(t, 'private final ArrayList<Enchantment> allowedEnchantments = new ArrayList();',
        'private final ArrayList<' + RK + '> allowedEnchantments = new ArrayList();', 'staff field')
t = rep(t, 'protected ArrayList<Enchantment> getAllowedEnchantments() {', 'protected ArrayList<' + RK + '> getAllowedEnchantments() {', 'staff getter')
t = rep(t, 'protected void buildAllowedEnchantments(ArrayList<Enchantment> defaultList) {', 'protected void buildAllowedEnchantments(ArrayList<' + RK + '> defaultList) {', 'staff build')
t = re.sub(r'\(Enchantment\)EnchantmentInit\.(\w+)\.get\(\)', r'EnchantmentInit.\1.getKey()', t)
t = rep(t, '''        for (Enchantment ench : enchantments.keySet()) {
            if (this.getAllowedEnchantments().contains(ench)) continue;
            return false;
        }
        return true;''',
        '''        for (''' + H + ''' ench : enchantments.keySet()) {
            if (this.getAllowedEnchantments().contains(ench.unwrapKey().orElse(null))) continue;
            return false;
        }
        return true;''', 'staff book')
t = rep(t, 'return this.getAllowedEnchantments().contains(enchantment);',
        'return this.getAllowedEnchantments().contains(' + MREG + '.keyOf(enchantment));', 'staff table')
save(p, t)

print('== FortuneRing ==')
p = 'src/main/java/com/mna/items/artifice/FortuneRing.java'
t = load(p)
t = rep(t, 'private ArrayList<Enchantment> getAllowedEnchantments() {', 'private ArrayList<' + RK + '> getAllowedEnchantments() {', 'fr getter')
t = rep(t, 'return this.getAllowedEnchantments().contains(enchantment);',
        'return this.getAllowedEnchantments().contains(' + MREG + '.keyOf(enchantment));', 'fr table')
save(p, t)

print('== HellfireTrident ==')
p = 'src/main/java/com/mna/items/artifice/HellfireTrident.java'
t = load(p)
t = rep(t, 'protected ArrayList<Enchantment> getAllowedEnchantments() {', 'protected ArrayList<' + RK + '> getAllowedEnchantments() {', 'ht getter')
t = re.sub(r'\(Enchantment\)EnchantmentInit\.(\w+)\.get\(\)', r'EnchantmentInit.\1.getKey()', t)
t = rep(t, '''        for (Enchantment ench : enchantments.keySet()) {
            if (this.getAllowedEnchantments().contains(ench)) continue;
            return false;
        }
        return true;''',
        '''        for (''' + H + ''' ench : enchantments.keySet()) {
            if (this.getAllowedEnchantments().contains(ench.unwrapKey().orElse(null))) continue;
            return false;
        }
        return true;''', 'ht book')
t = rep(t, 'return this.getAllowedEnchantments().contains(enchantment);',
        'return this.getAllowedEnchantments().contains(' + MREG + '.keyOf(enchantment));', 'ht table')
save(p, t)

print('== AstroBlade ==')
p = 'src/main/java/com/mna/items/relic/AstroBlade.java'
t = load(p)
t = rep(t, 'protected ArrayList<Enchantment> getAllowedEnchantments() {', 'protected ArrayList<' + RK + '> getAllowedEnchantments() {', 'ab getter')
t = re.sub(r'\(Enchantment\)EnchantmentInit\.(\w+)\.get\(\)', r'EnchantmentInit.\1.getKey()', t)
t = rep(t, '''        for (Enchantment ench : enchantments.keySet()) {
            if (this.getAllowedEnchantments().contains(ench)) continue;
            return false;
        }
        return true;''',
        '''        for (''' + H + ''' ench : enchantments.keySet()) {
            if (this.getAllowedEnchantments().contains(ench.unwrapKey().orElse(null))) continue;
            return false;
        }
        return true;''', 'ab book')
t = rep(t, 'return this.getAllowedEnchantments().contains(enchantment);',
        'return this.getAllowedEnchantments().contains(' + MREG + '.keyOf(enchantment));', 'ab table')
save(p, t)

print('== BoneScythe ==')
p = 'src/main/java/com/mna/items/sorcery/BoneScythe.java'
t = load(p)
t = rep(t, 'protected void buildAllowedEnchantments(ArrayList<Enchantment> defaultList) {', 'protected void buildAllowedEnchantments(ArrayList<' + RK + '> defaultList) {', 'bs build')
t = rep(t, 'defaultList.remove(EnchantmentInit.BLUDGEONING.get());', 'defaultList.remove(EnchantmentInit.BLUDGEONING.getKey());', 'bs remove')
save(p, t)

# import MNAEnchantmentRegistry onde usado
for rel in ['items/sorcery/MagicStaff.java', 'items/artifice/FortuneRing.java', 'items/artifice/HellfireTrident.java', 'items/relic/AstroBlade.java']:
    p = 'src/main/java/com/mna/' + rel
    t = load(p)
    if 'MNAEnchantmentRegistry.' in t and 'import com.mna.api.enchantments.MNAEnchantmentRegistry;' not in t:
        sep = '\r\n' if '\r\n' in t else '\n'
        lines = t.split(sep)
        idx = None
        for i, l in enumerate(lines):
            if l.startswith('import '):
                idx = i
        lines.insert(idx + 1, 'import com.mna.api.enchantments.MNAEnchantmentRegistry;')
        save(p, sep.join(lines))
        print('  import ok:', rel)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
