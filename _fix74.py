# _fix74.py
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

# 1 PlayerProgression
p = find1('PlayerProgression.java')
t = load(p)
t = t.replace('public void pushCodexBreadcrumb(CodexBreadcrumb.breadcrumbType type, String key', 'public void pushCodexBreadcrumb(CodexBreadcrumb.Type type, String key')
save(p, t)
print('1 ok')

# 2 ModSpawnEggItem: remove ctor duplicado
p = find1('ModSpawnEggItem.java')
t = load(p)
dup = '''    public ModSpawnEggItem(java.util.function.Supplier<? extends EntityType<?>> entityTypeSupplier, int color_a, int color_b, Item.Properties properties) {
        super(null, color_a, color_b, properties);
        this.entityTypeSupplier = Lazy.of(entityTypeSupplier);
'''
if t.count('public ModSpawnEggItem(java.util.function.Supplier') > 1:
    # remove a SEGUNDA ocorrencia inteira (corpo ate o proximo fechamento do ctor)
    first = t.find('public ModSpawnEggItem(java.util.function.Supplier')
    second = t.find('public ModSpawnEggItem(java.util.function.Supplier', first + 10)
    end = t.find('\n    }\n', second)
    if end != -1:
        t = t[:second] + t[end + len('\n    }\n'):]
        save(p, t)
print('2 ok')

# 3 MAParticleType ctor de 9 args
p = find1('MAParticleType.java')
t = load(p)
if 'public MAParticleType(int color, float scale' not in t and 'MAParticleType(java.awt.Color' not in t:
    idx = t.rfind('    public MAParticleType(ParticleType<MAParticleType> type) {')
    if idx == -1:
        idx = t.rstrip().rfind('}')
    ctor = '''    public MAParticleType(java.awt.Color color, float scale, int life, int lifePadding, float gravity, float physics, ItemStack stack, org.joml.Vector3f mover, boolean depthDisable) {
        this.color = color;
        this.scale = scale;
        this.life = life;
        this.lifePadding = lifePadding;
        this.gravity = gravity;
        this.physics = physics;
        this.stack = stack;
        this.mover = mover;
        this.depthDisable = depthDisable;
    }

'''
    t = t[:idx] + ctor + t[idx:]
    save(p, t)
print('3 ok')

# 4 ConstructAITask loot table
p = find1('ConstructAITask.java')
t = load(p)
t = t.replace('reloadableRegistries().getLootTable(lootTable)', 'reloadableRegistries().getLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, lootTable))')
save(p, t)
print('4 ok')

# 5 ConstructTask
p = find1('ConstructTask.java')
t = load(p)
t = t.replace('Class<ConstructAITask<?>> aiClass = this.getAIClass();', 'Class<? extends ConstructAITask<?>> aiClass = this.getAIClass();')
save(p, t)
print('5 ok')

# 6 FillerBlock useItemOn + conversao
p = find1('FillerBlock.java')
t = load(p)
t = t.replace('return world.getBlockState(offsetPos).getBlock().use(world.getBlockState(offsetPos), world, offsetPos, player, hand, brtr);',
'''net.minecraft.world.ItemInteractionResult __ir = world.getBlockState(offsetPos).getBlock().useItemOn(player.getItemInHand(hand), world.getBlockState(offsetPos), world, offsetPos, player, hand, brtr);
            return __ir.consumesAction() ? InteractionResult.sidedSuccess(world.isClientSide) : InteractionResult.PASS;''')
save(p, t)
print('6 ok')

# 7 CoffinBlock super.use -> useItemOn
p = find1('CoffinBlock.java')
t = load(p)
t = t.replace('return super.use(state, worldIn, pos, player, handIn, hit);',
'''net.minecraft.world.ItemInteractionResult __ir = super.useItemOn(player.getItemInHand(handIn), state, worldIn, pos, player, handIn, hit);
            return __ir.consumesAction() ? InteractionResult.sidedSuccess(worldIn.isClientSide) : InteractionResult.FAIL;''')
save(p, t)
print('7 ok')

# 8 ConstructActivate / ComponentActivate useItemOn
p = find1('ConstructActivate.java')
t = load(p)
t = t.replace('state.getBlock().use(state, c.level(), this.interactPos, player, InteractionHand.MAIN_HAND, brtr);',
              'state.getBlock().useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), state, c.level(), this.interactPos, player, InteractionHand.MAIN_HAND, brtr);')
save(p, t)
p = find1('ComponentActivate.java')
t = load(p)
t = t.replace('state.getBlock().use(state, c.level(), c.blockPosition(), player, InteractionHand.MAIN_HAND, brtr);',
              'state.getBlock().useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), state, c.level(), c.blockPosition(), player, InteractionHand.MAIN_HAND, brtr);')
save(p, t)
print('8 ok')

# 9 PylonTileBase lambda arity
p = find1('PylonTileBase.java')
t = load(p)
t = t.replace('return ClientboundBlockEntityDataPacket.create((BlockEntity)this, be -> {', 'return ClientboundBlockEntityDataPacket.create((BlockEntity)this, () -> {')
save(p, t)
print('9 ok')

# 10 WizardLabBlock openMenu
p = find1('WizardLabBlock.java')
t = load(p)
t = t.replace('player.openMenu(provider, dataWriter);', 'player.openMenu(provider, (java.util.function.Consumer)dataWriter);')
save(p, t)
print('10 ok')

# 11 MAEnchantmentBridge
p = find1('MAEnchantmentBridge.java')
t = load(p)
t = t.replace('return net.minecraft.core.registries.BuiltInRegistries.ITEM.getOrThrow(tag);',
              'return net.minecraft.core.HolderSet.emptyNamed(net.minecraft.core.registries.BuiltInRegistries.ITEM, tag);')
save(p, t)
print('11 ok')

# 12 MNAEnchantmentHelper
p = 'src/main/java/com/mna/api/enchantments/MNAEnchantmentHelper.java'
t = load(p)
t = t.replace('return (int)EnchantmentHelper.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY, entity);',
              'return (int)EnchantmentHelper.getEnchantmentLevel(entity.level().registryAccess().holderOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY), entity);')
save(p, t)
print('12 ok')

# 13 ConstructCommandTileEntityInteraction
p = find1('ConstructCommandTileEntityInteraction.java')
if p:
    t = load(p)
    t = t.replace('this._cached_te_ref = te;', 'this._cached_te_ref = (T)te;')
    save(p, t)
print('13 ok')

# 14 ConstructEntityAreaTask
p = find1('ConstructEntityAreaTask.java')
t = load(p)
t = t.replace('E selected = this.selectTarget(entities);', 'E selected = (E)this.selectTarget(entities);')
save(p, t)
print('14 ok')

# 15 ConstructTaskAreaParameter
p = find1('ConstructTaskAreaParameter.java')
t = load(p)
t = t.replace('this.area = new AABB(((DirectionalPoint)this.points.getFirst()).getPosition(), ((DirectionalPoint)this.points.getSecond()).getPosition()).expandTowards(1.0, 1.0, 1.0);',
              'this.area = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(((DirectionalPoint)this.points.getFirst()).getPosition()), net.minecraft.world.phys.Vec3.atLowerCornerOf(((DirectionalPoint)this.points.getSecond()).getPosition())).expandTowards(1.0, 1.0, 1.0);')
save(p, t)
print('15 ok')

# 16 DefaultFluidEventHandlers
p = find1('DefaultFluidEventHandlers.java')
t = load(p)
t = t.replace('event.getTarget().curePotionEffects(new ItemStack((ItemLike)Items.MILK_BUCKET));', '// [PORT 1.21.1] curePotionEffects removido')
save(p, t)
print('16 ok')

# 17 Construct createMobAttributes wrapAsHolder
p = find1('Construct.java')
t = load(p)
t = re.sub(r'Mob\.createMobAttributes\(\)((?:\.add\(Attributes\.\w+, [0-9.]+\))+)',
           lambda m: 'Mob.createMobAttributes()' + re.sub(r'\.add\(Attributes\.(\w+), ', r'.add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(Attributes.\1), ', m.group(1)), t)
save(p, t)
print('17 ok')

# 18 FactionRaidRegistry
p = find1('FactionRaidRegistry.java')
t = load(p)
t = t.replace('anyMatch(i -> i <= raidStrength)', 'anyMatch(i -> (Integer)i <= raidStrength)')
save(p, t)
print('18 ok')

# 19 ManaAndArtificeMod
p = find1('ManaAndArtificeMod.java')
t = load(p)
t = t.replace('return com.mna.Registries.IFaction;', 'return com.mna.Registries.Factions;')
save(p, t)
print('19 ok')

# 20 TimedDelayedEvent
p = find1('TimedDelayedEvent.java')
t = load(p)
t = t.replace('this.callback.accept(this.identifier, (String)this.data);', 'this.callback.accept(this.identifier, this.data);')
save(p, t)
print('20 ok')

# 21 MATags
p = find1('MATags.java')
t = load(p)
t = t.replace('return ((HolderSet.Named)tag.get()).stream().collect(Collectors.toList());',
              'return ((net.minecraft.core.HolderSet.Named<net.minecraft.world.level.levelgen.structure.Structure>)tag.get()).stream().collect(Collectors.toList());')
save(p, t)
print('21 ok')

# 22 APIBridge container
p = find1('APIBridge.java')
t = load(p)
t = t.replace('public class APIBridge {', 'public class APIBridge {\n    private static net.neoforged.fml.ModContainer modContainer;\n')
t = t.replace('ManaAndArtificeMod.setAPIHelper(new ConfigHelper());', 'ManaAndArtificeMod.setAPIHelper(new ConfigHelper());\n        modContainer = container;')
t = t.replace('SpellConfig.finalizeServerConfig(container);', 'SpellConfig.finalizeServerConfig(modContainer);')
save(p, t)
print('22 ok')

# 23 Shape/Modifier/AttributeValuePair copyOf
for fn in ['Shape.java', 'Modifier.java', 'AttributeValuePair.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = re.sub(r'ImmutableList\.copyOf\(\(Object\[\]\)(\w+)\)', r'ImmutableList.copyOf(\1)', t)
    save(p, t)
print('23 ok')

# 24 SpellEffect double cast
p = find1('SpellEffect.java')
t = load(p)
t = t.replace('for (Direction direction : (java.util.List<Direction>)dirs) {', 'for (Direction direction : (java.util.List<Direction>)(java.util.List)dirs) {')
save(p, t)
print('24 ok')

# 25 IEldrinConsumerTile
p = find1('IEldrinConsumerTile.java')
t = load(p)
t = t.replace('for (BlockPos pos : suppliers) {', 'for (BlockPos pos : (java.util.Set<BlockPos>)suppliers) {')
save(p, t)
print('25 ok')

# 26 IceSpikeBlock / ImpaleBlock getCollisionShape
for fn in ['IceSpikeBlock.java', 'ImpaleBlock.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'blockstate\.getBlock\(\)\.getCollisionShape\(blockstate, ', 'blockstate.getCollisionShape(', t)
    save(p, t)
print('26 ok')

# 27 BrazierBlock
p = find1('BrazierBlock.java')
t = load(p)
t = t.replace('color = spell.getParticleColorOverride();', 'color = ((BrazierTile)be).getColor();')
save(p, t)
print('27 ok')

# 28 HellfireBlock GRASS
p = find1('HellfireBlock.java')
t = load(p)
t = t.replace('fireblock.setFireInfo(Blocks.GRASS, 60, 100);', 'fireblock.setFireInfo(Blocks.SHORT_GRASS, 60, 100);')
save(p, t)
print('28 ok')

# 29 FluidJugTile
p = find1('FluidJugTile.java')
t = load(p)
t = t.replace('this.saveAdditional(base);', 'this.saveAdditional(base, registries);')
save(p, t)
print('29 ok')

# 30 ManaweaveCacheTile MobEffectInstance 6 args
p = find1('ManaweaveCacheTile.java')
t = load(p)
t = t.replace('new MobEffectInstance(effect, this.duration, this.amplifier, false, false, true)', 'new MobEffectInstance(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), this.duration, this.amplifier, false, false, true)')
save(p, t)
print('30 ok')

# 31 BookshelfTile super.onDataPacket
p = find1('BookshelfTile.java')
t = load(p)
t = rl(t, '    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {\n        super.onDataPacket(net, pkt);\n', '    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {\n')
save(p, t)
print('31 ok')

# 32 ChalkRuneBlock List<Ritual>
p = find1('ChalkRuneBlock.java')
t = load(p)
t = t.replace('        List rituals;\n', '        List<Ritual> rituals;\n')
t = t.replace('        List rituals;\r\n', '        List<Ritual> rituals;\r\n')
save(p, t)
print('32 ok')

# 33 ManaweavingRecipe
p = find1('ManaweavingRecipe.java')
t = load(p)
t = t.replace('if (!(inv instanceof ItemAndPatternCraftingInventory)) {', 'if (!((Object)inv instanceof ItemAndPatternCraftingInventory)) {')
t = t.replace('ItemAndPatternCraftingInventory mwInv = (ItemAndPatternCraftingInventory)inv;', 'ItemAndPatternCraftingInventory mwInv = (ItemAndPatternCraftingInventory)(Object)inv;')
save(p, t)
print('33 ok')

# 34 ConstructWorkbenchTile keySet
p = find1('ConstructWorkbenchTile.java')
t = load(p)
t = t.replace('for (Affinity aff : affinities.keySet()) {', 'for (Affinity aff : (java.util.Set<Affinity>)(java.util.Set)affinities.keySet()) {')
save(p, t)
print('34 ok')

# 35 GuiInit dist
p = find1('GuiInit.java')
t = load(p)
t = t.replace('@SubscribeEvent(dist = Dist.CLIENT)', '@SubscribeEvent')
save(p, t)
print('35 ok')

# 36 InventoryUtilities import
p = find1('InventoryUtilities.java')
t = load(p)
t = re.sub(r'import net\.neoforged\.neoforge\.capabilities\.ForgeCapabilities;\r?\n', '', t)
save(p, t)
print('36 ok')

# 37 PilgrimsPathStair
p = find1('PilgrimsPathStair.java')
t = load(p)
t = t.replace('super(state, BlockBehaviour.Properties.of()', 'super(state.get(), BlockBehaviour.Properties.of()')
save(p, t)
print('37 ok')

# 38 BlockInit POTTED_AUM
p = find1('BlockInit.java')
t = load(p)
t = t.replace('() -> (FlowerPotBlock)BuiltInRegistries.BLOCK.get(Blocks.FLOWER_POT).get()', '() -> (FlowerPotBlock)Blocks.FLOWER_POT')
save(p, t)
print('38 ok')
print('FIM')
