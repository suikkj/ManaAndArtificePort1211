# -*- coding: utf-8 -*-
# Auditoria estatica derivada do pacote Minecraft-Skills (plano §11)
import io, os, re, json, glob, sys

ROOT = 'C:/Users/lucas/Documents/coisas/Entre Mods e Codigos/Mana And Artifice 1.21.1/mna-port-workspace'
ROOT = os.getcwd()
A = 'src/main/resources/assets/mna'
D = 'src/main/resources/data/mna'

print('=' * 70)
print('S1: UTF-8 + JSON parse de TODOS os assets/ e data/')
bad = []
count = 0
for base in ('src/main/resources/assets', 'src/main/resources/data'):
    for f in glob.glob(base + '/**/*.json', recursive=True):
        count += 1
        raw = open(f, 'rb').read()
        try:
            txt = raw.decode('utf-8')
        except UnicodeDecodeError as e:
            bad.append((f, 'NAO-UTF8: ' + str(e)))
            continue
        try:
            json.loads(txt.lstrip('\ufeff'))
        except Exception as e:
            bad.append((f, 'JSON-INVALIDO: ' + str(e)[:90]))
print('arquivos verificados:', count, '| falhas:', len(bad))
for f, e in bad[:20]:
    print('  ', f, '->', e)

print()
print('=' * 70)
print('S3: integridade de referencias (blockstate->model->parent->texture)')
missing = []
def res_path(ref, kind):
    # ref: namespace:path
    if ':' not in ref:
        return None
    ns, p = ref.split(':', 1)
    if ns != 'mna':
        return None  # vanilla/neoforge assets nao verificaveis aqui
    return 'src/main/resources/assets/mna/' + kind + '/' + p

nbs = 0
for f in glob.glob(A + '/blockstates/**/*.json', recursive=True):
    nbs += 1
    t = io.open(f, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'"model"\s*:\s*"([^"]+)"', t):
        p = res_path(m.group(1), 'models')
        if p and not os.path.exists(p + '.json'):
            missing.append((f, 'model', m.group(1)))
nm = 0
for f in glob.glob(A + '/models/**/*.json', recursive=True):
    nm += 1
    t = io.open(f, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'"parent"\s*:\s*"([^"]+)"', t):
        p = res_path(m.group(1), 'models')
        if p and not os.path.exists(p + '.json'):
            missing.append((f, 'parent', m.group(1)))
    for m in re.finditer(r'"textures"\s*:\s*\{([^}]*)\}', t, re.S):
        for mm in re.finditer(r'"\w+"\s*:\s*"([^"]+)"', m.group(1)):
            ref = mm.group(1)
            if ref.startswith('#'):
                continue
            p = res_path(ref, 'textures')
            if p and not os.path.exists(p + '.png'):
                missing.append((f, 'texture', ref))
    for m in re.finditer(r'"model"\s*:\s*"(mna:models/[^"]+)"', t):
        p = 'src/main/resources/assets/mna/models/' + m.group(1).split(':', 1)[1]
        if not os.path.exists(p):
            missing.append((f, 'obj', m.group(1)))
print('blockstates:', nbs, '| models:', nm, '| refs quebradas:', len(missing))
for f, k, r in missing[:25]:
    print('  ', f, '|', k, '|', r)

print()
print('=' * 70)
print('S4: varredura de padroes 1.20.1 restantes')
def scan(base, pattern, label, flags=0):
    hits = []
    for f in glob.glob(base + '/**/*', recursive=True):
        if not os.path.isfile(f):
            continue
        try:
            t = io.open(f, encoding='utf-8', errors='replace').read()
        except Exception:
            continue
        for m in re.finditer(pattern, t, flags):
            hits.append((f, m.group(0)[:60]))
    print(label + ':', len(hits))
    for f, h in hits[:10]:
        print('   ', f, '->', h)
    return hits

scan('src/main/resources/assets', r'(?<!neo)forge:', 'a) "forge:" fora de neoforge (assets)')
scan('src/main/resources/data', r'(?<!neo)forge:', 'b) "forge:" fora de neoforge (data)')
scan('src/main/resources/data', r'"type"\s*:\s*"c:', 'c) biome modifier "c:"')
scan('src/main/resources/data', r'set_nbt', 'd) set_nbt restante')
scan('src/main/resources', r'"icon"\s*:\s*\{\s*"item"', 'e) icon {"item"} restante')
scan('src/main/resources/data', r'set_contents', 'f) set_contents restante')
for plural in ['recipes', 'advancements', 'loot_tables', 'structures', 'predicates', 'item_modifiers', 'functions']:
    p = 'src/main/resources/data/mna/' + plural
    if os.path.isdir(p):
        print('g) PASTA PLURAL EXISTE:', p)

print()
print('=' * 70)
print('S5: instrumentacao de debug no codigo')
dbg = []
for f in glob.glob('src/main/java/**/*.java', recursive=True):
    t = io.open(f, encoding='utf-8', errors='replace').read()
    for pat in ['MNA-DEBUG', 'System.out.print', 'printStackTrace']:
        if pat in t:
            dbg.append((f, pat))
print('arquivos com debug:', len(dbg))
for f, p in dbg[:15]:
    print('  ', f, '|', p)

print()
print('=' * 70)
print('S8: .save( direto (ItemStack/FluidStack) fora de helpers')
sv = []
for f in glob.glob('src/main/java/**/*.java', recursive=True):
    for i, line in enumerate(io.open(f, encoding='utf-8', errors='replace'), 1):
        if re.search(r'\.save\(\s*(?:provider\(\)|registries|this\.getLevel\(\)\.registryAccess\(\)|level)', line) and 'saveAdditional' not in line and 'super.save' not in line:
            sv.append((f, i, line.strip()[:110]))
for f, i, l in sv[:25]:
    print('  ', f, ':', i, '|', l)

print()
print('=' * 70)
print('S9: optionalFieldOf(..., null) restantes')
of = []
for f in glob.glob('src/main/java/**/*.java', recursive=True):
    t = io.open(f, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'optionalFieldOf\([^)]*null\s*\)', t):
        of.append((f, m.group(0)[:90]))
print('ocorrencias:', len(of))
for f, m in of[:10]:
    print('  ', f, '|', m)

print()
print('=' * 70)
print('S13: ResourceLocation.parse com literal sem namespace')
rp = []
for f in glob.glob('src/main/java/**/*.java', recursive=True):
    t = io.open(f, encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'ResourceLocation\.parse\(\s*"([^"]*)"', t):
        lit = m.group(1)
        if ':' not in lit and lit:
            rp.append((f, lit))
print('literais sem ":" (default minecraft:):', len(rp))
for f, l in rp[:20]:
    print('  ', f, '->', l)
