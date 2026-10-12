# _fix29.py - isSameItemSameComponents, BuiltInRegistries.getValue->get, canChangeDimensions helper,
#             setupOverlayRenderState, EntityInit register casts
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


print('== MATools helper ==')
p = 'src/main/java/com/mna/tools/MATools.java'
t = load(p)
if 'canChangeDimensions(net.minecraft.world.entity.Entity' in t:
    print('  ja existe')
else:
    idx = t.rstrip().rfind('}')
    helper = '''
    /** [PORT 1.21.1] Entity.canChangeDimensions() (sem args) virou canChangeDimensions(Level, Level). */
    public static boolean canChangeDimensions(net.minecraft.world.entity.Entity entity) {
        return entity.canChangeDimensions(entity.level(), entity.level());
    }
'''
    t = t[:idx] + helper + t[idx:]
    save(p, t)
    print('  ok helper adicionado')

print('== globais ==')
total = 0
n_cc = 0
for p in all_java():
    t = load(p)
    o = t
    t = t.replace('.isSameItemSameTags(', '.isSameItemSameComponents(')
    t = re.sub(r'BuiltInRegistries\.(\w+)\.getValue\(', r'BuiltInRegistries.\1.get(', t)
    t, n = re.subn(r'([\w.()]+?)\.canChangeDimensions\(\)', r'com.mna.api.tools.MATools.canChangeDimensions(\1)', t)
    n_cc += n
    t = t.replace('gui.setupOverlayRenderState(true, false);',
                  'com.mojang.blaze3d.systems.RenderSystem.enableBlend(); com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();')
    t = t.replace('event.register((EntityType)', 'event.register(')
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total, '| canChangeDimensions:', n_cc)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
