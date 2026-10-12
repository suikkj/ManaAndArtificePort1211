# _fix19.py - campos allowedEnchantments restantes, renames de enchants 1.21.1, getDefaultAttributeModifiers
import os
import re

FAILS = []
RK = 'net.minecraft.resources.ResourceKey<Enchantment>'
H = 'net.minecraft.core.Holder<Enchantment>'


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


print('== campos restantes ==')
for rel in ['items/artifice/FortuneRing.java', 'items/artifice/HellfireTrident.java', 'items/relic/AstroBlade.java']:
    p = 'src/main/java/com/mna/' + rel
    t = load(p)
    t = rep(t, 'private final ArrayList<Enchantment> allowedEnchantments = new ArrayList();',
            'private final ArrayList<' + RK + '> allowedEnchantments = new ArrayList();', rel)
    save(p, t)

print('== map decls ==')
for rel in ['items/sorcery/MagicStaff.java', 'items/artifice/HellfireTrident.java', 'items/relic/AstroBlade.java']:
    p = 'src/main/java/com/mna/' + rel
    t = load(p)
    t = t.replace('Map enchantments = EnchantmentUtils.enchantmentMap((ItemStack)book);',
                  'Map<' + H + ', Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)book);')
    save(p, t)
print('  ok')

print('== renames enchants 1.21.1 ==')
total = 0
for p in all_java():
    t = load(p)
    if 'import net.minecraft.world.item.enchantment.Enchantments;' not in t:
        continue
    o = t
    t = t.replace('Enchantments.MOB_LOOTING', 'Enchantments.LOOTING')
    t = t.replace('Enchantments.BLOCK_EFFICIENCY', 'Enchantments.EFFICIENCY')
    t = t.replace('Enchantments.FISHING_SPEED', 'Enchantments.LURE')
    t = t.replace('Enchantments.POWER_ARROWS', 'Enchantments.POWER')
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total)

print('== getDefaultAttributeModifiers ==')
DAM = '''    public net.minecraft.world.item.component.ItemAttributeModifiers getDefaultAttributeModifiers() {
        net.minecraft.world.item.component.ItemAttributeModifiers.Builder builder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
        this.defaultModifiers.forEach((attribute, modifier) -> builder.add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifier, net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND));
        return builder.build();
    }'''
for rel, old in [
        ('items/relic/AstroBlade.java', '''    public Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_43274_) {
        return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_43274_);
    }'''),
        ('items/artifice/HellfireTrident.java', '''    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
        return pEquipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
    }''')]:
    p = 'src/main/java/com/mna/' + rel
    t = load(p)
    t = rep(t, old, DAM, rel + ' DAM')
    save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
