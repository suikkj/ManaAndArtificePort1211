# _fix23.py - symbol:get() sites (Holder.value(), holder direto, x.value())
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


print('== globais ==')
total = 0
for p in all_java():
    t = load(p)
    o = t
    t = t.replace('(Attribute)NeoForgeMod.SWIM_SPEED.get()', 'NeoForgeMod.SWIM_SPEED')
    t = t.replace('(FluidType)NeoForgeMod.LAVA_TYPE.get()', 'NeoForgeMod.LAVA_TYPE')
    t = t.replace('NeoForgeMod.LAVA_TYPE.get()', 'NeoForgeMod.LAVA_TYPE.value()')
    t = t.replace('(IChunkMagic)this.holder.get()', 'this.holder')
    t = t.replace('((Biome)biome.get())', 'biome.value()')
    t = t.replace('((Biome)b.get())', 'b.value()')
    t = t.replace('registry.getKey(((Structure)structure.get()))', 'registry.getKey(structure.value())')
    t = re.sub(r'x\.get\(\) == (\w+_POI)\.get\(\)', r'x.value() == \1.get()', t)
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
