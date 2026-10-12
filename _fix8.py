# _fix8.py - save/load de ItemStack (Provider), ContainerHelper, saveAdditional/load,
# getUpdateTag, saveWithFullMetadata
import os
import re

FAILS = []
ROOT = 'src/main/java/com/mna/'
PROV = 'com.mna.api.tools.MNAItemData.provider()'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = new.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + '] x' + str(t.count(o)))
    return t.replace(o, n)


def all_java():
    out = []
    for dp, _, fns in os.walk('src/main/java'):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


# ============================================================
# 0) MNAItemData: helpers novos
# ============================================================
print('== MNAItemData helpers ==')
p = ROOT + 'api/tools/MNAItemData.java'
t = load(p)
if 'public static net.minecraft.core.HolderLookup.Provider provider()' not in t:
    add = '''
    /** Provider de registries para serializacao (server ativo; fallback vazio). */
    public static net.minecraft.core.HolderLookup.Provider provider() {
        net.minecraft.server.MinecraftServer srv = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        return srv != null ? srv.registryAccess() : net.minecraft.core.RegistryAccess.EMPTY;
    }

    /** [PORT 1.21.1] ItemStack.save agora exige provider. */
    public static CompoundTag save(ItemStack stack, CompoundTag tag) {
        return stack.save(provider(), tag);
    }

    /** [PORT 1.21.1] ItemStack.load agora exige provider. */
    public static void load(ItemStack stack, CompoundTag tag) {
        stack.load(provider(), tag);
    }

    /** [PORT 1.21.1] ContainerHelper.loadAllItems agora exige provider (ultimo arg). */
    public static void loadItems(CompoundTag tag, net.minecraft.core.NonNullList<ItemStack> list) {
        net.minecraft.world.ContainerHelper.loadAllItems(tag, list, provider());
    }

    /** [PORT 1.21.1] ContainerHelper.saveAllItems agora exige provider (ultimo arg). */
    public static CompoundTag saveItems(CompoundTag tag, net.minecraft.core.NonNullList<ItemStack> list) {
        return net.minecraft.world.ContainerHelper.saveAllItems(tag, list, provider());
    }
}'''
    t = t.rstrip()
    if t.endswith('}'):
        t = t[:-1] + add + '\n'
    save(p, t)
    print('  ok helpers adicionados')

# ============================================================
# 1) ItemStack .save(...) / .load(...)
# ============================================================
print('== ItemStack save/load ==')
R_NEW = r'(this\.lastCraftOutput|this\.hat|this\.banner|this\.heldItem|this\.staffStack|this\.defaultStack|this\.stack|this\.constructCapabilities\.getHat\(\)|this\.constructCapabilities\.getBanner\(\)|itemstack1|itemstack)\.save\(new CompoundTag\(\)\)'
R_VAR = r'(this\.getItem\([^)]*\)|this\.stack|this\.lastCraftOutput|this\.defaultStack|itemstack1|itemstack|recipe\.getOutput\(this\.level\))\.save\(([A-Za-z_]\w*)\)'
R_LOAD = r'(this\.stack|this\.heldItem|this\.defaultStack|itemstack1|itemstack|this\.getItem\([^)]*\)|this\.staffStack)\.load\('
n = 0
for p in all_java():
    t = load(p)
    o = t
    t = re.sub(R_NEW, r'MNAItemData.save(\1, new CompoundTag())', t)
    t = re.sub(R_VAR, r'MNAItemData.save(\1, \2)', t)
    t = t.replace('((ItemStack)e.getSecond()).save(new CompoundTag())',
                  'MNAItemData.save((ItemStack)e.getSecond(), new CompoundTag())')
    t = t.replace('inst.save(new CompoundTag())', '(CompoundTag)inst.save()')
    t = re.sub(R_LOAD, r'MNAItemData.load(', t)
    if t != o:
        if 'com.mna.api.tools.MNAItemData' not in t and 'MNAItemData' in t and 'import com.mna.api.tools.MNAItemData;' not in t:
            sep = '\r\n' if '\r\n' in t else '\n'
            lines = t.split(sep)
            idx = None
            for i, l in enumerate(lines):
                if l.startswith('import '):
                    idx = i
            lines.insert(idx + 1, 'import com.mna.api.tools.MNAItemData;')
            t = sep.join(lines)
        save(p, t)
        n += 1
print('  arquivos:', n)

# scan: .load( restantes suspeitos
print('  -- .load( restantes (verificar):')
for p in all_java():
    t = load(p)
    for ln, line in enumerate(t.split('\n'), 1):
        if re.search(r'\.load\(', line) and 'loadAdditional' not in line and 'MNAItemData' not in line and 'ContainerHelper' not in line:
            s = line.strip()
            if s.startswith('*') or s.startswith('//'):
                continue
            print('    ', p.replace('src/main/java/com/mna/', ''), ln, '->', s[:100])

# ============================================================
# 2) ContainerHelper
# ============================================================
print('== ContainerHelper ==')
n = 0
for p in all_java():
    t = load(p)
    if 'ContainerHelper.' not in t:
        continue
    o = t
    for fn, helper in (('loadAllItems', 'loadItems'), ('saveAllItems', 'saveItems')):
        needle = 'ContainerHelper.' + fn + '('
        out = []
        i = 0
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
            # splitar em 2 args de nivel 0
            d = 0
            q = -1
            for idx, ch in enumerate(a):
                if ch == '(':
                    d += 1
                elif ch == ')':
                    d -= 1
                elif ch == ',' and d == 0:
                    q = idx
                    break
            if q > 0:
                a1 = a[:q].strip()
                a2 = a[q + 1:].strip()
                out.append('MNAItemData.' + helper + '(' + a1 + ', ' + a2 + ')')
            else:
                out.append(needle + a + ')')
            i = j
        t = ''.join(out)
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

# ============================================================
# 3) saveAdditional / load -> 2-arg (tiles)
# ============================================================
print('== saveAdditional/load ==')
n = 0
for p in all_java():
    t = load(p)
    o = t
    # saveAdditional
    t = re.sub(r'void saveAdditional\(CompoundTag (\w+)\) \{',
               r'void saveAdditional(CompoundTag \1, net.minecraft.core.HolderLookup.Provider registries) {', t)
    t = re.sub(r'super\.saveAdditional\((\w+)\);', r'super.saveAdditional(\1, registries);', t)
    # load -> loadAdditional (somente arquivos que tem saveAdditional OU super.load)
    if 'void saveAdditional(CompoundTag' in o or 'super.load(' in o:
        t = re.sub(r'void load\(CompoundTag (\w+)\) \{',
                   r'void loadAdditional(CompoundTag \1, net.minecraft.core.HolderLookup.Provider registries) {', t)
        t = re.sub(r'super\.load\((\w+)\);', r'super.loadAdditional(\1, registries);', t)
        t = re.sub(r'this\.load\(([^)]+)\);', r'this.loadAdditional(\1, ' + PROV + ');', t)
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

# ============================================================
# 4) getUpdateTag / saveWithFullMetadata
# ============================================================
print('== getUpdateTag/saveWithFullMetadata ==')
n = 0
for p in all_java():
    t = load(p)
    o = t
    t = re.sub(r'CompoundTag getUpdateTag\(\) \{',
               r'CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {', t)
    t = t.replace('super.getUpdateTag()', 'super.getUpdateTag(registries)')
    t = t.replace('.getUpdateTag()', '.getUpdateTag(' + PROV + ')')
    t = t.replace('saveWithFullMetadata()', 'saveWithFullMetadata(' + PROV + ')')
    t = t.replace('saveWithoutMetadata()', 'saveWithoutMetadata(' + PROV + ')')
    t = t.replace('saveWithId()', 'saveWithId(' + PROV + ')')
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
