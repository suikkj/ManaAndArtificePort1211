# _fix86.py
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

# 1 AMRecipeBaseSerializer: remover um codec()
p = find1('AMRecipeBaseSerializer.java')
t = load(p)
blk = '''    @Override
    public com.mojang.serialization.MapCodec<T> codec() {
        // [PORT 1.21.1 - RUNTIME TODO] codec real de receita nao migrado.
        return com.mojang.serialization.MapCodec.unit((T)null);
    }

'''
if t.count('MapCodec<T> codec()') > 1:
    t = rl(t, blk, '')
    save(p, t)
print('1 ok', t.count('MapCodec<T> codec()'))

# 2 MNAReflection FQN
p = 'src/main/java/com/mna/tools/MNAReflection.java'
t = load(p)
t = t.replace('net.minecraft.util.valueproviders.BinomialDistributionGenerator', 'net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator')
save(p, t)
print('2 ok')

# 3 restore(3)
for fn in ['ItemCloudBottle.java', 'WakebloomItem.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('blocksnapshot.restore(true)', 'blocksnapshot.restore(3)')
    save(p, t)
print('3 ok')

# 4 AddChimerite/Beheading lambdas
p = find1('AddChimeriteModifier.java')
t = load(p)
t = t.replace('.apply(inst, (LootItemCondition[] conditions, Integer numChimerite, Integer magicLevel, Float chance, Float fortuneBonusChance, Item dropsWith) -> new AddChimeriteModifier(conditions, numChimerite, magicLevel, chance, fortuneBonusChance, dropsWith))));',
              '.apply(inst, (java.util.List<LootItemCondition> conditions, Integer numChimerite, Integer magicLevel, Float chance, Float fortuneBonusChance, Item dropsWith) -> new AddChimeriteModifier(conditions.toArray(new LootItemCondition[0]), numChimerite, magicLevel, chance, fortuneBonusChance, dropsWith))));')
save(p, t)
p = find1('BeheadingModifier.java')
t = load(p)
t = t.replace('.apply(inst, BeheadingModifier::new)));', '.apply(inst, (java.util.List<LootItemCondition> conditions, Float chancePerLevel, EntityType<?> mobType, Item head) -> new BeheadingModifier(conditions.toArray(new LootItemCondition[0]), chancePerLevel, mobType, head))));')
save(p, t)
print('4 ok')

# 5 RitualEffectGate witness
p = find1('RitualEffectGate.java')
t = load(p)
t = t.replace('MNACap.get(context.getCaster(), PlayerProgressionProvider.PROGRESSION)', 'MNACap.<IPlayerProgression>get(context.getCaster(), PlayerProgressionProvider.PROGRESSION)')
t = t.replace('MNACap.get(context.getCaster(), PlayerMagicProvider.MAGIC)', 'MNACap.<IPlayerMagic>get(context.getCaster(), PlayerMagicProvider.MAGIC)')
save(p, t)
print('5 ok')

# 6 WorldUtils rewrite
p = find1('WorldUtils.java')
t = load(p)
t = t.replace('HolderSet holderset = (HolderSet)(Object)registry.getHolder(pStructureId).map(p_214491_ -> HolderSet.direct((Holder[])new Holder[]{p_214491_})).get();',
'''java.util.Optional __opt = registry.getHolder(pStructureId);
            HolderSet holderset = __opt.isPresent() ? HolderSet.direct((Holder)__opt.get()) : HolderSet.empty();''')
save(p, t)
print('6 ok')

# 7 ComponentImpulse
p = find1('ComponentImpulse.java')
t = load(p)
t = t.replace('ProjectileUtil.getMobArrow((LivingEntity)source.getCaster(), (ItemStack)offhandItem, (float)1.0f)', 'ProjectileUtil.getMobArrow((LivingEntity)source.getCaster(), (ItemStack)offhandItem, (float)1.0f, (ItemStack)null)')
save(p, t)
print('7 ok')

# 8 ComponentPurge holder
p = find1('ComponentPurge.java')
t = load(p)
t = t.replace('if (target.getLivingEntity().removeEffect(((net.minecraft.world.effect.MobEffectInstance)ae).getEffect().value())) {', 'if (target.getLivingEntity().removeEffect(((net.minecraft.world.effect.MobEffectInstance)ae).getEffect())) {')
save(p, t)
print('8 ok')

# 9 ComponentTransmute
p = find1('ComponentTransmute.java')
t = load(p)
t = t.replace('getLootTable(recipe.getLootTable())', 'getLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, recipe.getLootTable()))')
save(p, t)
print('9 ok')

# 10 BiomeUtils / StructureUtils
p = find1('BiomeUtils.java')
t = load(p)
t = t.replace('registry.entrySet().forEach(entry -> result.add(entry.getKey().location()));',
              'registry.entrySet().forEach(entry -> result.add(((java.util.Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>, net.minecraft.world.level.biome.Biome>)entry).getKey().location()));')
save(p, t)
p = find1('StructureUtils.java')
t = load(p)
t = t.replace('registry.entrySet().forEach(entry -> result.add(((ResourceKey)entry.getKey()).location()));',
              'registry.entrySet().forEach(entry -> result.add(((java.util.Map.Entry<ResourceKey, Object>)entry).getKey().location()));')
save(p, t)
print('10 ok')

# 11 BlockUtils Comparable
p = find1('BlockUtils.java')
t = load(p)
t = t.replace('return (S)((net.minecraft.world.level.block.state.StateHolder)pStateHolder).setValue(pProperty, optional.get());',
              'return (S)((net.minecraft.world.level.block.state.StateHolder)pStateHolder).setValue(pProperty, (Comparable)optional.get());')
save(p, t)
print('11 ok')

# 12 NBTUtilities
p = find1('NBTUtilities.java')
t = load(p)
t = t.replace('new NbtAccounter(0x200000L)', 'NbtAccounter.create(0x200000L)')
save(p, t)
print('12 ok')

# 13 PossessionHelper
p = find1('PossessionHelper.java')
t = load(p)
t = t.replace('ent.setSharedFlag(7, false);', 'ent.setInvisible(false);')
save(p, t)
print('13 ok')

# 14 ClientProxy 248
p = find1('ClientProxy.java')
t = load(p)
t = t.replace('this.queryAdvancement = advancements.get(advancementID);', 'this.queryAdvancement = advancements.get(advancementID) != null ? advancements.get(advancementID).value() : null;')
save(p, t)
print('14 ok')

# 15 ServerProxy
p = find1('ServerProxy.java')
t = load(p)
old = 'if (player instanceof ServerPlayer && (advancement = (sp = (ServerPlayer)player).level().getServer().getAdvancements().getAdvancement(advancementID)) != null) {'
new = '''net.minecraft.advancements.AdvancementHolder __ah = null;
        if (player instanceof ServerPlayer && (__ah = (sp = (ServerPlayer)player).level().getServer().getAdvancements().get(advancementID)) != null && (advancement = __ah.value()) != null) {'''
t = rl(t, old, new)
t = t.replace('AdvancementProgress progress = advancements.getOrStartProgress(advancement);', 'AdvancementProgress progress = advancements.getOrStartProgress(__ah);')
save(p, t)
print('15 ok')

# 16 GuiRenderUtils
p = find1('GuiRenderUtils.java')
t = load(p)
t = t.replace('BufferBuilder buffer = tesselator.getBuilder();', 'BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);')
t = t.replace('buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);', '')
save(p, t)
print('16 ok')

# 17 MultiblockRenderer
p = find1('MultiblockRenderer.java')
t = load(p)
t = t.replace('for (Map.Entry e : layerBuffers.entrySet()) {', 'for (Map.Entry e : (java.util.Set<Map.Entry>)layerBuffers.entrySet()) {')
t = t.replace('''        protected GhostBuffers(BufferBuilder fallback, Map<RenderType, BufferBuilder> layerBuffers) {
            super(fallback, layerBuffers);
        }''', '''        protected GhostBuffers(com.mojang.blaze3d.vertex.ByteBufferBuilder fallback, java.util.SequencedMap<RenderType, com.mojang.blaze3d.vertex.ByteBufferBuilder> layerBuffers) {
            super(fallback, layerBuffers);
        }''')
save(p, t)
print('17 ok')

# 18 ShearHelper
p = find1('ShearHelper.java')
t = load(p)
t = t.replace('net.minecraft.world.ItemInteractionResult.PASS', 'net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION')
save(p, t)
print('18 ok')

# 19 SummonUtils
p = find1('SummonUtils.java')
t = load(p)
old = 'if (living instanceof Player && (prog = (IPlayerProgression)(targetPlayer = MNACap.get((Player)living, PlayerProgressionProvider.PROGRESSION)).orElse(null)) != null && prog.getAlliedFaction() != null && prog.getAlliedFaction().isAlliedTo(summonerFactionEntity.getFaction())) {'
new = 'if (living instanceof Player && (prog = (IPlayerProgression)(Object)MNACap.get((Player)living, PlayerProgressionProvider.PROGRESSION).orElse(null)) != null && prog.getAlliedFaction() != null && prog.getAlliedFaction().isAlliedTo(summonerFactionEntity.getFaction())) {'
t = t.replace(old, new)
save(p, t)
print('19 ok')
print('FIM')
