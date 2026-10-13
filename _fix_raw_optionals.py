import os, re

SRC = 'src/main/java'
RH = 'net.minecraft.world.item.crafting.RecipeHolder'
# declaracoes raw: Optional X = ...byKey(
pat = re.compile(r'Optional\s+(\w+)\s*=\s*([^;\n]*byKey\()')
count = 0
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding='utf-8', errors='replace').read()
        t2 = pat.sub(lambda m: 'Optional<%s<?>> %s = %s' % (RH, m.group(1), m.group(2)), t)
        if t2 != t:
            open(p, 'w', encoding='utf-8', newline='').write(t2)
            n = len(pat.findall(t))
            count += n
            print(f'  {n}  {p.replace(os.sep, "/")}')
print('total:', count)
