import os, re, glob

missing = []
for f in glob.glob('src/main/resources/assets/mna/models/**/*.json', recursive=True):
    try:
        txt = open(f, encoding='utf-8', errors='replace').read()
    except Exception:
        continue
    for m in re.finditer(r'"model"\s*:\s*"mna:models/([^"]+)"', txt):
        p = 'src/main/resources/assets/mna/models/' + m.group(1)
        if not os.path.exists(p):
            missing.append((f.replace(os.sep, '/'), m.group(1)))

print(str(len(missing)) + ' refs .obj faltando (real):')
for f, r in missing[:20]:
    print('  ' + f + ' -> ' + r)
