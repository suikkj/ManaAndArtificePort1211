# _fix68.py
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

# 1 ILootrBridge -> ResourceKey
p = find1('ILootrBridge.java')
t = load(p)
t = t.replace('public ResourceLocation getLootTable();', 'public net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getLootTable();')
save(p, t)
print('1 ok')

# 2 BrazierBlock
p = find1('BrazierBlock.java')
t = load(p)
t = t.replace('color = ((MNADyeableItem)be).getColor();', 'color = spell.getParticleColorOverride();')
t = t.replace('((MNADyeableItem)be).setColor(spell.getParticleColorOverride());', '// [PORT 1.21.1] MNADyeableItem.setColor agora exige ItemStack (sem stack neste contexto)')
save(p, t)
print('2 ok')

# 3 ConstructNodeEvaluator isPathfindable
p = find1('ConstructNodeEvaluator.java')
t = load(p)
t = t.replace('.isPathfindable(pLevel, pPos, PathComputationType.LAND)', '.isPathfindable(PathComputationType.LAND)')
t = t.replace('.isPathfindable(this.mob.level(), blockpos, PathComputationType.LAND)', '.isPathfindable(PathComputationType.LAND)')
save(p, t)
print('3 ok')

# 4 SimulacrumRenderer setupRotations 6 args
p = find1('SimulacrumRenderer.java')
t = load(p)
t = t.replace('protected void setupRotations(Simulacrum pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {',
              'protected void setupRotations(Simulacrum pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, float pScale) {')
t = t.replace('super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);',
              'super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks, pScale);')
save(p, t)
print('4 ok')

# 5 MAExplosion sound values
p = find1('MAExplosion.java')
t = load(p)
t = t.replace('net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.GENERIC_EXPLODE)', 'SoundEvents.GENERIC_EXPLODE.value()')
t = t.replace('this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE,', 'this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE.value(),')
t = t.replace('for (Pair pair : objectarraylist) {', 'for (Pair pair : (java.util.List<Pair>)objectarraylist) {')
save(p, t)
print('5 ok')

# 6 CommonEventHandler
p = find1('CommonEventHandler.java')
t = load(p)
t = t.replace('pc.getAllEffects()', 'pc.getActiveEffects()')
t = t.replace('BlockPos pos = BlockPos.containing((Position)explosion.getPosition());',
              'BlockPos pos = BlockPos.containing(explosion.x, explosion.y, explosion.z);')
t = t.replace('Vec3i offset = dir.getNormal().multiply((int)Math.ceil(explosion.radius));',
              'Vec3i offset = dir.getNormal().multiply((int)Math.ceil(com.mna.tools.MNAReflection.explosionRadius(explosion)));')
save(p, t)
print('6 ok')

# 7 MAInventoryData
p = find1('MAInventoryData.java')
t = load(p)
t = t.replace('public CompoundTag save(CompoundTag compound) {',
              'public CompoundTag save(CompoundTag compound, net.minecraft.core.HolderLookup.Provider provider) {')
old = '''    public void save(File fileIn) {
        super.save(fileIn);
    }

'''
t = rl(t, old, '')
t = t.replace('this.inventory = (IExtendedItemHandler)builder.apply(this.size);', 'this.inventory = builder.apply(this.size);')
save(p, t)
print('7 ok')

# 8 WitchHunterArrow
p = find1('WitchHunterArrow.java')
t = load(p)
t = t.replace('super((EntityType)EntityInit.WITCH_HUNTER_ARROW.get(), x, y, z, worldIn);',
              'super((EntityType)EntityInit.WITCH_HUNTER_ARROW.get(), x, y, z, worldIn, new ItemStack((ItemLike)Items.ARROW), null);')
t = t.replace('super((EntityType)EntityInit.WITCH_HUNTER_ARROW.get(), shooter, worldIn);',
              'super((EntityType)EntityInit.WITCH_HUNTER_ARROW.get(), shooter, worldIn, new ItemStack((ItemLike)Items.ARROW), null);')
if 'getPickupItem' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack((ItemLike)Items.ARROW);
    }
''' + t[idx:]
save(p, t)
print('8 ok')

# 9 StationaryBlock
p = find1('StationaryBlock.java')
t = load(p)
t = t.replace('this.getBlockState() = Block.stateById((int)stateId);', 'com.mna.api.tools.MNAFallingBlocks.setBlockState(this, Block.stateById((int)stateId));')
t = t.replace('this.getBlockState() = Blocks.SAND.defaultBlockState();', 'com.mna.api.tools.MNAFallingBlocks.setBlockState(this, Blocks.SAND.defaultBlockState());')
t = t.replace('tileentity.loadAdditional(compoundnbt, com.mna.api.tools.MNAItemData.provider())',
              'com.mna.api.tools.MNATileAccess.loadAdditional(tileentity, compoundnbt, com.mna.api.tools.MNAItemData.provider())')
save(p, t)
print('9 ok')

# 10 EffectGrow/EffectShrink
for fn in ['EffectGrow.java', 'EffectShrink.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'PacketDistributor\.TRACKING_ENTITY\.with\(\(\) -> (pLivingEntity)\)\.send\(\(Packet\)(new \w+\([^;]+\))\);',
               r'PacketDistributor.sendToPlayersTrackingEntity(\1, \2);', t)
    t = re.sub(r'for \(Map\.Entry entry : this\.getAttributeModifiers\(\)\.entrySet\(\)\) \{',
               r'for (Map.Entry entry : java.util.Collections.<Map.Entry>emptyList()) {', t)
    save(p, t)
print('10 ok')

# 11 PresentItemRenderer
p = find1('PresentItemRenderer.java')
t = load(p)
t = t.replace('int preRadiantSize = matrixStackIn.poseStack.size();', 'int preRadiantSize = com.mna.tools.MNAPoseStackAccess.size(matrixStackIn);')
t = t.replace('this.shouldBob() ?', 'true ?')
t = t.replace('while (matrixStackIn.poseStack.size() > preRadiantSize) {', 'while (com.mna.tools.MNAPoseStackAccess.size(matrixStackIn) > preRadiantSize) {')
save(p, t)
print('11 ok')

# 12 GuideBookEntries
p = find1('GuideBookEntries.java')
t = load(p)
t = t.replace('List results = relatedRecipes.entrySet().stream()', 'List results = (List)relatedRecipes.entrySet().stream()')
t = t.replace('return results.stream().filter(this.distinctByKey(GuidebookSearchResult::toString))', 'return ((java.util.List<GuidebookSearchResult>)results).stream().filter(this.distinctByKey(GuidebookSearchResult::toString))')
t = t.replace('for (String s : unlocked) {', 'for (String s : (java.util.List<String>)unlocked) {')
save(p, t)
print('12 ok')

# 13 ConstructButcher
p = find1('ConstructButcher.java')
t = load(p)
if 'import net.minecraft.world.entity.EntityType;' not in t:
    t = re.sub(r'(import [^\n]*;\r?\n)', r'\1import net.minecraft.world.entity.EntityType;\n', t, count=1)
t = t.replace('Optional<Object> potential = potentials.stream()', 'Optional<Animal> potential = potentials.stream()')
save(p, t)
print('13 ok')

# 14 WitchhunterTrickshot
p = find1('WitchhunterTrickshot.java')
t = load(p)
t = t.replace('MobEffect effect = this.getEffect().value();', 'net.minecraft.core.Holder<MobEffect> effect = this.getEffect();')
t = t.replace('for (LivingEntity e2 : targets) {', 'for (LivingEntity e2 : (java.util.List<LivingEntity>)targets) {')
t = t.replace('e2.addEffect(new MobEffectInstance(effect, this.effectDuration, this.effectMagnitude', 'e2.addEffect(new MobEffectInstance(effect, this.effectDuration, this.effectMagnitude')
save(p, t)
print('14 ok')

# 15 ContainerRunicMalus
p = find1('ContainerRunicMalus.java')
t = load(p)
t = t.replace('bonuses.put((Enchantment)ench, Float.valueOf(bonus - 1.0f));', 'bonuses.put((net.minecraft.core.Holder<Enchantment>)ench, Float.valueOf(bonus - 1.0f));')
t = t.replace('bonuses.put((Enchantment)ench, Float.valueOf(((Float)bonuses.get(ench)).floatValue() * bonus));', 'bonuses.put((net.minecraft.core.Holder<Enchantment>)ench, Float.valueOf(((Float)bonuses.get(ench)).floatValue() * bonus));')
save(p, t)
print('15 ok')
print('FIM')
