import os, re

SRC = 'src/main/java'
# if (...) { } vazio — captura multiline
pat = re.compile(r'if\s*\(([^{}]*)\)\s*\{\s*\}', re.S)
hits = []
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding='utf-8', errors='replace').read()
        # remover strings e comentarios grosseiramente
        tt = re.sub(r'"(\\.|[^"\\])*"', '""', t)
        tt = re.sub(r'//[^\n]*', '', tt)
        tt = re.sub(r'/\*.*?\*/', '', tt, flags=re.S)
        for m in pat.finditer(tt):
            cond = m.group(1).strip().replace('\n', ' ')[:80]
            line = t[:m.start()].count('\n') + 1
            hits.append((p.replace(os.sep, '/'), line, cond))

print(len(hits), 'if-blocks vazios:')
for p, line, cond in hits:
    print(f'  {p}:{line}  if ({cond})')
