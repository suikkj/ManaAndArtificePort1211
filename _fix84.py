# _fix84.py
import os, re

def find1(name):
    for dp, _, fns in os.walk('src/main/java'):
        if name in fns:
            return os.path.join(dp, fns and name)
    return None

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

# 1 AMRecipeBaseSerializer: remover MEU codec duplicado
p = find1('AMRecipeBaseSerializer.java')
t = load(p)
blk = '''    @Override
    public com.mojang.serialization.MapCodec<T> codec() {
        // [PORT 1.21.1 - RUNTIME TODO] codec real de receita nao migrado; receitas destes tipos nao carregam de JSON.
        return com.mojang.serialization.MapCodec.unit((T)null);
    }

'''
t = rl(t, blk, '')
save(p, t)
print('1 ok')

# 2 RandomSpellStaff: simplificar (conditions vazias)
p = find1('RandomSpellStaff.java')
t = load(p)
t = re.sub(r'LootItemConditionalFunction\.commonFields\(inst\)\.and\(inst\.group\(', 'RecordCodecBuilder.mapCodec(inst2 -> inst2.group(', t)
t = t.replace('.apply(inst, (java.util.List<net.minecraft.world.level.storage.loot.predicates.LootItemCondition> conditions, Float a, Float b, Boolean c) -> new RandomSpellStaff(conditions, a, b, c))));',
              '.apply(inst2, (a, b, c) -> new RandomSpellStaff(java.util.List.of(), a, b, c)));')
save(p, t)
print('2 ok')

# 3 ClientPacketListenerMixin:53 revert
p = find1('ClientPacketListenerMixin.java')
t = load(p)
t = t.replace('(mobeffect = packet.effect().value()) != null', '(mobeffect = packet.getEffect().value()) != null')
save(p, t)
print('3 ok')

# 4 ContainerTools
p = find1('ContainerTools.java')
t = load(p)
t = t.replace('(NonNullList<ItemStack>)NonNullList.of(item, (Object[])new ItemStack[]{item})', '(NonNullList<ItemStack>)(NonNullList)NonNullList.of(item, (Object[])new ItemStack[]{item})')
save(p, t)
print('4 ok')

# 5 RitualEffectGate casts
p = find1('RitualEffectGate.java')
t = load(p)
t = t.replace('MNACap.get((IPlayerProgression)context.getCaster(), PlayerProgressionProvider.PROGRESSION)', 'MNACap.get(context.getCaster(), PlayerProgressionProvider.PROGRESSION)')
t = t.replace('MNACap.get((IPlayerMagic)context.getCaster(), PlayerMagicProvider.MAGIC)', 'MNACap.get(context.getCaster(), PlayerMagicProvider.MAGIC)')
save(p, t)
print('5 ok')

# 6 RitualEffectShadowSoul casts
p = find1('RitualEffectShadowSoul.java')
t = load(p)
t = t.replace('CrystalPhylactery.setEntityType(phylacteryStack, crystalType, context.getLevel());', 'CrystalPhylactery.setEntityType(phylacteryStack, (net.minecraft.world.entity.EntityType)crystalType, context.getLevel());')
t = t.replace('((IPhylacteryItem)phylacteryStack.getItem()).fill(phylacteryStack, crystalType,', '((IPhylacteryItem)phylacteryStack.getItem()).fill(phylacteryStack, (net.minecraft.world.entity.EntityType)crystalType,')
save(p, t)
print('6 ok')

# 7 ComponentActivate useItemOn
p = find1('ComponentActivate.java')
t = load(p)
t = t.replace('if (state.use((Level)context.getServerLevel(), source.getPlayer(), InteractionHand.MAIN_HAND, result) == InteractionResult.SUCCESS) {',
              'if (state.useItemOn(source.getPlayer().getItemInHand(InteractionHand.MAIN_HAND), (Level)context.getServerLevel(), source.getPlayer(), InteractionHand.MAIN_HAND, result).consumesAction()) {')
save(p, t)
print('7 ok')

# 8 ComponentAnimus super inline
p = find1('ComponentAnimus.java')
t = load(p)
old = '''            attributeValuePairArray = attributeValuePairArray3;
            attributeValuePairArray3[0] = new AttributeValuePair(Attribute.DURATION, 10.0f, 5.0f, 60.0f, 5.0f, 2.0f);
        }
        super(guiIcon, attributeValuePairArray);'''
new = '''            attributeValuePairArray = attributeValuePairArray3;
            attributeValuePairArray3[0] = new AttributeValuePair(Attribute.DURATION, 10.0f, 5.0f, 60.0f, 5.0f, 2.0f);
        }
        super(guiIcon, attributeValuePairArray != null ? attributeValuePairArray : new AttributeValuePair[0]);'''
t = rl(t, old, new)
save(p, t)
print('8 ok')

# 9 SpellBalanceCalculator id()
p = find1('SpellBalanceCalculator.java')
t = load(p)
t = t.replace('Registries.Shape.getValue(r.id())', 'Registries.Shape.getValue(r.getId())')
t = t.replace('Registries.SpellEffect.getValue(r.id())', 'Registries.SpellEffect.getValue(r.getId())')
save(p, t)
print('9 ok')

# 10 BeheadingModifier KILLER
p = find1('BeheadingModifier.java')
t = load(p)
t = t.replace('LootContextParams.KILLER_ENTITY', 'LootContextParams.ATTACKING_ENTITY')
save(p, t)
print('10 ok')

# 11 getCodec -> byNameCodec (GLM files)
for fn in ['AddChimeriteModifier.java', 'BeheadingModifier.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('BuiltInRegistries.ITEM.getCodec()', 'BuiltInRegistries.ITEM.byNameCodec()')
    save(p, t)
print('11 ok')

# 12 ComponentHeal startConverting
p = find1('ComponentHeal.java')
t = load(p)
t = re.sub(r'\(\(ZombieVillager\)target\.getEntity\(\)\)\.startConverting\(([^;]+)\);',
           r'com.mna.tools.MNAReflection.startConverting((net.minecraft.world.entity.monster.ZombieVillager)target.getEntity(), \1);', t)
save(p, t)
print('12 ok')

# 13 ComponentPurge removeEffect
p = find1('ComponentPurge.java')
t = load(p)
t = t.replace('target.getLivingEntity().removeEffect(ae.getEffect())', 'target.getLivingEntity().removeEffect(ae.getEffect().value())')
save(p, t)
print('13 ok')

# 14 ComponentDrown canDrownInFluidType
p = find1('ComponentDrown.java')
t = load(p)
t = t.replace('target.getLivingEntity().canDrownInFluidType(fluidtype)', 'target.getLivingEntity().canDrownInFluidType((net.neoforged.neoforge.fluids.FluidType)fluidtype)')
save(p, t)
print('14 ok')

# 15 finalizeSpawn 4-arg
for fn in ['ComponentSummonInsectSwarm.java', 'ComponentSummonSkeletalHorse.java', 'ShapeSummon.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'\.finalizeSpawn\((\(ServerLevelAccessor\)[^,]+|\(ServerLevelAccessor\)\(\(ServerLevel\)world\)[^,]*), ([^,]+), ([^,]+), ([^,]+), ([^)]+)\);', r'.finalizeSpawn(\1, \2, \3, \4);', t)
    save(p, t)
print('15 ok')

# 16 ComponentSummonSkeletalHorse equipSaddle
p = find1('ComponentSummonSkeletalHorse.java')
t = load(p)
t = t.replace('horse.equipSaddle(null);', '// [PORT 1.21.1] equipSaddle(ItemStack,SoundEvent) - sem args validos aqui')
save(p, t)
print('16 ok')

# 17 ComponentImpulse getMobArrow + save
p = find1('ComponentImpulse.java')
t = load(p)
t = t.replace('ProjectileUtil.getMobArrow((LivingEntity)source', 'ProjectileUtil.getMobArrow((LivingEntity)source')
t = t.replace('worroa.getPersistentData().put("return_stack", (Tag)returnItem.save(new CompoundTag()));', 'worroa.getPersistentData().put("return_stack", (Tag)com.mna.api.tools.MNAItemData.save(returnItem, new CompoundTag()));')
save(p, t)
print('17 ok')

# 18 ComponentTransmute loottable
p = find1('ComponentTransmute.java')
t = load(p)
t = t.replace('context.getServerLevel().getLootTable(', 'context.getServerLevel().getLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, ')
save(p, t)
print('18 ok')

# 19 EventBus.post splits (ComponentBlink/EldrinFlight/Recall/Transplace/SpellCaster)
for fn in ['ComponentBlink.java', 'ComponentEldrinFlight.java', 'ComponentRecall.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'if \(!NeoForge\.EVENT_BUS\.post\((\w+)\)\) \{', r'NeoForge.EVENT_BUS.post(\1);\n            if (\1.isCanceled()) {', t)
    save(p, t)
p = find1('ComponentTransplace.java')
t = load(p)
t = re.sub(r'if \(!NeoForge\.EVENT_BUS\.post\((\w+)\)\) \{', r'NeoForge.EVENT_BUS.post(\1);\n        if (\1.isCanceled()) {', t)
t = re.sub(r'if \(!NeoForge\.EVENT_BUS\.post\(\((\w+) = (new EntityTeleportEvent\([^;]+\))\)\) \{', r'\1 = \2;\n        NeoForge.EVENT_BUS.post(\1);\n        if (\1.isCanceled()) {', t)
save(p, t)
p = find1('SpellCaster.java')
t = load(p)
t = re.sub(r'if \(NeoForge\.EVENT_BUS\.post\((new ComponentApplyingEvent\([^;]+\))\)\) \{', r'ComponentApplyingEvent __cae = \1;\n            NeoForge.EVENT_BUS.post(__cae);\n            if (__cae.isCanceled()) {', t)
save(p, t)
print('19 ok')

# 20 ComponentBloodSacrifice getCategory
p = find1('ComponentBloodSacrifice.java')
t = load(p)
t = t.replace('.getEffect().getCategory()', '.getEffect().value().getCategory()')
save(p, t)
print('20 ok')

# 21 ShapeSummon SlotResult
p = find1('ShapeSummon.java')
t = load(p)
t = t.replace('for (SlotResult sr : results) {', 'for (SlotResult sr : (java.util.List<SlotResult>)results) {')
save(p, t)
print('21 ok')

# 22 BiomeUtils
p = find1('BiomeUtils.java')
t = load(p)
t = t.replace('result.add(((ResourceKey)entry.getKey())', 'result.add((ResourceKey)entry.getKey()')
save(p, t)
print('22 ok')

# 23 BlockUtils setValue
p = find1('BlockUtils.java')
t = load(p)
t = t.replace('return (S)((StateHolder)pStateHolder.setValue(pProperty, (Comparable)optional.get()));', 'return (S)((net.minecraft.world.level.block.state.StateHolder)(Object)pStateHolder.setValue(pProperty, optional.get()));')
save(p, t)
print('23 ok')

# 24 ForgeLoggerTweaker
p = find1('ForgeLoggerTweaker.java')
t = load(p)
t = t.replace('for (LoggerConfig logger : map.values()) {', 'for (LoggerConfig logger : (java.util.Collection<LoggerConfig>)map.values()) {')
save(p, t)
print('24 ok')

# 25 RitualEffectAlteration
p = find1('RitualEffectAlteration.java')
t = load(p)
t = t.replace('(NonNullList<ResourceLocation>)NonNullList.of(', '(NonNullList<ResourceLocation>)(NonNullList)NonNullList.of(')
save(p, t)
print('25 ok')
print('FIM')
