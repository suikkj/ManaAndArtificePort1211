import os, re

SRC = 'src/main/java'
# if (cond) { }  — no TEXTO ORIGINAL (com comentarios). Aceita apenas espacos/comentarios dentro das chaves.
pat = re.compile(r'(if\s*\([^{}]*\)\s*\{)((?:\s|//[^\n]*|/\*.*?\*/)*)(\})', re.S)

changed = []
for dp, _, fns in os.walk(SRC):
    for fn in fns:
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding='utf-8', errors='replace').read()

        def repl(m):
            head, inner, tail = m.group(1), m.group(2), m.group(3)
            # indentacao: pegar do comeco da linha do match
            start = m.start()
            line_start = t.rfind('\n', 0, start) + 1
            indent = re.match(r'[ \t]*', t[line_start:start]).group(0)
            if inner.strip():
                # tem comentario: inserir return apos o comentario
                return head + inner + '\n' + indent + '    return;' + '\n' + indent + tail
            return head + '\n' + indent + '    return;' + '\n' + indent + tail

        t2 = pat.sub(repl, t)
        if t2 != t:
            open(p, 'w', encoding='utf-8', newline='').write(t2)
            n = len(pat.findall(t))
            changed.append((p, n))

print('arquivos alterados:', len(changed))
for p, n in changed:
    print(f'  {n:2d}  {p}')
