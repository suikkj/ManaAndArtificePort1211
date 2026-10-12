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

# <expr>.getEffect().<metodoDeMobEffect>(...)  ->  <expr>.getEffect().value().<metodo>(...)
# metodos de MobEffect (nao de Holder)
MOB_METHODS = r'(?:isBeneficial|getColor|getCategory|getDescriptionId|isInstantenous|getAttributeModifiers|createTickEffects|applyEffectTick|applyInstantenousEffect|isDurationEffectTick|getGuiSort|isInstantenous)'

pats = [
    # e.getEffect().isBeneficial()  onde e = MobEffectInstance
    re.compile(r'(\b\w+)\.getEffect\(\)\.(' + MOB_METHODS + r')\('),
    # this.getEffect().isBeneficial()  em MobEffectInstance (variavel implicita)
    re.compile(r'(\.)getEffect\(\)\.(' + MOB_METHODS + r')\('),
]

tot = 0
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    orig = body
    for pat in pats:
        body = pat.sub(lambda m: m.group(1) + '.getEffect().value().' + m.group(2) + '(', body)
    if body != orig:
        open(p, 'w', encoding='utf-8').write(body)
        fl += 1
        tot += sum(len(pat.findall(orig)) for pat in pats)

print('arquivos:', fl, 'substituicoes:', tot)
