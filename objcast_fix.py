import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')

BLOCK = re.compile(r'/\*.*?\*/', re.S)
LINE = re.compile(r'//[^\n]*')

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

# entityData.set(ACC, (Object)X)  ->  entityData.set(ACC, X)
pat = re.compile(r'(entityData\.set\(\s*[A-Za-z0-9_]+\s*,\s*)\(Object\)')

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

changed = []
tot = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if not pat.search(body):
        continue
    new = pat.sub(lambda m: m.group(1), body)
    if new != body:
        n = len(pat.findall(body))
        open(p, 'w', encoding='utf-8').write(new)
        tot += n
        changed.append((os.path.basename(p), n))

print('arquivos:', len(changed), ' casts (Object) removidos:', tot)
for c in changed:
    print('  ', c)
