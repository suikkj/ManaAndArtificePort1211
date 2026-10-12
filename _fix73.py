# _fix73.py
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

# 1 CLOSED nos enums State
for fn in ['GenericRadialMenu.java', 'SpellModifierRadialSelect.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = re.sub(r'(public static enum State \{\r?\n(?:[^}]*?\r?\n)*?\s*CLOSING,)(\r?\n)', r'\1\n        CLOSED,\2', t, count=1)
    save(p, t)
print('1 ok')

# 2 GuiSpellAdjust forceUnicodeFont
p = find1('GuiSpellAdjust.java')
t = load(p)
t = t.replace('float summaryYPos = (Boolean)this.minecraft.options.forceUnicodeFont.get() != false ? 180.0f : 182.0f;', 'float summaryYPos = 182.0f;')
t = t.replace('float f = (Boolean)this.minecraft.options.forceUnicodeFont.get() != false ? 180 : 184;', 'float f = 184;')
save(p, t)
print('2 ok')

# 3 FillerBlock
p = find1('FillerBlock.java')
t = load(p)
t = t.replace('return world.getBlockState(offsetPos).use(world, player, hand, brtr);',
              'return world.getBlockState(offsetPos).getBlock().use(world.getBlockState(offsetPos), world, offsetPos, player, hand, brtr);')
save(p, t)
print('3 ok')

# 4 ConstructActivate / ComponentActivate
p = find1('ConstructActivate.java')
t = load(p)
t = t.replace('state.use(c.level(), player, InteractionHand.MAIN_HAND, brtr);',
              'state.getBlock().use(state, c.level(), this.interactPos, player, InteractionHand.MAIN_HAND, brtr);')
save(p, t)
p = find1('ComponentActivate.java')
t = load(p)
t = t.replace('state.use(c.level(), player, InteractionHand.MAIN_HAND, brtr);',
              'state.getBlock().use(state, c.level(), c.blockPosition(), player, InteractionHand.MAIN_HAND, brtr);')
save(p, t)
print('4 ok')

# 5 PotionEffectComponent getCategory
p = find1('PotionEffectComponent.java')
t = load(p)
t = t.replace('this.getEffect().getCategory()', 'this.getEffect().value().getCategory()')
save(p, t)
print('5 ok')

# 6 AABB Vec3 (OcculusTile, WellspringPillarTile)
for fn in ['OcculusTile.java', 'WellspringPillarTile.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'new AABB\(this\.worldPosition, this\.worldPosition\.offset\(([^)]+)\)\)',
               r'new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(this.worldPosition), net.minecraft.world.phys.Vec3.atLowerCornerOf(this.worldPosition.offset(\1)))', t)
    save(p, t)
print('6 ok')

# 7 onEquippedItemBroken
p = find1('StoneRune.java')
t = load(p)
t = t.replace('player.broadcastBreakEvent(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);',
              'player.onEquippedItemBroken(held, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);')
save(p, t)
p = find1('WandOfCloning.java')
t = load(p)
t = t.replace('playerIn.broadcastBreakEvent(handIn == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);',
              'playerIn.onEquippedItemBroken(stack, handIn == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);')
save(p, t)
p = find1('ScrollOfIcarianFlight.java')
t = load(p)
t = t.replace('player.broadcastBreakEvent(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);',
              'player.onEquippedItemBroken(held, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);')
save(p, t)
print('7 ok')

# 8 entityData byte
for fn in ['Odin.java', 'WitherLich.java', 'SpellSigil.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'entityData\.set\((DATA_\w+), (\d+)\)', r'entityData.set(\1, (byte)\2)', t)
    save(p, t)
print('8 ok')

# 9 RegistryFriendlyByteBuf casts
for fn in ['LootDrop.java', 'MagicSyncMessageToClient.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.item);',
                  'net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, this.item);')
    t = t.replace('net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.smeltedItem);',
                  'net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf, this.smeltedItem);')
    save(p, t)
print('9 ok')

# 10 ICancellableEvent nos eventos MNA
for fn, pat in [('EldrinPowerTransferredEvent.java', 'public class EldrinPowerTransferredEvent\nextends Event {'),
                ('RunicAnvilItemUsedEvent.java', 'public class RunicAnvilItemUsedEvent\nextends Event {'),
                ('AffinityChangedEvent.java', 'public class AffinityChangedEvent\nextends Event {')]:
    p = find1(fn)
    t = load(p)
    if 'ICancellableEvent' not in t:
        t = rl(t, pat, pat.replace('extends Event {', 'extends Event\nimplements net.neoforged.bus.api.ICancellableEvent {'))
        save(p, t)
print('10 ok')

# 11 RunicMalus conditional
p = find1('RunicMalus.java')
t = load(p)
t = t.replace('return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers();',
              'return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : (com.google.common.collect.Multimap)super.getDefaultAttributeModifiers();')
save(p, t)
print('11 ok')
print('FIM')
