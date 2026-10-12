import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')

BLOCK = re.compile(r'/\*.*?\*/', re.S)
LINE = re.compile(r'//[^\n]*')

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

# 1) <var>.poseStack.getLast()  ->  <var>.last()
p1 = re.compile(r'(\w+(?:\.\w+)*)\.poseStack\.getLast\(\)')

# 2) setNormal(<Matrix3f var>, ...) -> setNormal(<poseStack>.last(), ...)
#    O mod declara: Matrix3f normal = <poseStack>.poseStack.getLast().normal();
#    Solucao: declarar PoseStack.Pose normal = ... .last();  e passar direto.
#    Estratégia segura: converter a DECLARACAO do local e remover o .normal()
#    apenas nas linhas que alimentam setNormal.

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

a = b = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    orig = body
    # passo 1
    new = p1.sub(lambda m: m.group(1) + '.last()', body)
    # passo 2: declaracoes "Matrix3f x = <expr>.last().normal();" -> "PoseStack.Pose x = <expr>.last();"
    new = re.sub(
        r'\bMatrix3f\s+(\w+)\s*=\s*([^;]*?)\.last\(\)\.normal\(\)',
        lambda m: 'PoseStack.Pose ' + m.group(1) + ' = ' + m.group(2) + '.last()',
        new)
    if new != orig:
        if 'import com.mojang.blaze3d.vertex.PoseStack;' not in new:
            # so adiciona se realmente usarmos PoseStack.Pose
            if 'PoseStack.Pose ' in new:
                lines = new.split('\n')
                last = max(i for i, l in enumerate(lines) if l.startswith('import '))
                lines.insert(last + 1, 'import com.mojang.blaze3d.vertex.PoseStack;')
                new = '\n'.join(lines)
        open(p, 'w', encoding='utf-8').write(new)
        a += 1

# contar
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    b += len(re.findall(r'PoseStack\.Pose \w+ = ', body))

print('arquivos alterados:', a)
print('declaracoes PoseStack.Pose criadas:', b)
