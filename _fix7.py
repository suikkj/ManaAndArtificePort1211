# _fix7.py - CommonEventHandler (BlockEvent cancel, Explosion.center, setCancellationResult
# indevidos, event.player) + imports do Lodestar (subpacote)
import os

FAILS = []
ROOT = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = n.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + '] x' + str(t.count(o)))
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


print('== CommonEventHandler ==')
p = ROOT + 'events/CommonEventHandler.java'
t = load(p)
# 1) BlockEvent base nao e cancelable -> ICancellableEvent
t = rep(t, '''            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {''',
        '''            ((net.neoforged.bus.api.ICancellableEvent)event).setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {''', 'blockevent cancel')
# 2) Explosion.getPosition() -> center()
t = rep(t, 'event.getExplosion().getPosition()', 'event.getExplosion().center()', 'explosion center')
# 3) setCancellationResult em eventos que nao suportam
t = rep(t, '''        if (!CommonEventHandler.handleWardBlockEvent(world, event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
            event.setCanceled(true); event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }''', '''        if (!CommonEventHandler.handleWardBlockEvent(world, event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
        }''', 'break cancelresult')
t = rep(t, '''            event.setCanceled(true); event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            event.setCanceled(true);
        }
        super.onEntityJoinWorld(event);''', '''            event.setCanceled(true);
        }
        super.onEntityJoinWorld(event);''', 'entityjoin 1')
# 4) event.player restantes -> event.getEntity()
n = t.count('event.player')
t = t.replace('event.player', 'event.getEntity()')
print('  event.player restantes corrigidos:', n)
save(p, t)

# conferir variante 2 do EntityJoinLevel (870)
t = load(p)
t = rep(t, '''                event.setCanceled(true); event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
                event.setCanceled(true);''', '''                event.setCanceled(true);''', 'entityjoin 2')
save(p, t)

print('== Lodestar imports ==')
for rel in ['gui/widgets/lodestar/LodestarNode.java', 'gui/widgets/lodestar/LodestarParameter.java']:
    p = ROOT + rel
    t = load(p)
    if 'extends MNAImageButton' in t and 'import com.mna.gui.widgets.MNAImageButton;' not in t:
        t = add_import(t, 'com.mna.gui.widgets.MNAImageButton')
        save(p, t)
        print('  ok ' + rel)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
