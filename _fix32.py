# _fix32.py - getOrCreateTagElement helper, RenderGuiEvent.Pre, getWindow, dataSlots->MNAMenuAccess,
#             readNbt, lookScale decls, ConstructRenderer.vertex
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


S = 'src/main/java/com/mna/'

print('== MNAItemData helper (force) ==')
p = S + 'api/tools/MNAItemData.java'
t = load(p)
if 'public static CompoundTag getOrCreateTagElement' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** Equivalente ao antigo {@code stack.getOrCreateTagElement(key)} (escritas persistem). */
    public static CompoundTag getOrCreateTagElement(ItemStack stack, String key) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        CompoundTag element = tag.getOrCreate(key);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return element;
    }
''' + t[idx:]
    save(p, t)
    print('  ok')
else:
    print('  ja existe')

print('== globais ==')
total = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = t.replace('CustomizeGuiOverlayEvent.Pre', 'net.neoforged.neoforge.client.event.RenderGuiEvent.Pre')
        t = t.replace('event.getWindow()', 'net.minecraft.client.Minecraft.getInstance().getWindow()')
        t = t.replace('this.dataSlots', 'com.mna.gui.containers.MNAMenuAccess.dataSlots(this)')
        t = t.replace('.readAnySizeNbt()', '.readNbt()')
        t = re.sub(r'(^\s*)lookScale = (0\.06|-0\.01);', r'\1double lookScale = \2;', t, flags=re.M)
        t = t.replace('scaled_look = look.scale(lookScale);', 'net.minecraft.world.phys.Vec3 scaled_look = look.scale(lookScale);')
        t = t.replace('ConstructRenderer.addVertex(', 'ConstructRenderer.vertex(')
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
