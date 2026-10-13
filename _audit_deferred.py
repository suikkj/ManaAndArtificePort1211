import os, re

SRC = r'src/main/java'
decl = {}   # field -> (class, file)
regs = set()  # class.field registered

decl_re = re.compile(r'(?:public|private|protected)?\s*static\s+final\s+DeferredRegister\s*<[^>]+>\s+(\w+)\s*=')
usage_re = re.compile(r'([\w.]+)\.(\w+)\.register\(')
new_re = re.compile(r'DeferredRegister\.create')

for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding='utf-8', errors='replace').read()
        if 'DeferredRegister' not in t:
            continue
        cls = fn[:-5]
        for m in decl_re.finditer(t):
            decl[(cls, m.group(1))] = p
        # registros: X.Y.register(...) ou Y.register(...) dentro do proprio arquivo
        for m in usage_re.finditer(t):
            prefix, field = m.group(1), m.group(2)
            if 'DeferredRegister' in t:
                regs.add((cls if prefix in ('this', '') else prefix.split('.')[-1], field))

print('=== DeferredRegisters declarados:', len(decl))
for (cls, field), p in sorted(decl.items()):
    print(f'  {cls}.{field}')
print()
print('=== registrados (via .register( ):')
for r in sorted(regs):
    print('  ', r)
print()
print('=== NAO registrados (declarados sem registro aparente):')
for (cls, field), p in sorted(decl.items()):
    # heuristica: registrado se (cls, field) ou (X, field) com field unico aparece em regs
    fields_reg = {f for (_, f) in regs}
    if field not in fields_reg:
        print(f'  {cls}.{field}   ({p})')
