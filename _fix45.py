# _fix45.py - OPTIONAL_STREAM_CODEC decode cast, UUID->RLoc global, REACH_DISTANCE_ID, BlockUtils stack,
#             GuiGuideBook btn, FLUIDT .value(), canBreatheUnderwater rename
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


S = 'src/main/java/com/mna/'

print('== ManaAndArtificeMod.REACH_DISTANCE_ID ==')
p = S + 'api/ManaAndArtificeMod.java'
t = load(p)
if 'REACH_DISTANCE_ID' not in t:
    old = '    public static final UUID REACH_DISTANCE_UUID = UUID.fromString("cfb81e6d-8447-4e92-8683-ae23800cecca");'
    new = old + '\n    public static final net.minecraft.resources.ResourceLocation REACH_DISTANCE_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mna", "reach_distance");'
    if old in t:
        t = t.replace(old, new)
        save(p, t)
        print('  ok')
    else:
        print('  FALHOU')
        FAILS.append('reach')

print('== globais ==')
total = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = t.replace('ManaAndArtificeMod.REACH_DISTANCE_UUID', 'ManaAndArtificeMod.REACH_DISTANCE_ID')
        t = re.sub(r'OPTIONAL_STREAM_CODEC\.decode\((\w+)\)', r'OPTIONAL_STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf)\1)', t)
        t = re.sub(r'new AttributeModifier\(UUID\.fromString\("([0-9a-fA-F-]+)"\), ', r'new AttributeModifier(ResourceLocation.fromNamespaceAndPath("mna", "\1"), ', t)
        t = re.sub(r'getFluidTypeHeight\((NeoForgeMod\.\w+)\)', r'getFluidTypeHeight((\1).value())', t)
        t = re.sub(r'jumpInFluid\((NeoForgeMod\.\w+)\)', r'jumpInFluid((\1).value())', t)
        if f == 'BlockUtils.java':
            t = t.replace('            stack = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);', '            ItemStack stack = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);')
        if f == 'GuiGuideBook.java':
            t = t.replace('btn = new ImageSpellPartButton(', 'ImageButton btn = new ImageSpellPartButton(')
            t = t.replace('btn = new ImageItemStackButton(', 'ImageButton btn = new ImageItemStackButton(')
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print('== canBreatheUnderwater ==')
for f in ['entities/constructs/animated/Construct', 'entities/constructs/MagicBroom', 'entities/faction/util/FactionWar', 'entities/summon/SummonedSpectralHorse']:
    p = S + f + '.java'
    t = load(p)
    o = t
    t = t.replace('@Override\n    public boolean canBreatheUnderwater()', '    public boolean mnaCanBreatheUnderwater()')
    t = t.replace('public boolean canBreatheUnderwater()', 'public boolean mnaCanBreatheUnderwater()')
    if t != o:
        save(p, t)
        print('  ok', f.split('/')[-1])

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
