# _fix26.py - CapabilitiesInit interface-typed, serializers, consumidores, List<Cls>, super(...),
#             setValue casts, neighborChanged->handleNeighborChanged, ChalkRune getEntities
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    if old not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + ']')
    return t.replace(old, new)


def all_java():
    out = []
    for dp, _, fns in os.walk('src/main/java'):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


I = 'com.mna.api.capabilities.'

print('== CapabilitiesInit ==')
p = 'src/main/java/com/mna/capabilities/CapabilitiesInit.java'
t = load(p)
for name, iface, cls, extra in [
    ('PLAYER_MAGIC', 'IPlayerMagic', 'PlayerMagic', '.copyOnDeath()'),
    ('PLAYER_PROGRESSION', 'IPlayerProgression', 'PlayerProgression', '.copyOnDeath()'),
    ('PLAYER_ROTE_SPELLS', 'IPlayerRoteSpells', 'PlayerRoteSpells', '.copyOnDeath()'),
    ('WORLD_MAGIC', 'IWorldMagic', 'WorldMagic', ''),
    ('CHUNK_MAGIC', 'IChunkMagic', 'ChunkMagic', ''),
]:
    t = rep(t,
            'public static final Supplier<AttachmentType<' + cls + '>> ' + name + ' =\n'
            '        ATTACHMENT_TYPES.register(',
            'public static final Supplier<AttachmentType<' + I + iface + '>> ' + name + ' =\n'
            '        ATTACHMENT_TYPES.register(', name + '-decl')
    t = rep(t,
            '() -> AttachmentType.builder((Supplier<' + cls + '>) ' + cls + '::new)',
            '() -> AttachmentType.<' + I + iface + '>builder(() -> new ' + cls + '())', name + '-builder')
save(p, t)

print('== Serializers ==')
for path, iface, cls in [
    ('src/main/java/com/mna/capabilities/worlddata/WorldMagicSerializer.java', 'IWorldMagic', 'WorldMagic'),
    ('src/main/java/com/mna/capabilities/chunkdata/ChunkMagicSerializer.java', 'IChunkMagic', 'ChunkMagic'),
    ('src/main/java/com/mna/capabilities/playerdata/magic/PlayerMagicSerializer.java', 'IPlayerMagic', 'PlayerMagic'),
    ('src/main/java/com/mna/capabilities/playerdata/progression/PlayerProgressionSerializer.java', 'IPlayerProgression', 'PlayerProgression'),
    ('src/main/java/com/mna/capabilities/playerdata/rote/PlayerRoteSpellsSerializer.java', 'IPlayerRoteSpells', 'PlayerRoteSpells'),
]:
    t = load(path)
    t = rep(t, 'implements IAttachmentSerializer<Tag, ' + cls + '>',
            'implements IAttachmentSerializer<Tag, ' + I + iface + '>', os.path.basename(path) + '-impl')
    t = rep(t, 'public Tag write(' + cls + ' instance,', 'public Tag write(' + I + iface + ' instance,', os.path.basename(path) + '-write')
    save(path, t)

print('== consumidores (Optional<XProvider>) ==')
total = 0
for p in all_java():
    t = load(p)
    o = t
    t = t.replace('Optional<PlayerMagicProvider>', 'Optional<' + I + 'IPlayerMagic>')
    t = t.replace('Optional<PlayerProgressionProvider>', 'Optional<' + I + 'IPlayerProgression>')
    t = t.replace('Optional<PlayerRoteSpellsProvider>', 'Optional<' + I + 'IPlayerRoteSpells>')
    t = t.replace('Optional<WorldMagicProvider>', 'Optional<' + I + 'IWorldMagic>')
    t = t.replace('Optional<ChunkMagicProvider>', 'Optional<' + I + 'IChunkMagic>')
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total)

print('== globais (List/super/setValue/neighbor) ==')
total = 0
for p in all_java():
    t = load(p)
    o = t
    # List<Cls> x = ...getEntitiesOfClass(Cls.class, ...)
    t = re.sub(r'\bList (\w+) = ([^;\n]*?)getEntitiesOfClass\((\w+)\.class', r'List<\3> \1 = \2getEntitiesOfClass(\3.class', t)
    # super((AbstractContainerMenu)x, y, (Component)z);
    t = re.sub(r'super\(\(AbstractContainerMenu\)(\w+), (\w+), \(Component\)([^)]+)\);', r'super(\1, \2, \3);', t)
    # setValue lines: tira casts (Comparable)/(Property)
    lines = t.split('\n')
    for i, ln in enumerate(lines):
        if '.setValue(' in ln:
            ln2 = ln.replace('(Comparable)', '').replace('(Property)', '')
            lines[i] = ln2
    t = '\n'.join(lines)
    # neighborChanged 5-arg -> handleNeighborChanged
    t = t.replace('pLevel.getBlockState(offsetPos).neighborChanged(pLevel, offsetPos, pNeighborBlock, pNeighborPos, pMovedByPiston);',
                  'pLevel.getBlockState(offsetPos).handleNeighborChanged(pLevel, offsetPos, pNeighborBlock, pNeighborPos, pMovedByPiston);')
    t = t.replace('state.neighborChanged((Level)world, pos, world.getBlockState(neighbor).getBlock(), neighbor, false);',
                  'state.handleNeighborChanged((Level)world, pos, world.getBlockState(neighbor).getBlock(), neighbor, false);')
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total)

print('== ChalkRuneBlock getEntities ==')
p = 'src/main/java/com/mna/blocks/ritual/ChalkRuneBlock.java'
t = load(p)
t = rep(t, 'List rituals = world.getEntities((EntityTypeTest)EntityInit.RITUAL_ENTITY.get(), new AABB(pos).inflate(16.0), e -> true);',
        'List<Ritual> rituals = world.getEntities((net.minecraft.world.level.entity.EntityTypeTest<Ritual, ?>)EntityInit.RITUAL_ENTITY.get(), new AABB(pos).inflate(16.0), e -> true);',
        'chalk')
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
