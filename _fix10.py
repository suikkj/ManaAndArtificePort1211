# _fix10.py - loot functions/conditions: Serializer (removido) -> MapCodec + TYPE
import os
import re

FAILS = []
ROOT = 'src/main/java/com/mna/loot/'

SIMPLE = ['RandomArtifactSpell', 'RandomConstructPart', 'RandomHat', 'RandomJokeSpell',
          'RandomJournalPage', 'RandomManaweaveRecipe', 'RandomRunescribeRecipe',
          'RandomThaumaturgicLink']


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


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


def add_imports(t, imps):
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    idx = None
    for i, l in enumerate(lines):
        if l.startswith('import '):
            idx = i
    for imp in imps:
        line = 'import ' + imp + ';'
        if line not in lines:
            lines.insert(idx + 1, line)
            idx += 1
    return sep.join(lines)


def delete_inner_serializer(t):
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    out = []
    i = 0
    removed = 0
    while i < len(lines):
        if lines[i].strip().startswith('public static class Serializer'):
            depth = 0
            started = False
            while i < len(lines):
                depth += lines[i].count('{') - lines[i].count('}')
                if '{' in lines[i]:
                    started = True
                if started and depth <= 0:
                    break
                i += 1
            i += 1
            if i < len(lines) and lines[i].strip() == '':
                i += 1
            removed += 1
            continue
        out.append(lines[i])
        i += 1
    return sep.join(out), removed


print('== Random* (simples) ==')
for name in SIMPLE:
    p = ROOT + name + '.java'
    t = load(p)
    o = t
    t = rep(t, 'protected ' + name + '(LootItemCondition[] conditionsIn) {',
            'protected ' + name + '(java.util.List<LootItemCondition> conditionsIn) {', name + ' ctor')
    t = rep(t, '''    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_CONTENTS;
    }''', '''    public LootItemFunctionType<''' + name + '''> getType() {
        return TYPE;
    }''', name + ' getType')
    t, rem = delete_inner_serializer(t)
    print('  ' + name + ': serializer removido=' + str(rem))
    statics = '''    public static final com.mojang.serialization.MapCodec<''' + name + '''> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> LootItemConditionalFunction.commonFields(inst).apply(inst, ''' + name + '''::new));
    public static final LootItemFunctionType<''' + name + '''> TYPE = new LootItemFunctionType<''' + name + '''>(CODEC);

'''
    t = rep(t, 'extends LootItemConditionalFunction {\n', 'extends LootItemConditionalFunction {\n' + statics, name + ' statics')
    t = add_imports(t, ['com.mojang.serialization.MapCodec', 'com.mojang.serialization.codecs.RecordCodecBuilder'])
    if t != o:
        save(p, t)

print('== RandomSilverSpell ==')
p = ROOT + 'RandomSilverSpell.java'
t = load(p)
t = rep(t, 'protected RandomSilverSpell(LootItemCondition[] conditionsIn, ResourceLocation factionID, boolean thesis) {',
        'protected RandomSilverSpell(java.util.List<LootItemCondition> conditionsIn, ResourceLocation factionID, boolean thesis) {', 'silver ctor')
t = rep(t, '''    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_CONTENTS;
    }''', '''    public LootItemFunctionType<RandomSilverSpell> getType() {
        return TYPE;
    }''', 'silver getType')
t, rem = delete_inner_serializer(t)
print('  serializer removido=' + str(rem))
statics = '''    public static final com.mojang.serialization.MapCodec<RandomSilverSpell> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
        LootItemConditionalFunction.commonFields(inst),
        ResourceLocation.CODEC.optionalFieldOf("faction").forGetter(f -> java.util.Optional.ofNullable(f.factionID))
    ).apply(inst, (conditions, faction) -> new RandomSilverSpell(conditions, faction.orElse(null), true)));
    public static final LootItemFunctionType<RandomSilverSpell> TYPE = new LootItemFunctionType<RandomSilverSpell>(CODEC);

'''
t = rep(t, 'extends LootItemConditionalFunction {\n', 'extends LootItemConditionalFunction {\n' + statics, 'silver statics')
t = add_imports(t, ['com.mojang.serialization.MapCodec', 'com.mojang.serialization.codecs.RecordCodecBuilder'])
save(p, t)

print('== RandomSpellStaff ==')
p = ROOT + 'RandomSpellStaff.java'
t = load(p)
t = rep(t, 'protected RandomSpellStaff(LootItemCondition[] conditionsIn, float chanceForForceSelf, float chanceForSkipTags, boolean allowMessWithMori) {',
        'protected RandomSpellStaff(java.util.List<LootItemCondition> conditionsIn, float chanceForForceSelf, float chanceForSkipTags, boolean allowMessWithMori) {', 'staff ctor')
t = rep(t, '''    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_CONTENTS;
    }''', '''    public LootItemFunctionType<RandomSpellStaff> getType() {
        return TYPE;
    }''', 'staff getType')
t, rem = delete_inner_serializer(t)
print('  serializer removido=' + str(rem))
statics = '''    public static final com.mojang.serialization.MapCodec<RandomSpellStaff> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
        LootItemConditionalFunction.commonFields(inst),
        com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chanceForForceSelf", 0.0f).forGetter(f -> f.chanceForForceSelf),
        com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chanceForSkipTags", 0.0f).forGetter(f -> f.chanceForSkipTags),
        com.mojang.serialization.Codec.BOOL.optionalFieldOf("allowMessWithMori", false).forGetter(f -> f.allowMessWithMori)
    ).apply(inst, (conditions, a, b, c) -> new RandomSpellStaff(conditions, a, b, c)));
    public static final LootItemFunctionType<RandomSpellStaff> TYPE = new LootItemFunctionType<RandomSpellStaff>(CODEC);

'''
t = rep(t, 'extends LootItemConditionalFunction {\n', 'extends LootItemConditionalFunction {\n' + statics, 'staff statics')
t = add_imports(t, ['com.mojang.serialization.MapCodec', 'com.mojang.serialization.codecs.RecordCodecBuilder'])
save(p, t)

print('== GlobalLootModifiers ==')
p = ROOT + 'GlobalLootModifiers.java'
t = load(p)
n = len(re.findall(r'new LootItemFunctionType\(\(Serializer\)new (Random\w+)\.Serializer\(\)\)', t))
t = re.sub(r'new LootItemFunctionType\(\(Serializer\)new (Random\w+)\.Serializer\(\)\)', r'\1.TYPE', t)
print('  substituicoes:', n)
save(p, t)

print('== PlayerFactionCheck ==')
p = ROOT + 'conditions/PlayerFactionCheck.java'
t = load(p)
t = rep(t, 'public static final LootItemConditionType PLAYER_FACTION = new LootItemConditionType((net.minecraft.world.level.storage.loot.Serializer)new Serializer());',
        '''public static final com.mojang.serialization.MapCodec<PlayerFactionCheck> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> inst.group(
        ResourceLocation.CODEC.optionalFieldOf("faction").forGetter(c -> java.util.Optional.ofNullable(c.factionID))
    ).apply(inst, faction -> new PlayerFactionCheck(faction.orElse(null))));
    public static final LootItemConditionType PLAYER_FACTION = new LootItemConditionType(CODEC);''', 'pfc type')
t, rem = delete_inner_serializer(t)
print('  serializer removido=' + str(rem))
t = add_imports(t, ['com.mojang.serialization.MapCodec', 'com.mojang.serialization.codecs.RecordCodecBuilder'])
save(p, t)

print('== remover import Serializer ==')
n = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        if 'import net.minecraft.world.level.storage.loot.Serializer;' in t:
            sep = '\r\n' if '\r\n' in t else '\n'
            lines = [l for l in t.split(sep) if l != 'import net.minecraft.world.level.storage.loot.Serializer;']
            save(p, sep.join(lines))
            n += 1
print('  arquivos:', n)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
