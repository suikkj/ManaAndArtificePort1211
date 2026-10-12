import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')
LINE = re.compile(r'//[^\n]*')
BLOCK = re.compile(r'/\*.*?\*/', re.S)

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

PROV = {
    'PlayerMagicProvider':       ('com.mna.capabilities.CapabilitiesInit.PLAYER_MAGIC', 'com.mna.api.capabilities.IPlayerMagic', 'MAGIC'),
    'PlayerProgressionProvider': ('com.mna.capabilities.CapabilitiesInit.PLAYER_PROGRESSION', 'com.mna.api.capabilities.IPlayerProgression', 'PROGRESSION'),
    'PlayerRoteSpellsProvider':  ('com.mna.capabilities.CapabilitiesInit.PLAYER_ROTE_SPELLS', 'com.mna.api.capabilities.IPlayerRoteSpells', 'ROTE'),
    'ChunkMagicProvider':        ('com.mna.capabilities.CapabilitiesInit.CHUNK_MAGIC', 'com.mna.api.capabilities.IChunkMagic', 'MAGIC'),
    'MAPFXProvider':             ('com.mna.capabilities.CapabilitiesInit.ENTITY_MAPFX', 'com.mna.capabilities.entity.MAPFX', 'MAPFX'),
    'ParticleAuraProvider':      ('com.mna.capabilities.CapabilitiesInit.ENTITY_PARTICLE_AURA', 'com.mna.capabilities.particles.ParticleAura', 'AURA'),
    'WorldMagicProvider':        ('com.mna.capabilities.CapabilitiesInit.WORLD_MAGIC', 'com.mna.api.capabilities.IWorldMagic', 'MAGIC'),
}

for base, (field, typ, name) in PROV.items():
    p = None
    for dp, _, fns in os.walk(SRC):
        if base + '.java' in fns:
            p = os.path.join(dp, base + '.java')
    if not p:
        print('nao achou', base)
        continue
    t = sc(open(p, encoding='utf-8', errors='replace').read())
    new_field = (f'    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<{typ}>> '
                 f'{name} = {field};')
    # campo original (uma linha)
    pat = re.compile(r'public static final Capability<[^;]+;')
    m = pat.search(t)
    if not m:
        print('campo nao encontrado em', base, '->', [l for l in t.split('\n') if 'Capability<' in l][:2])
        continue
    t = t[:m.start()] + new_field.strip() + t[m.end():]
    # imports: remover Capability/CapabilityManager/CapabilityToken, adicionar Supplier + AttachmentType
    t = re.sub(r'import net\.neoforged\.neoforge\.capabilities\.Capability;\n', '', t)
    t = re.sub(r'import net\.neoforged\.neoforge\.common\.capabilities\.CapabilityManager;\n', '', t)
    t = re.sub(r'import net\.neoforged\.neoforge\.common\.capabilities\.CapabilityToken;\n', '', t)
    if 'import java.util.function.Supplier;' not in t:
        t = t.replace('import net.neoforged.neoforge.common.util.LazyOptional;',
                      'import net.neoforged.neoforge.common.util.LazyOptional;\nimport java.util.function.Supplier;')
    open(p, 'w', encoding='utf-8').write(t)
    print('ok', base)
