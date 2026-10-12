import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')

BLOCK = re.compile(r'/\*.*?\*/', re.S)
LINE = re.compile(r'//[^\n]*')

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

# hasEffect((MobEffect)X   -> hasEffect(X
# addEffect(new MobEffectInstance((MobEffect)X  -> ...
pats = [
    re.compile(r'(hasEffect\(\s*)\(MobEffect\)'),
    re.compile(r'(MobEffectInstance\(\s*)\(MobEffect\)'),
    re.compile(r'(getEffect\(\s*)\(MobEffect\)'),
    re.compile(r'(removeEffect\(\s*)\(MobEffect\)'),
]

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

changed = {}
for name, pat in zip(['hasEffect', 'MobEffectInstance', 'getEffect', 'removeEffect'], pats):
    tot = 0
    fl = 0
    for p in files:
        body = sc(open(p, encoding='utf-8', errors='replace').read())
        if not pat.search(body):
            continue
        new = pat.sub(lambda m: m.group(1), body)
        if new != body:
            open(p, 'w', encoding='utf-8').write(new)
            tot += len(pat.findall(body))
            fl += 1
    changed[name] = (fl, tot)

for k, (fl, tot) in changed.items():
    print(f'{k:20s} arquivos={fl:4d}  removidos={tot}')
