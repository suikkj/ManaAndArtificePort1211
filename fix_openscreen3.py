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

def strip_player(expr):
    e = expr.strip()
    e = re.sub(r'^\(ServerPlayer\)\s*\(\(ServerPlayer\)\s*', '', e)
    e = re.sub(r'^\(ServerPlayer\)\s*', '', e)
    if e.endswith(')'):
        pass
    return e

# Parser: acha "NetworkHooks.openScreen(" e balanceia parenteses
tot = 0
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if 'NetworkHooks.openScreen' not in body:
        continue
    out = []
    i = 0
    n = len(body)
    changed = False
    while True:
        k = body.find('NetworkHooks.openScreen(', i)
        if k < 0:
            out.append(body[i:])
            break
        out.append(body[i:k])
        # balanceia a partir do '('
        depth = 0
        j = k + len('NetworkHooks.openScreen')
        start = j
        while j < n:
            if body[j] == '(':
                depth += 1
            elif body[j] == ')':
                depth -= 1
                if depth == 0:
                    break
            j += 1
        inner = body[start + 1:j]
        # dividir argumentos no topo
        args = []
        d = 0
        cur = ''
        for ch in inner:
            if ch == '(':
                d += 1
            elif ch == ')':
                d -= 1
            if ch == ',' and d == 0:
                args.append(cur.strip())
                cur = ''
            else:
                cur += ch
        if cur.strip():
            args.append(cur.strip())
        if len(args) >= 2:
            pl = strip_player(args[0])
            prov = args[1]
            if len(args) >= 3:
                out.append(f'{pl}.openMenu({prov}, {args[2]})')
            else:
                out.append(f'{pl}.openMenu({prov})')
            tot += 1
            changed = True
        else:
            out.append(body[k:j + 1])
        i = j + 1
    if changed:
        open(p, 'w', encoding='utf-8').write(''.join(out))
        fl += 1

print('arquivos:', fl, 'migrados:', tot)
