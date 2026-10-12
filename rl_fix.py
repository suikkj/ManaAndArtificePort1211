import os, re, sys

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')

BLOCK = re.compile(r'/\*.*?\*/', re.S)
LINE = re.compile(r'//[^\n]*')

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

# 1-arg: new ResourceLocation( <expr sem virgula de topo> )
pat1 = re.compile(r'new ResourceLocation\(\s*((?:[^(),]|\([^()]*\))*?)\s*\)')

changed = []
tot = 0
for p in files:
    raw = open(p, encoding='utf-8', errors='replace').read()
    body = sc(raw)
    hits = pat1.findall(body)
    if not hits:
        continue
    new = pat1.sub(lambda m: 'ResourceLocation.parse(' + m.group(1) + ')', body)
    if new != body:
        tot += len(hits)
        open(p, 'w', encoding='utf-8').write(new)
        changed.append((os.path.basename(p), len(hits)))

print('arquivos alterados:', len(changed), ' substituicoes:', tot)
for c in changed[:20]:
    print('  ', c)
