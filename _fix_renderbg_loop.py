import os, re

SRC = 'src/main/java'
# encontra 'void renderBg(...) {' e remove a chamada this.renderBackground(...) nas linhas seguintes
pat = re.compile(
    r'(void\s+renderBg\s*\([^)]*\)\s*\{\s*\n)((?:\s*//[^\n]*\n)*)(\s*this\.renderBackground\([^;]*\);\s*\n)',
    re.S)

changed = []
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding='utf-8', errors='replace').read()
        if 'renderBg' not in t:
            continue
        t2, n = pat.subn(
            lambda m: m.group(1) + m.group(2) +
                      '        // [PORT 1.21.1] NAO chamar renderBackground aqui: AbstractContainerScreen.renderBackground\n'
                      '        // chama renderBg -> recursao infinita (StackOverflow).\n',
            t)
        if n:
            open(p, 'w', encoding='utf-8', newline='').write(t2)
            changed.append((p, n))

print('arquivos:', len(changed))
for p, n in changed:
    print(f'  {n}  {p.replace(os.sep, "/")}')
