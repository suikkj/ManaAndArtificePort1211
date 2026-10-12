# _fix72.py
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

# 1 IFactionEnemy line rewrite
p = find1('IFactionEnemy.java')
t = load(p)
t = t.replace('return world.isAreaLoaded(pos, 1) && (chunkAccess = (net.minecraft.world.level.chunk.ChunkAccess)world.getChunk(pos)) instanceof LevelChunk && (chunkMagic = (IChunkMagic)(levelChunk = MNACap.get((LevelChunk)chunkAccess, ChunkMagicProvider.MAGIC)).orElse(null)) != null && chunkMagic.getResidualMagic() > 0.0f;',
              'return world.isAreaLoaded(pos, 1) && (chunkAccess = (net.minecraft.world.level.chunk.ChunkAccess)world.getChunk(pos)) instanceof LevelChunk && (chunkMagic = (IChunkMagic)MNACap.get((LevelChunk)chunkAccess, ChunkMagicProvider.MAGIC).orElse(null)) != null && chunkMagic.getResidualMagic() > 0.0f;')
save(p, t)
print('1 ok')

# 2 FluidParameterRegistry
p = find1('FluidParameterRegistry.java')
t = load(p)
t = t.replace(' && effect.isCurativeItem(new ItemStack((ItemLike)Items.MILK_BUCKET)));', ');')
save(p, t)
print('2 ok')

# 3 EldrinConduitBlock
p = find1('EldrinConduitBlock.java')
t = load(p)
t = t.replace('player.openMenu(provider, dataWriter);', 'player.openMenu(provider, (java.util.function.Consumer)dataWriter);')
t = t.replace('pLevel.getChunkAt(pPos).getCapability(ManaAndArtificeMod.getChunkMagicCapability())', 'com.mna.api.tools.MNACap.get(pLevel.getChunkAt(pPos), com.mna.capabilities.CapabilitiesInit.CHUNK_MAGIC)')
save(p, t)
print('3 ok')

# 4 ChalkRuneBlock
p = find1('ChalkRuneBlock.java')
t = load(p)
t = t.replace('worldIn.getEntities((EntityTypeTest)EntityInit.RITUAL_ENTITY.get(), new AABB(pos).inflate(16.0), e -> true)', 'worldIn.getEntitiesOfClass(Ritual.class, new AABB(pos).inflate(16.0), e -> true)')
t = addimport(t, 'import net.minecraft.world.level.block.state.properties.BlockStateProperties;')
save(p, t)
print('4 ok')

# 5 ManaweaveCacheTile
p = find1('ManaweaveCacheTile.java')
t = load(p)
t = t.replace('new MobEffectInstance(effect, this.duration, this.amplifier, false, false)', 'new MobEffectInstance(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), this.duration, this.amplifier, false, false)')
t = t.replace('return this.lootTableId;', 'return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, this.lootTableId);')
save(p, t)
print('5 ok')

# 6 MNAItemData.hurt helper
p = find1('MNAItemData.java')
t = load(p)
if 'public static boolean hurt(' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** [PORT 1.21.1] ItemStack.hurt(int, RandomSource, Player) removido - helper com semantica de quebra. */
    public static boolean hurt(ItemStack stack, int amount, net.minecraft.util.RandomSource random) {
        if (!stack.isDamageableItem()) {
            return false;
        }
        int newDamage = stack.getDamageValue() + amount;
        if (newDamage >= stack.getMaxDamage()) {
            return true;
        }
        stack.setDamageValue(newDamage);
        return false;
    }
''' + t[idx:]
    save(p, t)
print('6 ok')

# 7 RunescribingTableTile hurt
p = find1('RunescribingTableTile.java')
t = load(p)
t = t.replace('chiselStack.hurt(1, source, null)', 'com.mna.api.tools.MNAItemData.hurt(chiselStack, 1, source)')
t = t.replace('hammerStack.hurt(1, source, null)', 'com.mna.api.tools.MNAItemData.hurt(hammerStack, 1, source)')
save(p, t)
print('7 ok')

# 8 TranscriptionTableTile
p = find1('TranscriptionTableTile.java')
t = load(p)
t = t.replace('ink.hurt(this.getInkRequired(), this.level.random, (ServerPlayer)crafter)', 'com.mna.api.tools.MNAItemData.hurt(ink, this.getInkRequired(), this.level.random)')
t = t.replace('if (this.getItem(2).hasCustomHoverName()) {', 'if (this.getItem(2).has(DataComponents.CUSTOM_NAME)) {')
save(p, t)
print('8 ok')

# 9 ManaPylonTile
p = find1('ManaPylonTile.java')
t = load(p)
t = t.replace('(T construct) -> {', '(Construct construct) -> {')
t = t.replace('this.forAllEntities((EntityTypeTest)EntityInit.SPELL_RUNE.get(), false, (T e) -> {', 'this.forAllEntities(EntityInit.SPELL_RUNE.get(), false, (SpellRune e) -> {')
save(p, t)
print('9 ok')

# 10 FixedBookModel
p = find1('FixedBookModel.java')
t = load(p)
t = t.replace('this.root.render(p_102317_, p_102318_, p_102319_, p_102320_, p_102321_, p_102322_, p_102323_, p_102324_);', 'this.root.render(p_102317_, p_102318_, p_102319_, p_102320_, -1);')
if 'renderToBuffer' not in t:
    old = '''    public void render(PoseStack p_102317_, VertexConsumer p_102318_, int p_102319_, int p_102320_, float p_102321_, float p_102322_, float p_102323_, float p_102324_) {'''
    new = '''    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    public void render(PoseStack p_102317_, VertexConsumer p_102318_, int p_102319_, int p_102320_, float p_102321_, float p_102322_, float p_102323_, float p_102324_) {'''
    t = rl(t, old, new)
save(p, t)
print('10 ok')

# 11 SimpleGeoBlockRenderer
p = find1('SimpleGeoBlockRenderer.java')
t = load(p)
t = re.sub(r'SimpleGeoBlockRenderer<V extends BlockEntity>', 'SimpleGeoBlockRenderer<V extends BlockEntity & software.bernie.geckolib.animatable.GeoAnimatable>', t)
t = t.replace('super.render(tile, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);', 'super.render((V)tile, partialTicks, poseStack, bufferSource, packedLight, packedOverlay);')
save(p, t)
print('11 ok')

# 12 MACapabilityForgeEventHandlers
p = find1('MACapabilityForgeEventHandlers.java')
t = load(p)
t = t.replace('event.setResult(Result.DENY);', 'event.setCanceled(true);')
t = t.replace('event.setCanceled(true);\n                }\n            });\n        }\n    }\n\n    @SubscribeEvent\n    public static void onMobGriefing', 'event.setCanGrief(false);\n                }\n            });\n        }\n    }\n\n    @SubscribeEvent\n    public static void onMobGriefing')
save(p, t)
print('12 ok')

# 13 MagicXPGainedEvent ICancellableEvent
p = find1('MagicXPGainedEvent.java')
t = load(p)
t = t.replace('public class MagicXPGainedEvent\nextends Event {', 'public class MagicXPGainedEvent\nextends Event\nimplements net.neoforged.bus.api.ICancellableEvent {')
save(p, t)
print('13 ok')
print('FIM')
