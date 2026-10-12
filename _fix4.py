# _fix4.py - restaura o import vanilla ImageButton onde ainda ha referencias de tipo
import os
import re

BS = chr(92)


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def strip_comments(text):
    out = []
    i = 0
    n = len(text)
    while i < n:
        c = text[i]
        if c == '/' and i + 1 < n and text[i + 1] == '/':
            j = text.find('\n', i)
            i = n if j < 0 else j
            continue
        if c == '/' and i + 1 < n and text[i + 1] == '*':
            j = text.find('*/', i + 2)
            i = n if j < 0 else j + 2
            continue
        if c in '"\'':
            q = c
            out.append(c)
            i += 1
            while i < n:
                ch = text[i]
                if ch == BS:
                    out.append(text[i:i + 2])
                    i += 2
                    continue
                out.append(ch)
                if ch == q:
                    i += 1
                    break
                i += 1
            continue
        out.append(c)
        i += 1
    return ''.join(out)


IMP = 'import net.minecraft.client.gui.components.ImageButton;'
fixed = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        if 'extends MNAImageButton' not in t:
            continue
        code = strip_comments(t)
        if not re.search(r'\bImageButton\b', code):
            continue
        if IMP in t:
            continue
        sep = '\r\n' if '\r\n' in t else '\n'
        lines = t.split(sep)
        idx = None
        for i, l in enumerate(lines):
            if l.startswith('import '):
                idx = i
        lines.insert(idx + 1, IMP)
        save(p, sep.join(lines))
        fixed += 1
        print('  +import vanilla:', p)
print('arquivos corrigidos:', fixed)
