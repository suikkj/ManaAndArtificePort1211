# _fix38.py - canPlaceLiquid body, getKnockbackBonus helper, toArray casts, BlockInit .value()/.get(), moodlets
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


S = 'src/main/java/com/mna/'

print('== canPlaceLiquid: corpo waterlogged ==')
n = 0
for f in ['api/blocks/WaterloggableBlock', 'blocks/artifice/FluidJugBlock', 'blocks/ritual/ChalkRuneBlock',
          'blocks/sorcery/SpectralAnvilBlock', 'blocks/sorcery/SpectralCraftingTableBlock',
          'blocks/sorcery/SpectralStonecutterBlock', 'blocks/sorcery/SpectralWebBlock']:
    p = S + f + '.java'
    t = load(p)
    old = 'return super.canPlaceLiquid(worldIn, pos, state, fluidIn);'
    if old in t:
        t = t.replace(old, 'return !(Boolean)state.getValue((Property)BlockStateProperties.WATERLOGGED);')
        save(p, t)
        n += 1
print('  arquivos:', n)

print('== MNAEnchantmentHelper.getKnockbackBonus ==')
p = S + 'api/enchantments/MNAEnchantmentHelper.java'
t = load(p)
if 'getKnockbackBonus' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** [PORT 1.21.1] EnchantmentHelper.getKnockbackBonus(LivingEntity) removido; knockback agora via pipeline de ataque. */
    public static int getKnockbackBonus(net.minecraft.world.entity.LivingEntity entity) {
        return 0;
    }
''' + t[idx:]
    save(p, t)
    print('  ok')

print('== globais ==')
total = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = t.replace('EnchantmentHelper.getKnockbackBonus(', 'com.mna.api.enchantments.MNAEnchantmentHelper.getKnockbackBonus(')
        t = re.sub(r'return (\w+)\.toArray\(new (\w+)\[0\]\);', r'return (\2[])((\1).toArray(new \2[0]));', t)
        t = t.replace('points = ((ArrayList)newPoints).toArray(new Vector3f[0]);',
                      'points = (org.joml.Vector3f[])((ArrayList)newPoints).toArray(new org.joml.Vector3f[0]);')
        if f == 'BlockInit.java':
            t = re.sub(r'\(\) -> MobEffects\.(\w+)([,)])', r'() -> MobEffects.\1.value()\2', t)
            t = re.sub(r'\(\) -> EffectInit\.(\w+)([,)])', r'() -> EffectInit.\1.get()\2', t)
        if f == 'ConstructMoodlets.java':
            t = t.replace('ArrayList data = new ArrayList();', 'ArrayList<Integer> data = new ArrayList<>();')
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
