# _fix9.py - capabilities: MNACaps, delete getCapability mortos, ForgeCapabilities -> MNACaps,
# LazyOptional -> java.util.Optional
import os
import re

FAILS = []
ROOT = 'src/main/java/com/mna/'
IMPORT_CAPS = 'import net.neoforged.neoforge.common.capabilities.'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def all_java():
    out = []
    for dp, _, fns in os.walk('src/main/java'):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


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
    lines.insert(idx + 1, line)
    return sep.join(lines)


# ============================================================
# 1) MNACaps.java
# ============================================================
print('== MNACaps ==')
p = ROOT + 'api/tools/MNACaps.java'
if not os.path.exists(p):
    src = '''package com.mna.api.tools;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/** [PORT 1.21.1] Ponte para as capabilities padrao do NeoForge (substitui BlockEntity.getCapability). */
public final class MNACaps {

    private MNACaps() {
    }

    /** Item handler de um BlockEntity (lado opcional). */
    public static Optional<IItemHandler> itemHandler(BlockEntity be, Direction side) {
        if (be == null || be.getLevel() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(Capabilities.ItemHandler.BLOCK.getCapability(be.getLevel(), be.getBlockPos(), null, be, side));
    }
}
'''
    save(p, src)
    print('  ok criado')

# ============================================================
# 2) deletar getCapability mortos (LazyOptional<T> getCapability)
# ============================================================
print('== deletar getCapability mortos ==')
n = 0
for p in all_java():
    t = load(p)
    if 'LazyOptional<T> getCapability' not in t:
        continue
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    out = []
    i = 0
    while i < len(lines):
        l = lines[i]
        if 'LazyOptional<T> getCapability' in l:
            # anotacoes imediatamente acima
            while out and out[-1].strip().startswith('@'):
                out.pop()
            # pular ate fechar as chaves do metodo
            depth = 0
            started = False
            while i < len(lines):
                depth += lines[i].count('{') - lines[i].count('}')
                if '{' in lines[i]:
                    started = True
                if started and depth <= 0:
                    break
                i += 1
            i += 1
            # pular linha em branco seguinte
            if i < len(lines) and lines[i].strip() == '':
                i += 1
            n += 1
            continue
        out.append(l)
        i += 1
    save(p, sep.join(out))
print('  metodos removidos:', n)

# ============================================================
# 3) provider files: implements ICapabilitySerializable + imports
# ============================================================
print('== limpeza providers/imports ==')
n = 0
for p in all_java():
    t = load(p)
    o = t
    t = t.replace(' implements ICapabilitySerializable<Tag>', '')
    t = t.replace('\nimplements ICapabilitySerializable<Tag>', '')
    # imports da package removida
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = [l for l in t.split(sep) if not l.startswith(IMPORT_CAPS)]
    t = sep.join(lines)
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

# ============================================================
# 4) ForgeCapabilities -> MNACaps
# ============================================================
print('== ForgeCapabilities ==')
n = 0
for p in all_java():
    t = load(p)
    if 'ForgeCapabilities' not in t:
        continue
    o = t
    t = re.sub(r'([A-Za-z_][\w.]*(?:\(\))?)\.getCapability\(ForgeCapabilities\.ITEM_HANDLER, ',
               r'MNACaps.itemHandler(\1, ', t)
    t = t.replace('java.util.Optional<Object> handler', 'java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler')
    # ConstructIsFluidInContainer
    t = t.replace('java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandler> itemHandler = this.fluid.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);',
                  'java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandlerItem> itemHandler = net.neoforged.neoforge.fluids.FluidUtil.getFluidHandler(this.fluid);')
    if 'MNACaps.' in t:
        t = add_import(t, 'com.mna.api.tools.MNACaps')
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

# ============================================================
# 5) LazyOptional -> java.util.Optional (declaracoes)
# ============================================================
print('== LazyOptional decl ==')
n = 0
for p in all_java():
    t = load(p)
    o = t
    t = t.replace('LazyOptional worldMagicContainer;', 'java.util.Optional worldMagicContainer;')
    t = t.replace('(IWorldMagic)worldMagicContainer', '(IWorldMagic)worldMagicContainer.get()')
    t = t.replace('LazyOptional op;', 'java.util.Optional op;')
    t = t.replace('LazyOptional magicContainer;', 'java.util.Optional magicContainer;')
    t = t.replace('(IPlayerMagic)magicContainer', '(IPlayerMagic)magicContainer.get()')
    t = t.replace('LazyOptional magic;', 'java.util.Optional magic;')
    t = t.replace('(IPlayerMagic)magic)', '(IPlayerMagic)magic.get())')
    t = t.replace('LazyOptional progression;', 'java.util.Optional progression;')
    t = t.replace('LazyOptional handler;', 'java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler;')
    t = t.replace('(handler = te.getCapability(ForgeCapabilities.ITEM_HANDLER, redirFace)).isPresent()',
                  '(handler = MNACaps.itemHandler(te, redirFace)).isPresent()')
    t = t.replace('(IItemHandler)handler.get()', 'handler.get()')
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

# scan final: LazyOptional restante
print('== scan LazyOptional restante ==')
for p in all_java():
    t = load(p)
    if 'LazyOptional' in t:
        for ln, line in enumerate(t.split('\n'), 1):
            if 'LazyOptional' in line:
                print('   ', p.replace('src/main/java/com/mna/', ''), ln, line.strip()[:100])

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
