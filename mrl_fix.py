import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')

BLOCK = re.compile(r'/\*.*?\*/', re.S)
LINE = re.compile(r'//[^\n]*')

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

# ESTRITO: event.register(Ident.Ident)  -- apenas referencias simples a campos estaticos
# (sem parenteses -> sem risco de comer argumento de funcao)
pat = re.compile(r'\bevent\.register\(\s*([A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+)\s*\)')

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

changed = []
tot = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    hits = pat.findall(body)
    if not hits:
        continue
    new = pat.sub(lambda m: f'event.register(ModelResourceLocation.standalone({m.group(1)}))', body)
    if new == body:
        continue
    if 'import net.minecraft.client.resources.model.ModelResourceLocation;' not in new:
        lines = new.split('\n')
        last = max(i for i, l in enumerate(lines) if l.startswith('import '))
        lines.insert(last + 1, 'import net.minecraft.client.resources.model.ModelResourceLocation;')
        new = '\n'.join(lines)
    open(p, 'w', encoding='utf-8').write(new)
    tot += len(hits)
    changed.append((os.path.basename(p), len(hits), hits[:3]))

print('arquivos:', len(changed), ' substituicoes:', tot)
for c in changed:
    print('  ', c[0], c[1], c[2])
