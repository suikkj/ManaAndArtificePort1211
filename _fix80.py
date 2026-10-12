# _fix80.py
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
    """remove a n-esima ocorrencia (1-based) de block, tolerando CRLF"""
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

# 1 ConstructNodeEvaluator: remover a 2a copia do getPathType
p = find1('ConstructNodeEvaluator.java')
t = load(p)
blk = '''    @Override
    public PathType getPathType(net.minecraft.world.level.pathfinder.PathfindingContext context, int x, int y, int z) {
        return this.getBlockPathType((BlockGetter)context.level(), x, y, z);
    }

'''
if t.count('getPathType(net.minecraft.world.level.pathfinder.PathfindingContext') > 1:
    t = remove_nth(t, blk, 2)
# remove @Override duplicado adjacente
t = t.replace('    @Override\n    @Override\n', '    @Override\n')
t = t.replace('    @Override\r\n    @Override\r\n', '    @Override\r\n')
save(p, t)
print('1 ok')

# 2 GreaterAnimus: remover a 2a copia 5-arg
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
if t.count('shootCrossbowProjectile(LivingEntity shooter, LivingEntity target, Projectile projectile, float velocity, float inaccuracy)') > 1:
    t = remove_nth(t, blk2, 2)
save(p, t)
print('2 ok')

# 3 WitchhunterTrickshotRenderer: addVertex simples
p = find1('WitchhunterTrickshotRenderer.java')
t = load(p)
t = t.replace('''com.mojang.blaze3d.vertex.PoseStack.Pose __pose = new com.mojang.blaze3d.vertex.PoseStack.Pose(matrix, normals);
        vertexBuilder.addVertex(__pose, (float)offsetX, (float)offsetY, (float)offsetZ).setColor(255, 255, 255, 255).setUv(textureX, textureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLightIn).setNormal(__pose, (float)p_229039_9_, (float)p_229039_11_, (float)p_229039_10_);''',
'''vertexBuilder.addVertex((float)offsetX, (float)offsetY, (float)offsetZ).setColor(255, 255, 255, 255).setUv(textureX, textureY).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLightIn).setNormal((float)p_229039_9_, (float)p_229039_11_, (float)p_229039_10_);''')
save(p, t)
print('3 ok')

# 4 FMLClientEventHandlers PlayerSkin.Model
p = find1('FMLClientEventHandlers.java')
t = load(p)
t = t.replace('LivingEntityRenderer playerRenderer = event.getSkin(net.minecraft.client.model.geom.ModelLayers.PLAYER);',
              'net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer = event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.WIDE);')
t = t.replace('LivingEntityRenderer slimRenderer = event.getSkin(net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM);',
              'net.minecraft.client.renderer.entity.player.PlayerRenderer slimRenderer = event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.SLIM);')
save(p, t)
print('4 ok')

# 5 ContainerRunicMalus chants cast
p = find1('ContainerRunicMalus.java')
t = load(p)
t = t.replace('chants.forEach((k, v) -> chMut.set(k, v));', 'chants.forEach((k, v) -> chMut.set((net.minecraft.core.Holder<Enchantment>)k, (Integer)v));')
save(p, t)
print('5 ok')

# 6 ItemInventoryBase removeTagKey (replace simples)
p = find1('ItemInventoryBase.java')
t = load(p)
t = rl(t, '            this.stack.removeTagKey(TAG_ITEMS);', '''            CompoundTag __t = this.stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            __t.remove(TAG_ITEMS);
            this.stack.set(DataComponents.CUSTOM_DATA, CustomData.of(__t));''')
save(p, t)
print('6 ok')

# 7 HUDOverlayRenderer segundo setColor
p = find1('HUDOverlayRenderer.java')
t = load(p)
t = t.replace('FastColor.ARGB32.setColor((int)0, (int)42, (int)42, (int)42)', 'FastColor.ARGB32.color((int)0, (int)42, (int)42, (int)42)')
save(p, t)
print('7 ok')

# 8 GuiEnderDisc index2
p = find1('GuiEnderDisc.java')
t = load(p)
t = rl(t, '''                    int index;
                    for (index = 0; index < this.rune_indices.length && this.rune_indices[index] != 0; ++index) {
                    }
                    ((ContainerEnderDisc)this.menu).setPattern(BuiltInRegistries.ITEM.getKey(this.dragStack.getItem()), index);''', '''                    int index2;
                    for (index2 = 0; index2 < this.rune_indices.length && this.rune_indices[index2] != 0; ++index2) {
                    }
                    ((ContainerEnderDisc)this.menu).setPattern(BuiltInRegistries.ITEM.getKey(this.dragStack.getItem()), index2);''')
t = rl(t, '            this.v = 244;', '            // [PORT 1.21.1] v final no parente')
save(p, t)
print('8 ok')

# 9 GuiRoteBook addWidget
p = find1('GuiRoteBook.java')
t = load(p)
t = re.sub(r'        this\.addWidget\(\(GuiEventListener\)this\.list\);\r?\n', '', t)
save(p, t)
print('9 ok')

# 10 RGBPicker forceUnicodeFont
p = find1('RGBPicker.java')
t = load(p)
t = t.replace('float scale = (Boolean)mc.options.forceUnicodeFont.get() != false ? 1.0f : 0.7f;', 'float scale = 1.0f;')
t = t.replace('float textY = ((float)this.getY() + ((Boolean)mc.options.forceUnicodeFont.get() != false ? 6.0f : 7.5f)) / scale;', 'float textY = ((float)this.getY() + 6.0f) / scale;')
save(p, t)
print('10 ok')

# 11 ItemStackRadialMenuItem
p = find1('ItemStackRadialMenuItem.java')
t = load(p)
t = t.replace('this.stack.hasCustomHoverName()', 'this.stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)')
save(p, t)
print('11 ok')

# 12 ModifierRadialSelect raw cast
p = find1('ModifierRadialSelect.java')
t = load(p)
t = t.replace('this.knownModifiers.addAll(this.mc.player.level().getRecipeManager().getAllRecipesFor(RecipeInit.MODIFIER_TYPE.get()).stream().filter(m -> m.value().getTier() <= tier).map(m -> m.value().getComponent()).collect(Collectors.toList()));',
              'this.knownModifiers.addAll((java.util.List)this.mc.player.level().getRecipeManager().getAllRecipesFor(RecipeInit.MODIFIER_TYPE.get()).stream().filter(m -> m.value().getTier() <= tier).map(m -> m.value().getComponent()).collect(Collectors.toList()));')
save(p, t)
print('12 ok')

# 13 RecipeEldrinAltar + RecipeManaweaving resolveRecipe wildcard
for fn in ['RecipeEldrinAltar.java', 'RecipeManaweaving.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('Optional<Recipe<?>> pattern = this.resolveRecipe(rLoc);', 'Optional<? extends Recipe<?>> pattern = this.resolveRecipe(rLoc);')
    t = t.replace('Optional<Recipe<?>> pattern = this.resolveRecipe(recipeLocation);', 'Optional<? extends Recipe<?>> pattern = this.resolveRecipe(recipeLocation);')
    save(p, t)
print('13 ok')

# 14 RecipeEntity renderEntityInInventory
p = find1('RecipeEntity.java')
t = load(p)
old = t[t.find('InventoryScreen.renderEntityInInventory'):]
old = old[:old.find(';') + 1]
new = '''InventoryScreen.renderEntityInInventory((GuiGraphics)pGuiGraphics, (float)((float)this.getX() / this.scale + (float)(this.width / 2) - 25.0f), (float)((float)this.getY() / this.scale + (float)(this.height / 2) + (float)this.height * bbHeight / 4.0f), (float)this.scale, new org.joml.Vector3f(0.0f, 0.0f, 0.0f), (Quaternionf)quaternionf, (Quaternionf)null, (LivingEntity)this.dummyEntity);'''
t = t.replace(old, new)
save(p, t)
print('14 ok')

# 15 TextSection fromJsonLenient
p = find1('TextSection.java')
t = load(p)
t = t.replace('Component.Serializer.fromJsonLenient((String)this.rawText)', 'Component.Serializer.fromJson((String)this.rawText)')
save(p, t)
print('15 ok')

# 16 CuriosInterop simplify
p = find1('CuriosInterop.java')
t = load(p)
t = t.replace('handler.findFirstCurio(item).ifPresent(slotResult -> exists.setValue(slotResult.slotContext().identifier().equalsIgnoreCase(slot.identifier())))', 'handler.findFirstCurio(item).ifPresent(slotResult -> exists.setValue(true))')
save(p, t)
print('16 ok')

# 17 FeyArmorModel explicit witness
p = find1('FeyArmorModel.java')
t = load(p)
t = t.replace('(progression = (IPlayerProgression)(player = MNACap.get((Player)livingEntity, PlayerProgressionProvider.PROGRESSION)).orElse(null))', '(progression = (IPlayerProgression)(player = MNACap.<IPlayerProgression>get((Player)livingEntity, PlayerProgressionProvider.PROGRESSION)).orElse(null))')
save(p, t)
print('17 ok')

# 18 AllfatherAxe
p = find1('AllfatherAxe.java')
t = load(p)
t = re.sub(r'super\(\(Tier\)Tiers\.NETHERITE, 10\.0f, 1\.0f, (new Item\.Properties\(\)[^;]+)\);', r'super((Tier)Tiers.NETHERITE, \1);', t)
save(p, t)
print('18 ok')

# 19 ArcaneCrown ICurioItem import
p = find1('ArcaneCrown.java')
t = load(p)
if 'import top.theillusivec4.curios' not in t:
    t = re.sub(r'(import [^\n]*;\r?\n)', r'\1import top.theillusivec4.curios.api.type.capability.ICurioItem;\n', t, count=1)
save(p, t)
print('19 ok')

# 20 TrickeryBracelet .id()
p = find1('TrickeryBracelet.java')
t = load(p)
t = t.replace('attr.hasModifier(TRICKERY_SPEED_BOOST)', 'attr.hasModifier(TRICKERY_SPEED_BOOST.id())')
save(p, t)
print('20 ok')

# 21 FluidJug / InfiniteFluidJug: remover initCapabilities
for fn in ['FluidJug.java', 'InfiniteFluidJug.java']:
    p = find1(fn)
    t = load(p)
    t = re.sub(r'    public ICapabilityProvider initCapabilities\([^)]*\) \{\r?\n[^}]*\}\r?\n\r?\n', '', t)
    save(p, t)
print('21 ok')

# 22 ItemSpell getUseDuration
p = find1('ItemSpell.java')
t = load(p)
t = t.replace('spellStack.getUseDuration()', 'spellStack.getUseDuration(player)')
save(p, t)
print('22 ok')

# 23 HellfireTrident
p = find1('HellfireTrident.java')
t = load(p)
t = t.replace('player.startAutoSpinAttack(20);', 'player.startAutoSpinAttack(20, 0.0f, player.getMainHandItem());')
t = t.replace('pLevel.playSound((Player)null, (Entity)player, SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.0f, 1.0f);', 'pLevel.playSound((Player)null, (Entity)player, SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.0f, 1.0f);')
save(p, t)
print('23 ok')

# 24 ThaumaturgicCompass isLodestoneCompass + codec raw
p = find1('ThaumaturgicCompass.java')
t = load(p)
t = t.replace('if (!ThaumaturgicCompass.isLodestoneCompass((ItemStack)stack)) {', 'if (!((ItemStack)stack).has(net.minecraft.core.component.DataComponents.LODESTONE_TRACKER)) {')
t = t.replace('Level.RESOURCE_KEY_CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, dimension)', '((com.mojang.serialization.Codec)Level.RESOURCE_KEY_CODEC).encodeStart((DynamicOps)NbtOps.INSTANCE, dimension)')
save(p, t)
print('24 ok')

# 25 CodexArcana
p = find1('CodexArcana.java')
t = load(p)
t = t.replace('p.peekCodexBreadcrumb().Type ==', 'p.peekCodexBreadcrumb().breadcrumbType ==')
t = t.replace('LecternBlock.tryPlaceBook((Entity)context.getPlayer(),', 'LecternBlock.tryPlaceBook((net.minecraft.world.entity.LivingEntity)context.getPlayer(),')
save(p, t)
print('25 ok')

# 26 ConstructPartAxeArm* / BladeArm*
for fn in ['ConstructPartAxeArmLeft.java', 'ConstructPartAxeArmRight.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('brokenItem -> player.onEquippedItemBroken(brokenItem, ctx.getHand())', 'brokenItem -> player.onEquippedItemBroken(brokenItem, net.minecraft.world.entity.EquipmentSlot.MAINHAND)')
    t = t.replace('return p_40990_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers();', 'return p_40990_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : (com.google.common.collect.Multimap)super.getDefaultAttributeModifiers();')
    save(p, t)
for fn in ['ConstructPartBladeArmLeft.java', 'ConstructPartBladeArmRight.java']:
    p = find1(fn)
    t = load(p)
    t = t.replace('return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers();', 'return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : (com.google.common.collect.Multimap)super.getDefaultAttributeModifiers();')
    save(p, t)
print('26 ok')

# 27 ConstructPartFluidNozzleLeft Optional<IFluidHandlerItem>
p = find1('ConstructPartFluidNozzleLeft.java')
t = load(p)
t = t.replace('java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandler> fluid = FluidUtil.getFluidHandler((ItemStack)((SlotResult)equipped.get()).stack());',
              'java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandlerItem> fluid = FluidUtil.getFluidHandler((ItemStack)((SlotResult)equipped.get()).stack());')
save(p, t)
print('27 ok')
print('FIM')
