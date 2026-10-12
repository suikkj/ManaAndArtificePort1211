# _fix14.py - efeitos: pass A (implements faltante) + pass B (arquivos sem super call)
import os
import re

FAILS = []
EFF = 'src/main/java/com/mna/effects'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def extract_body(t, sig):
    k = t.find(sig)
    if k < 0:
        return None
    j = t.find('{', k)
    depth = 0
    i = j
    while i < len(t):
        c = t[i]
        if c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                return (k, i + 1, t[j + 1:i])
        i += 1
    return None


print('== pass A: implements IMNAEffectHook ==')
n = 0
for dp, _, fns in os.walk(EFF):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        if 'onEffectAdded(' not in t and 'onEffectRemoved(' not in t:
            continue
        if 'IMNAEffectHook' in t:
            continue
        m = re.search(r'^implements ([\w., <>]+) \{$', t, re.M)
        if m:
            t = t[:m.start()] + 'implements ' + m.group(1) + ', com.mna.api.effects.IMNAEffectHook {' + t[m.end():]
        else:
            m2 = re.search(r'^(public (?:abstract )?class \w+ extends [\w.<>, ?]+) \{$', t, re.M)
            if not m2:
                print('  sem decl:', p)
                FAILS.append(p)
                continue
            t = t[:m2.start()] + m2.group(1) + ' implements com.mna.api.effects.IMNAEffectHook {' + t[m2.end():]
        save(p, t)
        n += 1
print('  arquivos:', n)

print('== pass B: sem super call ==')
n = 0
for dp, _, fns in os.walk(EFF):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        sep = '\r\n' if '\r\n' in t else '\n'
        for kind, hookname, args21, supcall in (
                ('addAttributeModifiers', 'onEffectAdded', 'AttributeMap {m}, int {a}', 'super.addAttributeModifiers({m}, {a});'),
                ('removeAttributeModifiers', 'onEffectRemoved', 'AttributeMap {m}', 'super.removeAttributeModifiers({m});')):
            sig = re.search(r'public void ' + kind + r'\(LivingEntity (\w+), AttributeMap (\w+), int (\w+)\) \{', t)
            if not sig:
                continue
            e, m, a = sig.group(1), sig.group(2), sig.group(3)
            r = extract_body(t, sig.group(0))
            if not r:
                continue
            k, end, body = r
            newsig = 'public void ' + kind + '(' + args21.format(m=m, a=a) + ') {'
            new_method = newsig + sep + '        ' + supcall.format(m=m, a=a) + sep + '    }'
            t = t[:k] + new_method + t[end:]
            rest = body.strip('\r\n')
            if rest.strip():
                if kind == 'addAttributeModifiers':
                    hook_sig = 'public void ' + hookname + '(LivingEntity ' + e + ', int ' + a + ') {'
                else:
                    hook_sig = 'public void ' + hookname + '(LivingEntity ' + e + ', int ' + a + ') {'
                lines = [l for l in rest.split(sep)]
                pre = []
                if re.search(r'\b' + m + r'\b', rest):
                    pre = ['        AttributeMap ' + m + ' = ' + e + '.getAttributes();']
                hook = hook_sig + sep + sep.join(pre + ['    ' + l if l.strip() else l for l in lines]) + sep + '    }'
                kk = t.find(newsig)
                t = t[:kk] + hook + sep + sep + '    ' + t[kk:]
        if t != o:
            if 'IMNAEffectHook' not in t:
                mdecl = re.search(r'^implements ([\w., <>]+) \{$', t, re.M)
                if mdecl:
                    t = t[:mdecl.start()] + 'implements ' + mdecl.group(1) + ', com.mna.api.effects.IMNAEffectHook {' + t[mdecl.end():]
                else:
                    m2 = re.search(r'^(public (?:abstract )?class \w+ extends [\w.<>, ?]+) \{$', t, re.M)
                    if m2:
                        t = t[:m2.start()] + m2.group(1) + ' implements com.mna.api.effects.IMNAEffectHook {' + t[m2.end():]
                    else:
                        print('  sem decl (B):', p)
                        FAILS.append(p)
            save(p, t)
            n += 1
print('  arquivos:', n)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
