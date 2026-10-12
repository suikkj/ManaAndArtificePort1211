import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')
BLOCK = re.compile(r'/\*.*?\*/', re.S)
LINE = re.compile(r'//[^\n]*')

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

# Apenas dentro de chamadas que exigem Holder<MobEffect>:
#   hasEffect(X.get())      -> hasEffect(X)
#   getEffect(X.get())      -> getEffect(X)
#   removeEffect(X.get())   -> removeEffect(X)
#   new MobEffectInstance(X.get(), ...) -> new MobEffectInstance(X, ...)
# X = (EffectInit|MAEffects|MobEffects|BuiltInRegistries.MOB_EFFECT).\w+  + cadeia .get() opcional

EFF = r'(?:(?:EffectInit|MAEffects|MobEffects|BuiltInRegistries\.MOB_EFFECT)\.\w+(?:\.get\(\))*)'

def strip_get(expr):
    # remove todos os .get() no FINAL da expressao
    while expr.endswith('.get()'):
        expr = expr[:-7]
    return expr

pats = [
    re.compile(r'(\bhasEffect\(\s*)(' + EFF + r')(\s*\))'),
    re.compile(r'(\bgetEffect\(\s*)(' + EFF + r')(\s*\))'),
    re.compile(r'(\bremoveEffect\(\s*)(' + EFF + r')(\s*\))'),
    re.compile(r'(new MobEffectInstance\(\s*)(' + EFF + r')(\s*,)'),
]

tot = 0
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if 'EffectInit.' not in body and 'MAEffects.' not in body and 'MobEffects.' not in body:
        continue
    orig = body
    for pat in pats:
        body = pat.sub(lambda m: m.group(1) + strip_get(m.group(2)) + m.group(3), body)
    if body != orig:
        open(p, 'w', encoding='utf-8').write(body)
        fl += 1
        tot += sum(len(pat.findall(orig)) for pat in pats)

print('arquivos:', fl, 'substituicoes:', tot)
