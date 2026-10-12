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

# NetworkHooks.openScreen(<player>, <provider>[, <extra>])  ->  <player>.openMenu(<provider>[, <extra>])
# captura 2 ou 3 argumentos; o player pode ter casts.
pat = re.compile(
    r'NetworkHooks\.openScreen\(\s*'
    r'(\(ServerPlayer\)(?:\(ServerPlayer\))?[A-Za-z_][A-Za-z0-9_.]*'
    r'|\(ServerPlayer\)player'
    r'|[A-Za-z_][A-Za-z0-9_.]*)\s*,\s*'
    r'(\(MenuProvider\)[\s\S]*?|[\s\S]*?)\s*'
    r'(?:,\s*([\s\S]*?))?\)')

def repl(m):
    player = m.group(1)
    prov = m.group(2).strip()
    extra = (m.group(3) or '').strip()
    # remover casts obvios do player
    p = player
    p = re.sub(r'^\(ServerPlayer\)\(\(ServerPlayer\)', '', p).rstrip(')')
    p = re.sub(r'^\(ServerPlayer\)', '', p)
    if extra:
        return f'{p}.openMenu({prov}, {extra})'
    return f'{p}.openMenu({prov})'

tot = 0
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if 'NetworkHooks.openScreen' not in body:
        continue
    new = pat.sub(repl, body)
    if new != body:
        n = len(pat.findall(body))
        open(p, 'w', encoding='utf-8').write(new)
        fl += 1
        tot += n

print('arquivos:', fl, 'migrados:', tot)
print('restantes:', )
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    for m in re.finditer(r'NetworkHooks\.openScreen\([^\n]{0,120}', body):
        print('   ', os.path.basename(p), '::', m.group(0)[:110])
