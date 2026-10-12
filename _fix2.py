# _fix2.py - lote 2 de correcoes do port 1.21.1
import os
import re

FAILS = []
ROOT = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag, count=0):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = n.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    c = t.count(o)
    print('  ok [' + tag + '] x' + str(c))
    return t.replace(o, n)


def add_import(t, imp):
    line = 'import ' + imp + ';'
    if line in t:
        return t
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    idx = None
    for i, l in enumerate(lines):
        if l.startswith('import '):
            idx = i
    if idx is None:
        return t
    lines.insert(idx + 1, line)
    print('  +import ' + imp)
    return sep.join(lines)


def all_java():
    out = []
    for dp, _, fns in os.walk(ROOT):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


# ============================================================
# 0) Helpers novos
# ============================================================
print('== helpers novos ==')
mnageo = 'src/main/java/com/mna/tools/render/MNAGeo.java'
if not os.path.exists(mnageo):
    save(mnageo, '''package com.mna.tools.render;

/**
 * [PORT 1.21.1] GeckoLib 4.8.4 trocou os parametros float red/green/blue/alpha
 * por um unico int colour (ARGB empacotado).
 */
public final class MNAGeo {
    private MNAGeo() {}

    public static int packColour(float red, float green, float blue, float alpha) {
        int r = (int) (red * 255.0F) & 0xFF;
        int g = (int) (green * 255.0F) & 0xFF;
        int b = (int) (blue * 255.0F) & 0xFF;
        int a = (int) (alpha * 255.0F) & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
''')
    print('  criado MNAGeo.java')

mnanbt = 'src/main/java/com/mna/api/tools/MNANbt.java'
if not os.path.exists(mnanbt):
    save(mnanbt, '''package com.mna.api.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * [PORT 1.21.1] Ajudantes de NBT para formatos legados.
 */
public final class MNANbt {
    private MNANbt() {}

    /** Le BlockPos no formato antigo {X,Y,Z} (pre-1.21). */
    public static BlockPos readBlockPosLegacy(CompoundTag tag) {
        if (tag == null || !tag.contains("X")) {
            return null;
        }
        return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }
}
''')
    print('  criado MNANbt.java')

# ============================================================
# 1) addAttributeModifier(String) -> ResourceLocation.parse(String)
# ============================================================
print('== addAttributeModifier ==')
n = 0
for p in all_java():
    t = load(p)
    if 'addAttributeModifier(' not in t:
        continue
    t2 = re.sub(r'addAttributeModifier\(([^,]+), "([^"]+)"', r'addAttributeModifier(\1, ResourceLocation.parse("\2")', t)
    if t2 != t:
        t2 = add_import(t2, 'net.minecraft.resources.ResourceLocation')
        save(p, t2)
        n += 1
print('  arquivos:', n)

# ============================================================
# 2) NbtUtils.readBlockPos
# ============================================================
print('== readBlockPos ==')
# 2a) caso especial MultiblockConfiguration (compound direto, formato legado)
p = ROOT + 'recipes/multiblock/MultiblockConfiguration.java'
t = load(p)
t = rep(t, 'NbtUtils.readBlockPos((CompoundTag)((CompoundTag)nbt.get("offset")))',
        'com.mna.api.tools.MNANbt.readBlockPosLegacy((CompoundTag)nbt.get("offset"))', 'multiblock legacy')
save(p, t)

# 2b) demais: readBlockPos((CompoundTag)PARENT.getCompound(KEY)) -> readBlockPos(PARENT, KEY).orElse(null)
n = 0
sites = 0
for p in all_java():
    t = load(p)
    if 'NbtUtils.readBlockPos(' not in t:
        continue
    out = []
    i = 0
    hit = False
    needle = 'NbtUtils.readBlockPos('
    while True:
        k = t.find(needle, i)
        if k < 0:
            out.append(t[i:])
            break
        out.append(t[i:k])
        j = k + len(needle)
        depth = 1
        a = ''
        while depth > 0 and j < len(t):
            c = t[j]
            if c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
            if depth > 0:
                a += c
            j += 1
        m = re.match(r'^\(CompoundTag\)(.+)\.getCompound\((.+)\)$', a.strip())
        if m:
            parent = m.group(1).strip()
            key = m.group(2).strip()
            out.append('NbtUtils.readBlockPos(' + parent + ', ' + key + ').orElse(null)')
            hit = True
            sites += 1
        else:
            out.append(needle + a + ')')
        i = j
    if hit:
        save(p, ''.join(out))
        n += 1
print('  arquivos:', n, 'sites:', sites)

# ============================================================
# 3) GlassBlock -> TransparentBlock
# ============================================================
print('== GlassBlock ==')
p = ROOT + 'blocks/decoration/RunicGlassBlock.java'
t = load(p)
t = rep(t, 'import net.minecraft.world.level.block.GlassBlock;',
        'import net.minecraft.world.level.block.TransparentBlock;', 'glass import')
t = rep(t, 'extends GlassBlock', 'extends TransparentBlock', 'glass extends')
save(p, t)

# ============================================================
# 4) GeckoLib rgba -> colour
# ============================================================
print('== GeckoLib ==')
gecko_files = []
for p in all_java():
    t = load(p)
    if 'red, green, blue, alpha' in t:
        gecko_files.append(p)
print('  arquivos com rgba:', len(gecko_files))

DECL = re.compile(r'(public void (?:actuallyRender|renderRecursively|preRender|postRender)\([^)]*?), float red, float green, float blue, float alpha\)')


def gecko_convert(t, p):
    ch = False
    # declaracoes de override
    t2 = DECL.sub(r'\1, int colour)', t)
    if t2 != t:
        ch = True
        t = t2
    # MAGeckoRenderer/ConstructRenderer renderPass decl (rgba + packedOverlay)
    t2 = t.replace(', float red, float green, float blue, float alpha, int packedOverlay)',
                   ', int colour, int packedOverlay)')
    if t2 != t:
        ch = True
        t = t2
    # getPackedOverlay 2-arg -> 3-arg
    t2 = re.sub(r'getPackedOverlay\((?:\(Entity\)|\(GeoAnimatable\))?animatable, ([^,)]+)\)',
                r'getPackedOverlay(animatable, \1, 0.0f)', t)
    if t2 != t:
        ch = True
        t = t2
    # forwards com rgba -> colour (dentro dos metodos convertidos) - ANTES do packColour
    t2 = t.replace(', red, green, blue, alpha)', ', colour)')
    if t2 != t:
        ch = True
        t = t2
    # call-sites de renderPass com rgba local -> packColour (depois, para nao ser corrompido)
    t2 = re.sub(r'(this\.renderPass\([^;]*?), red, green, blue, alpha, packedOverlay\)',
                r'\1, com.mna.tools.render.MNAGeo.packColour(red, green, blue, alpha), packedOverlay)', t)
    if t2 != t:
        ch = True
        t = t2
    # casts de animatable removidos
    for cast in ['(GeoAnimatable)animatable', '(Entity)animatable', '(BlockEntity)animatable']:
        if cast in t:
            t = t.replace(cast, 'animatable')
            ch = True
    return t, ch


for p in gecko_files:
    t = load(p)
    t2, ch = gecko_convert(t, p)
    if ch:
        save(p, t2)

# MAGeckoRenderer: bound do generico
p = ROOT + 'entities/renderers/MAGeckoRenderer.java'
t = load(p)
t = rep(t, 'public abstract class MAGeckoRenderer<T extends LivingEntity>',
        'public abstract class MAGeckoRenderer<T extends LivingEntity & GeoAnimatable>', 'MAGecko bound')
save(p, t)

# ============================================================
# 5) MAParticleType
# ============================================================
print('== MAParticleType ==')
p = ROOT + 'api/particles/MAParticleType.java'
t = load(p)
t = rep(t, 'public static final Codec<MAParticleType> CODEC = RecordCodecBuilder.create(',
        'public static final com.mojang.serialization.MapCodec<MAParticleType> CODEC = RecordCodecBuilder.mapCodec(', 'particle CODEC')
t = rep(t, 'public Codec<MAParticleType> codec() {',
        'public com.mojang.serialization.MapCodec<MAParticleType> codec() {', 'particle codec()')
t = rep(t, 'static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, MAParticleType> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of(MAParticleType::writeToNetwork, MAParticleType::readFromNetwork);',
        'static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, MAParticleType> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, value) -> value.writeToNetwork(buf), MAParticleType::readFromNetwork);',
        'particle STREAM_CODEC')
t = rep(t, '''    public com.mojang.serialization.MapCodec<MAParticleType> codec() {
        return CODEC;
    }''', '''    public com.mojang.serialization.MapCodec<MAParticleType> codec() {
        return CODEC;
    }

    @Override
    public net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, MAParticleType> streamCodec() {
        return STREAM_CODEC;
    }''', 'particle streamCodec')
save(p, t)

# ============================================================
# 6) ring_large -> ModelResourceLocation
# ============================================================
print('== ring_large ==')
p = ROOT + 'blocks/tileentities/renderers/ArcaneSentryRenderer.java'
t = load(p)
t = rep(t, 'getModel(RunicAnvilRenderer.ring_large)',
        'getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(RunicAnvilRenderer.ring_large))', 'ring_large')
save(p, t)

# ============================================================
# 7) setLootTable
# ============================================================
print('== setLootTable ==')
p = ROOT + 'blocks/tileentities/ManaweaveCacheTile.java'
t = load(p)
t = rep(t, 'super.setLootTable(lootTableIn, seedIn);',
        'super.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, lootTableIn), seedIn);',
        'loot table')
save(p, t)

# ============================================================
# scanner final
# ============================================================
print()
print('== sobras ==')
leftover = 0
for p in all_java():
    t = load(p)
    for pat, name in [(r'red, green, blue, alpha', 'rgba'),
                      (r'NbtUtils\.readBlockPos\(\(CompoundTag\)', 'readBlockPos-antigo'),
                      (r'addAttributeModifier\([^,]+, "', 'attr-string'),
                      (r'extends GlassBlock\b', 'GlassBlock')]:
        if re.search(pat, t):
            for ln, line in enumerate(t.split('\n'), 1):
                if re.search(pat, line):
                    print('  [' + name + ']', p, ':', ln, '->', line.strip()[:110])
                    leftover += 1
print('sobras:', leftover)
print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
