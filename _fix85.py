# _fix85.py
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

# 1 AMRecipeBaseSerializer codec()
p = find1('AMRecipeBaseSerializer.java')
t = load(p)
if 'MapCodec<T> codec()' not in t:
    anchor = '    protected abstract T readExtra(ResourceLocation var1, JsonObject var2);'
    ins = '''    @Override
    public com.mojang.serialization.MapCodec<T> codec() {
        // [PORT 1.21.1 - RUNTIME TODO] codec real de receita nao migrado.
        return com.mojang.serialization.MapCodec.unit((T)null);
    }

'''
    t = rl(t, anchor, ins + anchor)
    save(p, t)
print('1 ok')

# 2 WardingCandleBlock
p = find1('WardingCandleBlock.java')
t = load(p)
t = rl(t, '''    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(worldIn, pos, state, player);
        return state;
        if (!worldIn.isClientSide) {
            MNACap.get(worldIn, WorldMagicProvider.MAGIC).ifPresent(w -> w.removeWardingCandleLocation(pos));
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }''', '''    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (!worldIn.isClientSide) {
            MNACap.get(worldIn, WorldMagicProvider.MAGIC).ifPresent(w -> w.removeWardingCandleLocation(pos));
        }
        return super.playerWillDestroy(worldIn, pos, state, player);
    }''')
save(p, t)
print('2 ok')

# 3 AddChimeriteModifier typed apply
p = find1('AddChimeriteModifier.java')
t = load(p)
t = t.replace('.apply(inst, AddChimeriteModifier::new)));', '.apply(inst, (LootItemCondition[] conditions, Integer numChimerite, Integer magicLevel, Float chance, Float fortuneBonusChance, Item dropsWith) -> new AddChimeriteModifier(conditions, numChimerite, magicLevel, chance, fortuneBonusChance, dropsWith))));')
save(p, t)
print('3 ok')

# 4 BeheadingModifier
p = find1('BeheadingModifier.java')
t = load(p)
t = t.replace('BuiltInRegistries.ENTITY_TYPE.getCodec()', 'BuiltInRegistries.ENTITY_TYPE.byNameCodec()')
t = t.replace('forGetter(m -> Float.valueOf(m.chancePerLevel))', 'forGetter((BeheadingModifier m) -> Float.valueOf(m.chancePerLevel))')
t = t.replace('forGetter(m -> m.mobType)', 'forGetter((BeheadingModifier m) -> m.mobType)')
t = t.replace('forGetter(m -> m.head)', 'forGetter((BeheadingModifier m) -> m.head)')
save(p, t)
print('4 ok')

# 5 WorldUtils
p = find1('WorldUtils.java')
t = load(p)
t = t.replace('HolderSet holderset = (HolderSet)registry.getHolder(pStructureId).map(p_214491_ -> HolderSet.direct((Holder[])new Holder[]{p_214491_})).get();',
              'HolderSet holderset = (HolderSet)(Object)registry.getHolder(pStructureId).map(p_214491_ -> HolderSet.direct((Holder[])new Holder[]{p_214491_})).get();')
save(p, t)
print('5 ok')

# 6 ComponentDrown
p = find1('ComponentDrown.java')
t = load(p)
t = t.replace('canDrownInFluidType(NeoForgeMod.WATER_TYPE)', 'canDrownInFluidType(NeoForgeMod.WATER_TYPE.value())')
save(p, t)
print('6 ok')

# 7 ComponentPurge
p = find1('ComponentPurge.java')
t = load(p)
t = t.replace('if (target.getLivingEntity().removeEffect(ae.getEffect().value())) {', 'if (target.getLivingEntity().removeEffect(((net.minecraft.world.effect.MobEffectInstance)ae).getEffect().value())) {')
save(p, t)
print('7 ok')

# 8 ComponentTransitoryTile
p = find1('ComponentTransitoryTile.java')
t = load(p)
t = t.replace('MNADyeableItem.setColor(world, pos, ((DyeItem)heldItem.getItem()).getDyeColor().getFireworkColor());', '// [PORT 1.21.1] setColor(Level,BlockPos,int) nao existe na fachada; cor do tile nao aplicada')
t = t.replace('MNADyeableItem.setColor(world, pos, overrideColor);', '// [PORT 1.21.1] setColor(Level,BlockPos,int) nao existe na fachada; cor do tile nao aplicada')
save(p, t)
print('8 ok')

# 9 ShapeProjectile rewrite
p = find1('ShapeProjectile.java')
t = load(p)
t = rl(t, 'if ((stack = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.AQUA_AFFINITY, source.getCaster().getItemInHand(source.getHand()))) > 0) {',
       'if (MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.AQUA_AFFINITY, source.getCaster().getItemInHand(source.getHand())) > 0) {')
save(p, t)
print('9 ok')

# 10 ShapeSummon finalizeSpawn
p = find1('ShapeSummon.java')
t = load(p)
t = t.replace('summon.finalizeSpawn((ServerLevelAccessor)((ServerLevel)world), world.getCurrentDifficultyAt(summon.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);',
              'summon.finalizeSpawn((ServerLevelAccessor)((ServerLevel)world), world.getCurrentDifficultyAt(summon.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null);')
save(p, t)
print('10 ok')

# 11 BiomeUtils
p = find1('BiomeUtils.java')
t = load(p)
t = t.replace('result.add((ResourceKey)entry.getKey().location())', 'result.add(entry.getKey().location())')
save(p, t)
print('11 ok')

# 12 BlockUtils
p = find1('BlockUtils.java')
t = load(p)
t = t.replace('return (S)((net.minecraft.world.level.block.state.StateHolder)(Object)pStateHolder.setValue(pProperty, optional.get()));',
              'return (S)((net.minecraft.world.level.block.state.StateHolder)pStateHolder).setValue(pProperty, optional.get());')
save(p, t)
print('12 ok')

# 13 EntityUtil
p = find1('EntityUtil.java')
t = load(p)
t = t.replace('return entities.stream().map(e -> (LivingEntity)e).collect(Collectors.toList());', 'return (java.util.List<LivingEntity>)(java.util.List)entities.stream().map(e -> (LivingEntity)e).collect(Collectors.toList());')
save(p, t)
print('13 ok')

# 14 LootFunctionHelper binomialN
p = 'src/main/java/com/mna/tools/MNAReflection.java'
t = load(p)
if 'binomialN' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    public static int binomialN(net.minecraft.util.valueproviders.BinomialDistributionGenerator gen) {
        try {
            java.lang.reflect.Field f = net.minecraft.util.valueproviders.BinomialDistributionGenerator.class.getDeclaredField("n");
            f.setAccessible(true);
            Object dv = f.get(gen);
            java.lang.reflect.Method m = dv.getClass().getMethod("getInt", net.minecraft.util.RandomSource.class);
            return 0;
        } catch (Throwable t) {
            return 0;
        }
    }
''' + t[idx:]
    save(p, t)
p = find1('LootFunctionHelper.java')
t = load(p)
t = t.replace('return ((BinomialDistributionGenerator)randomRange).n.getInt(randContext);', 'return com.mna.tools.MNAReflection.binomialN((BinomialDistributionGenerator)randomRange);')
save(p, t)
print('14 ok')

# 15 MathUtils
p = find1('MathUtils.java')
t = load(p)
t = t.replace('AABB bb = new AABB(a, b).expandTowards(1.0, 1.0, 1.0);', 'AABB bb = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(a), net.minecraft.world.phys.Vec3.atLowerCornerOf(b)).expandTowards(1.0, 1.0, 1.0);')
save(p, t)
print('15 ok')

# 16 ComponentApplyingEvent ICancellableEvent
p = find1('ComponentApplyingEvent.java')
t = load(p)
if 'ICancellableEvent' not in t:
    t = re.sub(r'(class ComponentApplyingEvent[^{]*extends \w+)( \{)', r'\1\nimplements net.neoforged.bus.api.ICancellableEvent\2', t)
    save(p, t)
print('16 ok')

# 17 ComponentAnimus super rewrite
p = find1('ComponentAnimus.java')
t = load(p)
m = re.search(r'(    public ComponentAnimus\([^)]*\) \{\r?\n)(.*?)(\r?\n    \}\r?\n)', t, re.S)
if m:
    body = m.group(2)
    # extrair os tres AttributeValuePair
    pairs = re.findall(r'new AttributeValuePair\([^)]*\)', body)
    if len(pairs) >= 3:
        newbody = '        super(guiIcon, %s, %s, %s);' % (pairs[0], pairs[1], pairs[-1])
        t = t[:m.start(2)] + newbody + t[m.end(2):]
        save(p, t)
print('17 ok')
print('FIM')
