# _fix25.py - SpawnPlacementTypes, hasTag->CUSTOM_DATA, parseOptional.orElse, isAddedToLevel,
#             CauldronInteraction.map().put, mapas raw tipados, enchantmentMap tipado
import os
import re

FAILS = []


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


ENCH_MAP = 'java.util.Map<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer>'

print('== globais ==')
total = 0
counts = {}
for p in all_java():
    t = load(p)
    o = t
    t = t.replace('SpawnPlacements.Type.', 'net.minecraft.world.entity.SpawnPlacementTypes.')
    t = t.replace('.hasTag()', '.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)')
    t = re.sub(r'(ItemStack\.parseOptional\([^;]*?\))\.orElse\(ItemStack\.EMPTY\)', r'\1', t)
    t = t.replace('.isAddedToWorld()', '.isAddedToLevel()')
    t = re.sub(r'CauldronInteraction\.(\w+)\.put\(', r'CauldronInteraction.\1.map().put(', t)
    # mapas raw -> tipados
    t = t.replace('HashMap locatedEntities = new HashMap();',
                  'HashMap<EntityType<?>, List<Animal>> locatedEntities = new HashMap<>();')
    t = t.replace('HashMap blockCounts = new HashMap();', 'HashMap<Block, Integer> blockCounts = new HashMap<>();')
    t = t.replace('HashMap changes = new HashMap();', 'HashMap<Long, Integer> changes = new HashMap<>();')
    t = t.replace('((HashMap)e.getValue()).entrySet()', '((HashMap<Affinity, Float>)e.getValue()).entrySet()')
    t = t.replace('Map itemEnchantments = EnchantmentUtils.enchantmentMap(', ENCH_MAP + ' itemEnchantments = EnchantmentUtils.enchantmentMap(')
    t = t.replace('Map enchs = EnchantmentUtils.enchantmentMap(', ENCH_MAP + ' enchs = EnchantmentUtils.enchantmentMap(')
    t = t.replace('Map enchantments = EnchantmentUtils.enchantmentMap(', ENCH_MAP + ' enchantments = EnchantmentUtils.enchantmentMap(')
    t = t.replace('HashMap<Enchantment, Integer> moved = new HashMap<Enchantment, Integer>();',
                  'HashMap<net.minecraft.core.Holder<Enchantment>, Integer> moved = new HashMap<>();')
    t = t.replace('moved.put((Enchantment)e.getKey(), (Integer)e.getValue());', 'moved.put(e.getKey(), (Integer)e.getValue());')
    if t != o:
        save(p, t)
        total += 1
        counts[p] = counts.get(p, 0) + 1
print('  arquivos:', total)
for k in sorted(counts):
    print('   ', k)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
