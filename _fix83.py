# _fix83.py
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

# 1 AMRecipeBaseSerializer.codec()
p = find1('AMRecipeBaseSerializer.java')
t = load(p)
if 'MapCodec<T> codec()' not in t:
    anchor = '    protected abstract T readExtra(ResourceLocation var1, JsonObject var2);'
    ins = '''    @Override
    public com.mojang.serialization.MapCodec<T> codec() {
        // [PORT 1.21.1 - RUNTIME TODO] codec real de receita nao migrado; receitas destes tipos nao carregam de JSON.
        return com.mojang.serialization.MapCodec.unit((T)null);
    }

'''
    t = rl(t, anchor, ins + anchor)
    save(p, t)
print('1 ok')

# 2 ClientPacketListenerMixin effect()
p = find1('ClientPacketListenerMixin.java')
t = load(p)
t = t.replace('packet.getEffect().value() == EffectInit.REDUCE.get()', 'packet.effect().value() == EffectInit.REDUCE.get()')
t = t.replace('(mobeffect = packet.getEffect().value()) != null', '(mobeffect = packet.effect().value()) != null')
save(p, t)
print('2 ok')

# 3 ExtendedItemStackPacketBuffer accept
p = find1('ExtendedItemStackPacketBuffer.java')
t = load(p)
t = t.replace('p_178354_.accept(this, (ExtendedItemStackPacketBuffer)(Object)t);', 'p_178354_.accept(this, t);')
save(p, t)
print('3 ok')

# 4 RandomSpellStaff typed lambda
p = find1('RandomSpellStaff.java')
t = load(p)
t = t.replace('.apply(inst, (conditions, a, b, c) -> new RandomSpellStaff(conditions, a, b, c))));', '.apply(inst, (java.util.List<net.minecraft.world.level.storage.loot.predicates.LootItemCondition> conditions, Float a, Float b, Boolean c) -> new RandomSpellStaff(conditions, a, b, c))));')
save(p, t)
print('4 ok')

# 5 WardingCandleBlock rewrite
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
print('5 ok')

# 6 ManaweavingPatternHelper
p = find1('ManaweavingPatternHelper.java')
t = load(p)
t = t.replace('(Container)ContainerTools.createTemporaryContainer(ItemStack.EMPTY)', 'net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(ItemStack.EMPTY))')
save(p, t)
print('6 ok')

# 7 ItemClientInit import
p = find1('ItemClientInit.java')
t = load(p)
t = addimport(t, 'import com.mna.api.tools.MNADyeableItem;')
save(p, t)
print('7 ok')

# 8 DemonArmorRenderer
p = find1('DemonArmorRenderer.java')
t = load(p)
t = t.replace('super.getRenderType((Item)animatable, texture, bufferSource, partialTick)', 'super.getRenderType(animatable, texture, bufferSource, partialTick)')
save(p, t)
print('8 ok')

# 9 AuraSyncMessageToServer
p = find1('AuraSyncMessageToServer.java')
t = load(p)
t = t.replace('a.load(this.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())', 'a.load(new CompoundTag())')
save(p, t)
print('9 ok')

# 10 ManaweavePatternDrawnMessage:134
p = find1('ManaweavePatternDrawnMessage.java')
t = load(p)
t = t.replace('tile.pushPattern((ManaweavingPattern)pattern, (Player)player)', 'tile.pushPattern((ManaweavingPattern)pattern.value(), (Player)player)')
save(p, t)
print('10 ok')

# 11 MultiblockSyncRequestMessage
p = find1('MultiblockSyncRequestMessage.java')
t = load(p)
t = t.replace('if (r instanceof MultiblockDefinition) {', 'if (r.value() instanceof MultiblockDefinition) {')
t = t.replace('MultiblockDefinition mbd = (MultiblockDefinition)r;', 'MultiblockDefinition mbd = (MultiblockDefinition)r.value();')
save(p, t)
print('11 ok')

# 12 OpenBeltBagMessage
p = find1('OpenBeltBagMessage.java')
t = load(p)
t = t.replace('for (SlotResult slot : slots) {', 'for (SlotResult slot : (java.util.List<SlotResult>)slots) {')
save(p, t)
print('12 ok')

# 13 RoteSpellsSyncMessageToServer
p = find1('RoteSpellsSyncMessageToServer.java')
t = load(p)
t = re.sub(r'net\.minecraft\.world\.item\.ItemStack\.OPTIONAL_STREAM_CODEC\.encode\(buf,', 'net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf)buf,', t)
save(p, t)
print('13 ok')

# 14 MAParticleBase
p = find1('MAParticleBase.java')
t = load(p)
t = t.replace('return this.sprite.getV((double)((this.vo + 1.0f) / 4.0f * 16.0f));', 'return this.sprite.getV((float)((this.vo + 1.0f) / 4.0f * 16.0f));')
save(p, t)
print('14 ok')

# 15 FX textureManager
for fn in ['FXAir.java', 'FXBlueFlame.java', 'FXDrip.java', 'FXDroplet.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = t.replace('Minecraft.getInstance().textureManager.getTexture(', 'Minecraft.getInstance().getTextureManager().getTexture(')
    save(p, t)
print('15 ok')

# 16 ItemCloudBottle / WakebloomItem restore
for fn in ['ItemCloudBottle.java', 'WakebloomItem.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('blocksnapshot.restore(true, false)', 'blocksnapshot.restore(true)')
    save(p, t)
print('16 ok')

# 17 AddChimeriteModifier typed lambdas
p = find1('AddChimeriteModifier.java')
t = load(p)
t = t.replace('forGetter(m -> m.numChimerite)', 'forGetter((AddChimeriteModifier m) -> m.numChimerite)')
t = t.replace('forGetter(m -> m.magicLevel)', 'forGetter((AddChimeriteModifier m) -> m.magicLevel)')
t = t.replace('forGetter(m -> Float.valueOf(m.chance))', 'forGetter((AddChimeriteModifier m) -> Float.valueOf(m.chance))')
t = t.replace('forGetter(m -> Float.valueOf(m.fortuneBonusChance))', 'forGetter((AddChimeriteModifier m) -> Float.valueOf(m.fortuneBonusChance))')
t = t.replace('forGetter(m -> m.dropsWith)', 'forGetter((AddChimeriteModifier m) -> m.dropsWith)')
save(p, t)
print('17 ok')

# 18 BeheadingModifier import
p = find1('BeheadingModifier.java')
t = load(p)
t = addimport(t, 'import net.minecraft.world.level.storage.loot.parameters.LootContextParams;')
save(p, t)
print('18 ok')
print('FIM')
