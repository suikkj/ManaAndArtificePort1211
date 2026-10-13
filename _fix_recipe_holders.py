import os, re

SRC = 'src/main/java'

changed = []
# 1) guia: pattern.get()/resolved.get() -> .value() (RecipeHolder unwrap)
guide_dir = os.path.join(SRC, 'com', 'mna', 'guide', 'recipe')
for fn in os.listdir(guide_dir):
    if not fn.endswith('.java'):
        continue
    p = os.path.join(guide_dir, fn)
    t = open(p, encoding='utf-8', errors='replace').read()
    orig = t
    t = t.replace('pattern.get()', 'pattern.get().value()')
    t = t.replace('resolved.get()', 'resolved.get().value()')
    if t != orig:
        open(p, 'w', encoding='utf-8', newline='').write(t)
        changed.append(p)

print('guide/recipe alterados:', len(changed))
for c in changed:
    print('  ', c.replace(os.sep, '/'))

# 2) varredura global: byType( / byKey( + cast de holder — listar candidatos
print()
print('=== candidatos globais (byType/byKey):')
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding='utf-8', errors='replace').read()
        if 'byType(' in t or 'byKey(' in t:
            for i, line in enumerate(t.split('\n'), 1):
                if 'byType(' in line or 'byKey(' in line:
                    print(f'  {p.replace(os.sep, "/")}:{i}: {line.strip()[:110]}')
