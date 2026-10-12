# _fix17.py - BuiltInRegistries.ENCHANTMENT -> MNAEnchantmentRegistry/Helper; ItemEldritchOrb; ManaweavingRecipe
import os
import re

FAILS = []
MREG = 'com.mna.api.enchantments.MNAEnchantmentRegistry'
MHEL = 'com.mna.api.enchantments.MNAEnchantmentHelper'
BAD = 'net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT'


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


def add_import(t, imp):
    line = 'import ' + imp + ';'
    if line in t:
        return t
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    idx = None
    for i, l in enumerate(lines):
        if l.startswith('import '):
            idx = i
    lines.insert(idx + 1, line)
    return sep.join(lines)


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


# ============================================================
# 1) global: getItemEnchantmentLevel(BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(K), R) -> helper
# ============================================================
print('== getItemEnchantmentLevel global ==')
total = 0
for p in all_java():
    t = load(p)
    if BAD + '.getHolderOrThrow(' not in t:
        continue
    o = t
    t = re.sub(r'EnchantmentHelper\.getItemEnchantmentLevel\(' + re.escape(BAD) + r'\.getHolderOrThrow\(([^)]+)\), ',
               r'MNAEnchantmentHelper.getItemEnchantmentLevel(\1, ', t)
    if t != o:
        if 'MNAEnchantmentHelper' in t:
            t = add_import(t, MHEL)
        save(p, t)
        total += 1
print('  arquivos:', total)

# ============================================================
# 2) outros usos de BuiltInRegistries.ENCHANTMENT
# ============================================================
print('== outros usos ==')
total = 0
for p in all_java():
    t = load(p)
    if BAD not in t:
        continue
    o = t
    if 'EnchantmentBehaviors.java' in p:
        t = t.replace(BAD + '.getResourceKey(enchantment).map(EnchantmentBehaviors::get).orElse(null)',
                      'java.util.Optional.ofNullable(' + MREG + '.keyOf(enchantment)).map(EnchantmentBehaviors::get).orElse(null)')
    if 'EffectDisjunction.java' in p:
        t = t.replace(BAD + '.getKey(e).toString()', 'String.valueOf(' + MREG + '.idOf(e))')
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total)

# ============================================================
# 3) ItemEldritchOrb
# ============================================================
print('== ItemEldritchOrb ==')
p = 'src/main/java/com/mna/items/artifice/curio/ItemEldritchOrb.java'
t = load(p)
t = rep(t, '    private final ArrayList<Enchantment> allowedEnchantments = new ArrayList();',
        '    private final ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> allowedEnchantments = new ArrayList();', 'orb field')
t = rep(t, '''        this.allowedEnchantments.clear();
        this.allowedEnchantments.add(''' + BAD + '''.getHolderOrThrow(Enchantments.BANE_OF_ARTHROPODS).value());
        this.allowedEnchantments.add(''' + BAD + '''.getHolderOrThrow(Enchantments.FLAME).value());
        this.allowedEnchantments.add(''' + BAD + '''.getHolderOrThrow(Enchantments.KNOCKBACK).value());
        this.allowedEnchantments.add(''' + BAD + '''.getHolderOrThrow(Enchantments.MULTISHOT).value());
        this.allowedEnchantments.add(''' + BAD + '''.getHolderOrThrow(Enchantments.SMITE).value());
        this.allowedEnchantments.add(''' + BAD + '''.getHolderOrThrow(Enchantments.QUICK_CHARGE).value());
    }''',
        '''        this.allowedEnchantments.clear();
        this.allowedEnchantments.add(Enchantments.BANE_OF_ARTHROPODS);
        this.allowedEnchantments.add(Enchantments.FLAME);
        this.allowedEnchantments.add(Enchantments.KNOCKBACK);
        this.allowedEnchantments.add(Enchantments.MULTISHOT);
        this.allowedEnchantments.add(Enchantments.SMITE);
        this.allowedEnchantments.add(Enchantments.QUICK_CHARGE);
    }''', 'orb ctor')
t = rep(t, '''        Map<Enchantment, Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)book);
        for (Enchantment ench : enchantments.keySet()) {
            if (this.allowedEnchantments.contains(ench)) continue;
            return false;
        }
        return true;''',
        '''        Map<net.minecraft.core.Holder<Enchantment>, Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)book);
        for (net.minecraft.core.Holder<Enchantment> ench : enchantments.keySet()) {
            if (this.allowedEnchantments.contains(ench.unwrapKey().orElse(null))) continue;
            return false;
        }
        return true;''', 'orb book')
t = rep(t, '''    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return this.allowedEnchantments.contains(enchantment);
    }''',
        '''    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return this.allowedEnchantments.contains(''' + MREG + '''.keyOf(enchantment));
    }''', 'orb table')
t = rep(t, '''        int numTargets = enchants.containsKey(''' + BAD + '''.getHolderOrThrow(Enchantments.MULTISHOT).value()) ? (Integer)enchants.get(''' + BAD + '''.getHolderOrThrow(Enchantments.MULTISHOT).value()) + 1 : 1;''',
        '''        int numTargets = 1 + ''' + MHEL + '''.getItemEnchantmentLevel(Enchantments.MULTISHOT, orb);''', 'orb multitarget')
t = rep(t, '''    private void applyOrbEnchantmentEffects(Player caster, Mob target, Map<Enchantment, Integer> enchants) {
        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            if ((target instanceof Spider || target instanceof Silverfish || target instanceof Endermite) && e.getKey() == ''' + BAD + '''.getHolderOrThrow(Enchantments.BANE_OF_ARTHROPODS).value()) {''',
        '''    private void applyOrbEnchantmentEffects(Player caster, Mob target, Map<net.minecraft.core.Holder<Enchantment>, Integer> enchants) {
        for (Map.Entry<net.minecraft.core.Holder<Enchantment>, Integer> e : enchants.entrySet()) {
            net.minecraft.resources.ResourceKey<Enchantment> enchKey = e.getKey().unwrapKey().orElse(null);
            if ((target instanceof Spider || target instanceof Silverfish || target instanceof Endermite) && Enchantments.BANE_OF_ARTHROPODS.equals(enchKey)) {''', 'orb effects head')
t = rep(t, 'if (target.isInvertedHealAndHarm() && e.getKey() == ' + BAD + '.getHolderOrThrow(Enchantments.SMITE).value()) {',
        'if (target.isInvertedHealAndHarm() && Enchantments.SMITE.equals(enchKey)) {', 'orb smite')
t = rep(t, 'if (e.getKey() == ' + BAD + '.getHolderOrThrow(Enchantments.FLAME).value()) {',
        'if (Enchantments.FLAME.equals(enchKey)) {', 'orb flame')
t = rep(t, 'if (e.getKey() != Enchantments.KNOCKBACK) continue;',
        'if (!Enchantments.KNOCKBACK.equals(enchKey)) continue;', 'orb knockback')
t = rep(t, '''        long l = delay = enchants.containsKey(''' + BAD + '''.getHolderOrThrow(Enchantments.QUICK_CHARGE).value()) ? (long)(20 - (Integer)enchants.get(''' + BAD + '''.getHolderOrThrow(Enchantments.QUICK_CHARGE).value())) : 20L;''',
        '''        long l = delay = (long)(20 - ''' + MHEL + '''.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack));''', 'orb delay')
t = add_import(t, MHEL)
t = add_import(t, MREG)
save(p, t)

# ============================================================
# 4) ManaweavingRecipe
# ============================================================
print('== ManaweavingRecipe ==')
p = 'src/main/java/com/mna/recipes/manaweaving/ManaweavingRecipe.java'
t = load(p)
t = rep(t, 'if (!this.enchantment.equals(' + BAD + '.getKey(ench)) || this.enchantmentMagnitude != runeEnchantment.get(ench)) continue;',
        'if (!this.enchantment.equals(' + MREG + '.idOf(ench)) || this.enchantmentMagnitude != runeEnchantment.get(ench)) continue;', 'mw matches')
t = rep(t, '''            if (this.enchantment != null) {
                Enchantment resolved;
                if (__enchantRegistry == null) {
                    __enchantRegistry = ''' + BAD + ''';
                }
                if (__enchantRegistry != null && (resolved = (Enchantment)__enchantRegistry.get(this.enchantment)) != null) {
                    LinkedHashMap map = Maps.newLinkedHashMap();
                    map.put(resolved, this.enchantmentMagnitude);
                    EnchantmentHelper.setEnchantments((Map)map, (ItemStack)this.__outputItem);
                }
            }''',
        '''            if (this.enchantment != null) {
                net.minecraft.core.Holder<Enchantment> resolvedHolder = ''' + MREG + '''.holder(this.enchantment);
                if (resolvedHolder != null) {
                    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                    mutable.set(resolvedHolder, this.enchantmentMagnitude);
                    EnchantmentHelper.setEnchantments(this.__outputItem, mutable.toImmutable());
                }
            }''', 'mw result')
t = rep(t, '''        if (this.isEnchantment() && (ench = (Enchantment)''' + BAD + '''.getValue(this.getEnchantment())) != null) {
            out.set(DataComponents.ITEM_NAME, ench.getFullname(this.enchantmentMagnitude));
        }''',
        '''        net.minecraft.core.Holder<Enchantment> enchHolder = ''' + MREG + '''.holder(this.getEnchantment());
        if (this.isEnchantment() && enchHolder != null) {
            out.set(DataComponents.ITEM_NAME, Enchantment.getFullname(enchHolder, this.enchantmentMagnitude));
        }''', 'mw gui')
# assemble faltante
t = rep(t, '''    public boolean matches(Map<Enchantment, Integer> runeEnchantment) {''',
        '''    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input, net.minecraft.core.HolderLookup.Provider registries) {
        return this.getResultItem().copy();
    }

    public boolean matches(Map<Enchantment, Integer> runeEnchantment) {''', 'mw assemble')
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
