# _fix79.py
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

# 1 ConstructNodeEvaluator: adicionar getPathType(PathfindingContext)
p = find1('ConstructNodeEvaluator.java')
t = load(p)
if 'getPathType(net.minecraft.world.level.pathfinder.PathfindingContext' not in t:
    anchor = '    public net.minecraft.world.level.pathfinder.PathType getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext ctx, int x, int y, int z, net.minecraft.world.entity.Mob mob) {'
    ins = '''    @Override
    public PathType getPathType(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z);
    }

    public net.minecraft.world.level.pathfinder.PathType getPathTypeOfMob(net.minecraft.world.level.pathfinder.PathfindingContext ctx, int x, int y, int z, net.minecraft.world.entity.Mob mob) {'''
    t = rl(t, anchor, ins)
    save(p, t)
print('1 ok')

# 2 GreaterAnimus: re-adicionar 5-arg
p = find1('GreaterAnimus.java')
t = load(p)
if 'shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy)' not in t:
    anchor = '    public void shootCrossbowProjectile(LivingEntity p_33275_, ItemStack p_33276_, Projectile p_33277_, float p_33278_) {'
    ins = '''    protected void shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy) {
        double d0 = target.getX() - shooter.getX();
        double d1 = target.getY(0.3333333333333333) - projectile.getY();
        double d2 = target.getZ() - shooter.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        projectile.shoot(d0, d1 + d3 * 0.2, d2, velocity, inaccuracy);
    }

    public void shootCrossbowProjectile(LivingEntity p_33275_, ItemStack p_33276_, Projectile p_33277_, float p_33278_) {'''
    t = rl(t, anchor, ins)
    save(p, t)
print('2 ok')

# 3 EffectWrithingBrambles
p = find1('EffectWrithingBrambles.java')
t = load(p)
t = rl(t, '            pLivingEntity.getPersistentData().putDouble("brambles_old_z", pos.z);\n            return;', '            pLivingEntity.getPersistentData().putDouble("brambles_old_z", pos.z);\n            return true;')
save(p, t)
print('3 ok')

# 4 renderToBuffer callsites (com pontos nos args)
for fn in ['OrangeChickenRenderer.java', 'BubbleBoatRenderer.java', 'SpectralElytraLayer.java', 'AncientCouncilRenderer.java', 'DemonStoneRenderer.java', 'FlatLandsRenderer.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = re.sub(r'renderToBuffer\(([\w.]+), ([\w.]+), ([\w.]+), ([\w.]+), [^;]+\);', r'renderToBuffer(\1, \2, \3, \4, -1);', t)
    save(p, t)
print('4 ok')

# 5 ConstructRenderer super.renderNameTag sem cast
p = find1('ConstructRenderer.java')
t = load(p)
t = t.replace('super.renderNameTag((Entity)construct, name, matrixStack, buffer, p_225629_5_, partialTick);', 'super.renderNameTag(construct, name, matrixStack, buffer, p_225629_5_, partialTick);')
save(p, t)
print('5 ok')

# 6 WitchhunterTrickshotRenderer Pose
p = find1('WitchhunterTrickshotRenderer.java')
t = load(p)
t = t.replace('vertexBuilder.addVertex(matrix, (float)offsetX, (float)offsetY, (float)offsetZ).setColor(255, 255, 255, 255).setUv(textureX, textureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLightIn).setNormal(normals, (float)p_229039_9_, (float)p_229039_11_, (float)p_229039_10_);',
'''com.mojang.blaze3d.vertex.PoseStack.Pose __pose = new com.mojang.blaze3d.vertex.PoseStack.Pose(matrix, normals);
        vertexBuilder.addVertex(__pose, (float)offsetX, (float)offsetY, (float)offsetZ).setColor(255, 255, 255, 255).setUv(textureX, textureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLightIn).setNormal(__pose, (float)p_229039_9_, (float)p_229039_11_, (float)p_229039_10_);''')
save(p, t)
print('6 ok')

# 7 MAGeckoFactionRenderer green -> partialTicks
p = find1('MAGeckoFactionRenderer.java')
t = load(p)
t = t.replace('super.renderRecursively(stack, entity, bone, renderType, multiBufferSource, consumer, arg6, green, packedLightIn, packedOverlayIn, colour);', 'super.renderRecursively(stack, entity, bone, renderType, multiBufferSource, consumer, arg6, partialTicks, packedLightIn, packedOverlayIn, colour);')
save(p, t)
print('7 ok')

# 8 BarklingRenderer cast
p = find1('BarklingRenderer.java')
t = load(p)
t = t.replace('super.scaleModelForRender(widthScale, heightScale, poseStack, (GeoAnimatable)animatable, model, isReRender, partialTick, packedLight, packedOverlay);', 'super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);')
save(p, t)
print('8 ok')

# 9 WanderingWizardRenderer package
p = find1('WanderingWizardRenderer.java')
t = load(p)
t = t.replace('this.getTextureLocation((com.mna.entities.faction.WanderingWizard)entityIn)', 'this.getTextureLocation((com.mna.entities.utility.WanderingWizard)entityIn)')
save(p, t)
print('9 ok')

# 10 CommonEventHandler StreamSupport
p = find1('CommonEventHandler.java')
t = load(p)
t = t.replace('pc.getAllEffects().stream().anyMatch(', 'java.util.stream.StreamSupport.stream(pc.getAllEffects().spliterator(), false).anyMatch(')
save(p, t)
print('10 ok')

# 11 FMLClientEventHandlers ModelLayers
p = find1('FMLClientEventHandlers.java')
t = load(p)
t = t.replace('event.getSkin("default")', 'event.getSkin(net.minecraft.client.model.geom.ModelLayers.PLAYER)')
t = t.replace('event.getSkin("slim")', 'event.getSkin(net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM)')
save(p, t)
print('11 ok')

# 12 SpellPartList RecipeHolder
p = find1('SpellPartList.java')
t = load(p)
t = t.replace('Optional<Recipe> pattern = this.minecraft.level.getRecipeManager().getRecipes().stream().filter(r -> r.id().equals(p.getRegistryName())).findFirst();',
              'Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> pattern = this.minecraft.level.getRecipeManager().getRecipes().stream().filter(r -> r.id().equals(p.getRegistryName())).findFirst();')
t = t.replace('if (pattern.isPresent() && pattern.get() instanceof ItemAndPatternRecipe) {', 'if (pattern.isPresent() && pattern.get().value() instanceof ItemAndPatternRecipe) {')
t = t.replace('valid &= ((ItemAndPatternRecipe)pattern.get()).getTier() <= this.tier;', 'valid &= ((ItemAndPatternRecipe)pattern.get().value()).getTier() <= this.tier;')
save(p, t)
print('12 ok')

# 13 GuiLodestarV2 super 5 args
p = find1('GuiLodestarV2.java')
t = load(p)
t = t.replace('super(GuiLodestarV2.this.minecraft, 46, 135, GuiLodestarV2.this.topPos + 10, GuiLodestarV2.this.topPos + 153, 18);',
              'super(GuiLodestarV2.this.minecraft, 46, 135, GuiLodestarV2.this.topPos + 10, GuiLodestarV2.this.topPos + 153);')
save(p, t)
print('13 ok')

# 14 renderBackground 4-arg
for fn in ['GuiMagiciansWorkbench.java', 'GuiSpectralCraftingTable.java', 'GuiFilterItem.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = t.replace('this.renderBackground(pGuiGraphics);', 'this.renderBackground(pGuiGraphics, 0, 0, 0.0f);')
    save(p, t)
print('14 ok')

# 15 addWidget recipeBookGui
for fn in ['GuiSpectralCraftingTable.java', 'GuiWanderingWizard.java']:
    p = find1(fn)
    if not p:
        continue
    t = load(p)
    t = re.sub(r'        this\.addWidget\(\(GuiEventListener\)this\.recipeBookGui\);\r?\n', '', t)
    t = re.sub(r'        this\.addWidget\(\(GuiEventListener\)this\.list\);\r?\n', '', t)
    save(p, t)
print('15 ok')

# 16 ContainerPractitionersPouch cast
p = find1('ContainerPractitionersPouch.java')
t = load(p)
t = t.replace('((ArrayList)e.getValue()).forEach(v -> v.setActive(false))', '((java.util.ArrayList<ExtendedItemStackFilterSlot>)e.getValue()).forEach(v -> v.setActive(false))')
save(p, t)
print('16 ok')

# 17 ContainerRunicMalus forEach + import
p = find1('ContainerRunicMalus.java')
t = load(p)
t = t.replace('chants.forEach(chMut::set);', 'chants.forEach((k, v) -> chMut.set(k, v));')
if 'import net.minecraft.core.component.DataComponents;' not in t:
    t = re.sub(r'(import [^\n]*;\r?\n)', r'\1import net.minecraft.core.component.DataComponents;\n', t, count=1)
save(p, t)
print('17 ok')

# 18 ItemInventoryBase removeTagKey
p = find1('ItemInventoryBase.java')
t = load(p)
t = t.replace('''        if (this.isEmpty()) {
            this.stack.removeTagKey(TAG_ITEMS);''', '''        if (this.isEmpty()) {
            CompoundTag __t = this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            __t.remove(TAG_ITEMS);
            this.stack.set(DataComponents.CUSTOM_DATA, CustomData.of(__t));''')
save(p, t)
print('18 ok')

# 19 ItemStackEntry getOrThrow
p = find1('ItemStackEntry.java')
t = load(p)
t = t.replace('.getOrThrow(false, arg_0 -> ((Logger)ManaAndArtifice.LOGGER).error(arg_0))', '.getOrThrow()')
save(p, t)
print('19 ok')

# 20 NamedAuras
p = find1('NamedAuras.java')
t = load(p)
t = t.replace('java.util.Optional<ParticleAuraProvider> auraCap = MNACap.get(player, ParticleAuraProvider.AURA);', 'java.util.Optional<ParticleAura> auraCap = MNACap.get(player, ParticleAuraProvider.AURA);')
t = t.replace('((ParticleAura)auraCap.get()).save()', 'auraCap.get().save()')
save(p, t)
print('20 ok')

# 21 NonStupidFurnaceFuelSlot
p = find1('NonStupidFurnaceFuelSlot.java')
t = load(p)
t = t.replace('CommonHooks.getBurnTime((ItemStack)stack, (RecipeType)RecipeType.SMELTING)', '((ItemStack)stack).getBurnTime((RecipeType)RecipeType.SMELTING)')
save(p, t)
print('21 ok')

# 22 HUDOverlayRenderer FastColor
p = find1('HUDOverlayRenderer.java')
t = load(p)
t = t.replace('FastColor.ARGB32.setColor((int)255, (int)42, (int)42,', 'FastColor.ARGB32.color((int)255, (int)42, (int)42,')
save(p, t)
print('22 ok')

# 23 GuiCantrips List<Component>
p = find1('GuiCantrips.java')
t = load(p)
t = t.replace('ArrayList<MutableComponent> ttLines = new ArrayList<MutableComponent>();', 'java.util.List<net.minecraft.network.chat.Component> ttLines = new java.util.ArrayList<net.minecraft.network.chat.Component>();')
save(p, t)
print('23 ok')

# 24 GuiEnderDisc
p = find1('GuiEnderDisc.java')
t = load(p)
t = t.replace('''                    int index;
                    for (index = 0; index < this.rune_indices.length && this.rune_indices[index] != 0; ++index) {
                    }
                    ((ContainerEnderDisc)this.menu).setPattern(BuiltInRegistries.ITEM.getKey(this.dragStack.getItem()), index);''', '''                    int index2;
                    for (index2 = 0; index2 < this.rune_indices.length && this.rune_indices[index2] != 0; ++index2) {
                    }
                    ((ContainerEnderDisc)this.menu).setPattern(BuiltInRegistries.ITEM.getKey(this.dragStack.getItem()), index2);''')
t = t.replace('            this.v = 244;', '            // [PORT 1.21.1] v final no parente - atribuicao removida')
save(p, t)
print('24 ok')

# 25 GuiGuideBook requireNonNull
p = find1('GuiGuideBook.java')
t = load(p)
t = re.sub(r'            Objects\.requireNonNull\(\(\(GuiGuideBook\)GuiGuideBook\.this\)\.minecraft\.font\);\r?\n', '', t)
save(p, t)
print('25 ok')

# 26 ConstructIsFluidInContainer tank local
p = find1('ConstructIsFluidInContainer.java')
t = load(p)
t = t.replace('''handler = (net.neoforged.neoforge.fluids.capability.IFluidHandlerItem)FluidUtil.getFluidHandler((Level)world, (BlockPos)this.pos.getPosition(), (Direction)this.pos.getDirection()).orElse(null);
        if (handler == null) {''', '''net.neoforged.neoforge.fluids.capability.IFluidHandler __tank = (net.neoforged.neoforge.fluids.capability.IFluidHandler)FluidUtil.getFluidHandler((Level)world, (BlockPos)this.pos.getPosition(), (Direction)this.pos.getDirection()).orElse(null);
        if (__tank == null) {''')
t = t.replace('IFluidHandler tank = (IFluidHandler)handler.get();', 'IFluidHandler tank = __tank;')
save(p, t)
print('26 ok')
print('FIM')
