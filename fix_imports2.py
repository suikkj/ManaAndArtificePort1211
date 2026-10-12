import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
SRC = os.path.join(W, 'src', 'main', 'java')
LINE = re.compile(r'//[^\n]*')
BLOCK = re.compile(r'/\*.*?\*/', re.S)

def sc(t):
    return LINE.sub(' ', BLOCK.sub(' ', t))

files = []
for dp, _, fns in os.walk(SRC):
    for f in fns:
        if f.endswith('.java'):
            files.append(os.path.join(dp, f))

# classe -> (novo import, novo nome de classe)
RENAMES = [
    ('EntityItemPickupEvent', 'net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent'),
    ('ForgeSlider', 'net.neoforged.neoforge.client.gui.widget.ExtendedSlider'),
    ('RegisterGuiOverlaysEvent', 'net.neoforged.neoforge.client.event.RegisterGuiLayersEvent'),
    ('RenderGuiOverlayEvent', 'net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent'),
    ('IForgeMenuType', 'net.neoforged.neoforge.common.extensions.IMenuTypeExtension'),
    ('IForgeItem', 'net.neoforged.neoforge.common.extensions.IItemExtension'),
    ('IForgeBlock', 'net.neoforged.neoforge.common.extensions.IBlockExtension'),
    ('IPlantable', 'net.neoforged.neoforge.common.IPlantable'),
    ('PlantType', 'net.neoforged.neoforge.common.PlantType'),
]

tot = 0
fl = 0
for p in files:
    body = sc(open(p, encoding='utf-8', errors='replace').read())
    orig = body
    for cls, newimp in RENAMES:
        # troca import
        old_imp = re.search(r'^import [\w.]*\.' + cls + r';$', body, re.M)
        if old_imp:
            new_cls = newimp.split('.')[-1]
            body = re.sub(r'^import [\w.]*\.' + cls + r';$', 'import ' + newimp + ';', body, flags=re.M)
            # troca usos do tipo (word boundary), exceto quando ja for o novo nome
            body = re.sub(r'\b' + cls + r'\b(?!\w)', new_cls, body)
    if body != orig:
        open(p, 'w', encoding='utf-8').write(body)
        fl += 1

print('arquivos alterados:', fl)
