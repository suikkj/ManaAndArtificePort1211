import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')
LINE = re.compile(r'//[^\n]*')
BLOCK = re.compile(r'/\*.*?\*/', re.S)

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

# Provider -> campo do AttachmentType em CapabilitiesInit
PROV = {
    'PlayerMagicProvider':       ('com.mna.capabilities.CapabilitiesInit.PLAYER_MAGIC', 'PlayerMagic'),
    'PlayerProgressionProvider': ('com.mna.capabilities.CapabilitiesInit.PLAYER_PROGRESSION', 'PlayerProgression'),
    'PlayerRoteSpellsProvider':  ('com.mna.capabilities.CapabilitiesInit.PLAYER_ROTE_SPELLS', 'PlayerRoteSpells'),
    'ChunkMagicProvider':        ('com.mna.capabilities.CapabilitiesInit.CHUNK_MAGIC', 'ChunkMagic'),
    'MAPFXProvider':             ('com.mna.capabilities.CapabilitiesInit.ENTITY_MAPFX', 'MAPFX'),
    'ParticleAuraProvider':      ('com.mna.capabilities.CapabilitiesInit.ENTITY_PARTICLE_AURA', 'ParticleAura'),
    'WorldMagicProvider':        ('com.mna.capabilities.CapabilitiesInit.WORLD_MAGIC', 'WorldMagic'),
}

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

for base, (field, cls) in PROV.items():
    p = None
    for dp, _, fns in os.walk(SRC):
        if base + '.java' in fns:
            p = os.path.join(dp, base + '.java')
    if not p:
        print('nao achou', base)
        continue
    t = sc(open(p, encoding='utf-8', errors='replace').read())
    # campo: public static final Capability<X> NOME = ...;  ->  Supplier<AttachmentType<X>>
    t = re.sub(r'public static final Capability<[\w.<>]+ >? ?(\w+) = CapabilityManager\.get\([^)]*\)[^;]*;',
               lambda m: f'public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<'
                         + {'PlayerMagicProvider': 'com.mna.api.capabilities.IPlayerMagic',
                            'PlayerProgressionProvider': 'com.mna.api.capabilities.IPlayerProgression',
                            'PlayerRoteSpellsProvider': 'com.mna.api.capabilities.IPlayerRoteSpells',
                            'ChunkMagicProvider': 'com.mna.api.capabilities.IChunkMagic',
                            'MAPFXProvider': 'com.mna.capabilities.entity.MAPFX',
                            'ParticleAuraProvider': 'com.mna.capabilities.particles.ParticleAura',
                            'WorldMagicProvider': 'com.mna.api.capabilities.IWorldMagic'}[base]
                         + f'>> {m.group(1)} = ' + field + ';',
               t)
    open(p, 'w', encoding='utf-8').write(t)
    print('atualizado', base)
