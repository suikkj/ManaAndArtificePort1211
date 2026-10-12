import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')
BLOCK = re.compile(r'/\*.*?\*/', re.S)
LINE = re.compile(r'//[^\n]*')

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

# 1) getKey((Object)X) -> getKey(X)   e   getKey((ConstructTask)X) -> getKey(X)
p1 = re.compile(r'\.getKey\(\s*\((?:Object|ConstructTask|ItemStack|Item|Block|TileEntity|EntityType|Shape|Modifier|SpellEffect)\)\s*')
n1 = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if not p1.search(body):
        continue
    new = p1.sub('.getKey(', body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        n1 += 1

# 2) new StairBlock(() -> <expr>, props) -> new StairBlock(<expr>, props)
p2 = re.compile(r'new StairBlock\(\s*\(\)\s*->\s*([^,]+?)\s*,\s*')
n2 = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if not p2.search(body):
        continue
    new = p2.sub(lambda m: 'new StairBlock(' + m.group(1) + ', ', body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        n2 += 1

# 3) getDelegateOrThrow((Object)X) -> get(X)  / getDelegateOrThrow(X) -> get(X)
p3 = re.compile(r'\.getDelegateOrThrow\(\s*\((?:Object|Block|Item|EntityType|MobEffect)\)\s*')
n3 = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if not p3.search(body):
        continue
    new = p3.sub('.get(', body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        n3 += 1

print('getKey casts limpos em', n1, 'arquivos')
print('StairBlock supplier resolvido em', n2, 'arquivos')
print('getDelegateOrThrow resolvido em', n3, 'arquivos')
