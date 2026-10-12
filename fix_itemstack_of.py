import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')
LINE = re.compile(r'//[^\n]*')
BLOCK = re.compile(r'/\*.*?\*/', re.S)

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

# Mapa arquivo -> expressao do provider disponivel naquele escopo
PROVIDER = {
    'PedestalTile.java': 'this.getLevel().registryAccess()',
    'ChalkRuneTile.java': 'this.getLevel().registryAccess()',
    'EldrinAltarTile.java': 'this.getLevel().registryAccess()',
    'ManaweavingAltarTile.java': 'this.getLevel().registryAccess()',
    'RuneForgeTile.java': 'this.getLevel().registryAccess()',
    'RunicAnvilTile.java': 'this.getLevel().registryAccess()',
    'Construct.java': 'this.registryAccess()',
    'ConstructTaskItemStackParameter.java': 'this.level.registryAccess()',
    'ParticleItemStack.java': 'ManaAndArtifice.instance.proxy.getClientWorld() == null ? net.minecraft.server.MinecraftServer.getServer().registryAccess() : ManaAndArtifice.instance.proxy.getClientWorld().registryAccess()',
}

pat = re.compile(r'ItemStack\.of\(\s*\(CompoundTag\)([^()]*(?:\([^()]*\))?[^()]*)\)')

tot = 0
fl = 0
for base, prov in PROVIDER.items():
    p = None
    for dp, _, fns in os.walk(SRC):
        if base in fns:
            p = os.path.join(dp, base)
    if not p:
        print('nao achou', base)
        continue
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    hits = pat.findall(body)
    if not hits:
        continue
    new = pat.sub(lambda m: f'ItemStack.parseOptional({prov}, {m.group(1).strip()})', body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        fl += 1
        tot += len(hits)
        print(f'  {base}: {len(hits)}')

print('arquivos:', fl, 'substituicoes:', tot)
