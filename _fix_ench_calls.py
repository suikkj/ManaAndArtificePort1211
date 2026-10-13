import os, re

SRC = 'src/main/java'
# 1) EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.X, ...) -> MNAEnchantmentHelper.getEnchantmentLevel(EnchantmentInit.X.getKey(), ...)
# 2) ...MNAEnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.X, ...) -> (..., EnchantmentInit.X.getKey(), ...)
pat1 = re.compile(r'EnchantmentHelper\.getEnchantmentLevel\(EnchantmentInit\.(\w+),')
pat2 = re.compile(r'MNAEnchantmentHelper\.getItemEnchantmentLevel\(EnchantmentInit\.(\w+),')

changed = []
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding='utf-8', errors='replace').read()
        orig = t
        # nao tocar no proprio MNAEnchantmentHelper
        if 'MNAEnchantmentHelper.java' in p:
            continue
        t = pat1.sub(lambda m: 'com.mna.api.enchantments.MNAEnchantmentHelper.getEnchantmentLevel(EnchantmentInit.%s.getKey(),' % m.group(1), t)
        t = pat2.sub(lambda m: 'com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.%s.getKey(),' % m.group(1), t)
        if t != orig:
            open(p, 'w', encoding='utf-8', newline='').write(t)
            changed.append(p)

print('arquivos alterados:', len(changed))
for p in changed:
    print('  ', p.replace(os.sep, '/'))
