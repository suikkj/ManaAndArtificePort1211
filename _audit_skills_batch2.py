# -*- coding: utf-8 -*-
# S3 (mapeamento corrigido) + S13 dump + S2 sounds audit
import io, os, re, json, glob

A = 'src/main/resources/assets/mna'

print('=' * 70)
print('S3-FIX: integridade de referencias (mapeamento correto)')
missing = []

def exists(p):
    return os.path.exists(p)

# blockstates -> models: ref "mna:block/x" -> assets/mna/models/block/x.json
nbs = 0
for f in glob.glob(A + '/blockstates/**/*.json', recursive=True):
    nbs += 1
    t = io.open(f, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'"model"\s*:\s*"([^"]+)"', t):
        ref = m.group(1)
        if ref.startswith('mna:'):
            p = 'src/main/resources/assets/mna/models/' + ref.split(':', 1)[1] + '.json'
            if not exists(p):
                missing.append((f, 'blockstate->model', ref))

# models: parent "mna:block/x" -> models/block/x.json ; "mna:models/..." -> assets/mna/models/...
nm = 0
for f in glob.glob(A + '/models/**/*.json', recursive=True):
    nm += 1
    t = io.open(f, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'"parent"\s*:\s*"([^"]+)"', t):
        ref = m.group(1)
        if ref.startswith('mna:'):
            path = ref.split(':', 1)[1]
            p = 'src/main/resources/assets/mna/models/' + path + '.json'
            if not exists(p):
                missing.append((f, 'parent', ref))
    for m in re.finditer(r'"textures"\s*:\s*\{([^}]*)\}', t, re.S):
        for mm in re.finditer(r'"\w+"\s*:\s*"([^"]+)"', m.group(1)):
            ref = mm.group(1)
            if ref.startswith('#'):
                continue
            if ref.startswith('mna:'):
                p = 'src/main/resources/assets/mna/textures/' + ref.split(':', 1)[1] + '.png'
                if not exists(p):
                    missing.append((f, 'texture', ref))
    # obj loader refs: "model": "mna:models/..." -> assets/mna/models/...
    for m in re.finditer(r'"model"\s*:\s*"mna:models/([^"]+)"', t):
        p = 'src/main/resources/assets/mna/models/' + m.group(1)
        if not exists(p):
            missing.append((f, 'obj', 'mna:models/' + m.group(1)))
print('blockstates:', nbs, '| models:', nm, '| refs quebradas REAIS:', len(missing))
for f, k, r in missing[:30]:
    print('  ', f.replace(os.sep, '/'), '|', k, '|', r)

print()
print('=' * 70)
print('S13-DUMP: ResourceLocation.parse("literal") sem ":" (todos)')
for f in glob.glob('src/main/java/**/*.java', recursive=True):
    t = io.open(f, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'ResourceLocation\.parse\(\s*"([^"]*)"', t):
        lit = m.group(1)
        if ':' not in lit and lit:
            # linha inteira p/ contexto
            start = t.rfind('\n', 0, m.start()) + 1
            end = t.find('\n', m.start())
            line = t[start:end].strip()
            print('  ', f.replace(os.sep, '/').split('/')[-1], '|', lit, '|', line[:120])

print()
print('=' * 70)
print('S2a: sounds.json - refs de arquivo existentes + namespace')
sj = A + '/sounds.json'
data = json.load(io.open(sj, encoding='utf-8-sig'))
missing_ogg = []
events = 0
unqualified = []
for ev, spec in data.items():
    events += 1
    for snd in spec.get('sounds', []):
        if isinstance(snd, str):
            name = snd
        else:
            name = snd.get('name') or snd.get('type') or ''
            if snd.get('type') == 'event':
                continue  # evento -> outro evento, nao arquivo
        if not name:
            continue
        if ':' not in name:
            unqualified.append((ev, name))
            name = 'minecraft:' + name
        ns, pth = name.split(':', 1)
        if ns == 'mna':
            ogg = 'src/main/resources/assets/mna/sounds/' + pth + '.ogg'
            if not os.path.exists(ogg):
                missing_ogg.append((ev, name))
print('eventos:', events, '| ogg faltando (mna):', len(missing_ogg), '| nomes sem namespace:', len(unqualified))
for ev, n in missing_ogg[:15]:
    print('   MISSING', ev, '->', n)
for ev, n in unqualified[:15]:
    print('   UNQUALIFIED', ev, '->', n)
