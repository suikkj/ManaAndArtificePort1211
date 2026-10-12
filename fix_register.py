import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')
LINE = re.compile(r'//[^\n]*')
BLOCK = re.compile(r'/\*.*?\*/', re.S)

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

# 1) helper.register(RL, (Object)X) -> helper.register(RL, X)
p1 = re.compile(r'(helper\.register\(\s*[^,]+,\s*)\(Object\)')
n1 = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if not p1.search(body):
        continue
    new = p1.sub(lambda m: m.group(1), body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        n1 += 1

# 2) registry.register(RL, (Object)X) -> registry.register(RL, X)   (RegisterHelper)
p2 = re.compile(r'(registry\.register\([^,]+,\s*)\(Object\)')
n2 = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if not p2.search(body):
        continue
    new = p2.sub(lambda m: m.group(1), body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        n2 += 1

# 3) helper.register(ResourceLocation.fromNamespaceAndPath("mna","x"), (Object)VAL) onde helper é lambda de creative tabs
p3 = re.compile(r'helper\.register\(\s*ResourceLocation\.fromNamespaceAndPath\(\s*"(\w+)"\s*,\s*"([\w/]+)"\s*\)\s*,\s*')
n3 = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if not p3.search(body):
        continue
    new = p3.sub(lambda m: f'helper.register(ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("{m.group(1)}", "{m.group(2)}"))), ResourceLocation.fromNamespaceAndPath("{m.group(1)}", "{m.group(2)}"), () -> ', body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        n3 += 1

print('helper.register (Object) limpos:', n1)
print('registry.register (Object) limpos:', n2)
print('creative tabs migrados:', n3)
