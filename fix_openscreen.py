import os, re

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

# NetworkHooks.openScreen(PLAYER, PROVIDER, X) -> PLAYER.openMenu(PROVIDER, X)
# onde PLAYER = (ServerPlayer)((ServerPlayer)algo
pat = re.compile(
    r'NetworkHooks\.openScreen\(\s*'
    r'\(ServerPlayer\)\(\(ServerPlayer\)([A-Za-z_][A-Za-z0-9_.]*)\)\s*,\s*'
    r'\(MenuProvider\)([A-Za-z_][A-Za-z0-9_.]*)\s*,\s*'
    r'([^;]+?)\)\s*;')

tot = 0
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    hits = pat.findall(body)
    if not hits:
        continue
    new = pat.sub(lambda m: f'{m.group(1)}.openMenu({m.group(2)}, {m.group(3)});', body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        fl += 1
        tot += len(hits)

print('arquivos:', fl, 'migrados:', tot)
