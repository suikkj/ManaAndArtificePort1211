# _fix76.py
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

# 1 MADamageEnchantmentBase package
p = find1('MADamageEnchantmentBase.java')
t = load(p)
t = t.replace('extends com.mna.enchantments.framework.MAEnchantmentBase', 'extends com.mna.enchantments.base.MAEnchantmentBase')
save(p, t)
print('1 ok')

# 2 ModSpawnEggItem dup ctor
p = find1('ModSpawnEggItem.java')
t = load(p)
dup = '''    public ModSpawnEggItem(java.util.function.Supplier<? extends EntityType<?>> entityTypeSupplier, int color_a, int color_b, Item.Properties properties) {
        super(null, color_a, color_b, properties);
        this.entityTypeSupplier = Lazy.of(entityTypeSupplier);
        UNADDED_EGGS.add(this);
    }

'''
if 'Lazy.of(entityTypeSupplier);' in t and t.count('public ModSpawnEggItem(') > 1:
    t = rl(t, dup, '')
    save(p, t)
print('2 ok')

# 3 MAParticleType ctor correto
p = find1('MAParticleType.java')
t = load(p)
old = '''    public MAParticleType(java.awt.Color color, float scale, int life, int lifePadding, float gravity, float physics, ItemStack stack, org.joml.Vector3f mover, boolean depthDisable) {'''
new = '''    public MAParticleType(ParticleColor color, ParticleFloat scale, ParticleInt life, ParticleInt lifePadding, ParticleFloat gravity, ParticleBoolean physics, ParticleItemStack stack, IParticleMoveType mover, ParticleBoolean depthDisable) {'''
t = rl(t, old, new)
save(p, t)
print('3 ok')

# 4 AttributeValuePair
p = find1('AttributeValuePair.java')
t = load(p)
t = t.replace('Object[] values = new AttributeValuePair[source.size()];', 'AttributeValuePair[] values = new AttributeValuePair[source.size()];')
save(p, t)
print('4 ok')

# 5 Construct attrs
p = find1('Construct.java')
t = load(p)
t = t.replace('.add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.MAX_HEALTH), 20.0)', '.add(Attributes.MAX_HEALTH, 20.0)')
t = t.replace('.add((net.minecraft.world.entity.ai.attributes.Attribute)AttributeInit.PERCEPTION_DISTANCE.get(), 8.0)', '.add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeInit.PERCEPTION_DISTANCE.get()), 8.0)')
t = t.replace('.add((net.minecraft.world.entity.ai.attributes.Attribute)AttributeInit.INTELLIGENCE.get(), 8.0)', '.add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeInit.INTELLIGENCE.get()), 8.0)')
t = t.replace('.add((net.minecraft.world.entity.ai.attributes.Attribute)AttributeInit.RANGED_DAMAGE.get(), 0.0)', '.add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(AttributeInit.RANGED_DAMAGE.get()), 0.0)')
save(p, t)
print('5 ok')

# 6 ConstructTask ctor
p = find1('ConstructTask.java')
t = load(p)
t = t.replace('Constructor<ConstructAITask<?>> ctor = aiClass.getConstructor(IConstruct.class, ResourceLocation.class);', 'Constructor<? extends ConstructAITask<?>> ctor = aiClass.getConstructor(IConstruct.class, ResourceLocation.class);')
save(p, t)
print('6 ok')

# 7 PylonTileBase BiFunction
p = find1('PylonTileBase.java')
t = load(p)
t = t.replace('return ClientboundBlockEntityDataPacket.create((BlockEntity)this, () -> {', 'return ClientboundBlockEntityDataPacket.create((BlockEntity)this, (be, reg) -> {')
save(p, t)
print('7 ok')

# 8 MAEnchantmentBridge cast
p = find1('MAEnchantmentBridge.java')
t = load(p)
t = t.replace('return net.minecraft.core.HolderSet.emptyNamed(net.minecraft.core.registries.BuiltInRegistries.ITEM, tag);',
              'return net.minecraft.core.HolderSet.emptyNamed((net.minecraft.core.HolderOwner<Item>)(Object)net.minecraft.core.registries.BuiltInRegistries.ITEM, tag);')
save(p, t)
print('8 ok')

# 9 ConstructCommandTileEntityInteract
p = find1('ConstructCommandTileEntityInteract.java')
t = load(p)
t = t.replace('this._cached_te_ref = te;', 'this._cached_te_ref = (T)te;')
save(p, t)
print('9 ok')

# 10 playerWillDestroy -> BlockState
for fn in ['WardingCandleBlock.java', 'MATarmaRootBlock.java', 'ChimeriteCrystalBlock.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'public void playerWillDestroy\(Level (\w+), BlockPos (\w+), BlockState (\w+), Player (\w+)\) \{',
               r'public BlockState playerWillDestroy(Level \1, BlockPos \2, BlockState \3, Player \4) {\n        super.playerWillDestroy(\1, \2, \3, \4);\n        return \3;', t)
    save(p, t)
print('10 ok')

# 11 BlockClientInit
p = find1('BlockClientInit.java')
t = load(p)
t = t.replace('BlockInit.BLOCKS.getEntries().stream().map(RegistryObject::get)', 'BlockInit.BLOCKS.getEntries().stream().map(h -> h.get())')
save(p, t)
print('11 ok')

# 12 tiles saveAdditional/load/onDataPacket
for fn in ['TransitoryTile.java', 'LodestarTile.java', 'MagiciansWorkbenchTile.java', 'OffsetBlockTile.java', 'ScrollShelfTile.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'this\.saveAdditional\(tag\);', 'this.saveAdditional(tag, this.level.registryAccess());', t)
    t = re.sub(r'this\.load\(pkt\.getTag\(\)\);', 'this.loadAdditional(pkt.getTag(), this.level.registryAccess());', t)
    t = rl(t, '        super.onDataPacket(net, pkt);\n', '')
    save(p, t)
print('12 ok')

# 13 SlipstreamGeneratorTile AABB
p = find1('SlipstreamGeneratorTile.java')
t = load(p)
t = re.sub(r'new AABB\(this\.getBlockPos\(\), bbOffset\.offset\(this\.getLevitateDir\(\)\)\)',
           r'new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(this.getBlockPos()), net.minecraft.world.phys.Vec3.atLowerCornerOf(bbOffset.offset(this.getLevitateDir())))', t)
save(p, t)
print('13 ok')

# 14 ThesisDeskTile hurt
p = find1('ThesisDeskTile.java')
t = load(p)
t = re.sub(r'this\.getItem\(1\)\.hurt\(this\.getInkDamage\(\), this\.level\.random, \(ServerPlayer\)(\w+)\)',
           r'com.mna.api.tools.MNAItemData.hurt(this.getItem(1), this.getInkDamage(), this.level.random)', t)
save(p, t)
print('14 ok')

# 15 ResourceLocation.parse
for fn in ['ArcanaAltarTile.java', 'PlayerProgressionProvider.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'new ResourceLocation\(', 'ResourceLocation.parse(', t)
    save(p, t)
print('15 ok')

# 16 getBurnTime global
for fn in ['EldrinFumeTile.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'CommonHooks\.getBurnTime\(\(ItemStack\)([^,]+), null\)', r'((ItemStack)\1).getBurnTime(null)', t)
    save(p, t)
print('16 ok')

# 17 ManaPylonTile SpellRune import
p = find1('ManaPylonTile.java')
t = load(p)
import re as _re
m = _re.search(r'import ([a-zA-Z0-9_.]+)\.SpellRune;', t)
if not m:
    t = addimport(t, 'import com.mna.entities.rituals.SpellRune;')
save(p, t)
print('17 ok')

# 18 SpellRecipe 627
p = find1('SpellRecipe.java')
t = load(p)
old = 'this.components.forEach((Consumer<ModifiedSpellPart<SpellEffect>>)((Consumer<ModifiedSpellPart>)c -> c.getContainedAttributes().forEach(a -> c.setValue((Attribute)((a)), this.shape.getMaximumValue((Attribute)((a)))))));'
new = '((java.util.List<ModifiedSpellPart<SpellEffect>>)this.components).forEach(c -> c.getContainedAttributes().forEach(a -> c.setValue((Attribute)((a)), this.shape.getMaximumValue((Attribute)((a))))));'
t = rl(t, old, new)
save(p, t)
print('18 ok')

# 19 FixedBookModel renderToBuffer 5-arg
p = find1('FixedBookModel.java')
t = load(p)
old = '''    public void renderToBuffer(PoseStack p_102298_, VertexConsumer p_102299_, int p_102300_, int p_102301_, float p_102302_, float p_102303_, float p_102304_, float p_102305_) {
        this.render(p_102298_, p_102299_, p_102300_, p_102301_, p_102302_, p_102303_, p_102304_, p_102305_);'''
new = '''    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);'''
t = rl(t, old, new)
save(p, t)
print('19 ok')

# 20 DisenchanterRenderer
p = find1('DisenchanterRenderer.java')
t = load(p)
t = t.replace('WorldRenderUtil.', 'WorldRenderUtils.')
save(p, t)
print('20 ok')

# 21 CantripRegistry EffectCure
p = find1('CantripRegistry.java')
t = load(p)
t = t.replace('net.neoforged.neoforge.common.EffectCure.MILK', 'net.neoforged.neoforge.common.EffectCures.MILK')
save(p, t)
print('21 ok')

# 22 MACapability
p = find1('MACapabilityForgeEventHandlers.java')
t = load(p)
t = t.replace('event.setCanGrief(false);\n                }\n            });\n        }\n    }\n\n    @SubscribeEvent\n    public static void onMobGriefing',
              '// [PORT 1.21.1] MobSpawnEvent.SpawnPlacementCheck nao tem setResult/setCanGrief; bloqueio de spawn perdido\n                }\n            });\n        }\n    }\n\n    @SubscribeEvent\n    public static void onMobGriefing')
t = t.replace('event.setCanceled(true);\n                }\n            });\n        }\n    }\n\n    @SubscribeEvent\n    public static void onChunkLoad',
              'event.setCanGrief(false);\n                }\n            });\n        }\n    }\n\n    @SubscribeEvent\n    public static void onChunkLoad')
save(p, t)
print('22 ok')

# 23 PlayerMagicSerializer provider
p = find1('PlayerMagicSerializer.java')
t = load(p)
t = t.replace('private CompoundTag saveInventory(Container inventory) {', 'private CompoundTag saveInventory(Container inventory, HolderLookup.Provider provider) {')
t = t.replace('private void readInventory(Container inventory, CompoundTag nbt) {', 'private void readInventory(Container inventory, CompoundTag nbt, HolderLookup.Provider provider) {')
t = t.replace('this.saveInventory(instance.getRiftInventory())', 'this.saveInventory(instance.getRiftInventory(), provider)')
t = t.replace('this.saveInventory(instance.getGrimoireInventory())', 'this.saveInventory(instance.getGrimoireInventory(), provider)')
t = t.replace('this.saveInventory(instance.getRoteInventory())', 'this.saveInventory(instance.getRoteInventory(), provider)')
t = t.replace('this.readInventory(instance.getRiftInventory(), cnbt.getCompound("rift"))', 'this.readInventory(instance.getRiftInventory(), cnbt.getCompound("rift"), provider)')
t = t.replace('this.readInventory(instance.getGrimoireInventory(), cnbt.getCompound("grimoire"))', 'this.readInventory(instance.getGrimoireInventory(), cnbt.getCompound("grimoire"), provider)')
t = t.replace('this.readInventory(instance.getRoteInventory(), cnbt.getCompound("rote"))', 'this.readInventory(instance.getRoteInventory(), cnbt.getCompound("rote"), provider)')
save(p, t)
print('23 ok')

# 24 coldEnoughToSnow
p = find1('CastingResourceGuiRegistry.java')
t = load(p)
t = re.sub(r'(\w+)\.value\(\)\.coldEnoughToSnow\(mc\.player\.blockPosition\(\)\)', r'\1.value().getBaseTemperature() < 0.15f', t)
save(p, t)
print('24 ok')

# 25 EffectUndeadFrostWalker / EffectWrithingBrambles
for fn in ['EffectUndeadFrostWalker.java', 'EffectWrithingBrambles.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'(if \(living\.level\(\)\.isClientSide\(\)\) \{\r?\n\s*)return;', r'\1return true;', t)
    t = re.sub(r'(if \(!pLivingEntity\.getPersistentData\(\)\.contains\("brambles_old_x"\)\) \{)', r'\1', t)
    save(p, t)
print('25 ok')

# 26 EffectLivingBomb stray return
p = find1('EffectLivingBomb.java')
t = load(p)
old = '''((LivingEntity)e).addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(this), 60, amplifier - 1));

        return true;
    }
                    });'''
new = '''((LivingEntity)e).addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(this), 60, amplifier - 1));
                    });'''
t = rl(t, old, new)
save(p, t)
print('26 ok')

# 27 Aura post + AuraEvent.Tick ICancellableEvent
p = find1('Aura.java')
t = load(p)
t = re.sub(r'if \(NeoForge\.EVENT_BUS\.post\((new AuraEvent\.Tick\(this, \(LivingEntity\)source\))\)\) \{',
           r'AuraEvent.Tick __tickEvt = \1;\n        NeoForge.EVENT_BUS.post(__tickEvt);\n        if (__tickEvt.isCanceled()) {', t)
save(p, t)
p = find1('AuraEvent.java')
t = load(p)
if 'ICancellableEvent' not in t:
    t = re.sub(r'(public static class Tick\r?\n\s*extends Event)( \{)', r'\1\n    implements net.neoforged.bus.api.ICancellableEvent\2', t)
    save(p, t)
print('27 ok')
print('FIM')
