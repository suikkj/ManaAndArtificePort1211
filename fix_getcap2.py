import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')
LINE = re.compile(r'//[^\n]*')
BLOCK = re.compile(r'/\*.*?\*/', re.S)

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

PROV = r'[\w.]*Provider\.\w+'

# 1) X.getCapability(P).orElse(null) -> X.getData(P.get())
p1 = re.compile(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)\.orElse\(\s*null\s*\)')
# 2) X.getCapability(P).orElse(expr) -> X.getData(P.get())   (nao-nulo tambem)
p2 = re.compile(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)\.orElse\(')

n1 = n2 = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if 'getCapability(' not in body:
        continue
    o = body
    body = p1.sub(lambda m: f'{m.group(1)}.getData({m.group(2)}.get())', body)
    n1 += len(p1.findall(o))

    # orElse(X) generico: X.getCapability(P).orElse(Y) -> (X.getData(P.get()))
    # para expr simples usamos o proprio getData; para expressoes complexas, mantemos simples
    m = re.findall(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)\.orElse\(', body)
    if m:
        body = re.sub(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)\.orElse\(',
                      lambda mm: f'{mm.group(1)}.getData({mm.group(2)}.get()) != null ? {mm.group(1)}.getData({mm.group(2)}.get()) : (', body)
    if body != o:
        open(p, 'w', encoding='utf-8').write(body)
        n2 += len(m)

print('orElse(null):', n1, '| orElse(outros):', n2)
