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

# EffectInit.CAMPO.get()  ou  MAEffects.CAMPO.get()
# seguido de ) , ;  ou seja: fim de expressao -> remover .get()
# \w+ exige caractere word APOS o nome, entao ENLARGE.get() casa inteiro.
pat = re.compile(r'\b((?:EffectInit|MAEffects)\.\w+)\.get\(\)(?=\s*[),;])')

tot = 0
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    if 'EffectInit.' not in body and 'MAEffects.' not in body:
        continue
    hits = pat.findall(body)
    if not hits:
        continue
    new = pat.sub(lambda m: m.group(1), body)
    if new != body:
        open(p, 'w', encoding='utf-8').write(new)
        fl += 1
        tot += len(hits)

print('arquivos:', fl, 'substituicoes:', tot)
