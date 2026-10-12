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

n = 0
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    o = body
    # (double)expr  ->  (float)expr   apenas dentro de addVertex(...)
    def fix(m):
        inner = m.group(1)
        inner = re.sub(r'\(double\)', '(float)', inner)
        # literais double sem sufixo -> float
        inner = re.sub(r'(?<![\w.])(-?\d+\.\d+)(?![\w.])', r'\1f', inner)
        return '.addVertex(' + inner + ')'
    body = re.sub(r'\.addVertex\(((?:[^()]|\([^()]*\))*)\)', fix, body)
    # getU(0.0) / getV(0.0) -> float
    body = re.sub(r'\.(getU|getV)\((-?\d+\.\d+)\)', lambda m: f'.{m.group(1)}({m.group(2)}f)', body)
    if body != o:
        open(p, 'w', encoding='utf-8').write(body)
        fl += 1

print('arquivos alterados:', fl)
