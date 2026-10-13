import os, re

SRC = 'src/main/java'
pat = re.compile(r'if\s*\(([^{}]*)\)\s*\{\s*\}', re.S)

def strip_code(t):
    tt = re.sub(r'"(\\.|[^"\\])*"', '""', t)
    tt = re.sub(r'//[^\n]*', '', tt)
    tt = re.sub(r'/\*.*?\*/', '', tt, flags=re.S)
    return tt

def enclosing_method(t, pos):
    # achar assinatura do metodo que contem pos
    head = t[:pos]
    ms = list(re.finditer(r'(?:public|private|protected|static|final|synchronized|\s)+\s+[\w<>, ?\[\].]+\s+(\w+)\s*\([^;{]*\)\s*(?:throws [\w, .]+)?\s*\{', head))
    if not ms:
        return '?'
    m = ms[-1]
    return m.group(1)

out = []
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding='utf-8', errors='replace').read()
        tt = strip_code(t)
        for m in pat.finditer(tt):
            line = t[:m.start()].count('\n') + 1
            cond = ' '.join(m.group(1).split())[:90]
            # pegar proximas linhas do codigo (original, nao stripped)
            after = t[m.end():m.end()+260].replace('\n', ' | ')
            meth = enclosing_method(tt, m.start())
            out.append((p.replace(os.sep, '/'), line, meth, cond, after))

with open('_empty_guards_report.txt', 'w', encoding='utf-8') as f:
    for p, line, meth, cond, after in out:
        f.write(f'{p}:{line} [{meth}] if ({cond}) => NEXT: {after}\n\n')
print('relatorio:', len(out), 'itens em _empty_guards_report.txt')
