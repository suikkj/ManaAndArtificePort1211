# FIXLIST — Mana and Artifice → NeoForge 1.21.1

## 🎉 ESTADO FINAL (2026-10-12): **0 ERROS — BUILD SUCCESSFUL**

| Marco | Valor |
|---|---|
| Erros iniciais (clean compile, baseline real) | **46.940** |
| Erros finais | **0** |
| `gradle clean compileJava` | **exit=0** |
| `gradle build` | **BUILD SUCCESSFUL** |
| Jar gerado | `build/libs/mna-3.1.11.jar` (32 MB, 2.404 classes, `neoforge.mods.toml`) |
| Commits | 41+ (repo `suikkj/ManaAndArtificePort1211`) |

**Próximo passo:** teste em jogo (instalar o jar no pack) — ver seção
"⚠️ VERIFICAÇÃO DE RUNTIME PENDENTE" abaixo. O mod **compila e empacota**;
a validação de comportamento em runtime ainda não foi feita.

**Últimos lotes:** `_fix67`–`_fix87` (522→0): effects artifacts do decompiler,
lambdas effectively-final, `this$0` sintéticos, `lambda$X$N` sintéticos,
`boolean equal = false` (InventoryUtilities), etc. + limpeza de 2.434 `.class`
stale em `src/main/resources` (1.854+561 duplicados + 19 JEI órfãs sem fonte).

Lista viva de afazeres do port. Baseline inicial medida em **2026-10-11** por clean compile
(`gradle clean compileJava`): 5.086 erros; com o pipeline de build completo a medição
real chegou a **46.940 erros**.

Regra de ouro (do skill forge-to-neoforge-port): **nunca deixar o build pior**.
Cada bloco é validado por clean compile. Bloco reverte se ficar pior.

Snapshot de segurança: `_snapshots/snap-20261011-024252-pre-analise/` (1.861 .java).
Restaurar: `cp -r _snapshots/snap-.../src .`

**Repositório remoto:** https://github.com/suikkj/ManaAndArtificePort1211 (branch `main`).
Push: `GIT_TERMINAL_PROMPT=0 git push -u origin main` (repo git próprio do workspace —
⚠ o home `C:\Users\lucas\.git` é um repo acidental e NÃO deve ser usado).

---

## AUDITORIA PRÉVIA (fase B pedida pelo usuário)

**Erros causados por passes anteriores** — o que foi investigado e o resultado:

- ✅ **Fases A/D/F ESTÃO FEITAS** (corrige alerme anterior meu). Varredura filtrando
  comentário vs código real deu **0 ocorrências** em: `SimpleChannel`, `NetworkRegistry`,
  `MobType`, `ForgeEventFactory`, `NonNullLazy`, `IEntityAdditionalSpawnData`,
  `ToolActions`, `@ObjectHolder`, `entityData.define`, `Renderer.vertex(`, `mezz.jei`.
  As ocorrências que apareciam eram **javadoc do CFR** (imports antigos preservados
  como comentário) — lixo de decompilação, não erro.
- 🔴 **BUG REAL DE DESCOMPILAÇÃO (não é drift de API):** `AbstractSelectionList` do
  vanilla 1.21.1 agora estende `AbstractContainerWidget` e **removeu os campos
  protegidos `x0/y0/x1/y1`** (existentes em 1.20.1). O MNA os usa em 5 GUIs.
  → ver B1.

---

## B — Blocos de correção

### B1. `AbstractSelectionList` perdeu `x0/y0/x1/y1` (removidos na 1.21.1)
- [ ] **B1** `ReagentList.java` (16 erros) — declarar campos próprios no construtor
- [ ] **B1** `SpellPartList.java` (16)
- [ ] **B1** `GuiLodestarV2.java` (19)
- [ ] **B1** `GuiWanderingWizard.java` (22)
- [ ] **B1** `GuiOcculus.java` (6)
  Mapeamento: `x0→getX()`, `y0→getY()`, `x1→getX()+getWidth()`, `y1→getY()+getHeight()`.
  ⚠ Nas 3 classes que **atribuem** (`this.x0 = …`), os campos passam a ser privados
  da classe MNA (o vanilla não os tem mais). Nas que só leem, usar getters.
  **Total B1: 79 erros.**

### B2. `new ResourceLocation(a, b)` — ctor 2-args agora privado
- [ ] **B2** 40 ocorrências em ~N arquivos → `ResourceLocation.fromNamespaceAndPath` / `parse`
  (verificar 1-arg: `new ResourceLocation("")` é VÁLIDO, mantém.)

### B3. `ModelResourceLocation` (156 erros)
- [ ] **B3** investigar: em 1.21.1 `ModelResourceLocation` mudou de pacote/API?
  (bloco de render de itens — `ItemRenderer`, BEWLR)

### B4. `Matrix3f` → `Pose` (96 erros)
- [ ] **B4** render: assinatura mudou (WorldRenderUtils 79 erros)

### B5. `addRenderableWidget` (163 erros)
- [ ] **B5** ctor/uso mudou em 1.21.1

### B6. `package BuiltInRegistries does not exist` (129 erros)
- [ ] **B6** puramente import ausente — adicionar
  `net.minecraft.core.registries.BuiltInRegistries`

### B7. `MobEffect` ↔ `Holder<MobEffect>` (259)
- [ ] **B7** data-driven: efeitos agora são `Holder<MobEffect>`

### B8. `Enchantment` ↔ `Holder<Enchantment>` (72) + `EnchantmentCategory` (38)
    + `Enchantment.Rarity` (29) + `TierSortingRegistry` (32)
- [ ] **B8** bloco encantamentos data-driven (atômico)

### B9. `ItemStack` NBT residual: `getTag()` (72) + `hasTag()` (13) + `setTag()` (4)
- [ ] **B9** `getTag`→`getOrDefault(CUSTOM_DATA,…)`, etc. SÓ em receptores ItemStack

### B10. Capabilities → Attachments: `LazyOptional` (160) + `CapabilityToken` (21)
     + `ICapabilitySerializable` (14) + `ForgeCapabilities` (32)
- [ ] **B10** design pronto em `design_refs/CapabilitiesInit.attachments.reference.java`
  (atômico, maior bloco)

### B11. Networking residual: `NetworkHooks` (27) + `StreamCodec` (76) + `Serializer` (26)
- [ ] **B11**

### B12. Menus: `IForgeMenuType` (41 em ContainerInit.java)
- [ ] **B12**

### B13. Capibilities de item/bloco: `ForgeCapabilities` (32)
- [ ] **B13**

### B14. Misc: `PotionUtils` (13), `DyeableLeatherItem` (17), `IForgeShearable` (11),
     `RenderGuiOverlayEvent` (18), double→float (62), `ConstructTask` (58), `Tag` (56)
- [ ] **B14** pontuais, um a um

### B15. `incompatible types` / `no suitable method` genéricos (~2.050)
- [ ] **B15** resolvidos como subproduto dos blocos acima; reinvestigar após

## B16. 🔴 Pipeline de dano 1.21.1 — `DamageEventHandler` (109 erros, ATÔMICO)
- [ ] **B16** `LivingAttackEvent` e `LivingHurtEvent` **foram REMOVIDOS** no 1.21.1.
  Substituídos por `LivingDamageEvent.Pre` / `LivingDamageEvent.Post` +
  `DamageContainer` (ver `net.neoforged.neoforge.common.damagesource.DamageContainer`).
  - `event.setAmount(float)` → `event.getContainer().setNewDamage(float)` (Pre)
  - `event.setCanceled(true)` → `event.getContainer().setNewDamage(0)` (Pre)
  - `ShieldBlockEvent` → `LivingShieldBlockEvent`
  - `onLivingAttack` + `onLivingHurt` + `onLivingDamage` precisam ser **unificados**
    numa única passagem sobre o novo pipeline — bloco de 1 arquivo, mas profundo.
  ⚠️ NÃO fazer replace mecânico: a semântica de redução de dano mudou.

## B17. `EntityInit` (73) — builders de entidade
- [ ] **B17** `EntityType.Builder.of(...)` mudou de assinatura

## B18. `StreamCodec` (76) + `register(ResourceLocation,Object)` (94)
- [ ] **B18** codecs de rede/particle

## B25. ✅ LOTE 2-6 (sessão 2026-10-12 madrugada) — resumo dos blocos novos
- **B25** `addAttributeModifier(Holder, String, ...)` → `ResourceLocation.parse(...)` (11 arq, 23 sites)
- **B26** `NbtUtils.readBlockPos(CompoundTag)` → `(CompoundTag, String)` devolvendo `Optional<BlockPos>`
  (17 arq/20 sites). `writeBlockPos` agora grava `IntArrayTag` — par write/read consistente.
  Legado `{X,Y,Z}` (multiblock JSON) → helper `MNANbt.readBlockPosLegacy`.
- **B27** `GlassBlock` REMOVIDO (1.21.1 usa `TransparentBlock` p/ vidro liso) → RunicGlassBlock.
- **B28** GeckoLib 4.8.4: `float red/green/blue/alpha` → `int colour` em
  `actuallyRender/renderRecursively/preRender/postRender/renderFinal`;
  `getPackedOverlay(T,float,float)` 3 args; casts `(Entity)animatable` removidos;
  `MAGeckoRenderer<T extends LivingEntity & GeoAnimatable>`; helper `MNAGeo.packColour`.
- **B29** `MAParticleType`: `Deserializer`→`StreamCodec` (lambda `(buf,value)->value.writeToNetwork(buf)`),
  `CODEC` virou `MapCodec` via `RecordCodecBuilder.mapCodec`, `streamCodec()` override.
- **B30** `ArmorMaterial` virou RECORD → `ArmorMaterialInit` (DeferredRegister + Holder);
  armaduras usam `Holder<ArmorMaterial>`; durabilidade via `Properties.durability(type.getDurability(factor))`.
- **B31** `DyeableArmorItem`/`DyeableLeatherItem` removidos → `MNADyeableItem` (DYED_COLOR);
  `DyeableMageArmor.getColor` reescrito p/ `DataComponents.DYED_COLOR`.
- **B32** `AbstractWidget.render` virou FINAL → 9 overrides renomeados p/ `renderWidget`
  (listas, botões). ChiselImageButton: `this.renderWidget`→`super.renderWidget`;
  CategoryItemButton: gate de página movido p/ renderWidget.
- **B33** `damageItem(..., Consumer<T>)` → `Consumer<Item>` (assinatura NeoForge IItemExtension; 8 arq).
- **B34** `RecordItem` removido → `new Item(props.jukeboxPlayable(ResourceKey<JukeboxSong>))`.
- **B35** `new AttributeModifier("string", ...)` → `ResourceLocation.parse(...)`.
- **B36** `DistExecutor` removido → `if (FMLEnvironment.dist == Dist.CLIENT)` (APIBridge, ManaAndArtifice).
- **B37** `canSustainPlant` agora TriState: `soil.canSustainPlant(level,pos,dir,plantState)`;
  TRUE/FALSE mandam, DEFAULT → `mayPlaceOn` (flores MNA). `IPlantable`/`PlantType` removidos.
- **B38** `CodexBreadcrumb`: campo `Type` sombreava o enum → renomeado p/ `breadcrumbType`.
- **B39** `ItemStack.getEnchantmentLevel` → `EnchantmentHelper.getEnchantmentLevel(Holder, LivingEntity)`.
- **B40** `MAEffects`/`SFX`/`Components`/`Shapes`/`Modifiers`/`Enchantments` (@ObjectHolder nulos) — ✅ reconstruídos.
- **B41** `MNAImageButton`: ctors 9/11/12-arg p/ os call-sites antigos; 16 subclasses
  `extends ImageButton`→`extends MNAImageButton` (import vanilla mantido p/ refs de tipo).

### ⚠️ LIÇÃO CRÍTICA (perda de tempo real nesta sessão)
Scripts que reescrevem o arquivo com o texto **sem comentários** (`sc()`) CORROMPEM:
`"https://..."` vira `"https:` + resto da linha apagado (o `//` é tratado como comentário).
**REGRA: sempre escrever de volta o TEXTO ORIGINAL com replaces pontuais; nunca gravar a saída do sc().**


## B19. 🔴 `Enchantment` virou RECORD imutável (B8 real — ATÔMICO, maior bloco lógico)
No 1.21.1 `Enchantment` **não pode mais ser herdado**: é um `record`
`(Component description, EnchantmentDefinition definition, HolderSet<Enchantment> exclusiveSet,
DataComponentMap effects)`. Removidos: `EnchantmentCategory`, `Enchantment.Rarity`,
`TierSortingRegistry`, `Serializer`, e os overrides `isDiscoverable/isTradeable/
isAllowedOnBooks/canApplyAtEnchantingTable/isTreasureOnly/isCurse`.
- [ ] **B19a** `MAEnchantmentBase` (16 classes herdam) → bridge `MAEnchantmentBridge`
      (criado: `Rarity`, `Category`, `definition()`, `build()`)
- [ ] **B19b** `EnchantmentEnumExtender` (StaffOrWeapon/STAVES) → `HolderSet<Item>` equivalente
- [ ] **B19c** `EnchantmentInit` (73 registros) → `new Enchantment.Builder(def).build(id)`;
      `withEffect(...)`/`withManaCost(...)`/`withPredicate(...)` do MNA viram
      `EnchantmentAttributeEffect` + `DataComponentType` (ver `withEffect` no javap do Builder)

## B20. Curios 9.x: `ICurio` → `ICurioItem` (JÁ MIGRADO ✅)
Curios 9.5.1 (1.21.1): `CuriosCapability.ITEM`+`ICurio` → `ICurioItem`
(`curioTick(SlotContext, ItemStack)`). `CurioItemCapability` reescrito como
`ICapabilityProvider<ItemStack, Void, ICurioItem>`; `ChargeableItem` migrado.

## B21. Eventos REMOVIDOS sem substituto no 1.21.1 (features perdidas — documentadas)
- `LivingJumpEvent` → sem substituto (boost de pulo do MNA perdido)
- `LivingVisibilityEvent` → sem substituto (camuflagem/invisibilidade perdida)
- `SleepingTimeCheckEvent` → sem substituto
  → handlers comentados em `CommonEventHandler`; reimplementar via **mixin** se necessário.

## B22. `Event.HasResult` removido do bus NeoForge
Eventos custom do MNA (`AuraEvent.Compatibility`, `RunicAnvilShouldActivateEvent`)
perderam o `getResult()/setResult()` herdado. Migrados para enum próprio
`Result {ALLOW, DENY, DEFAULT}` no próprio evento. `PlayerInteractEvent` →
`setCancellationResult(InteractionResult.FAIL)`.

## B23. Atributos NeoForge movidos para vanilla
`NeoForgeMod.BLOCK_REACH`→`Attributes.BLOCK_INTERACTION_RANGE`,
`ENTITY_REACH`→`ENTITY_INTERACTION_RANGE`, `ENTITY_GRAVITY`→`Attributes.GRAVITY`
(agora `Holder<Attribute>`, sem `.get()`).

## B24. `DyeableLeatherItem` REMOVIDO
→ `MNADyeableItem` (bridge em `com.mna.api.tools`) usando
`DataComponents.DYED_COLOR` / `DyedItemColor(rgb, showInTooltip)`.

---

## Progresso
| Marco | Erros | Delta |
|---|---|---|
| Início do port | 46.940 | — |
| Sessão anterior | 5.118 | −41.822 |
| **Baseline 2026-10-11** | **5.086** | −41.854 |
| ✅ B6 imports BuiltInRegistries (66 arq) + B1 x0/y0/x1/y1 (5 arq) + B2 RL ctor (78 arq) | 4.770 | −316 |
| ✅ (Object) casts do CFR em entityData.set (223, 46 arq) | 4.553 | −217 |
| ✅ B3 ImageButton via helper `MNAImageButton` (40 call-sites, 15 arq) | 4.516 | −37 |
| ✅ **`MAEffects` estava 100% nula** (@ObjectHolder removido) — 62 NPEs de runtime evitadas | 4.512 | −4 |
| ✅ (MobEffect) casts do CFR removidos (248) + B3b ModelResourceLocation standalone (85) | 4.427 | −85 |
| ✅ B4 `poseStack.getLast()`→`last()` + `setNormal(Matrix3f)`→`setNormal(Pose)` (5 arq) | 4.367 | −60 |
| ✅ SFX.java regenerado (174 campos, mapa do Vineflower + sounds.json) | 4.432 | +65* |
| ✅ `getEffect().value()` em `MobEffectInstance` (20 subst, 8 arq) | 4.282 | −150 |
| ✅ Wrap em `holder()` em hasEffect/getEffect/removeEffect/MobEffectInstance (240 subst, 75 arq) | 4.053 | −229 |
| ✅ **B16** DamageEventHandler: LivingIncomingDamageEvent / LivingDamageEvent.Pre / LivingShieldBlockEvent | 3.817 | −236 |
| ✅ `ItemStack.of(CompoundTag)`→`parseOptional(provider, …)` (4 contextos) | 3.806 | −11 |
| ✅ TickEvent.Phase→PlayerTickEvent.Post (2); LivingTickEvent→EntityTickEvent+cast (4) | 3.672 | −134 |
| ✅ `addVertex(double…)`→float + literais `.0f` + `getU/getV(float)` (12 arq) | 3.614 | −58 |
| ✅ `renderBackground(GuiGraphics, float, int, int)` com params detectados por método (25 arq) | 3.440 | −174 |
| ✅ `appendHoverText` + `Item.TooltipContext` (59 assinaturas + 3 call-sites) | 3.428 | −12 |
| ✅ **B10** `LazyOptional` → campo direto / `java.util.Optional` (60+45 arq) | **3.360** | **−68** |
| ✅ TileEntityWithInventory: `LazyOptional`/`ForgeCapabilities` → campo `IItemHandler` direto | 3.421 | −61 |
| ✅ `AttributeModifier(uuid,"name",…)` → `AttributeModifier(loc, …)` (35 subst, 18 arq) | 3.395 | −26 |
| ✅ `ItemAttributes.BASE_ATTACK_DAMAGE_UUID` → `Item.BASE_ATTACK_DAMAGE_ID` (13 arq) | 3.421 | −0 |
| ✅ `mouseScrolled(double,double,double)` → `+double scrollX` (5 arq) | 3.385 | −6 |
| ✅ **B21** LivingJumpEvent/LivingVisibilityEvent/SleepingTimeCheckEvent removidos → handlers desativados | 3.357 | −28 |
| ✅ **B22** `Event.HasResult` → Result próprio nos eventos custom (8 arq) | 3.384 | −27 |
| ✅ **B23** NeoForgeMod.{BLOCK,ENTITY}_REACH / ENTITY_GRAVITY → `Attributes.*` (17 arq) | 3.354 | −30 |
| ✅ **B24** `DyeableLeatherItem` → `MNADyeableItem` (DataComponents.DYED_COLOR) (8 arq) | 3.377 | −15 |
| ✅ **B20** Curios 9.x: `ICurio`→`ICurioItem`, `CurioItemCapability` reescrito | 3.396 | −1 |
| ✅ **B19a** `Enchantment` virou record: bases (`MAEnchantmentBase`, `MADamageEnchantmentBase`) deixam de estender `Enchantment`; bridge `MAEnchantmentBridge` (Rarity/Category/definition/build) | 3.343 | −53 |
| ✅ `TierSortingRegistry` REMOVIDO → `MNAtierSorting` (ordem dos tiers vanilla) | 3.312 | −31 |
| ✅ `IForgeBlockEntity` → `IBlockEntityExtension` (7 arq) | 3.298 | −14 |
| ✅ `event.player.` → `event.getEntity().` (campo virou privado no bus novo; 4 arq) | 3.254 | −44 |
| ✅ `ItemStack.getTag()` → `getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()` (9 arq) | 3.236 | −18 |
| ✅ `Optional.resolve().get()` (artefato do patch de LazyOptional) → `.get()` (36 arq) | 3.188 | −48 |
| ✅ `ItemStack.getEnchantmentLevel(...)` REMOVIDO → `EnchantmentHelper.getEnchantmentLevel(Holder, LivingEntity)` (caso a caso, 6 arq) | **3.181** | −7 |
| ✅ **SESSÃO 2026-10-12 (lotes 2-6)**: _fix2 (addAttributeModifier, readBlockPos, GlassBlock, GeckoLib, MAParticleType, ring_large, setLootTable) | 3.055 | −126 |
| ✅ _fix3+_fix4 (WorldRenderUtils Pose, ImageButton swap, GuiGuideBook casts, ArmorMaterial record) | 2.955 | −100 |
| ✅ _fix5 (damageItem, RecordItem, render→renderWidget ×9, AttributeModifier strings) | 2.931 | −24 |
| ✅ _fix6 (CodexBreadcrumb campo Type, csr decl, gate, casts Collection) | 2.903 | −28 |
| ✅ _fix7 (CommonEventHandler: ICancellableEvent no BlockEvent, Explosion.center, setCancellationResult indevido, event.player) + import MNAImageButton no Lodestar (subpacote) | 2.779 | −124 |
| ✅ **Remoção global dos casts `(Object)` do CFR** (1.351 casts em ~350 arq) | 2.466 | **−312** |
| ✅ _fix8 (save/load de ItemStack c/ HolderLookup.Provider via MNAItemData, ContainerHelper→helpers, saveAdditional/load→loadAdditional 2-arg, getUpdateTag(Provider), saveWithFullMetadata(Provider)) | 2.409 | −57 |
| ✅ _fix9 (capabilities: `MNACaps.itemHandler` novo, 10 getCapability mortos removidos, ForgeCapabilities→MNACaps, LazyOptional→java.util.Optional, imports da pkg removida) | 2.322 | −87 |
| ✅ LootTableHelper REESCRITO (LootDataManager→ReloadableServerRegistries, LootTableReference→NestedLootTable, ObfuscationReflectionHelper→reflexão pura) + _fix10 (loot functions/conditions: `Serializer` removido→MapCodec+TYPE, GlobalLootModifiers) | 2.268 | −54 |
| ✅ _fix11 (CommonEventHandler 53 err: ItemEntityPickupEvent.Pre, ItemStack.of→parseOptional, getEnchantmentLevel→Holder, MNADyeableItem estático, reflexão spinAttack/AreaEffectCloud) | 2.224 | −44 |
| ✅ **B19c** `EnchantmentBehaviors` (ResourceKey→comportamento) + EnchantmentInit reescrito (build+defs de itens/tags) + AuraEvent/Aura/classes de ench + RecipeInit (helpers type/serializer) | 2.138 | −86 |
| ✅ _fix13/_fix15 (efeitos: add/removeAttributeModifiers 1.21.1 + `IMNAEffectHook`/`MAEffectHooks` via MobEffectEvent; handleUpdateTag(Provider); Enchantments curto; RecipeHolder sites) | 2.097 | −41 |
| ✅ _fix16 (ContainerMagiciansWorkbench: RecipeBookMenu<CraftingInput,CraftingRecipe> + getRecipeFor/assemble novos; LargeContainer: `MNAMenuAccess` reflexão p/ campos private; CantripRegistry: TriConsumer method-refs + curePotionEffects→EffectCure) | 2.097 | −106 |
| ✅ _fix17 (BuiltInRegistries.ENCHANTMENT não existe → `MNAEnchantmentRegistry` (registryAccess do server) + overloads por ResourceKey no `MNAEnchantmentHelper`; ItemEldritchOrb/ManaweavingRecipe/EffectDisjunction/EnchantmentBehaviors migrados) | 1.937 | −160 |
| ✅ _fix18/_fix19 (allowedEnchantments → `ArrayList<ResourceKey<Enchantment>>` em MagicStaff/BoneScythe/FortuneRing/HellfireTrident/AstroBlade; RLoc `fromNamespaceAndPath`; renames 1.21.1 MOB_LOOTING→LOOTING, BLOCK_EFFICIENCY→EFFICIENCY, FISHING_SPEED→LURE, POWER_ARROWS→POWER; `getDefaultAttributeModifiers()` no-arg) | 1.881 | −56 |
| ✅ _fix20 (hurtAndBreak lambda→slot, `ResourceLocation(a,b)`→`fromNamespaceAndPath` ×15) | 1.852 | −29 |
| ✅ _fix20b/_fix21 (MNAItemData: reparo de linhas automutiladas + imports ×22; TooltipContext→`.level()` ×15; ClipContext null→cast ×16) | 1.809 | −43 |
| ✅ _fix22/_fix23 (FallingBlockEntity.blockState via `MNAFallingBlocks`; EffectHelper/LanternWraith/WitchHunter `Holder<MobEffect>`; `symbol: get()` → `value()`/holder direto ×11 arq) | 1.770 | −39 |
| ✅ _fix24 (ModelManager.getModel/RegisterAdditional → `ModelResourceLocation.standalone` ×10 arq) | 1.751 | −19 |
| ✅ _fix25 (SpawnPlacementTypes, `hasTag`→CUSTOM_DATA, `parseOptional.orElse`, `isAddedToLevel`, `CauldronInteraction.map().put`, mapas raw tipados, `enchantmentMap` tipado) | 1.683 | −68 |
| ✅ _fix26 (capabilities interface-typed: CapabilitiesInit+serializers+26 consumidores; `List<Cls>` getEntitiesOfClass; `super((AbstractContainerMenu))`; casts `(Comparable)/(Property)` em setValue; `handleNeighborChanged`) | 1.632 | −51 |
| ✅ _fix27 (Tesselator/BufferBuilder 1.21.1: `tesselator.begin`+`buildOrThrow`; `ParticleRenderType` anônimos `begin(Tesselator,TextureManager)`) | 1.598 | −34 |
| ✅ _fix28 (IShearable; RecipeHolder `.value()/.id()` nos 18 sites raw RecipeType; `AttributeModifier.id()`; `AdvancementHolder.id()`; RecipeUtil holder) | 1.560 | −38 |
| ✅ _fix29 (_isSameItemSameComponents_; `BuiltInRegistries.get`; `MATools.canChangeDimensions`; `setupOverlayRenderState`→RenderSystem; EntityInit register sem cast raw) | 1.529 | −31 |
| ✅ _fix30 (migração do sistema de receitas: 14 recipes `CraftingContainer`→`CraftingInput`, getRecipeFor com `asCraftInput`/`map(h->h.value())`, `AMRecipeBase.getResultItem(Provider)`, `SingleRecipeInput` p/ cooking) | 1.492 | −37 |
| ✅ _fix31 (`ICancellableEvent` nos eventos MNA; `setResult`→`setCanceled`; `broadcastBreakEvent`→EquipmentSlot; `doEnchantDamageEffects` no-op; `MNAItemData.getOrCreateTagElement`; `MATools.getTierLevel`; brewing via `RegisterBrewingRecipesEvent`) | 1.456 | −36 |
| ✅ _fix32 (`RenderGuiEvent.Pre`, `getWindow`→Minecraft, `dataSlots`→`MNAMenuAccess`, `readNbt`, `lookScale` decls, `ConstructRenderer.vertex`) | 1.402 | −54 |
| ✅ _fix33 (placeLiquid removido ×7, `MNAEnchantmentHelper.getBlockEfficiency`, `reloadableRegistries`, `getTooltipLines` 3-arg, Fluid var) | 1.378 | −24 |
| ✅ _fix34 (`MNAGoalSelectorAccess` availableGoals, DemonLord FQN CastSpellAtTargetGoal, `ConstructNodeEvaluator` mob.level(), provider) | 1.331 | −47 |
| ✅ _fix35 (inner class raw outer fix, `stack.set` statement, `MNAItemData.setPotionContents`, `EVENT_BUS.post` cast, EffectDisjunction maps Holder) | 1.302 | −29 |
| ✅ _fix36/_fix37 (`PoseStack.Pose` nos helpers addVertex — 15 renderers + WorldRenderUtils; transformPosition p/ helpers sem normal; OPTIONAL_STREAM_CODEC; wrapAsHolder; getDamageBonus no-op) | 1.284 | −18 |
| ✅ _fix38 (`canPlaceLiquid` body, `MNAEnchantmentHelper.getKnockbackBonus`, toArray casts, BlockInit `.value()/.get()`, moodlets) | 1.244 | −40 |
| ✅ _fix39 (`MNAGoalSelectorAccess.getRunningGoals`, `getFeetBlockState`→`getBlockStateOn`, dimensions→`getDimensions(getPose())`) | 1.238 | −6 |
| ✅ _fix40/_fix41 (EnchantmentDefinition FQN, byKey holder `.value()`, pouch MNACap.orElse, mixin setReturnValue, loot codecs `commonFields.and(...)`, for-loops Entity cast) | 1.209 | −29 |
| ✅ _fix42 (`MNAPoseStackAccess` reflexão, AttributeModifier UUID→RLoc, `hasModifier(.id())`, STEP_HEIGHT, transformPosition Vector3f, writeItem codec, PortalRenderer) | 1.178 | −31 |
| ✅ _fix43 (getFireAspect helper, `getSkin().texture()`, setRenderSelection removido, renderList→renderWidget) | 1.159 | −19 |
| ✅ _fix44 (ObjectSelectionList 1.21.1: self-ref qualificado, super 5-args, casts raw; `MNAItemData.setComponent` genérico — 40 sites) | **1.116** | −43 |
| **➜ ATUAL** | **1.116** | **−45.824 desde o início (46.940)** |

### 🔴 Bug de medição que quase passou (2026-10-11)
Ao migrar `getEnchantmentLevel`, um `.replace()` removeu o `(` de abertura de
`((boots = ...))` → **3 errors** (medição FALSA: javac aborta cedo e não analisa
o resto). Após corrigir a sintaxe: **3.181**. **Regra: número muito baixo não é
ganho — é javac abortado.** Sempre verificaro log com `grep -B1 -A6 'error:'`.

### Bridges criadas no port (reutilizáveis)
| Bridge | Substitui | Onde |
|---|---|---|
| `com.mna.gui.widgets.MNAImageButton` | `ImageButton` (UV/textura) | 40 call-sites |
| `com.mna.api.tools.MNADyeableItem` | `DyeableLeatherItem` | DataComponents.DYED_COLOR |
| `com.mna.api.tools.MNAtierSorting` | `TierSortingRegistry` | ordem de tiers vanilla |
| `com.mna.api.enchantments.MAEnchantmentBridge` | `EnchantmentCategory`/`Rarity` | definition/build |
| `com.mna.api.enchantments.MNAEnchantmentHelper` | `ItemStack.getEnchantmentLevel` | static + Holder |
| `com.mna.api.capabilities.CurioItemCapability` | Curios `ICurio` | `ICurioItem` |

### Notas de medição
- `*` O salto de 4.353→4.432 foi um artefato: a regeneração do SFX corrigiu
  classes que antes não compilavam e portanto **escondiam** erros dos
  consumidores. O total real continuou caindo monotonicamente.
- Builds com **2 erros** são medições falsas: javac aborta cedo e não analisa
  o resto. Sempre confirmar com clean compile completo.


### 🔴 ACHADOS CRÍTICOS DE RUNTIME (não aparecem como erro de compilação)
1. **`MAEffects` tinha TODAS as 62 constantes = `null`** (era `@ObjectHolder`, removido no 1.21.1).
   Compilaria normal e daria **NPE em jogo** ao usar efeito nenhum. Corrigido.
2. **Snapshot**: `_snapshots/snap-20261011-031901-4367erros/` — restaurar com `cp -r`.

### ✅ Fase "classes @ObjectHolder quebradas" — 5 classes, 345 campos
O `@ObjectHolder` foi removido no 1.21.1. Todas essas classes tinham campos `= null`
(compilavam, mas crashariam em jogo). Reconstruídas:
- `SFX.java` — **174/174 campos** (`SoundEvent`). Mapa extraído do **Vineflower**
  (preserva `@ObjectHolder(value=...)`) e validado contra `assets/mna/sounds.json`.
  `ForAffinity` preservado. API pública intacta.
- `MAEffects.java` — 62 (→ `EffectInit`)
- `Components.java` — 121 (→ `Registries.SpellEffect`, 119 mapeados; 2 não existem mais)
- `Shapes.java` — 20 (→ `Registries.Shape`)
- `Modifiers.java` — 8 (→ `Registries.Modifier`)
- `Enchantments.java` — 22 (→ `EnchantmentInit`)
**Resultado: 4.422 erros.** Snapshot: `_snapshots/snap-20261011-035230-4432erros/`

> Método reutilizável: `java -jar vineflower.jar --only=<pkg> jar.jar out/`
> preserva `@ObjectHolder`, ao contrário do CFR. Use para qualquer outra classe
> que esteja com campos nulos.

---

## ✅ REVISÃO DA LISTA (2026-10-12) — o que os blocos antigos viraram

Verificação por `grep` no `_build_last.log` do build atual (1.881 erros):

| Bloco | Status | Evidência |
|---|---|---|
| B1 x0/y0/x1/y1 | ✅ | 0 erros de x0/y0 restantes |
| B2 `new ResourceLocation(a,b)` | ⚠️ 15 restantes | `ResourceLocation(String,String) has private access` = 15 |
| B3 `ModelResourceLocation` | ⚠️ 19 restantes (era 156) | grep = 19 |
| B4 `Matrix3f`→`Pose` | ⚠️ 15 restantes (era 96) | `Matrix3f cannot be converted to Pose` = 15 |
| B5 `addRenderableWidget` | ✅ 0 | grep = 0 |
| B6 imports `BuiltInRegistries` | ✅ 0 | |
| B7 `MobEffect`↔Holder | ✅ base feita | 14 residuais de `Holder<MobEffect>` |
| B8/B19 encantamentos | ✅ feito (B19a+b+c + `MNAEnchantmentRegistry`) | ver B19 |
| B9 NBT de ItemStack | ✅ 0 | |
| B10 capabilities/LazyOptional | ✅ 0 | |
| B11 networking | ✅ NetworkHooks 0 / StreamCodec 0 | |
| B12 `IForgeMenuType` | ✅ 0 | |
| B13 `ForgeCapabilities` | ✅ (→ `MNACaps`) | |
| B14 misc | ✅ PotionUtils 0 / RenderGuiOverlayEvent 0 / DyeableLeatherItem migrado | |
| B15 genéricos | 🔄 em andamento | inference T = 90, Object→X = 77, method refs = 37 |
| B16 pipeline de dano | 🔄 10 restantes (era 109) | `DamageEventHandler.java` = 10 |
| B17 `EntityInit` builders | 🔄 12 restantes (era 73) | grep = 12 |
| B18 codecs | ✅ StreamCodec 0 | |
| B20 Curios 9.x | ✅ | |
| B21 eventos removidos | ⚠️ features desativadas (jump/visibility/sleep) — reimplementar via mixin | |
| B22 `Event.HasResult` | ✅ | |
| B23 atributos | ✅ | |
| B24 `DyeableLeatherItem` | ✅ | |
| B25–B41 (lotes 2–6) | ✅ | ver tabela de Progresso |

---

## ✅ FILA ATUAL DE ERROS — **VAZIA** (build final = **0 erros**, 2026-10-12)

Todos os itens abaixo (registros históricos dos builds 1.116–1.456 e anteriores)
foram resolvidos até o build final com **0 erros / exit=0**. Seção mantida como
histórico de diagnóstico; o que ainda falta é a verificação de runtime (seção final).

**Regra (histórica):** sempre que um lote for aplicado, re-verificar esta seção com
`grep 'error:' _build_last.log` e ATUALIZAR as contagens — nada sai daqui sem
evidência de queda no build.

**Caiu nesta rodada (✓ resolvidos):** `new ResourceLocation(a,b)` privado (15→0), `EquipmentSlot is not a functional interface` (14→0), `symbol: MNAItemData` (14→0), `TooltipContext cannot be converted` (15→0), `ClipContext is ambiguous` (15→0), `blockState has private access in FallingBlockEntity` (14→0), `Holder<MobEffect> cannot be converted` (14→0), `symbol: get()` (18→0), `ResourceLocation cannot be converted` (19→0), SpawnPlacements.Type (13→0), `hasTag()` (13→0), `parseOptional.orElse` (12→0), `CauldronInteraction.put` (13→0), `symbol: getValue(ResourceLocation)` (12→0), `isSameItemSameTags` (12→0), `IForgeShearable` (13→0), `RecipeHolder<CAP#1>` nos streams (16→0 via `.value()`), `assemble/matches(CraftingContainer)` (14 recipes → CraftingInput), `canChangeDimensions()` (11→0), `setupOverlayRenderState` (12→0), `getRecipeFor(RecipeType,Container,Level)` (11→0), `doEnchantDamageEffects` (10→0), `broadcastBreakEvent(InteractionHand)` (12→0), `getOrCreateTagElement` (11→0), `addRecipe(IBrewingRecipe)` (10→0), `isCanceled()` em eventos MNA (10→0 via ICancellableEvent), `getLevel()` em Tier (4→0).

### Maiores arquivos (por contagem — build 1.456)
- [ ] `DemonLord.java` (29)
- [ ] `Construct.java` (28)
- [ ] `GuiOcculus.java` (27)
- [ ] `BlockUtils.java` (19)
- [ ] `ConstructNodeEvaluator.java` (18)
- [ ] `GuiLodestarV2.java` (17)
- [ ] `FaerieQueen.java` (17)
- [ ] `GuiGuideBook.java` (15)

### Padrões (prioridade de lote; contagens do build 1.456)
- [ ] `inference variable T has incompatible equality constraints` (65) — chamadas `MNACap.get(...)`/holders em tipos concretos; analisar por arquivo
- [ ] `invalid method reference` (37) — method-refs em interfaces raw (padrão `(TriConsumer)((TriConsumer)X::m)`)
- [ ] `bad return type in lambda` (15)
- [ ] `symbol: level` (15) — vars renomeadas (`world`→`level`) em contextos migrados
- [ ] `symbol: entity` (13) — idem
- [ ] `Matrix3f cannot be converted to Pose` (13) — B4 residual (WorldRenderUtils)
- [ ] `AbstractContainerMenu cannot be converted` (12) — resíduos de menus
- [ ] `MobEffect cannot be converted` (11) — mais sites MobEffect↔Holder
- [ ] `Object cannot be converted to Enum` (11)
- [ ] `dataSlots has private access in AbstractContainerMenu` (10) — usar `MNAMenuAccess`
- [ ] `MACapabilityForgeEventHandlers` residual — verificar
- [ ] `EntityInit` — verificar restos de register
- [ ] `ModelResourceLocation` (B3 residual) — verificar

---

## ⚠️ VERIFICAÇÃO DE RUNTIME PENDENTE (compila; ainda não testado em jogo)

- [ ] `MAEffectHooks`/`IMNAEffectHook` — hooks via `MobEffectEvent.Added/Remove/Expired`; validar ordem e casos (morte do mob, `/effect clear`)
- [ ] `MNAEnchantmentRegistry` — depende de server ativo (retorna `null` no cliente cedo); auditar chamadas em GUI/render
- [ ] **Enchants do MNA via `MAEnchantmentBridge.build()` têm `DataComponentMap.EMPTY`** → efeitos vanilla (dano/durabilidade/etc.) NÃO aplicam sozinhos; a lógica custom vive em `EnchantmentBehaviors`. Revisar quais precisam de `DataComponents.ENCHANTMENT_EFFECTS`
- [ ] `MNAMenuAccess` / `LootTableHelper` — reflexão em campos `private` do vanilla; validar em runtime
- [ ] Tiles com `getCapability` removido (FluidJugTile, ManaweavingAltarTile, InfiniteFluidCapabilityHandler) — expor handlers via `RegisterCapabilitiesEvent` (NeoForge)
- [ ] `MNACaps.itemHandler` — validar com baús/máquinas do MNA em jogo
- [ ] B16 pipeline de dano — validar reduções (escudo/armadura) com `LivingDamageEvent.Pre/Post`
- [ ] B21 eventos removidos — features desativadas (jump boost/camuflagem/sleep); mixin se necessário
- [ ] `MAEnchantmentBase`/`MADamageEnchantmentBase` são marcadores vazios — decidir se reimplementar via data
- [ ] `_snapshots/` fica fora do git (pesado) — o histórico do repo remoto cumpre o papel de ponto de retorno

> **COMO MANTER ESTA LISTA VIVA (pedido do usuário):** ao encontrar qualquer erro novo
> no caminho, adicionar aqui (arquivo/padrão + contagem) ANTES de seguir para o próximo
> lote, e re-checar as contagens ao final de cada lote. A tabela de Progresso registra
> o que já caiu; esta seção registra o que falta.

---

## 🚀 FASE DE RUNTIME (em jogo) — fixes encontrados nos launches

Estado: **o jogo ABRE até a tela inicial e CARREGA MUNDO** com `mna + curios 9.5.1 + geckolib 4.9.3` (paridade com a pasta mods do Source 1.20.1: curios-forge-5.14.1+1.20.1 / geckolib-forge-1.20.1-4.7 / mna-forge-1.20.1-3.1.11-all).

Fixes aplicados (ordem de descoberta):

1. **[FEITO] Mixins não aplicados** — NeoForge 21.1 ignora `MixinConfigs` do MANIFEST; adicionar `[[mixins]]` no `neoforge.mods.toml`. (verificar se a seção está presente no toml final)
2. **[FEITO] Mods no classpath** — removidos de `mna_cp.txt` (FML pulava como "already located earlier").
3. **[FEITO] `canEnterPose` removido no 1.21.1** — `PlayerPoseMixin` (Player.canPlayerFitWithinBlocksAndEntitiesWhen).
4. **[FEITO] `@EventBusSubscriber` sem handlers próprios** — removidas de 7 classes (NeoForge 21.1 lança no register).
5. **[FEITO] `EffectChill` UUID com maiúsculas** → lowercase (ResourceLocation inválido).
6. **[FEITO] Shims `@ObjectHolder` → nulos em runtime** (NeoForge removeu @ObjectHolder):
   - `Shapes`/`Components`/`Modifiers`: `ResourceLocation.parse("...")` gerava **`minecraft:`** (namespace errado) → trocado por `RLoc.create` (147 lookups). Sintoma: `DemonImp` clinit → `SpellRecipe.addComponent(part=null)` NPE.
   - `Components.FOSSILIZE` → aponta p/ `components/fortification` (nome real registrado); `CANNON` = stub morto (nunca registrado, sem usos — mantido null).
   - `Music.Construct.DANCE_MIX` (era @ObjectHolder `mna:music_big_car_theft`) → instância criada direto. Sintoma: NPE em todo som (`SoundEventListener.onPlaySound`).
   - `MAEffects`/`SFX` já tinham sido migrados para DeferredHolder/instâncias diretas ✓.
7. **[FEITO] Registros tardios (Forge 1.20.1 permitia; NeoForge congela)**:
   - `GlobalLootModifiers` (10 loot functions) — `FMLCommonSetupEvent` → `RegisterEvent`.
   - `CustomAdvancementTriggers` (29 triggers) — idem; e namespace: registrar como **`mna:<nome>`** (os advancement JSONs usam `mna:affinity_tinker` etc.).
8. **[FEITO] Modelos `"loader": "forge:obj/composite"`** → `neoforge:` (807 JSONs).
9. **[FEITO] Shader `portal_uv_rotate_vert.vsh`** — `fog_distance(ModelViewMat, pos, shape)` (3 args, 1.20.1) → `fog_distance(pos, FogShape)` (2 args, 1.21.1).
10. **[FEITO] Biome modifiers `"type": "c:add_features/add_spawns"`** → `neoforge:add_features/add_spawns` (24 JSONs). Sintoma: "Failed to load registries due to above errors" → crash ao abrir QUALQUER tela.
11. **[FEITO] `CapabilitiesInit.ATTACHMENT_TYPES` não registrado no mod bus** → "Trying to access unbound value: mna:world_magic" no load do mundo. Registrado em `ManaAndArtifice.java` junto aos demais DeferredRegisters (auditados os 18: só ele faltava).

Pendências de runtime (a verificar nos próximos launches):
- [ ] Curios: renderers (CurioRenderer/StaffRenderer/WandRenderer/HatRenderer/BookRenderer) em entidades com itens equipados
- [ ] GeckoLib: modelos/animations de entidades MNA em jogo (log dizia "Loaded 0 entity animations" no boot — normal? confirmar animação de um construct/imp)
- [ ] Capabilities de tiles (FluidJugTile etc.) e baús (MNACaps.itemHandler)
- [ ] Spells/casting em jogo (Shape/Component shims agora resolvem — validar cast real)
- [ ] Dimensão Reverie + estruturas/features do MNA na geração de mundo
- [ ] Mixins (`[[mixins]]` do toml) aplicando de fato (verificar log "mna.mixins.json" no startup)
