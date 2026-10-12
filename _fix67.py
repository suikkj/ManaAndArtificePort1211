# _fix67.py
import os

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

# 1 VillageStructures import
p = find1('VillageStructures.java')
t = load(p)
if 'import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;' not in t:
    t = t.replace('import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;',
                  'import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;\nimport net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;')
    save(p, t)
print('1 ok')

# 2 ChargeableItem
p = find1('ChargeableItem.java')
t = load(p)
t = t.replace('for (ItemStack invStack : invItems) {', 'for (ItemStack invStack : (java.util.List<ItemStack>)invItems) {')
t = t.replace('return super.initCapabilities(stack, nbt);', 'return null;')
t = t.replace('((Player)living).getCapability(ManaAndArtificeMod.getMagicCapability()).ifPresent(m -> {',
              'com.mna.api.tools.MNACap.get((Player)living, com.mna.capabilities.CapabilitiesInit.PLAYER_MAGIC).ifPresent(m -> {')
save(p, t)
print('2 ok')

# 3 BrazierBlock
p = find1('BrazierBlock.java')
t = load(p)
t = t.replace('color = MNADyeableItem.getColor();', 'color = ((MNADyeableItem)be).getColor();')
t = t.replace('MNADyeableItem.setColor(spell.getParticleColorOverride());', '((MNADyeableItem)be).setColor(spell.getParticleColorOverride());')
save(p, t)
print('3 ok')

# 4 ChalkRuneBlock
p = find1('ChalkRuneBlock.java')
t = load(p)
t = t.replace('((Boolean)state.getValue((Property)ACTIVATED)).booleanValue()', 'state.getValue(ACTIVATED)')
t = t.replace('!(Boolean)state.getValue((Property)BlockStateProperties.WATERLOGGED)', '!state.getValue(BlockStateProperties.WATERLOGGED)')
t = t.replace('world.getEntities((net.minecraft.world.level.entity.EntityTypeTest<Ritual, ?>)EntityInit.RITUAL_ENTITY.get(), new AABB(pos).inflate(16.0), e -> true)',
              'world.getEntitiesOfClass(Ritual.class, new AABB(pos).inflate(16.0), e -> true)')
save(p, t)
print('4 ok')

# 5 ManaweaveCacheTile
p = find1('ManaweaveCacheTile.java')
t = load(p)
t = t.replace('return super.getRenderBoundingBox().inflate(5.0);', 'return new net.minecraft.world.phys.AABB(this.worldPosition).inflate(5.0);')
t = t.replace('public ResourceLocation getLootTable() {', 'public net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getLootTable() {')
t = t.replace('player.addEffect(new MobEffectInstance(effect, this.duration, this.amplifier, false, false));',
              'player.addEffect(new MobEffectInstance(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), this.duration, this.amplifier, false, false));')
save(p, t)
print('5 ok')

# 6 DisenchanterTile
p = find1('DisenchanterTile.java')
t = load(p)
old = '''            HashMap<net.minecraft.core.Holder<Enchantment>, Integer> moved = new HashMap<>();
            moved.put(e.getKey(), (Integer)e.getValue());
            EnchantmentHelper.setEnchantments(moved, (ItemStack)output);'''
new = '''            HashMap<net.minecraft.core.Holder<Enchantment>, Integer> moved = new HashMap<>();
            moved.put(e.getKey(), (Integer)e.getValue());
            net.minecraft.world.item.enchantment.ItemEnchantments.Mutable movedMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
            moved.forEach(movedMut::set);
            ((ItemStack)output).set(DataComponents.ENCHANTMENTS, movedMut.toImmutable());'''
t = rl(t, old, new)
old2 = '''            if (input.has(DataComponents.CUSTOM_DATA)) {
                ListTag bookChants = EnchantedBookItem.getEnchantments((ItemStack)input);
                bookChants.remove(indexToMove % bookChants.size());
                if (bookChants.size() == 0) {
                    this.setItem(0, new ItemStack((ItemLike)Items.BOOK));
                } else {
                    input.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().put("StoredEnchantments", (Tag)new ListTag());
                }
            }'''
new2 = '''            if (input.has(DataComponents.CUSTOM_DATA)) {
                net.minecraft.world.item.enchantment.ItemEnchantments bookChants = ((ItemStack)input).getOrDefault(DataComponents.STORED_ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                java.util.List<net.minecraft.core.Holder<Enchantment>> bookKeys = new java.util.ArrayList<>(bookChants.keySet());
                if (!bookKeys.isEmpty()) {
                    bookKeys.remove(indexToMove % bookKeys.size());
                    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable bookMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                    bookKeys.forEach(k -> bookMut.set(k, bookChants.getLevel(k)));
                    ((ItemStack)input).set(DataComponents.STORED_ENCHANTMENTS, bookMut.toImmutable());
                    if (bookKeys.isEmpty()) {
                        this.setItem(0, new ItemStack((ItemLike)Items.BOOK));
                    }
                }
            }'''
t = rl(t, old2, new2)
t = t.replace('EnchantmentHelper.setEnchantments((Map)enchantments, (ItemStack)input);',
'''net.minecraft.world.item.enchantment.ItemEnchantments.Mutable inMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        enchantments.forEach(inMut::set);
        ((ItemStack)input).set(DataComponents.ENCHANTMENTS, inMut.toImmutable());''')
save(p, t)
print('6 ok')

# 7 EldrinFumeTile
p = find1('EldrinFumeTile.java')
t = load(p)
t = t.replace('CommonHooks.getBurnTime((ItemStack)potentialFuel, null)', '((ItemStack)potentialFuel).getBurnTime(null)')
t = t.replace('CommonHooks.getBurnTime((ItemStack)stack, null)', '((ItemStack)stack).getBurnTime(null)')
save(p, t)
print('7 ok')

# 8 SpellRecipe
p = find1('SpellRecipe.java')
t = load(p)
t = t.replace('this.components.forEach((Consumer<ModifiedSpellPart<SpellEffect>>)((Consumer<ModifiedSpellPart>)c -> {',
              '((java.util.List<ModifiedSpellPart<SpellEffect>>)this.components).forEach(c -> {')
old3 = '''        ItemStack newSpell = new ItemStack((ItemLike)ItemInit.SPELL.get());
        this.writeToNBT(newSpell.getTag());
        return newSpell;'''
new3 = '''        ItemStack newSpell = new ItemStack((ItemLike)ItemInit.SPELL.get());
        CompoundTag spellTag = com.mna.api.tools.MNAItemData.tag(newSpell);
        this.writeToNBT(spellTag);
        newSpell.set(DataComponents.CUSTOM_DATA, CustomData.of(spellTag));
        return newSpell;'''
t = rl(t, old3, new3)
save(p, t)
print('8 ok')

# 9 ArcanaAltarRenderer
p = find1('ArcanaAltarRenderer.java')
t = load(p)
t = t.replace('WorldRenderUtil.', 'WorldRenderUtils.')
save(p, t)
print('9 ok')

# 10 SimpleGeoBlockRenderer
p = find1('SimpleGeoBlockRenderer.java')
t = load(p)
t = t.replace('public class SimpleGeoBlockRenderer<V extends BlockEntity>\nextends GeoBlockRenderer<V> {',
              'public class SimpleGeoBlockRenderer<V extends BlockEntity & software.bernie.geckolib.animatable.GeoAnimatable>\nextends GeoBlockRenderer<V> {')
t = t.replace('super.render(tile, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);',
              'super.render((V)tile, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);')
save(p, t)
print('10 ok')

# 11 PlayerMagic
p = find1('PlayerMagic.java')
t = load(p)
t = t.replace('if (player != null && NeoForge.EVENT_BUS.post(evt)) {', 'NeoForge.EVENT_BUS.post(evt);\n        if (player != null && evt.isCanceled()) {')
t = t.replace('ItemStack replacement = new ItemStack((ItemLike)ItemInit.SPELL.get(), 1, stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());',
              'ItemStack replacement = com.mna.api.tools.MNAItemData.setComponent(new ItemStack((ItemLike)ItemInit.SPELL.get()), DataComponents.CUSTOM_DATA, stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY));')
save(p, t)
print('11 ok')

# 12 CommandSerializerInit
p = find1('CommandSerializerInit.java')
t = load(p)
t = t.replace('? extends ArgumentTypeInfo<?, ?>>', 'ArgumentTypeInfo<?, ?>>')
save(p, t)
print('12 ok')

# 13 EffectDisjunction
p = find1('EffectDisjunction.java')
t = load(p)
t = t.replace('ListTag listtag = stack.getEnchantmentTags();', 'ListTag listtag = new ListTag();')
t = t.replace('EnchantmentHelper.setEnchantments(chants, (ItemStack)stack);',
'''net.minecraft.world.item.enchantment.ItemEnchantments.Mutable chMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        chants.forEach(chMut::set);
        ((ItemStack)stack).set(DataComponents.ENCHANTMENTS, chMut.toImmutable());''')
save(p, t)
print('13 ok')

# 14 ConstructButcher
p = find1('ConstructButcher.java')
t = load(p)
if 'import net.minecraft.world.entity.EntityType;' not in t:
    t = t.replace('import net.minecraft.world.entity.Animal;', 'import net.minecraft.world.entity.Animal;\nimport net.minecraft.world.entity.EntityType;', 1)
t = t.replace('Optional<List> target = locatedEntities.entrySet().stream()', 'Optional target = locatedEntities.entrySet().stream()')
t = t.replace('List potentials = target.get();', 'List<Animal> potentials = (List<Animal>)target.get();')
save(p, t)
print('14 ok')
print('FIM')
