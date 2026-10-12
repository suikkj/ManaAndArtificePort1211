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

# 1) X.getCapability(P).orElse(null)  ->  X.getData(P.get())
p1 = re.compile(r'(\w[\w.]*)\.getCapability\(\s*([\w.]*Provider\.\w+)\s*\)\.orElse\(null\)')
# 2) X.getCapability(P).orElse(expr) ->  X.getData(P.get())
p2 = re.compile(r'(\w[\w.]*)\.getCapability\(\s*([\w.]*Provider\.\w+)\s*\)\.orElse\(([^()]*(?:\([^()]*\))?[^()]*)\)')
# 3) X.getCapability(P).ifPresent(lam) -> X.hasData(P.get()) ? lam.accept(X.getData(P.get())) : (nada)
p3 = re.compile(r'(\w[\w.]*)\.getCapability\(\s*([\w.]*Provider\.\w+)\s*\)\.ifPresent\(\s*([\w<>., ]+)\s*->')
# 4) X.getCapability(P).ifPresent(...)
p4 = re.compile(r'(\w[\w.]*)\.getCapability\(\s*([\w.]*Provider\.\w+)\s*\)\.ifPresent\(')

n1 = n2 = n3 = n4 = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if 'getCapability(' not in body:
        continue
    o = body
    body = p1.sub(lambda m: f'{m.group(1)}.getData({m.group(2)}.get())', body)
    body = p2.sub(lambda m: f'{m.group(1)}.getData({m.group(2)}.get())', body)
    body = p3.sub(lambda m: f'{{ if ({m.group(1)}.hasData({m.group(2)}.get())) {{ {m.group(3)} ->', body)
    body = p4.sub(lambda m: f'{{ if ({m.group(1)}.hasData({m.group(2)}.get())) {{ ', body)
    if body != o:
        open(p, 'w', encoding='utf-8').write(body)
        n1 += len(p1.findall(o)); n2 += len(p2.findall(o))
        n3 += len(p3.findall(o)); n4 += len(p4.findall(o))

print('orElse(null):', n1, '| orElse(x):', n2, '| ifPresent(lam):', n3, '| ifPresent(:', n4)
