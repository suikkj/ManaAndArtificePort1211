# _fix50.py - NAAG type witness, AnimusBlock fields, BlockItemRegistration, GreaterAnimus attrs
import os
import re

S = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


print('== 1. NAAG type witness (global) ==')
n = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = re.sub(r'new NearestAttackableTargetGoal\(this, (\w+)\.class, ', r'new NearestAttackableTargetGoal<\1>(this, \1.class, ', t)
        if t != o:
            save(p, t)
            n += 1
print('  arquivos:', n)

print('== 2. getEffect().value() global ==')
n = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = re.sub(r'((?:MobEffect|Holder<MobEffect>) \w+ = \w+\.getEffect\(\);)', lambda m: m.group(1)[:-1] + '.value();', t)
        if t != o:
            save(p, t)
            n += 1
print('  arquivos:', n)

print('== 3. AnimusBlock ==')
p = S + 'entities/summon/AnimusBlock.java'
t = load(p)
o = t
t = t.replace('this.noJumpDelay', 'this.mnaNoJumpDelay')
if 'mnaNoJumpDelay' in t and 'private int mnaNoJumpDelay' not in t:
    t = t.replace('public class AnimusBlock', 'public class AnimusBlock')
    # declara o campo logo apos a primeira '{' da classe
    idx = t.find('class AnimusBlock')
    brace = t.find('{', idx)
    t = t[:brace + 1] + '\n    private int mnaNoJumpDelay = 0;' + t[brace + 1:]
t = t.replace('this.lyHeadRot', 'this.yHeadRotO')
t = t.replace('areaEffectCloud.setPotion(new Potion(new MobEffectInstance[]{new MobEffectInstance(MobEffects.POISON, 1200, 0)}));',
              'areaEffectCloud.setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(java.util.Optional.empty(), java.util.Optional.empty(), java.util.List.of(new MobEffectInstance(MobEffects.POISON, 1200, 0))));')
t = t.replace('areaeffectcloud.setPotion(new Potion(new MobEffectInstance[]{new MobEffectInstance(MobEffects.POISON, 1200, 0)}));',
              'areaeffectcloud.setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(java.util.Optional.empty(), java.util.Optional.empty(), java.util.List.of(new MobEffectInstance(MobEffects.POISON, 1200, 0))));')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== 4. BlockItemRegistration ==')
p = S + 'blocks/BlockItemRegistration.java'
t = load(p)
o = t
t = t.replace('BuiltInRegistries.ITEM.getRegistryKey()', 'BuiltInRegistries.ITEM.key()')
t = t.replace('.map(RegistryObject::get)', '.map(b -> b.get())')
t = re.sub(r'ResourceKey\.create\(ResourceKey\.createRegistryKey\(ResourceLocation\.fromNamespaceAndPath\("mna", "[a-z_0-9]+"\)\)\), ', 'net.minecraft.core.registries.Registries.ITEM, ', t)
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== 5. GreaterAnimus attribute modifiers ==')
p = S + 'entities/summon/GreaterAnimus.java'
t = load(p)
old = '''                Multimap attrs = this.heldItem.getItem().getAttributeModifiers(EquipmentSlot.MAINHAND, this.heldItem);
                attrs.forEach((a, m) -> {
                    try {
                        AttributeInstance ai = this.getAttribute((Attribute)a);
                        if (ai != null) {
                            ai.addPermanentModifier(m);
                        }
                    }'''
new = '''                net.minecraft.world.item.component.ItemAttributeModifiers attrs = this.heldItem.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
                attrs.modifiers().forEach(entry -> {
                    try {
                        AttributeInstance ai = this.getAttribute(entry.attribute());
                        if (ai != null) {
                            ai.addPermanentModifier(entry.modifier());
                        }
                    }'''
if old in t:
    t = t.replace(old, new)
    save(p, t)
    print('  ok')
else:
    print('  FALHOU (shape diferente)')
print()
print('FIM')
