# _fix81.py
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

def remove_nth(t, block, n=2):
    for eol in ['\n', '\r\n']:
        b = block.replace('\n', eol)
        pos = -1
        for _ in range(n):
            pos = t.find(b, pos + 1)
            if pos == -1:
                break
        if pos != -1:
            return t[:pos] + t[pos + len(b):]
    return t

# 1 ConstructNodeEvaluator: remover copia com @Override
p = find1('ConstructNodeEvaluator.java')
t = load(p)
blk = '''    @Override
    public PathType getPathType(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z);
    }

'''
t = rl(t, blk, '')
save(p, t)
print('1 ok', t.count('getPathType(net.minecraft.world.level.pathfinder.PathfindingContext'))

# 2 GreaterAnimus dup
p = find1('GreaterAnimus.java')
t = load(p)
blk2 = '''    protected void shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy) {
        double d0 = target.getX() - shooter.getX();
        double d1 = target.getY(0.3333333333333333) - projectile.getY();
        double d2 = target.getZ() - shooter.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        projectile.shoot(d0, d1 + d3 * 0.2, d2, velocity, inaccuracy);
    }

'''
t = remove_nth(t, blk2, 2)
save(p, t)
print('2 ok', t.count('shootCrossbowProjectile(LivingEntity shooter'))

# 3 InfiniteFluidJug: @Override duplo + getBurnTime
p = find1('InfiniteFluidJug.java')
t = load(p)
t = rl(t, '    @Override\n    @Override\n', '    @Override\n')
t = t.replace('CommonHooks.getBurnTime((ItemStack)bucket, recipeType)', '((ItemStack)bucket).getBurnTime(recipeType)')
save(p, t)
print('3 ok')

# 4 TextSection
p = find1('TextSection.java')
t = load(p)
t = t.replace('Component.Serializer.fromJson((String)this.rawText)', 'Component.Serializer.fromJson((String)this.rawText, net.minecraft.core.RegistryAccess.EMPTY)')
save(p, t)
print('4 ok')

# 5 FeyArmorModel rewrite
p = find1('FeyArmorModel.java')
t = load(p)
old_start = t.find('if (livingEntity instanceof Player &&')
if old_start != -1:
    old_end = t.find('WINTER_ICE)) {', old_start)
    close = t.find('}', old_end + 12)
    old_block = t[old_start:t.find('}', t.find('return ResourceLocation.fromNamespaceAndPath("mna", "textures/item/armor/model/fey_armor_winter.png");', old_end)) + 1]
    new_block = '''if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            IPlayerProgression progression = MNACap.get(player, PlayerProgressionProvider.PROGRESSION).orElse(null);
            IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
            if (progression != null && progression.getAlliedFaction() == Factions.FEY && magic != null && magic.getCastingResource().getRegistryName().equals(CastingResourceIDs.WINTER_ICE)) {
                return ResourceLocation.fromNamespaceAndPath("mna", "textures/item/armor/model/fey_armor_winter.png");
            }
        }'''
    t = t.replace(old_block, new_block)
    save(p, t)
print('5 ok')

# 6 BoundShield ItemAbility
p = find1('BoundShield.java')
t = load(p)
t = t.replace('public boolean canPerformAction(ItemStack stack, ItemAbilities toolAction) {', 'public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility toolAction) {')
save(p, t)
print('6 ok')

# 7 ItemInit forward ref
p = find1('ItemInit.java')
t = load(p)
t = t.replace('if (is.getItem() == MANAWEAVE_BOTTLE.get()) {', 'if (is.getItem() == ItemInit.MANAWEAVE_BOTTLE.get()) {')
save(p, t)
print('7 ok')

# 8 RunicMalus import + conditional
p = find1('RunicMalus.java')
t = load(p)
if 'import net.minecraft.resources.ResourceLocation;' not in t:
    t = re.sub(r'(import [^\n]*;\r?\n)', r'\1import net.minecraft.resources.ResourceLocation;\n', t, count=1)
t = t.replace('return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : (com.google.common.collect.Multimap)super.getDefaultAttributeModifiers();', 'return this.defaultModifiers;')
save(p, t)
print('8 ok')

# 9 ConstructPart* getDefaultAttributeModifiers
for fn in ['ConstructPartAxeArmLeft.java', 'ConstructPartAxeArmRight.java', 'ConstructPartHammerArmLeft.java', 'ConstructPartHammerArmRight.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('return p_40990_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : (com.google.common.collect.Multimap)super.getDefaultAttributeModifiers();', 'return this.defaultModifiers;')
    t = t.replace('return p_40990_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers();', 'return this.defaultModifiers;')
    save(p, t)
for fn in ['ConstructPartBladeArmLeft.java', 'ConstructPartBladeArmRight.java', 'ConstructPartGrabberArmLeft.java', 'ConstructPartGrabberArmRight.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : (com.google.common.collect.Multimap)super.getDefaultAttributeModifiers();', 'return this.defaultModifiers;')
    t = t.replace('return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers();', 'return this.defaultModifiers;')
    save(p, t)
print('9 ok')

# 10 BoundSword / BoundAxe
p = find1('BoundSword.java')
t = load(p)
t = t.replace('Multimap baseAttrs = super.getDefaultAttributeModifiers();', 'Multimap baseAttrs = this.defaultModifiers;')
save(p, t)
p = find1('BoundAxe.java')
t = load(p)
t = t.replace('Multimap baseAttrs = super.getDefaultAttributeModifiers();', 'Multimap baseAttrs = this.defaultModifiers;')
if 'BASE_RANGE_ID = ' not in t:
    idx = t.find('public BoundAxe()')
    if idx == -1:
        idx = t.find('public class BoundAxe')
        idx = t.find('{', idx) + 1
    t = t[:idx] + '\n    private static final net.minecraft.resources.ResourceLocation BASE_RANGE_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mna", "base_range");\n' + t[idx:]
save(p, t)
print('10 ok')

# 11 IBoundItem
p = find1('IBoundItem.java')
t = load(p)
t = t.replace('originalItem.save(nbt);', 'com.mna.api.tools.MNAItemData.save(originalItem, nbt);')
t = t.replace('EnchantmentHelper.setEnchantments((Map)enchantments, (ItemStack)bound);',
'''net.minecraft.world.item.enchantment.ItemEnchantments.Mutable __em = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
            enchantments.forEach(__em::set);
            ((ItemStack)bound).set(DataComponents.ENCHANTMENTS, __em.toImmutable());''')
save(p, t)
print('11 ok')

# 12 BookOfRote addAll cast + getTagElement
p = find1('BookOfRote.java')
t = load(p)
t = re.sub(r'modified_attrs\.addAll\(\(\(ModifiedSpellPart\)recipe\.getShape\(\)\)\.getContainedAttributes\(\)\.stream\(\)',
           'modified_attrs.addAll((java.util.List)((ModifiedSpellPart)recipe.getShape()).getContainedAttributes().stream()', t)
t = t.replace('CompoundTag compoundnbt = stack.getTagElement("display");', 'CompoundTag compoundnbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("display");')
save(p, t)
print('12 ok')

# 13 RuneMarkingPair AABB
p = find1('RuneMarkingPair.java')
t = load(p)
t = t.replace('this.writeAABB(nbt, new AABB(corner_a, corner_b));', 'this.writeAABB(nbt, new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(corner_a), net.minecraft.world.phys.Vec3.atLowerCornerOf(corner_b)));')
save(p, t)
print('13 ok')

# 14 FilledBucketFilter
p = find1('FilledBucketFilter.java')
t = load(p)
t = t.replace('return stack.getItem() instanceof BucketItem && ((BucketItem)stack.getItem()).getFluid() != null;', 'return stack.getItem() instanceof BucketItem;')
save(p, t)
print('14 ok')

# 15 Foods
p = find1('Foods.java')
t = load(p)
t = t.replace('.saturationMod(1.0f)', '.saturationModifier(1.0f)')
save(p, t)
print('15 ok')

# 16 ItemClientInit
p = find1('ItemClientInit.java')
t = load(p)
t = t.replace('((MNADyeableItem)stack.getItem()).getColor(stack)', 'MNADyeableItem.getColor(stack)')
save(p, t)
print('16 ok')

# 17 Grimoire / SpellBook
p = find1('Grimoire.java')
t = load(p)
t = t.replace('CompoundTag compoundnbt = stack.getTagElement("display");', 'CompoundTag compoundnbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("display");')
save(p, t)
p = find1('SpellBook.java')
t = load(p)
t = t.replace('stack.resetHoverName();', 'stack.remove(DataComponents.CUSTOM_NAME);')
save(p, t)
print('17 ok')

# 18 PurifiedVinteumDust
p = find1('PurifiedVinteumDust.java')
t = load(p)
t = t.replace('(NonNullList<RitualBlockPos>)NonNullList.create()', '(NonNullList<RitualBlockPos>)(NonNullList)NonNullList.create()')
save(p, t)
print('18 ok')

# 19 PractitionersPouch orElse
p = find1('PractitionersPouch.java')
t = load(p)
t = re.sub(r'(Registries\.RitualEffect\.getValues\(\)\.stream\(\)[^;]*?)\.findFirst\(\);', r'\1.findFirst().orElse(null);', t)
save(p, t)
print('19 ok')

# 20 ThaumaturgicCompass inbt cast
p = find1('ThaumaturgicCompass.java')
t = load(p)
t = t.replace('.ifPresent(inbt -> stackTag.put("LodestoneDimension", inbt));', '.ifPresent(inbt -> stackTag.put("LodestoneDimension", (Tag)inbt));')
save(p, t)
print('20 ok')

# 21 ItemManaweaverWand
p = find1('ItemManaweaverWand.java')
t = load(p)
t = t.replace('ItemManaweaverWand.getStoredPattern(worldIn.level(), stack)', 'ItemManaweaverWand.getStoredPattern(worldIn, stack)')
save(p, t)
print('21 ok')

# 22 SpellBookSlotChangeMessage
p = find1('SpellBookSlotChangeMessage.java')
t = load(p)
t = t.replace('buf.writeUtf(msg.context.getIdentifier());', 'buf.writeUtf(msg.context.identifier());')
t = t.replace('buf.writeInt(msg.context.getIndex());', 'buf.writeInt(msg.context.index());')
save(p, t)
print('22 ok')

# 23 onEquippedItemBroken Item
for fn in ['StoneRune.java', 'ScrollOfIcarianFlight.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('player.onEquippedItemBroken(held, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);', 'player.onEquippedItemBroken(held.getItem(), hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);')
    save(p, t)
p = find1('WandOfCloning.java')
t = load(p)
t = t.replace('playerIn.onEquippedItemBroken(stack, handIn == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);', 'playerIn.onEquippedItemBroken(stack.getItem(), handIn == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);')
save(p, t)
print('23 ok')

# 24 ConstructPartFluidNozzleRight
p = find1('ConstructPartFluidNozzleRight.java')
t = load(p)
t = re.sub(r'java\.util\.Optional<net\.neoforged\.neoforge\.fluids\.capability\.IFluidHandler> fluid = FluidUtil\.getFluidHandler',
           'java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandlerItem> fluid = FluidUtil.getFluidHandler', t)
save(p, t)
print('24 ok')

# 25 ArcaneCrown: remover initCapabilities
p = find1('ArcaneCrown.java')
t = load(p)
t = re.sub(r'    public ICapabilityProvider initCapabilities\(final ItemStack stack, CompoundTag nbt\) \{\r?\n(?:.*\r?\n)*?    \}\r?\n\r?\n', '', t, count=1)
save(p, t)
print('25 ok')
print('FIM')
