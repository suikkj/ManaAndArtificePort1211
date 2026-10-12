# _fix20.py - quick wins: hurtAndBreak (lambda -> slot), ResourceLocation 2-arg -> fromNamespaceAndPath
import os
import re

FAILS = []


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


def rep(t, old, new, tag):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = new.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + ']')
    return t.replace(o, n)


def fix_rl(t):
    """new ResourceLocation(A, B) -> ResourceLocation.fromNamespaceAndPath(A, B) (2 args de nivel 0)."""
    needle = 'new ResourceLocation('
    out = []
    i = 0
    n = 0
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
        # splitar em args de nivel 0
        d = 0
        args = []
        cur = ''
        for ch in a:
            if ch == '(':
                d += 1
            elif ch == ')':
                d -= 1
            if ch == ',' and d == 0:
                args.append(cur)
                cur = ''
            else:
                cur += ch
        if cur:
            args.append(cur)
        if len(args) == 2:
            out.append('ResourceLocation.fromNamespaceAndPath(' + args[0].strip() + ', ' + args[1].strip() + ')')
            n += 1
        else:
            out.append(needle + a + ')')
        i = j
    return ''.join(out), n


print('== hurtAndBreak MAINHAND ==')
n = 0
for p in all_java():
    t = load(p)
    o = t
    t = re.sub(r'\.hurtAndBreak\(([^,()]+(?:\([^()]*\))?[^,()]*), ([^,()]+(?:\([^()]*\))?[^,()]*), \w+ -> \w+\.broadcastBreakEvent\(EquipmentSlot\.MAINHAND\)\)',
               r'.hurtAndBreak(\1, \2, EquipmentSlot.MAINHAND)', t)
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

print('== hurtAndBreak empty lambda ==')
fixes = [
    ('components', 'is.hurtAndBreak(damage * 4, (LivingEntity)playerTarget, e -> {})',
     'is.hurtAndBreak(damage * 4, (LivingEntity)playerTarget, ((LivingEntity)playerTarget).getEquipmentSlotForItem(is))'),
]
n = 0
for p in all_java():
    t = load(p)
    o = t
    t = t.replace('is.hurtAndBreak(damage * 4, (LivingEntity)playerTarget, e -> {})',
                  'is.hurtAndBreak(damage * 4, (LivingEntity)playerTarget, ((LivingEntity)playerTarget).getEquipmentSlotForItem(is))')
    t = t.replace('stack.hurtAndBreak(-repairAmount, (LivingEntity)player, p -> {})',
                  'stack.hurtAndBreak(-repairAmount, (LivingEntity)player, ((LivingEntity)player).getEquipmentSlotForItem(stack))')
    if t != o:
        save(p, t)
        n += 1
print('  arquivos:', n)

print('== CuriosInterop ==')
p = 'src/main/java/com/mna/interop/CuriosInterop.java'
t = load(p)
t = rep(t, '        CuriosApi.getCuriosInventory((LivingEntity)entity).ifPresent(handler -> handler.findFirstCurio(item).ifPresent(slotResult -> slotResult.stack().hurtAndBreak(damage, entity, damager -> CuriosApi.broadcastCurioBreakEvent((SlotContext)slotResult.slotContext()))));',
        '''        CuriosApi.getCuriosInventory((LivingEntity)entity).ifPresent(handler -> handler.findFirstCurio(item).ifPresent(slotResult -> {
            if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                slotResult.stack().hurtAndBreak(damage, serverLevel, entity, damager -> CuriosApi.broadcastCurioBreakEvent((SlotContext)slotResult.slotContext()));
            } else {
                slotResult.stack().hurtAndBreak(damage, entity, entity.getEquipmentSlotForItem(slotResult.stack()));
            }
        }));''', 'curios')
save(p, t)

print('== WizardChalk ==')
p = 'src/main/java/com/mna/items/WizardChalk.java'
if not os.path.exists(p):
    import subprocess
    for dp, _, fns in os.walk('src/main/java'):
        if 'WizardChalk.java' in fns:
            p = os.path.join(dp, 'WizardChalk.java')
t = load(p)
t = rep(t, 'context.getItemInHand().hurtAndBreak(1, (LivingEntity)context.getPlayer(), player -> player.broadcastBreakEvent(context.getHand()));',
        'context.getItemInHand().hurtAndBreak(1, (LivingEntity)context.getPlayer(), ((LivingEntity)context.getPlayer()).getEquipmentSlotForItem(context.getItemInHand()));', 'chalk')
save(p, t)

print('== ResourceLocation 2-arg ==')
total = 0
for p in all_java():
    t = load(p)
    if 'new ResourceLocation(' not in t:
        continue
    t2, n = fix_rl(t)
    if n:
        save(p, t2)
        total += n
print('  sites:', total)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
