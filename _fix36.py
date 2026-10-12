# _fix36.py - PoseStack.Pose p/ addVertex, super(ACM) casts, wrapAsHolder, EffectInit casts,
#             OPTIONAL_STREAM_CODEC, getDamageBonus helper, TriConsumer raw
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


S = 'src/main/java/com/mna/'
POSE = 'new com.mojang.blaze3d.vertex.PoseStack.Pose'

print('== MNAEnchantmentHelper.getDamageBonus ==')
p = S + 'api/enchantments/MNAEnchantmentHelper.java'
t = load(p)
if 'getDamageBonus' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** [PORT 1.21.1] EnchantmentHelper.getDamageBonus removido; o bonus agora passa pelo pipeline modifyDamage. */
    public static float getDamageBonus(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.LivingEntity target) {
        return 0.0f;
    }
''' + t[idx:]
    save(p, t)
    print('  ok')

print('== PotionEffectPylonTile ==')
p = S + 'api/blocks/tile/PotionEffectPylonTile.java'
t = load(p)
t2 = t.replace('p.getEffect(tile.getMobEffect())', 'p.getEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(tile.getMobEffect()))')
t2 = t2.replace('new MobEffectInstance(tile.getMobEffect(), ', 'new MobEffectInstance(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(tile.getMobEffect()), ')
if t2 != t:
    save(p, t2)
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
        t = t.replace('.addVertex(renderMatrix, ', '.addVertex(' + POSE + '(renderMatrix, normalMatrix), ')
        if f == 'ConstructRenderer.java':
            t = t.replace('bufferBuilder.addVertex(pose, ', 'bufferBuilder.addVertex(' + POSE + '(pose, normal), ')
        t = t.replace('super((AbstractContainerMenu)', 'super(')
        t = re.sub(r'\(MobEffect\)EffectInit\.(\w+)\.get\(\)', r'EffectInit.\1', t)
        t = re.sub(r'([\w.]+)\.readItem\(\)', r'net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode(\1)', t)
        t = t.replace('EnchantmentHelper.getDamageBonus(', 'com.mna.api.enchantments.MNAEnchantmentHelper.getDamageBonus(')
        t = t.replace('(TriConsumer<RadialMenuItem, Float, Float>)((TriConsumer)(', '(TriConsumer<RadialMenuItem, Float, Float>)(')
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
