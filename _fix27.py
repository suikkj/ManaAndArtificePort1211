# _fix27.py - Tesselator/BufferBuilder 1.21.1 (getBuilder+begin -> tesselator.begin; end -> buildOrThrow),
#             ParticleRenderType anonymous classes (begin(Tesselator,TextureManager))
import os
import re

FAILS = []
DRAW = 'com.mojang.blaze3d.vertex.BufferUploader.drawWithShader'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def sub(t, pat, rep, tag, expect=None, flags=0):
    t2, n = re.subn(pat, rep, t, flags=flags)
    ok = (expect is None and n > 0) or (expect is not None and n == expect)
    print('  ' + ('ok' if ok else 'FALHOU') + ' [' + tag + '] n=' + str(n))
    if not ok:
        FAILS.append(tag)
    return t2


def all_java():
    out = []
    for dp, _, fns in os.walk('src/main/java'):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


print('== 1. getBuilder()+begin -> tesselator.begin ==')
total = 0
for p in all_java():
    t = load(p)
    t2 = sub(t, r'BufferBuilder (\w+) = ([\w.()]+)\.getBuilder\(\);[ \t]*\r?\n[ \t]*\1\.begin\(([^;]+)\);',
             r'BufferBuilder \1 = \2.begin(\3);', 'merge', expect=None)
    if t2 != t:
        save(p, t2)
        total += 1
print('  arquivos:', total)

print('== 2. RenderedBuffer -> buildOrThrow ==')
total = 0
for p in all_java():
    t = load(p)
    t2 = sub(t, r'BufferUploader\.drawWithShader\(\(BufferBuilder\.RenderedBuffer\)(\w+)\.end\(\)\);',
             DRAW + r'(\1.buildOrThrow());', 'rendered')
    if t2 != t:
        save(p, t2)
        total += 1
print('  arquivos:', total)

print('== 3. ParticleRenderTypes: classes anonimas ==')
p = 'src/main/java/com/mna/particles/types/render/ParticleRenderTypes.java'
t = load(p)
t = sub(t, r'public void begin\(BufferBuilder bufferBuilder, TextureManager textureManager\) \{([\s\S]*?)bufferBuilder\.begin\(VertexFormat\.Mode\.QUADS, DefaultVertexFormat\.PARTICLE\);',
        r'public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {\1return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);',
        'prt-begin', expect=2)
t = sub(t, r'\r?\n\s*public void end\(Tesselator tessellator\) \{\r?\n\s*tessellator\.end\(\);\r?\n[\s\S]*?\r?\n\s*\}',
        '', 'prt-end-del', expect=2)
save(p, t)

print('== 4. FX files: classes anonimas + beginRenderCommon ==')
for name in ['FXAir', 'FXBlueFlame', 'FXDrip', 'FXDroplet']:
    p = 'src/main/java/com/mna/particles/' + name + '.java'
    t = load(p)
    t = sub(t, r'public void begin\(BufferBuilder bufferBuilder, TextureManager textureManager\) \{\r?\n\s*' + name + r'\.beginRenderCommon\(bufferBuilder, textureManager\);\r?\n\s*\}\r?\n\r?\n\s*public void end\(Tesselator tessellator\) \{\r?\n\s*tessellator\.end\(\);\r?\n\s*' + name + r'\.endRenderCommon\(\);\r?\n\s*\}',
            'public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {\n'
            '            BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);\n'
            '            ' + name + '.beginRenderCommon(bufferBuilder, textureManager);\n'
            '            return bufferBuilder;\n'
            '        }', name + '-anon')
    t = sub(t, r'\r?\n\s*buffer\.begin\(VertexFormat\.Mode\.QUADS, DefaultVertexFormat\.PARTICLE\);', '', name + '-begincall')
    save(p, t)

print('== 5. tessellator.end() restantes ==')
for p, var, old in [
    ('src/main/java/com/mna/gui/HUDOverlayRenderer.java', 'bufferbuilder', '        tesselator.end();'),
    ('src/main/java/com/mna/gui/radial/GenericRadialMenu.java', 'buffer', '        tessellator.end();'),
    ('src/main/java/com/mna/gui/radial/SpellModifierRadialSelect.java', 'buffer', '        tessellator.end();'),
    ('src/main/java/com/mna/tools/render/GuiRenderUtils.java', 'buffer', '        tesselator.end();'),
]:
    t = load(p)
    if old in t:
        t = t.replace(old, '        ' + DRAW + '(' + var + '.buildOrThrow());')
        save(p, t)
        print('  ok [' + os.path.basename(p) + ']')
    else:
        print('  FALHOU [' + os.path.basename(p) + ']')
        FAILS.append(p)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
