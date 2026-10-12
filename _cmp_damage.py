import re, difflib

snap = '_snapshots/snap-20261011-100124-3094erros/src/main/java/com/mna/'
cur = 'src/main/java/com/mna/'
files = ['api/tools/MATags.java', 'blocks/worldgen/MAFlowerBlock.java', 'blocks/worldgen/MASandFlowerBlock.java',
         'blocks/worldgen/MAStoneFlowerBlock.java', 'blocks/worldgen/MATarmaRootBlock.java',
         'blocks/worldgen/MAWaterFlowerBlock.java', 'entities/renderers/ritual/RitualRenderer.java',
         'ManaAndArtifice.java']

BS = chr(92)
SQ = chr(39)
DQ = chr(34)


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
        if c == DQ or c == SQ:
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


def toks(t):
    t = strip_comments(t)
    return re.findall(r'\S+', t)


for f in files:
    try:
        raw_snap = open(snap + f, encoding='utf-8', errors='replace').read()
        raw_cur = open(cur + f, encoding='utf-8', errors='replace').read()
    except Exception as e:
        print(f + ': ERRO ' + str(e))
        continue
    a, b = toks(raw_snap), toks(raw_cur)
    q_snap, q_cur = raw_snap.count(DQ), raw_cur.count(DQ)
    note = ''
    if q_cur % 2 != 0:
        note = '  [ASPAS IMPARES no atual: ' + str(q_cur) + ']'
    if a == b:
        print(f + ': SO COMENTARIOS (codigo identico) -> restaurar' + note)
        continue
    print(f + ': DIFF DE CODIGO (' + str(len(a)) + ' vs ' + str(len(b)) + ' tokens)' + note)
    sm = difflib.SequenceMatcher(None, a, b)
    printed = 0
    for op, i1, i2, j1, j2 in sm.get_opcodes():
        if op == 'equal':
            continue
        printed += 1
        if printed > 5:
            print('   ...')
            break
        ctx = ' '.join(a[max(0, i1 - 4):i2 + 4])
        new = ' '.join(b[max(0, j1 - 4):j2 + 4])
        print('  [' + op + '] snap: ' + ctx[:150])
        print('         cur : ' + new[:150])
    print()
