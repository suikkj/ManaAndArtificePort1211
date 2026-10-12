# _fix82.py
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

def addimport(t, imp):
    if imp not in t:
        t = re.sub(r'(import [^\n]*;\r?\n)', lambda m: m.group(1) + imp + '\n', t, count=1)
    return t

# 0 MNADyeableItem package
import subprocess
r = subprocess.run(['bash', '-c', "grep -rl 'interface MNADyeableItem' src/main/java | head -1"], capture_output=True, text=True)
mp = r.stdout.strip()
pkg = ''
if mp:
    first = open(mp, encoding='utf-8', errors='replace').read().split('\n')
    for l in first:
        if l.startswith('package'):
            pkg = l.split()[1].rstrip(';')
            break
print('0 ok', pkg)

# 1 GlobalLootModifiers MapCodec
p = find1('GlobalLootModifiers.java')
t = load(p)
t = t.replace('DeferredRegister<Codec<? extends IGlobalLootModifier>>', 'DeferredRegister<com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>>')
t = t.replace('DeferredHolder<Codec<? extends IGlobalLootModifier>, ? extends Codec<? extends IGlobalLootModifier>>', 'DeferredHolder<com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>, ? extends com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>>')
save(p, t)
print('1 ok')

# 2 AddChimeriteModifier / BeheadingModifier mapCodec
for fn in ['AddChimeriteModifier.java', 'BeheadingModifier.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('RecordCodecBuilder.create(inst ->', 'RecordCodecBuilder.mapCodec(inst ->')
    save(p, t)
print('2 ok')

# 3 ExtendedItemStackPacketBuffer
p = find1('ExtendedItemStackPacketBuffer.java')
t = load(p)
t = t.replace('new NbtAccounter(0x200000L)', 'NbtAccounter.create(0x200000L)')
t = t.replace('(ExtendedItemStackPacketBuffer)(t)', '(ExtendedItemStackPacketBuffer)(Object)t')
save(p, t)
print('3 ok')

# 4 ItemBookRenderer putBulkData
p = find1('ItemBookRenderer.java')
t = load(p)
t = t.replace('p_229112_2_.putBulkData(matrixstack$entry, bakedquad, f, f1, f2, p_229112_5_, p_229112_6_);', 'p_229112_2_.putBulkData(matrixstack$entry, bakedquad, f, f1, f2, 1.0f, p_229112_5_, p_229112_6_);')
save(p, t)
print('4 ok')

# 5 AuraSyncMessageToClient (stack field?)
p = find1('AuraSyncMessageToClient.java')
t = load(p)
r2 = subprocess.run(['bash', '-c', 'grep -c "CompoundTag\\|ItemStack" ' + p.replace('\\', '/')], capture_output=True, text=True)
t = t.replace('a.load(this.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())', 'a.load(new CompoundTag())')
save(p, t)
print('5 ok', r2.stdout.strip())

# 6 SetLiftPositionMessage
p = find1('SetLiftPositionMessage.java')
t = load(p)
t = t.replace('IPlayerMagic magicProperties = (IPlayerMagic)magicCap;', 'IPlayerMagic magicProperties = (IPlayerMagic)magicCap.get();')
save(p, t)
print('6 ok')

# 7 ManaweavePatternDrawnMessage
p = find1('ManaweavePatternDrawnMessage.java')
t = load(p)
t = t.replace('Recipe pattern = player.level().getRecipeManager().byKey(this.getPatternID()).orElse(null);', 'net.minecraft.world.item.crafting.RecipeHolder<?> pattern = player.level().getRecipeManager().byKey(this.getPatternID()).orElse(null);')
t = t.replace('if (!(pattern instanceof IManaweavePattern)) {', 'if (!(pattern.value() instanceof IManaweavePattern)) {')
t = t.replace('EventDispatcher.DispatchManaweavePatternDrawn((IManaweavePattern)pattern, (LivingEntity)player)', 'EventDispatcher.DispatchManaweavePatternDrawn((IManaweavePattern)pattern.value(), (LivingEntity)player)')
save(p, t)
print('7 ok')

# 8 StructureSyncMessage
p = find1('StructureSyncMessage.java')
t = load(p)
t = t.replace('if (r instanceof MultiblockDefinition) {', 'if (r.value() instanceof MultiblockDefinition) {')
t = t.replace('MultiblockDefinition mbd = (MultiblockDefinition)r;', 'MultiblockDefinition mbd = (MultiblockDefinition)r.value();')
save(p, t)
print('8 ok')

# 9 ItemInteractionsInit PASS
p = find1('ItemInteractionsInit.java')
t = load(p)
t = t.replace('return ItemInteractionResult.PASS;', 'return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;')
save(p, t)
print('9 ok')

# 10 ItemSpellBookRenderer
p = find1('ItemSpellBookRenderer.java')
t = load(p)
t = t.replace('(PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer((Entity)mc.player)', '(PlayerRenderer)(Object)mc.getEntityRenderDispatcher().getRenderer((Entity)mc.player)')
save(p, t)
print('10 ok')

# 11 RandomConstructPart
p = find1('RandomConstructPart.java')
t = load(p)
t = t.replace('filter(i -> i.isPresent() && i.get() instanceof ItemConstructPart', 'filter(i -> i.isBound() && i.get() instanceof ItemConstructPart')
save(p, t)
print('11 ok')

# 12 Construct lambda sintetica
p = find1('Construct.java')
t = load(p)
t = t.replace('List<Mob> nearbyHostiles = this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(16.0), arg_0 -> this.lambda$tickDefeated$0((LivingEntity)owner, arg_0));',
              'List<Mob> nearbyHostiles = this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(16.0), arg_0 -> !SummonUtils.isTargetFriendly((Entity)arg_0, (LivingEntity)owner) && this.getSensing().hasLineOfSight((Entity)arg_0));')
t = re.sub(r'    private\s+boolean lambda\$tickDefeated\$0\(LivingEntity owner, Mob e\) \{\r?\n[^}]*\}\r?\n', '', t)
save(p, t)
print('12 ok')

# 13 FeyArmorModel decls antigas
p = find1('FeyArmorModel.java')
t = load(p)
t = rl(t, '        IPlayerMagic magic;\n        Player player;\n        IPlayerProgression progression;\n', '')
save(p, t)
print('13 ok')

# 14 ItemManaweaverWand null level
p = find1('ItemManaweaverWand.java')
t = load(p)
t = t.replace('ManaweavingPattern stored = ItemManaweaverWand.getStoredPattern(worldIn, stack);', 'ManaweavingPattern stored = ItemManaweaverWand.getStoredPattern((net.minecraft.world.level.Level)null, stack);')
save(p, t)
print('14 ok')

# 15 BoundSword / BoundAxe defaultModifiers
for fn in ['BoundSword.java', 'BoundAxe.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('Multimap baseAttrs = this.defaultModifiers;', 'Multimap baseAttrs = com.google.common.collect.ImmutableMultimap.of();')
    save(p, t)
p = find1('BoundSword.java')
t = load(p)
t = t.replace('new AttributeModifier(BASE_RANGE_UUID, (double)range', 'new AttributeModifier(BASE_RANGE_ID, (double)range')
if 'BASE_RANGE_ID = ' not in t:
    idx = t.find('public BoundSword(')
    if idx == -1:
        idx = t.find('public class BoundSword')
        idx = t.find('{', idx) + 1
    t = t[:idx] + '\n    private static final net.minecraft.resources.ResourceLocation BASE_RANGE_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mna", "base_range");\n' + t[idx:]
save(p, t)
print('15 ok')

# 16 MnADebugStick
p = find1('MnADebugStick.java')
t = load(p)
t = t.replace('return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers();', 'return this.defaultModifiers;')
save(p, t)
print('16 ok')

# 17 FoxStaff
p = find1('FoxStaff.java')
t = load(p)
t = t.replace('.isPathfindable((BlockGetter)player.level(), summonPos, PathComputationType.LAND)', '.isPathfindable(PathComputationType.LAND)')
save(p, t)
print('17 ok')

# 18 RandomSpellStaff group
p = find1('RandomSpellStaff.java')
t = load(p)
old = 'LootItemConditionalFunction.commonFields(inst).and(\n'
if old in t or old.replace('\n', '\r\n') in t:
    t = t.replace('LootItemConditionalFunction.commonFields(inst).and(', 'LootItemConditionalFunction.commonFields(inst).and(inst.group(')
    t = t.replace('.apply(inst, (conditions, a, b, c) -> new RandomSpellStaff(conditions, a, b, c)));', '.apply(inst, (conditions, a, b, c) -> new RandomSpellStaff(conditions, a, b, c))));')
save(p, t)
print('18 ok')

# 19 ClientPacketListenerMixin
p = find1('ClientPacketListenerMixin.java')
t = load(p)
t = t.replace('packet.getEffect() == EffectInit.REDUCE.get()', 'packet.getEffect().value() == EffectInit.REDUCE.get()')
t = t.replace('(mobeffect = packet.getEffect()) != null', '(mobeffect = packet.getEffect().value()) != null')
save(p, t)
print('19 ok')

# 20 Mixins casts
for fn in ['ArmorStandMixin.java', 'FireproofMixin.java', 'LivingEntityElytra.java', 'LivingEntityOnClimbableMixin.java', 'LocalPlayerElytra.java', 'PlayerElytra.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = re.sub(r'\((\w+)\)this\b', r'(\1)(Object)this', t)
    save(p, t)
print('20 ok')

# 21 MagicSyncMessageToClient casts
p = find1('MagicSyncMessageToClient.java')
t = load(p)
t = re.sub(r'net\.minecraft\.world\.item\.ItemStack\.OPTIONAL_STREAM_CODEC\.encode\(buf,', 'net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf,', t)
save(p, t)
print('21 ok')

# 22 WanderingWizardInventoryMessage MerchantOffers
p = find1('WanderingWizardInventoryMessage.java')
t = load(p)
t = t.replace('wwContainer.setClientSideOffers(new MerchantOffers(this.offers.createTag()));',
'''MerchantOffers __mo = new MerchantOffers();
            __mo.addAll(this.offers);
            wwContainer.setClientSideOffers(__mo);''')
save(p, t)
print('22 ok')

# 23 CurioCastMessage post
p = find1('CurioCastMessage.java')
t = load(p)
t = t.replace('if (!NeoForge.EVENT_BUS.post(event) && iSpell.castFromCurioSlot(stack, (Player)player)) break;',
'''NeoForge.EVENT_BUS.post(event);
                if (!event.isCanceled() && iSpell.castFromCurioSlot(stack, (Player)player)) break;''')
save(p, t)
print('23 ok')

# 24 ItemClientInit import
p = find1('ItemClientInit.java')
t = load(p)
if pkg:
    t = addimport(t, 'import ' + pkg + '.MNADyeableItem;')
save(p, t)
print('24 ok')
print('FIM')
