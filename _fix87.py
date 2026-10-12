# _fix87.py
import os, re

def find1(name):
    for dp, _, fns in os.walk('src/main/java'):
        if name in fns:
            return os.path.join(dp, name)
    return None

def load(p):
    return open(p, encoding='utf-8', newline='').read()

def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)

def rl(t, old, new):
    for eol in ['\n', '\r\n']:
        t = t.replace(old.replace('\n', eol), new.replace('\n', eol))
    return t

# 1 WardingCandleBlock: fix por linhas
p = find1('WardingCandleBlock.java')
t = load(p)
t = rl(t, '''        super.playerWillDestroy(worldIn, pos, state, player);
        return state;
        if (!worldIn.isClientSide) {''', '''        if (!worldIn.isClientSide) {''')
t = rl(t, '''        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override''', '''        }
        return super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override''')
save(p, t)
print('1 ok')

# 2 Function6/Function4 explicit
p = find1('AddChimeriteModifier.java')
t = load(p)
t = re.sub(r'\.apply\(inst, \(java\.util\.List<LootItemCondition> conditions[^;]*?\)\)\)\);',
           '.apply(inst, (com.mojang.datafixers.util.Function6<LootItemCondition[], Integer, Integer, Float, Float, Item, AddChimeriteModifier>) (conditions, numChimerite, magicLevel, chance, fortuneBonusChance, dropsWith) -> new AddChimeriteModifier(conditions, numChimerite, magicLevel, chance, fortuneBonusChance, dropsWith)));', t, count=1)
save(p, t)
p = find1('BeheadingModifier.java')
t = load(p)
t = re.sub(r'\.apply\(inst, \(java\.util\.List<LootItemCondition> conditions[^;]*?\)\)\)\);',
           '.apply(inst, (com.mojang.datafixers.util.Function4<LootItemCondition[], Float, EntityType<?>, Item, BeheadingModifier>) (conditions, chancePerLevel, mobType, head) -> new BeheadingModifier(conditions, chancePerLevel, mobType, head)));', t, count=1)
save(p, t)
print('2 ok')

# 3 RitualEffectGate orElse
p = find1('RitualEffectGate.java')
t = load(p)
t = t.replace('IPlayerProgression p = MNACap.<IPlayerProgression>get(context.getCaster(), PlayerProgressionProvider.PROGRESSION);', 'IPlayerProgression p = MNACap.<IPlayerProgression>get(context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);')
t = t.replace('IPlayerMagic m = MNACap.<IPlayerMagic>get(context.getCaster(), PlayerMagicProvider.MAGIC);', 'IPlayerMagic m = MNACap.<IPlayerMagic>get(context.getCaster(), PlayerMagicProvider.MAGIC).orElse(null);')
save(p, t)
print('3 ok')

# 4 NBTUtilities
p = find1('NBTUtilities.java')
t = load(p)
t = t.replace('(NbtAccounter)NbtAccounter.UNLIMITED', '(NbtAccounter)NbtAccounter.create(Long.MAX_VALUE)')
save(p, t)
print('4 ok')

# 5 ClientProxy onSelectedTabChanged
p = find1('ClientProxy.java')
t = load(p)
if 'onSelectedTabChanged' not in t:
    anchor = '    public void onPlaySound(SoundInstance inst, WeighedSoundEvents weight, float range) {'
    ins = '''    @Override
    public void onSelectedTabChanged(net.minecraft.advancements.AdvancementHolder advancement) {
    }

    public void onPlaySound(SoundInstance inst, WeighedSoundEvents weight, float range) {'''
    t = rl(t, anchor, ins)
    save(p, t)
print('5 ok')

# 6 MultiblockRenderer GhostBuffers call
p = find1('MultiblockRenderer.java')
t = load(p)
t = t.replace('return new GhostBuffers(fallback, (Map<RenderType, BufferBuilder>)remapped);',
              'return new GhostBuffers(new com.mojang.blaze3d.vertex.ByteBufferBuilder(256), (java.util.SequencedMap<RenderType, com.mojang.blaze3d.vertex.ByteBufferBuilder>)(java.util.SequencedMap)remapped);')
save(p, t)
print('6 ok')
print('FIM')
