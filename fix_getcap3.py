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

# A) X.getCapability(P).ifPresent(...)   ->  MNACap.get(X, P).ifPresent(...)
pa = re.compile(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)\.ifPresent\(')
# B) X.getCapability(P).orElse(...)      ->  MNACap.get(X, P).orElse(...)
pb = re.compile(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)\.orElse\(')
# C) X.getCapability(P).orElse(null)     ->  MNACap.get(X, P).orElse(null)
pc = re.compile(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)\.orElse\(null\)')
# D) X.getCapability(P).map(...)         ->  MNACap.get(X, P).map(...)
pd = re.compile(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)\.map\(')
# E) X.getCapability(P) sozinho          ->  MNACap.get(X, P)
pe = re.compile(r'(\w[\w.]*)\.getCapability\(\s*(' + PROV + r')\s*\)')

IMP = 'import com.mna.api.tools.MNACap;'
counts = {}
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if 'getCapability(' not in body:
        continue
    o = body
    c = {}
    for name, pat in (('ifPresent', pa), ('orElseNull', pc), ('orElse', pb), ('map', pd), ('plain', pe)):
        hits = pat.findall(body)
        if hits:
            c[name] = len(hits)
            body = pat.sub(lambda m, nn=name: f'MNACap.get({m.group(1)}, {m.group(2)}).{"" if nn=="plain" else nn+"("}', body)
    # plain precisa fechar o parentese
    if 'plain' in c:
        body = re.sub(r'MNACap\.get\(([^;]+?)\)\.(?![A-Za-z])', r'MNACap.get(\1)', body)
        body = re.sub(r'MNACap\.get\(([^;]+?)\)(?!\))', r'MNACap.get(\1)', body)
    if body != o:
        if IMP not in body:
            lines = body.split('\n')
            try:
                last = max(i for i, l in enumerate(lines) if l.startswith('import '))
                lines.insert(last + 1, IMP)
                body = '\n'.join(lines)
            except ValueError:
                pass
        open(p, 'w', encoding='utf-8').write(body)
        for k, v in c.items():
            counts[k] = counts.get(k, 0) + v
        fl += 1

print('arquivos:', fl)
print(counts)
