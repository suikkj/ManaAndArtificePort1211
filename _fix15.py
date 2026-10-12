# _fix15.py - migracao LIMPA dos efeitos (in-place, sem re-find)
import os
import re

FAILS = []
EFF = 'src/main/java/com/mna/effects'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def extract_body(t, sig_start):
    j = t.find('{', sig_start)
    depth = 0
    i = j
    while i < len(t):
        c = t[i]
        if c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                return (j + 1, i)
        i += 1
    return None


def migrate(p):
    t = load(p)
    o = t
    sep = '\r\n' if '\r\n' in t else '\n'
    need_hook = False
    for kind, hookname in (('addAttributeModifiers', 'onEffectAdded'), ('removeAttributeModifiers', 'onEffectRemoved')):
        sig = re.search(r'public void ' + kind + r'\(LivingEntity (\w+), AttributeMap (\w+), int (\w+)\) \{', t)
        if not sig:
            continue
        e, m, a = sig.group(1), sig.group(2), sig.group(3)
        r = extract_body(t, sig.start())
        if not r:
            FAILS.append(p + ':' + kind + ':body')
            continue
        b0, b1 = r
        body = t[b0:b1]
        sup = re.compile(r'\s*super\.' + kind + r'\(\s*' + e + r'\s*,\s*' + m + r'\s*,\s*' + a + r'\s*\);\r?\n')
        ms = sup.search(body)
        if ms:
            rest = (body[:ms.start()] + body[ms.end():]).strip('\r\n')
        else:
            rest = body.strip('\r\n')
        if kind == 'addAttributeModifiers':
            newsig = 'public void addAttributeModifiers(AttributeMap ' + m + ', int ' + a + ') {'
            supcall = 'super.addAttributeModifiers(' + m + ', ' + a + ');'
            hook_sig = 'public void onEffectAdded(LivingEntity ' + e + ', int ' + a + ') {'
        else:
            newsig = 'public void removeAttributeModifiers(AttributeMap ' + m + ') {'
            supcall = 'super.removeAttributeModifiers(' + m + ');'
            hook_sig = 'public void onEffectRemoved(LivingEntity ' + e + ', int ' + a + ') {'
        new_method = newsig + sep + '        ' + supcall + sep + '    }'
        repl = new_method
        if rest.strip():
            need_hook = True
            pre = []
            if re.search(r'\b' + m + r'\b', rest):
                pre = ['        AttributeMap ' + m + ' = ' + e + '.getAttributes();']
            body_lines = rest.split(sep)
            hook = hook_sig + sep + sep.join(pre + ['    ' + l if l.strip() else l for l in body_lines]) + sep + '    }'
            repl = hook + sep + sep + '    ' + new_method
        # substituir do inicio da assinatura antiga ate o fim do corpo
        t = t[:sig.start()] + repl + t[b1 + 1:]
    if need_hook and 'IMNAEffectHook' not in t:
        mdecl = re.search(r'class (\w+)([^\{]*)\{', t)
        if mdecl:
            head = 'class ' + mdecl.group(1)
            tail = mdecl.group(2).rstrip()
            if 'implements' in tail:
                t = t[:mdecl.start()] + head + tail + ', com.mna.api.effects.IMNAEffectHook {' + t[mdecl.end():]
            else:
                t = t[:mdecl.start()] + head + tail + ' implements com.mna.api.effects.IMNAEffectHook {' + t[mdecl.end():]
        else:
            FAILS.append(p + ':decl')
    if t != o:
        save(p, t)
        return True
    return False


print('== migracao efeitos ==')
n = 0
for dp, _, fns in os.walk(EFF):
    for f in fns:
        if f.endswith('.java'):
            if migrate(os.path.join(dp, f)):
                n += 1
print('  arquivos:', n)

print('== this-as-holder + setSharedFlag ==')
n = 0
for dp, _, fns in os.walk(EFF):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = re.sub(r'\.removeEffect\(this\)', '.removeEffect(com.mna.api.tools.MAEffects.holder(this))', t)
        t = re.sub(r'\.getEffect\(this\)', '.getEffect(com.mna.api.tools.MAEffects.holder(this))', t)
        t = re.sub(r'\.hasEffect\(this\)', '.hasEffect(com.mna.api.tools.MAEffects.holder(this))', t)
        t = re.sub(r'new MobEffectInstance\(this,', 'new MobEffectInstance(com.mna.api.tools.MAEffects.holder(this),', t)
        t = re.sub(r'new MobEffectInstance\(\(MobEffect\)this,', 'new MobEffectInstance(com.mna.api.tools.MAEffects.holder(this),', t)
        # setSharedFlag(6, X) -> remover linha (setGlowingTag ja cuida)
        t = re.sub(r'\s*[\w.]+\.setSharedFlag\(6, (?:true|false)\);\r?\n', '\n', t)
        if t != o:
            save(p, t)
            n += 1
print('  arquivos:', n)

print('== addAttributeModifier wrap ==')
n = 0
for dp, _, fns in os.walk(EFF):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = re.sub(r'addAttributeModifier\((Attributes\.\w+),',
                   r'addAttributeModifier(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(\1),', t)
        t = re.sub(r'addAttributeModifier\(\(Attribute\)(NeoForgeMod\.\w+)\.get\(\),',
                   r'addAttributeModifier(\1,', t)
        if t != o:
            save(p, t)
            n += 1
print('  arquivos:', n)

print('== EffectCamouflage applyEffectTick ==')
p = EFF + '/beneficial/EffectCamouflage.java'
t = load(p)
old = '''    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        float allowedMovement = pAmplifier == 1 && pLivingEntity.isDiscrete() ? 0.075f : 0.01f;
        Vec3 movement = new Vec3(pLivingEntity.getDeltaMovement().x, 0.0, pLivingEntity.getDeltaMovement().z);
        if (movement.length() > (double)allowedMovement || pLivingEntity.isCurrentlyGlowing()) {
            EffectCamouflage.setCamoflagePercent(pLivingEntity, 0.0f);
            return;

        return true;
    }
        float curCamouflage = EffectCamouflage.getCamoflagePercent(pLivingEntity);
        curCamouflage = Math.min(curCamouflage + 0.0125f, 1.0f);
        EffectCamouflage.setCamoflagePercent(pLivingEntity, curCamouflage);
    }'''
new = '''    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        float allowedMovement = pAmplifier == 1 && pLivingEntity.isDiscrete() ? 0.075f : 0.01f;
        Vec3 movement = new Vec3(pLivingEntity.getDeltaMovement().x, 0.0, pLivingEntity.getDeltaMovement().z);
        if (movement.length() > (double)allowedMovement || pLivingEntity.isCurrentlyGlowing()) {
            EffectCamouflage.setCamoflagePercent(pLivingEntity, 0.0f);
            return true;
        }
        float curCamouflage = EffectCamouflage.getCamoflagePercent(pLivingEntity);
        curCamouflage = Math.min(curCamouflage + 0.0125f, 1.0f);
        EffectCamouflage.setCamoflagePercent(pLivingEntity, curCamouflage);
        return true;
    }'''
if '\r\n' in t and '\r\n' not in old:
    old = old.replace('\n', '\r\n')
    new = new.replace('\n', '\r\n')
if old in t:
    t = t.replace(old, new)
    save(p, t)
    print('  ok')
else:
    print('  NAO ACHOU (verificar manualmente)')
    FAILS.append('camouflage')

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
