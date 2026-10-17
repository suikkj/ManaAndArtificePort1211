# Mudanças de Versão 1.20.1 (Forge 47) → 1.21.1 (NeoForge 21.1)

Documento de referência técnica para o port do **Mana and Artifice 3.1.11**.
Escopo: o que quebra um port grande, com **o que mudou / impacto / como corrigir** e fontes.

> **Status das confirmações:** os itens que você já descobriu empiricamente foram
> **todos confirmados** por fontes oficiais (§1.1, §2.1, §2.3, §2.5, §3.1).
> **Uma correção importante:** `item_modifiers/` **também** vai para o singular
> (`item_modifier/`). Só `loot_modifiers/` fica plural — e ele nem é pasta de
> datapack vanilla, é pasta do **Global Loot Modifier do NeoForge**, que nunca
> passou pela depluralização (ver §1.1 e §1.2).

---

## Índice

1. [Mudanças de datapack / data-driven](#1-mudanças-de-datapack--data-driven)
2. [Mudanças de código / API](#2-mudanças-de-código--api)
3. [Mudanças de render](#3-mudanças-de-render)
4. [Mixin / MixinExtras / Access Transformer](#4-mixin--mixinextras--access-transformer)
5. [GeckoLib 4.7 → 4.9 e Curios 5.x → 9.x](#5-geckolib-47--49-e-curios-5x--9x)
6. [Gotchas de migração da comunidade](#6-gotchas-de-migração-da-comunidade)
7. [Checklist de maior risco para o MNA](#7-checklist-de-maior-risco-para-o-mna)
8. [Fontes](#8-fontes)

---

## 1. Mudanças de datapack / data-driven

### 1.1 Depluralização de pastas (`pack_format` 41 → **48**) — ⚠️ CORREÇÃO

**O que mudou.** A snapshot 24w21a (1.21, `pack_format` 48) renomeou **todas** as
pastas de registry de datapack de plural para singular. Não existe aliasing
automático: uma pasta plural num pack 1.21 é **silenciosamente ignorada**.

**Impacto.** Falha silenciosa — receitas/loot/advancements simplesmente não carregam,
sem erro no log. É a quebra de migração mais comum.

**Como corrigir.**

| ≤ 1.20.6 (plural) | 1.21.1 (singular) |
|---|---|
| `recipes/` | `recipe/` |
| `advancements/` | `advancement/` |
| `loot_tables/` | `loot_table/` |
| `structures/` | `structure/` |
| `predicates/` | `predicate/` |
| **`item_modifiers/`** | **`item_modifier/`** |
| `functions/` | `function/` |
| `tags/functions/` | `tags/function/` |
| `tags/items/` | `tags/item/` |
| `tags/blocks/` | `tags/block/` |
| `tags/entity_types/` | `tags/entity_type/` |
| `tags/fluids/` | `tags/fluid/` |
| `tags/game_events/` | `tags/game_event/` |
| `tags/damage_types/` | `tags/damage_type/` |
| `tags/worldgen/biomes/` | `tags/worldgen/biome/` |
| `tags/worldgen/structures/` | `tags/worldgen/structure/` |
| `tags/worldgen/world_presets/` | `tags/worldgen/world_preset/` |
| `tags/worldgen/flat_level_generator_presets/` | `tags/worldgen/flat_level_generator_preset/` |
| `tags/painting_variants/` | `tags/painting_variant/` |
| `tags/banner_patterns/` | `tags/banner_pattern/` |
| `tags/cat_variants/` | `tags/cat_variant/` |
| `tags/` (a pasta `tags` em si) | **continua plural** |

Nota: `dimension/`, `dimension_type/` e `worldgen/*` já eram singulares antes.

**Exceções (não renomear):**
- `loot_modifiers/` → **continua plural** (é a pasta do GLM do NeoForge, §1.2).
- `neoforge/biome_modifier/` → já era singular.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/),
[NeoForge 21.0 release](https://neoforged.net/news/21.0release),
[Fabric 1.21 post](https://fabricmc.net/2024/05/31/121.html),
[Datapack Codex](https://elkonia.com/datapack_codex/pack-format.html),
[Minecraft Wiki data pack format](https://minecraft.wiki/w/Template:Data_pack_format).

---

### 1.2 Biome modifiers e Global Loot Modifiers (namespace)

**O que mudou.** Biome modifiers vivem em `data/<modid>/neoforge/biome_modifier/<path>.json`
e GLMs em `data/<modid>/loot_modifiers/<path>.json`, com um índice
`data/neoforge/loot_modifiers/global_loot_modifiers.json`.

**Impacto.** Pastas `neoforge/` e `loot_modifiers/` são **PLURAIS e ficam assim** —
não aplicar a depluralização nelas. Seu `_audit/00_inventario.md` já registrou
`loot_modifiers` (11 arquivos, plural) e `neoforge/` (24 biome modifiers): **correto**.

**Como corrigir.** Confirmado pelo código de exemplo oficial:
`data/examplemod/loot_modifiers/example_glm_1.json`. Nenhuma ação se já estiver plural.

Fontes: [NeoForged Biome Modifiers](https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier),
[NeoForged GLM](https://docs.neoforged.net/docs/1.21.1/resources/server/loottables/glm).

---

### 1.3 `ItemStack` codec / serialização — ✅ confirmado

**O que mudou.** ItemStack não tem NBT cru desde 1.20.5; tem **data components**.
`ItemStack.save(CompoundTag)` deixa de existir → `ItemStack.save(HolderLookup.Provider)`
e `ItemStack.parseOptional(HolderLookup.Provider, CompoundTag)`.

**Gotchas específicos:**
- `ItemStack.save(registries, tag)` **NÃO modifica o tag passado** — use sempre o
  retorno, ou a sobrecarga que recebe só `registries`.
- Em receitas, `result` não é mais uma string: é `{"id": "mod:item"}`.
- Codecs de registries dinâmicos (enchantment, damage_type, etc.) exigem
  `RegistryOps`/`HolderLookup.Provider` — não rodam em `NbtOps.INSTANCE` puro.
- `ItemStack.getTag()/setTag()/getOrCreateTag()/hasTag()` **removidos** →
  `stack.get(DataComponents.X)` / `stack.set(...)` / `stack.has(DataComponents.CUSTOM_DATA)`.
- `isSameItemSameTags` → `isSameItemSameComponents`.

**Como corrigir.** Threading de `HolderLookup.Provider` por toda a cadeia de
save/load (`BlockEntity.saveAdditional`, `Recipe.assemble`, datagen `saveStable`).
Construir contexto com `registries.createSerializationContext(NbtOps.INSTANCE)`.

Fontes: [NeoForge 20.5 release](https://neoforged.net/news/20.5release/),
[NeoForged primer 1.20.5](https://docs.neoforged.net/primer/docs/1.20.5/),
[NeoForged Data Components](https://docs.neoforged.net/docs/1.21.1/items/datacomponents).

---

### 1.4 Ingredient / receitas data-driven — ✅ confirmado

**O que mudou.**
- `Recipe<T>` passa a receber `RecipeInput` em vez de `Container`. `Container`
  → `RecipeInput` (substitui o genérico `C` por `T`).
- Novas impls: `CraftingInput` (grid w×h), `SingleRecipeInput` (1 slot).
- `RecipeHolder`/`RecipeManager` também genericamente trocados.
- `Recipe.assemble(CraftingInput, RegistryAccess)` → `assemble(CraftingInput, HolderLookup.Provider)`.
- Serializers passam a expor **`MapCodec`** (não `Codec`); `Recipe#codec()` devolve `MapCodec`.
- `IShapedRecipe` **removido** → usar `ShapedRecipe` vanilla; `getRecipeWidth()` → `getWidth()`.
- Construtor de `CustomRecipe` **perdeu o parâmetro `ResourceLocation`**:
  `super(resourceLocation, category)` → `super(category)`.
- `SimpleCraftingRecipeSerializer(Foo::new)` → `SimpleCraftingRecipeSerializer<>(Foo::new)`
  (diamond necessário para inferência).

**Impacto no MNA.** 51 serializers custom em `com/mna/recipes/` + 1796 receitas.
Cada serializer precisa de `codec()` em vez de `fromJson`, e cada classe precisa
mudar o genérico e o construtor.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/),
[NeoForged recipes](https://docs.neoforged.net/docs/1.21.1/resources/server/recipes/).

---

### 1.5 Advancement triggers / criteria

**O que mudou.**
- `AbstractCriterionTriggerInstance` → `SimpleCriterionTrigger.SimpleInstance`.
- Triggers passam a ser **codec-driven**: `DeserializationContext` removido.
- `FrameType` → `AdvancementType`.
- `RequirementsStrategy` → `AdvancementRequirements.Strategy`.
- `CommandFunction.CacheableFunction.NONE` → `Optional.empty()`.
- `CommandFunction` movido para `net.minecraft.commands.functions`.
- Datagen: `Consumer<Advancement>` → `Consumer<AdvancementHolder>`.
- Runtime: `getAdvancements().getAdvancement(x)` → `getAdvancements().get(x)` devolvendo `AdvancementHolder`.
- `EnchantmentPredicate` agora recebe `HolderSet<Enchantment>` (construtor antigo
  ainda existe como overload).
- `EntityPredicate` ganhou *movement predicate* e *periodic tick*; `location`/`steppingOnLocation`
  foram para `$LocationWrapper`.
- Loot context renomes: `KILLER`→`ATTACKER`, `DIRECT_KILLER`→`DIRECT_ATTACKER`,
  `KILLER_PLAYER`→`ATTACKING_PLAYER` (também `LootContextParams.KILLER_ENTITY`→`ATTACKING_ENTITY`,
  `CopyNameFunction.NameSource.KILLER`→`ATTACKING_ENTITY`).

**Impacto no MNA.** 33 arquivos em `com/mna/advancements/` com **29 triggers custom** —
a maior mudança estrutural do setor.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/),
[NeoForged advancements](https://docs.neoforged.net/docs/1.21.1/resources/server/advancements).

---

### 1.6 Loot tables / loot functions

**O que mudou.**
- Pasta `loot_tables/` → `loot_table/` (§1.1).
- `LootTableReference` → `NestedLootTable`.
- `CopyNbtFunction` → `CopyCustomDataFunction`; copy de NBT de bloco/custom data
  agora usa componentes (`copy_custom_data`, `copy_components`).
- `LootItemRandomChanceWithLootingCondition` → `LootItemRandomChanceWithEnchantedBonusCondition`
  (e ganhou parâmetro de registro em datagen).
- `LootItemConditions#TYPED_CODEC/DIRECT_CODEC/CODEC` → `LootItemCondition`.
- Datagen: `BlockLootSubProvider`/`EntityLootSubProvider` ganham `HolderLookup.Provider`;
  `HAS_SILK_TOUCH` → `hasSilkTouch()`, etc. (constantes viram métodos de instância).
- `LootTableProvider$SubProviderEntry` passa a receber função que dá `HolderLookup.Provider`.
- Item modifiers: `looting_enchant` → `enchanted_count_increase` (+ campo `enchantment`);
  `enchant_randomly.enchantments` → `options`; `copy_name.killer`→`attacking_entity`.

**Impacto no MNA.** 15 arquivos em `com/mna/loot/` + 393 loot tables + 11 GLMs.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/),
[Datapack Wiki breaking changes](https://datapack.wiki/wiki/info/breaking-changes).

---

### 1.7 Worldgen / structures / templates

**O que mudou.**
- `structures/` → `structure/` (§1.1).
- `JigsawPlacement.addPieces` ganhou **dois** parâmetros novos: `DimensionPadding` e `LiquidSettings`.
- `SinglePoolElement` ganhou `Optional<LiquidSettings>`; `StructurePoolElement` e
  `JigsawStructure` também recebem `LiquidSettings`.
- `PoolElementStructurePiece` ganhou `LiquidSettings`.
- `StructurePlaceSettings.shouldKeepLiquids` → `shouldApplyWaterlogging`.
- `StructureTemplateManager.getPathToGeneratedStructure`/`createPathToStructure`
  → `createAndValidatePathToGeneratedStructure`.
- `net.minecraft.data.worldgen.Structures#structure` **removido**.
- `Structure$StructureSettings` ganhou construtor com defaults dado biome tag + `$Builder`.
- Chunk generation **reorganizada**: lógica movida para `ChunkGenerationTask`,
  `GeneratingChunkMap`, `GenerationChunkHolder`, `net.minecraft.world.level.chunk.status.*`,
  e um `ChunkPyramid` de etapas com `ChunkDependencies`.
- `ChunkGenerator.createBiomes`/`fillFromNoise` **perderam o `Executor`**;
  `ChunkGenerator.codec()` passa a devolver **`MapCodec`**.
- `net.minecraft.world.level.chunk.ChunkStatus` → `net.minecraft.world.level.chunk.status.ChunkStatus`.
- Novos: `FixedPlacement`, `EndPlatformFeature`, `StaticCache2D`.
- `Registries.elementsDirPath` / `tagsDirPath` (substituem `TagManager#getTagDir`).
- `LootDataType` não recebe mais diretório — deriva do `ResourceKey`.

**Impacto no MNA.** 61 structures + 66 worldgen. Mixins em worldgen e qualquer
código que iterava `StructureSettings`/chunk status quebram.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/).

---

### 1.8 Enchantments e Paintings viram datapack — ✅ confirmado

**O que mudou.** `Enchantment` deixa de ser registry estático e passa a registry de
datapack (`data/<ns>/enchantment/<path>.json`). `PaintingVariant` idem. `JukeboxSong`
também é registry de datapack.

**Impacto.** `DeferredHolder.get()` em cima de enchantment **lança** — o registro
congela antes. `Enchantments.X` passou a ser `ResourceKey<Enchantment>`, e tudo
que recebia `Enchantment` agora recebe `Holder<Enchantment>`.

**Como corrigir.** Remover o registro Java de enchantments; criar os JSON de datapack.
Para lookup: `CommonHooks.resolveLookup(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.X)`.
Para comparar: `holder.is(ResourceKey)` (não `==`).
Métodos afetados: `ItemStack.getEnchantmentLevel(Holder)`, `ItemStack.enchant(Holder, int)`,
`new EnchantmentInstance(Holder, int)`, `Item.canApplyAtEnchantingTable` →
`supportsEnchantment(ItemStack, Holder<Enchantment>)`, `MobEffectInstance.getEffect()`
devolve `Holder<MobEffect>`, `LivingEntity.hasEffect/getEffect(Holder)`.
Para lógica de efeito, use `EnchantmentEffectComponents` em vez de checar nível direto.
Removidos: `EnchantmentCategory`, `Entity#doEnchantDamageEffects`,
`AbstractArrow#setEnchantmentEffectsFromEntity`.

Fontes: [NeoForge 21.0 release](https://neoforged.net/news/21.0release),
[NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/),
[NeoForged enchantments](https://docs.neoforged.net/docs/1.21.1/resources/server/enchantments/).

---

## 2. Mudanças de código / API

### 2.1 Registries: `RegistryObject` → `DeferredHolder`, `ForgeRegistries` → vanilla — ✅ confirmado

**O que mudou.**
- `RegistryObject<T>` → **`DeferredHolder<R, T extends R>`** (2 parâmetros!). É a
  mudança de tipo mais mecânica e mais volumosa do port.
  - `RegistryObject<Block>` → `DeferredHolder<Block, ? extends Block>`
  - `RegistryObject<Item>` → `DeferredHolder<Item, ? extends Item>`
  - `RegistryObject<EntityType<Foo>>` → `DeferredHolder<EntityType<?>, EntityType<Foo>>`
  - `RegistryObject<BlockEntityType<Foo>>` → `DeferredHolder<BlockEntityType<?>, BlockEntityType<Foo>>`
  - `RegistryObject<RecipeType<Foo>>` → `DeferredHolder<RecipeType<?>, RecipeType<Foo>>`
  - `RegistryObject<FlowingFluid>` → `DeferredHolder<Fluid, FlowingFluid>`
  - `RegistryObject<SimpleParticleType>` → `DeferredHolder<ParticleType<?>, SimpleParticleType>`
- `ForgeRegistries.ITEMS` → `BuiltInRegistries.ITEM`; `.BLOCKS` → `.BLOCK`;
  `.ENTITY_TYPES` → `.ENTITY_TYPE`; `.ENCHANTMENTS` → `.ENCHANTMENT`;
  `.MOB_EFFECTS` → `.MOB_EFFECT`; `.MENU_TYPES` → `.MENU`; `.RECIPE_TYPES` → `.RECIPE_TYPE`;
  `.RECIPE_SERIALIZERS` → `.RECIPE_SERIALIZER`; `.CREATIVE_MODE_TABS` → `.CREATIVE_MODE_TAB`;
  `.ATTRIBUTES` → `.ATTRIBUTE`; `.PAINTING_VARIANTS` → `.PAINTING_VARIANT`;
  `.POTIONS` → `.POTION`; `.POI_TYPES` → `.POINT_OF_INTEREST_TYPE`;
  `.VILLAGER_PROFESSIONS` → `.VILLAGER_PROFESSION`; `.WORLD_CARVERS` → `.CARVER`.
- Registries NeoForge-only ficam em `NeoForgeRegistries` (`FLUID_TYPES`,
  `BIOME_MODIFIER_SERIALIZERS`, `GLOBAL_LOOT_MODIFIER_SERIALIZERS`,
  `ENTITY_DATA_SERIALIZERS`, `ATTACHMENT_TYPES`).
- Para `DeferredRegister`: `DeferredRegister.create(Registries.ITEM, "modid")` (chave de registry).
- `Registry.getValue(x)` → `Registry.get(x)`; `BuiltInRegistries.X.getValues()` →
  `X.stream().toList()`; `IForgeRegistry<T>` → `Registry<T>`.
- `MissingMappingsEvent` **removido** → usar `DeferredRegister.addAlias()`.
- Registros congelam antes do common setup: registrar via `RegisterEvent`
  (mod bus) ou `DeferredRegister` ligado ao mod bus.
- Registries custom: `NewRegistryEvent`; registries de datapack: `DataPackRegistryEvent.NewRegistry`.

**Gotchas de tipo que quebram silenciosamente:**
- `DeferredHolder` **não** é `ItemLike`: chamadas como `event.accept(holder)` viram
  `event.accept(holder.get())`. Vale para `BuildCreativeModeTabContentsEvent.accept`,
  `new BucketItem(holder)`, `new LiquidBlock(holder)`.
- Acesso a campos do holder precisa `.value()`: `biome.get().getBaseTemperature()` →
  `biome.value().getBaseTemperature()`; `SoundEvents.X.get()` → `SoundEvents.X.value()`.
- `DeferredRegister.register("name", Foo::new)` para blocos/itens pode precisar de
  lambda explícita `() -> new Foo()` para o NeoForge escolher a sobrecarga correta.

Fontes: [NeoForged registries](https://docs.neoforged.net/docs/1.21.1/concepts/registries),
[NeoForge 21.0 release](https://neoforged.net/news/21.0release).

---

### 2.2 Capabilities → Attachments — ✅ confirmado

**O que mudou.**
- `Capabilities.ITEM_HANDLER` → `Capabilities.ItemHandler.BLOCK`;
  `Capabilities.FLUID_HANDLER` → `Capabilities.FluidHandler.BLOCK`;
  `Capabilities.FLUID_HANDLER_ITEM` → `Capabilities.FluidHandler.ITEM`.
- Pacote: `net.neoforged.neoforge.common.capabilities` → `net.neoforged.neoforge.capabilities`.
- Registro via `RegisterCapabilitiesEvent` (mod bus), não mais override de capability.
- `ForgeCapabilities` → `Capabilities`.
- **Data attachments** para dados livres: `AttachmentType` registrado em
  `NeoForgeRegistries.ATTACHMENT_TYPES`, com `serializable(...)` (NBT) ou
  `.serialize(Codec)`, flag `copyOnDeath`, e leitura via `holder.getData(TYPE)`.
- Attachments de **ItemStack** foram substituídos por **data components**.
- `EmptyHandler` → `EmptyItemHandler.INSTANCE`.
- `LazyOptional` → em geral removido junto com as capabilities.

**Impacto no MNA.** 37 arquivos em `com/mna/capabilities/`. Cada capability
precisa virar attachment (block entity/chunk/entity) ou data component (item).
O mod já usa mana/energia — provavelmente vários viram components.

Fontes: [NeoForged attachments](https://docs.neoforged.net/docs/1.21.1/datastorage/attachments),
[NeoForged capabilities](https://docs.neoforged.net/docs/1.21.1/inventories/capabilities).

---

### 2.3 Componentes de item (substituem NBT) — ✅ confirmado

**O que mudou.**
- `ItemStack.getTag()/setTag()/getOrCreateTag()/hasTag()` **removidos**.
  - `stack.hasTag()` → `stack.has(DataComponents.CUSTOM_DATA)`
  - `stack.getTag()` → `stack.get(DataComponents.CUSTOM_DATA)` etc.
  - `stack.setTag(t)` → `stack.set(DataComponents.X, valor)`
- `isSameItemSameTags` → `isSameItemSameComponents`.
- Componentes custom: registrar `DataComponentType` via
  `DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, modid)`,
  com `.persistent(Codec)` e/ou `.networkSynchronized(StreamCodec)`, opcionalmente
  `.cacheEncoding()`. Se não deve sincronizar, use `networkSynchronized(StreamCodec.unit(valor))`.
- **Componentes devem ser imutáveis** com `equals`/`hashCode` corretos — cópias de
  stack são *shallow* (compartilham a instância). NeoForge adiciona sanity check.
- Para alterar componentes de item vanilla/de outro mod: `ModifyDefaultComponentEvent` (mod bus).
- Defaults por item: `Item.Properties#component(...)`.
- Rede: `StreamCodec<RegistryFriendlyByteBuf, ItemStack>` para stacks.
- `copy_nbt` → `copy_custom_data` + `copy_components`; `set_nbt` dividido em
  `set_custom_data` e `set_components`.
- `CustomData.update`/`read` passaram a receber `DynamicOps<Tag>`.
- `ItemInput` passou a receber `DataComponentPatch` em vez de `DataComponentMap`.

**Impacto no MNA.** 219 itens. TODO NBT custom de item (mana em staffs,grimórios,
runas, progressão de varinhas) precisa virar `DataComponentType` — é a maior
mudança de item do port. **Nota:** migração de NBT→componente **não é automática**
e não há downgrade.

Fontes: [NeoForged datacomponents](https://docs.neoforged.net/docs/1.21.1/items/datacomponents),
[NeoForge 20.5 release](https://neoforged.net/news/20.5release/).

---

### 2.4 Eventos renomeados / reposicionados (loader)

**Pacotes e classes base:**
- `net.minecraftforge.*` → `net.neoforged.neoforge.*`
- `net.minecraftforge.fml.*` → `net.neoforged.fml.*`
- `net.minecraftforge.eventbus` → `net.neoforged.bus` / `net.neoforged.bus.api`
- `MinecraftForge` → `NeoForge`; `ForgeMod` → `NeoForgeMod`; `ForgeConfig` → `NeoForgeConfig`
- `ForgeConfigSpec` → `ModConfigSpec`
- `ForgeEventFactory` → `EventHooks`; `ForgeHooks` → `CommonHooks`; `ForgeHooksClient` → `ClientHooks`
- `ForgeI18n` → `I18nExtension`; `ForgeRenderTypes` → `NeoForgeRenderTypes`
- `ForgeChunkManager` → `ForcedChunkManager`; `ForgeFlowingFluid` → `BaseFlowingFluid`
- `ForgeSpawnEggItem` → `DeferredSpawnEggItem`; `ForgeTier` → `SimpleTier`
- `ForgeSoundType` → `DeferredSoundType`; `ForgeBiomeModifiers` → `BiomeModifiers`
- `ForgeBlockTagsProvider` → `IntrinsicHolderTagsProvider`; `ForgeAdvancementProvider` → `AdvancementProvider`
- `ClientForgeMod` → `ClientNeoForgeMod`; `ForgeGameTestHooks` → `GameTestHooks`
- `ForgeGui`/`ExtendedGui` → `net.minecraft.client.gui.Gui`
- `META-INF/mods.toml` → `META-INF/neoforge.mods.toml`
- `FMLJavaModLoadingContext#get` **removido** → usar o `IEventBus` injetado no
  construtor do `@Mod`; `ModLoadingContext.get().registerConfig(...)` →
  `getActiveContainer().registerConfig(...)`.
- `DistExecutor` **removido sem substituto** → `if (FMLLoader.getDist() == Dist.CLIENT)`
  ou entrypoints separados.

**Anotações:**
- `@Mod.EventBusSubscriber` → `@EventBusSubscriber` (agora top-level), e
  `Mod.EventBusSubscriber.Bus` → `EventBusSubscriber.Bus`.
- ⚠️ **Cuidado:** o bus padrão do Forge para `@Mod.EventBusSubscriber` é o **game bus**.
  Eventos de mod bus (`FMLCommonSetupEvent`, `RegisterEvent`) exigem
  `bus = EventBusSubscriber.Bus.MOD` explícito, senão o listener **nunca roda**
  (falha silenciosa). Em NeoForge, `@EventBusSubscriber` registra nos dois buses
  e roteia automaticamente.
- EventBus 6: métodos `@SubscribeEvent` privados funcionam, mas **não há herança** —
  superclasses não podem ter `@SubscribeEvent`. Static/non-static não pode divergir.

**Eventos renomeados:**
| Forge 47 / 1.20.1 | NeoForge 21.1 / 1.21.1 |
|---|---|
| `LivingAttackEvent` | `LivingIncomingDamageEvent` |
| `LivingHurtEvent` | `LivingDamageEvent.Post` |
| `LivingDamageEvent` | `LivingDamageEvent.Pre` |
| `ShieldBlockEvent` | `LivingShieldBlockEvent` |
| `RenderGuiOverlayEvent` | `RenderGuiLayerEvent` |
| `RegisterGuiOverlaysEvent` | `RegisterGuiLayersEvent` |
| `VanillaGuiOverlay` | `VanillaGuiLayers` |
| `IGuiOverlay` | `LayeredDraw.Layer` |
| `GuiOverlayManager` | (removido; use `VanillaGuiLayers`) |
| `MobSpawnEvent.FinalizeSpawn` | `FinalizeSpawnEvent` |
| `SpawnPlacementRegisterEvent` | `RegisterSpawnPlacementsEvent` |
| `SpawnPlacements.Type` | `SpawnPlacementTypes` |
| `EntityItemPickupEvent` | `ItemEntityPickupEvent.Pre` |
| `ToolActions` / `ToolAction` | `ItemAbilities` / `ItemAbility` |
| `BlockToolModificationEvent.getToolAction()` | `getItemAbility()` |
| `InputEvent.MouseScrollingEvent.getScrollDelta()` | `getScrollDeltaY()` |
| `RenderHighlightEvent.getPartialTick()` | `getDeltaTracker()` |

**Damage pipeline (NeoForge 21.0.31+):** novo `DamageContainer` exposto nos eventos,
com `addModifier(DamageContainer.Reduction.ARMOR/ENCHANTMENTS/MOB_EFFECTS/ABSORPTION, fn)`.
Novo `EntityInvulnerabilityCheckEvent` (ponto mais cedo, sempre disparado).
`LivingDamageEvent.Pre` **não é cancelável** (armor/durabilidade de escudo já ocorreram).
`ILivingEntityExtension#onDamageTaken` para reagir a dano já aplicado sem override de `actuallyHurt`.

**PlantType (NeoForge 21.0.39+):** `PlantType`, `IPlantable` **removidos** →
interface `SpecialPlantable`, método `canSustainPlant`, tag `neoforge:villager_farmlands`.

Fontes: [NeoForge 21.0 release](https://neoforged.net/news/21.0release),
[NeoForged events](https://docs.neoforged.net/docs/1.21.1/concepts/events),
[NeoForge 20.2 eventbus changes](https://neoforged.net/news/20.2eventbus-changes).

---

### 2.5 Rede (networking)

**O que mudou.** `SimpleChannel` foi substituído por payloads registrados.
- `CustomPacketPayload` + `StreamCodec<RegistryFriendlyByteBuf, T>` (ou `ByteBuf`).
- `toBytes(FriendlyByteBuf)` → `write(FriendlyByteBuf)`.
- `Supplier<NetworkEvent.Context>` → `IPayloadContext`.
- `ctx.get().enqueueWork(...)` → `ctx.enqueueWork(...)`;
  `ctx.get().getSender()` → `(ServerPlayer) ctx.player()`;
  `setPacketHandled(true)` → **removido** (automático).
- `NetworkDirection` removido.
- `channel.send(PacketDistributor.SERVER.noArg(), m)` → `PacketDistributor.sendToServer(m)`
- `channel.send(PacketDistributor.PLAYER.with(() -> p), m)` → `PacketDistributor.sendToPlayer(p, m)`
- `channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> e), m)` → `PacketDistributor.sendToPlayersTrackingEntity(e, m)`
- Registro via `RegisterPayloadHandlersEvent` / `payloadRegistrar`.
- `RegistryFriendlyByteBuf` carrega o `registryAccess()` para codecs que usam IDs de registry.

**Impacto no MNA.** 88 arquivos em `com/mna/network/` — segundo maior setor.
Cada pacote precisa virar um `CustomPacketPayload` com `StreamCodec` e tipo
(`PacketType` com direção).

Fontes: [NeoForged payloads](https://docs.neoforged.net/docs/1.21.1/networking/payload),
[NeoForged primer 1.20.5](https://docs.neoforged.net/primer/docs/1.20.5/).

---

### 2.6 Mob effects / encantamentos dinâmicos — ✅ confirmado

**O que mudou.**
- `MobEffectInstance.getEffect()` devolve `Holder<MobEffect>` — usar `.value()` para
  acessar `MobEffect` (`getEffect().value().isBeneficial()`, `.getCategory()`).
- Construtores de `MobEffectInstance` aceitam `Holder<MobEffect>` (então `DeferredHolder` já serve).
- `LivingEntity.hasEffect(...)` / `getEffect(...)` aceitam `Holder<MobEffect>`.
- `MobEffect.applyEffectTick` passou a devolver **`boolean`**;
  `isDurationEffectTick` → `shouldApplyEffectTickThisTick`.
- `AttributeModifier` usa **`ResourceLocation`** em vez de `String` UUID:
  `AttributeMap.getModifiers()` devolve `Map<ResourceLocation, AttributeModifier>`;
  `getModifier/hasModifier/removeModifier(ResourceLocation)`; `removePermanentModifier`
  removido; novo `addOrReplacePermanentModifier`. `AttributeMap.assignValues` → `assignAllValues`.
  `getDirtyAttributes` → `getAttributesToUpdate`; novo `getAttributesToSync`.
- `MobType` **removido** → checar via `EntityTypeTags` (`getType().is(EntityTypeTags.ILLAGER)`).
  Overrides de `getMobType()` devem ser removidos.
- `ForgeRegistries.POTIONS` → `BuiltInRegistries.POTION`.

**Impacto no MNA.** 70 arquivos em `com/mna/effects/` + 21 em `enchantments/`.
Efeitos custom provavelmente usam `AttributeModifier` com UUID string — todos quebram.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/),
[NeoForged mob effects](https://docs.neoforged.net/docs/1.21.1/items/mobeffects).

---

### 2.7 Receitas e o registro via codec — ✅ confirmado

**O que mudou.** Receitas agora são registradas em `Registries.RECIPE_TYPE` /
`Registries.RECIPE_SERIALIZER` e carregadas por **codec**:
- `RecipeSerializer` passa a prover `StreamCodec` (ou `Packet#codec` que converte
  read/write legados) e `MapCodec` para o JSON.
- `fromJson(JsonObject, RecipeManager)` deixa de ser o caminho — implemente `codec()`.
- `Recipe#getResultItem`/resultado em receita: `{"id": ...}`.
- `RecipeManager.byKey(x)` devolve `RecipeHolder` → `.value()`; `getRecipeFor(x)`
  devolve `Optional<RecipeHolder<T>>` → `.map(RecipeHolder::value)`; `getRecipesFor(x)`
  devolve `List<RecipeHolder<T>>` → `.stream().map(RecipeHolder::value).toList()`.
- `now.getId()` → `now.id()` (RecipeHolder).
- `Consumer<RecipeOutput>` → `RecipeOutput`; `FinishedRecipe` → `RecipeOutput`.
- `CraftingMenu.slotChangedCraftingGrid` recebe `RecipeHolder`; `RecipeBookMenu`
  genérico trocado para `RecipeInput`/`Recipe`.
- `ShapedRecipePattern`/`RecipeBookCategory` podem exigir `RecipeCategory` em 1.21.1.

**Como corrigir.** O port já acertou isso empiricamente (`AMRecipeBaseSerializer.codec()`
+ `RecipeIdFixer`, 3086 receitas carregando). Reforçando: cada serializer custom
deve expor `codec()`/`StreamCodec` e não `fromJson`.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/),
[NeoForged recipes](https://docs.neoforged.net/docs/1.21.1/resources/server/recipes/).

---

### 2.8 Entidades, blocos e itens — mudanças de assinatura (alta densidade)

Estas são as mais numerosas e mais silenciosas. Referência rápida:

**Entidades**
- `defineSynchedData()` → `defineSynchedData(SynchedEntityData.Builder builder)`;
  `this.entityData.define(...)` → `builder.define(...)`; `super.defineSynchedData()` → `super.defineSynchedData(builder)`.
- `BlockSource` virou **record** e foi movido para `net.minecraft.core.dispenser`:
  `getLevel()`→`level()`, `getPos()`→`pos()`, `getBlockState()`→`state()`;
  `DefaultDispenseItemBehavior.dispense()` → `execute()`.
- `AbstractProjectileDispenseBehavior` → `ProjectileDispenseBehavior`.
- `changeDimension(ServerLevel)` → `changeDimension(DimensionTransition)`;
  `PortalInfo` **removido**; `handleInsidePortal`→`setAsInsidePortal`;
  `handleNetherPortal`→`handlePortal`; `findDimensionEntryPoint`→`Portal#getPortalDestination`;
  `getPortalWaitTime`→`Portal#getPortalTransitionTime`; `teleportToWithTicket` removido.
- `setSecondsOnFire` → `igniteForSeconds` (agora `float`).
- `Player.disableShield(boolean)` → `disableShield()`.
- `new ServerPlayer(level, x, profile)` ganhou `ClientInformation.createDefault()`.
- `PlayerList.placeNewPlayer(p, x)` ganhou `CommonListenerCookie.createInitial(...)`.
- `AbstractArrow.setKnockback/getKnockback/setShotFromCrossbow` **removidos**.
- `Leashable`: métodos de leash do `Mob` foram para a interface `Leashable`.
- `Mob.canWearBodyArmor` removido → `canUseSlot(EquipmentSlot.BODY)`.
- `EquipmentSlot.byTypeAndIndex` removido; construtor agora recebe limite por slot.
- `getAddEntityPacket` recebe `ServerEntity`; `ClientboundAddEntityPacket` idem.
- `getExperienceReward()` → `getBaseExperienceReward()`.
- `LivingEntity.broadcastBreakEvent` → `onEquippedItemBroken`.
- `hurtAndBreak(int, ServerLevel, Consumer<Item>)` (antes `Runnable`).
- `Saddleable.equipSaddle(ItemStack)`.
- `getEquipmentSlotForItem` virou método de instância.

**Blocos**
- `Block.Properties.copy(...)` → `BlockBehaviour.Properties.ofFullCopy(...)`;
  `Block.Properties.of(...)` → `BlockBehaviour.Properties.of(...)`.
- `AbstractGlassBlock`/`GlassBlock` → `TransparentBlock`.
- `AbstractTreeGrower` → `TreeGrower`.
- `BlockPathTypes` → `PathType`.
- `Blocks.GRASS` → `Blocks.SHORT_GRASS`.
- `isPathfindable(BlockState, BlockGetter, BlockPos, PathComputationType)` → `isPathfindable(BlockState, PathComputationType)`.
- `getCloneItemStack(BlockGetter,...)` → `(LevelReader, ...)`.
- `isValidBonemealTarget(level, pos, state, boolean)` → `(level, pos, state)` (perdeu `isClient`);
  `BlockGetter` → `LevelReader`.
- `isValidSpawn(state, getter, pos, SpawnPlacements.Type, EntityType)` → `(state, getter, pos, EntityType)`.
- `playerWillDestroy(...)` passou a devolver `BlockState` (use `return super.playerWillDestroy(...)`).
- `new AABB(BlockPos, BlockPos)` → `new AABB(Vec3.atLowerCornerOf(...), Vec3.atLowerCornerOf(...))`.
- `StairBlock(() -> x.defaultBlockState(), ...)` → `StairBlock(x.defaultBlockState(), ...)`.
- `DoorBlock`/`TrapDoorBlock`: `(Properties, BlockSetType)` → `(BlockSetType, Properties)`.
- `StandingSignBlock`/`WallSignBlock`/etc.: `(Properties, WoodType)` → `(WoodType, Properties)`.
- `LiquidBlockContainer.canPlaceLiquid(...)` ganhou `Player` como 1º parâmetro.
- Subclasses anônimas de `BushBlock` precisam implementar `codec()`.
- `isFertile(level, pos)` removido → `state.getValue(FarmBlock.MOISTURE) > 0`.
- `CropBlock.getGrowthSpeed(this, ...)` → `getGrowthSpeed(state, ...)`.
- `CommonHooks.onCropsGrowPre/Post` → `CommonHooks.canCropGrow`/`fireCropGrowPost`.

**Itens**
- `getUseDuration(ItemStack)` → `getUseDuration(ItemStack, LivingEntity)`.
- `appendHoverText(stack, Level, list, flag)` → `appendHoverText(stack, Item.TooltipContext, list, flag)`.
- `FoodProperties.getEffects()` → `.effects()`; `.saturationMod(...)` → `.saturationModifier(...)`;
  `.alwaysEat()` → `.alwaysEdible()`; `FoodProperties` agora recebe a stack resultante.
- `Item.getAttackDamageBonus(Player)` → `(Entity, DamageSource)`.
- `hurtAndBreak` recebe `Consumer<Item>` em vez de `Runnable` e `EquipmentSlot` em vez de lambda de mão.
- `Vanishable` e `DyeableLeatherItem` **removidos**.
- `ItemHandlerHelper.copyStackWithSize` → `ItemStack.copyWithCount`;
  `ItemHandlerHelper.canItemStacksStack` → `ItemStack.isSameItemSameComponents`.
- `BucketItem.getFluid()` → campo público `content`.
- `Inventory.size()` / `Container.size()` → `getContainerSize()`.
- `CommonHooks.getBurnTime(stack, null)` → `stack.getBurnTime(null)`.
- `CrossbowItem.getChargeDuration(ItemStack, LivingEntity)`.
- `TransmuteCopyIgnoreEmpty` virou private.

**Vanilla renames diversos**
- `EnchantmentTableBlock(Entity)` → `EnchantingTableBlock(Entity)`.
- `EnchantmentTableParticle` → `FlyTowardsPositionParticle` (`NautilusProvider` movido).
- `Tags.Items.SHEARS` → `TOOLS_SHEAR`; `TOOLS_SHIELDS` → `TOOLS_SHIELD`;
  `COBBLESTONE`→`COBBLESTONES`; `GRAVEL`→`GRAVELS`; `GLASS`→`GLASS_BLOCKS`;
  `GLASS_COLORLESS`→`GLASS_BLOCKS_COLORLESS`; `STRING`→`STRINGS`; `STONE`→`STONES`;
  `SAND`→`SANDS`; `LEATHER`→`LEATHERS`; `BONES`→`BONES`.
- Tags `forge:*` → `c:*` (ingots, ores, gems, nuggets, dusts, storage_blocks, rods,
  raw_materials, dyes, seeds, crops, glass→glass_blocks, cobblestone→cobblestones,
  sand→sands, glass_panes, chests, fences, eggs, bones, ender_pearls, end_stones,
  stone→stones, leather→leathers, string→strings).
- `ResourceLocation`: construtor privado! `new ResourceLocation(a,b)` → `fromNamespaceAndPath(a,b)`;
  `new ResourceLocation(s)` → `parse(s)`; `withDefaultNamespace(path)` para `minecraft:path`;
  `of` → `bySeparator`; `isValidResourceLocation` removido.
- `net.minecraft.commands.CommandFunction` → `net.minecraft.commands.functions.CommandFunction`.
- `NbtIo.readCompressed(...)` exige `NbtAccounter.unlimitedHeap()`.
- `ServerLevelData.getAllowCommands()` → `isAllowCommands()`.
- `Holder.getTagKeys()` → `tags()`.
- `Minecraft.getFrameTime()/getDeltaFrameTime()` → `getTimer().getRealtimeDeltaTicks()`;
  `Minecraft.Timer` → `DeltaTracker.Timer`; `Gui.render` recebe `DeltaTracker`.
- `Options.renderDebug` (campo) → `getDebugOverlay().showDebugScreen()`.
- `server.getServerDirectory()` devolve `Path` (use `.toFile()`).
- Telas de opções vanilla movidas para `net.minecraft.client.gui.screens.options`
  (e `.options.controls`).
- `GenericDirtMessageScreen` → `GenericMessageScreen`.
- `ChunkAccess.getStatus()` → `getPersistedStatus()`.
- `GameTestHelper.makeMockSurvivalPlayer()` → `makeMockPlayer(GameType.SURVIVAL)`.
- `ServerGamePacketListenerImpl.connection` (campo) → `getConnection()`.
- `DamageSource.isIndirect()` → `isDirect()` (**lógica invertida!**).
- `LootContext.EntityTarget.KILLER` → `ATTACKER`, etc.
- `Level.ExplosionInteraction` passou a implementar `StringRepresentable`.
- `AbstractWindCharge.WindChargeDamageCalculator` → `net.minecraft.world.level.SimpleExplosionDamageCalculator`.
- `Screen.narrationEnabled()` → `updateNarratorStatus(boolean)`.
- `Sound` (resource) passou a receber `ResourceLocation` em vez de `String`.
- `PacketListener.onDisconnect(Component)` → `(DisconnectionDetails)`.
- `CrashReport.getFriendlyReport/saveToFile` recebem `ReportType`.
- `MinecraftServer.isNetherEnabled` → `isLevelEnabled`.
- `CreativeModeTab.Builder.withTabsBefore(ResourceLocation)` → `(ResourceKey<CreativeModeTab>)`.
- `AbstractSelectionList.x0/y0/x1/y1` (campos) → `getX()/getY()/getRight()/getBottom()`.
- `mouseScrolled(x, y, delta)` → `mouseScrolled(x, y, scrollX, scrollY)` (4 args).
- `EditBox.tick()` removido da superclasse (remova `@Override` e `super.tick()`).
- `AbstractSelectionList.setScrollAmount` → `setClampedScrollAmount`.
- `MultiLineLabel.createFixed` → `create`; `renderBackgroundCentered` removido.
- `CreativeModeInventoryScreen(Player)` → `(LocalPlayer)`.
- `OptionsList` não recebe mais o inteiro não usado.
- `GameRenderer.MAX_BLUR_RADIUS` agora público e `int`; `Options.menuBackgroundBlurriness` devolve `int`.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/),
[NeoForged GUI/menus](https://docs.neoforged.net/docs/1.21.1/gui/menus),
[NeoForged GUI/screens](https://docs.neoforged.net/docs/1.21.1/gui/screens).

---

### 2.9 `ResourceLocation` — construtor privado

**O que mudou.** `ResourceLocation` é `final` com construtor **privado** em 1.21.

**Como corrigir.**
```java
new ResourceLocation("mna", "wand")     -> ResourceLocation.fromNamespaceAndPath("mna", "wand")
new ResourceLocation("mna:wand")        -> ResourceLocation.parse("mna:wand")
new ResourceLocation("minecraft", "x")  -> ResourceLocation.withDefaultNamespace("x")
ResourceLocation.of(a, ':')             -> ResourceLocation.bySeparator(a, ':')
```
Há variantes tolerantes: `tryBuild`, `tryParse`.

Fontes: [NeoForged resourcelocation](https://docs.neoforged.net/docs/1.21.1/misc/resourcelocation).

---

## 3. Mudanças de render

### 3.1 Vertex system reescrito

**O que mudou.**
- `VertexConsumer.vertex(...)` → `addVertex(...)` (existe overload com `PoseStack.Pose, Vector3f`).
- `.color(...)` → `setColor(...)`; `.uv(...)` → `setUv(...)`; `.normal(...)` → `setNormal(...)`.
- `.overlayCoords(x, y)` → `setOverlay(int)`; `.overlayCoords(packed)` → `setOverlay(packed)`;
  `overlayCoords(0, 10)` → `setOverlay(OverlayTexture.NO_OVERLAY)`.
- `.uv2(...)` → `setLight(...)`; `defaultColor` → `setWhiteAlpha()`.
- `.endVertex()` **removido** (chamado automaticamente).
- `setNormal(Matrix3f, x, y, z)` → `setNormal(x, y, z)`.
- `BufferBuilder$RenderedBuffer` → **`MeshData`**; `BufferBuilder.end()` → `buildOrThrow()`.
- `Tesselator.getBuilder()` **removido**; `Tesselator.begin(Mode, VertexFormat)` devolve `BufferBuilder`.
- `BufferBuilder.begin(...)` → `Tesselator.getInstance().begin(...)`.
- `Tesselator.end()` → `BufferUploader.drawWithShader(buffer.buildOrThrow())`.
- `MultiBufferSource` passou a usar `ByteBufferBuilder` em vez de `BufferBuilder`;
  `immediateWithBuffers` recebe `SequencedMap`.
- `RenderType.end()` → `draw()`; novo `sortOnUpload` (usa `VertexSorting`).
- `SectionBufferBuilderPack.builder` → `buffer`.
- `BufferVertexConsumer` e `DefaultedVertexConsumer` **removidos**.
- `DefaultVertexFormat.VertexFormatElement` → `VertexFormatElement`.
- `ModelPart#render` / `Cube#compile` / `Model#renderToBuffer` recebem **int ARGB**
  em vez de 4 floats (há overload sem tint).
- `RenderLayer.coloredCutoutModelCopyLayerRender` / `renderColoredCutoutModel` recebem **int**.
- `ShaderInstance` não pode mais mudar blend mode (só `EffectInstance`/`PostPass`);
  novo `setDefaultUniforms`.
- `ItemRenderer.getArmorFoilBuffer` perdeu o boolean; `getCompassFoilBufferDirect` removido.
- Chunk regions: `RenderRegionCache.createRegion(SectionPos)`; `SectionCompiler.compile`.

**Shaders renomeados/removidos:**
- `minecraft:position_color_tex` → `minecraft:position_tex_color`
- `minecraft:rendertype_armor_glint` → `minecraft:rendertype_armor_entity_glint`
- `minecraft:rendertype_glint_direct` → `minecraft:rendertype_glint`

**Impacto no MNA.** 38 partículas + 3 shaders. Partículas custom quase certamente
usam `vertex()/color()/endVertex()`/`Tesselator.getBuilder()`.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/).

---

### 3.2 Fog / shaders

**O que mudou.** O shader de fog passou a ter assinatura de 2 argumentos em vez de 1
(confirmação empírica sua). Vanilla reescreveu o pipeline de fog: os uniforms e a
assinatura do `fog_distance`/`fog` shader mudaram, e os `RenderType`s e core shaders
foram renomeados/concentrados (lista em §3.1).

**Como corrigir.** Atualizar os `.fsh`/`.vsh`/`.json` em `assets/<ns>/shaders/`
para a nova assinatura e os novos uniform names. Conferir também que os
`RenderType`s referenciados existem em 1.21.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/).

---

### 3.3 PoseStack / Matrix4f

**O que mudou.**
- `Matrix4f` passou a ser fornecido por `com.joml` (JOML) — vanilla migrou de
  `com.mojang.math.Matrix4f`/`Vector3f`/`Vector4f`/`Quaternionf` para as classes JOML.
  Em 1.21 os tipos vanilla são aliases/heranças de JOML, o que causou ajustes de
  import e de convenções (ex.: reset da view matrix rotation).
- `RenderSystem.glBindBuffer(int, IntSupplier)` → `(int, int)`;
  `glBindVertexArray(Supplier<Integer>)` → `(int)`;
  `setupOverlayColor(IntSupplier, int)` → `(int, int)`.
- `RenderSystem.initGameThread/isOnGameThread/assertInInitPhase/isInInitPhase/
  assertOnGameThreadOrInit/assertOnGameThread` **removidos**.
- `MemoryTracker` **removido**.
- `BlockMath.getUVLockTransform` não recebe mais `Supplier<String>`.
- `BlockElementFace` virou **record**; `ModelResourceLocation` virou **record**
  (com método `inventory`).
- `ModelBakery.getModel` ficou package-private; novo `ModelBakery$TextureGetter`
  e `BlockStateModelLoader`; `ModelBakery$ModelGroupKey` removido.
- `UnbakedModel.bakeQuad` não recebe mais `ResourceLocation`.

**Impacto no MNA.** Mixins de modelo e qualquer `IForgeBakedModel`/`IPoseStackExtension`.
`IForgePoseStack` → `IPoseStackExtension`; `IForgeBakedModel` → `IBakedModelExtension`.

Fontes: [NeoForged primer 1.21](https://docs.neoforged.net/primer/docs/1.21/).

---

### 3.4 GUI / Menu system — ✅ confirmado

**O que mudou.**
- `AbstractContainerScreen.renderBackground` chama `renderBg` (e a assinatura de
  `renderBackground` ganhou `mouseX, mouseY, partialTick`). **Não** chame
  `renderBackground` dentro de `renderBg` — recursão.
- `quickMoveStack` passou a receber `Player`: `quickMoveStack(Player, int)`.
- Movimentação de stacks: `moveItemStackTo(stack, start, end, reverse)` (era `moveItemStackTo` com índices diferentes).
- `MenuType` exige `MenuSupplier` + `FeatureFlagSet`:
  `new MenuType(MyMenu::new, FeatureFlags.DEFAULT_FLAGS)`.
- `NetworkHooks.openScreen(...)` → `ServerPlayer.openMenu(...)`.
- `renderBackground(guiGraphics)` explícito não é mais necessário (super.render cuida).
- `AbstractSelectionList.y0/y1/x0/x1` → `getY()/getBottom()/getX()/getRight()`.
- `setScrollAmount` → `setClampedScrollAmount`.
- `mouseScrolled` ganhou 4º parâmetro.
- `MultiLineLabel`: `createFixed`→`create`, `renderBackgroundCentered` removido,
  `TextAndWidth` virou record.
- `Checkbox` recebe `maxWidth` (via builder).
- `GenericDirtMessageScreen` → `GenericMessageScreen`.
- Telas de opções vanilla moveram de pacote (§2.8).
- `Gui#render` recebe `DeltaTracker`.
- `GuiGraphics.drawStringWithBackdrop(...)` novo.

**Impacto no MNA.** 164 arquivos em `com/mna/gui/` — maior setor de render.
Todos os `AbstractContainerScreen` precisam revisitar `renderBg`/`renderBackground`.

Fontes: [NeoForged menus](https://docs.neoforged.net/docs/1.21.1/gui/menus),
[NeoForged screens](https://docs.neoforged.net/docs/1.21.1/gui/screens).

---

### 3.5 `ItemRenderer.renderItem` / `GuiGraphics`

**O que mudou.** Em 1.21.1, `GuiGraphics.renderItem(ItemStack, int, int, int, int)`
(com partialTick/guioffsets) **foi removido** e substituído pelo modelo de
**RenderState**: a GUI passou a construir um `ItemStackRenderState` (com
`setAnimated`, `getModelBoundingBox`, `setOversizedInGui`, `appendModelIdentityElement`...)
e submetê-lo via `submit*RenderState`. O `ItemStackRenderState` é o que define
como o item é desenhado. O `GuiItemRenderState` cuida do caso especial
"item na tela" (usa `oversizedInGui` para decidir entre bbox do modelo e 16×16).

> **Atenção:** essa remoção é de **1.21.2+** (o primer 1.21.5 documenta o modelo de
> RenderState já maduro). Para **1.21.1** a assinatura antiga com sobrecargas
> ainda existe, mas mudou de forma entre 1.20.1 e 1.21.1 (parâmetro de
> `PoseStack`/posicionamento). Se o MNA chama `renderItem` com 5 args, revise a
> assinatura exata no MDK 1.21.1 — é um ponto de atenção alto por ser fácil de
> acertar por coincidência de tipos.

Fontes: [NeoForged primer 1.21.5](https://docs.neoforged.net/primer/docs/1.21.5/).

---

## 4. Mixin / MixinExtras / Access Transformer

### 4.1 `MixinConfigs` no MANIFEST é ignorado

**O que mudou.** NeoForge (como Forge 1.20.4+) usa **Fabric Mixin**, e o loader
**ignora** o atributo `MixinConfigs` do `META-INF/MANIFEST.MF`. Os mixins são
declarados no `neoforge.mods.toml`.

**Como corrigir.** Em `src/main/resources/META-INF/neoforge.mods.toml`:
```toml
[[mixins]]
config = "mna.mixins.json"
```
E manter o `<modid>.mixins.json` em `src/main/resources/`, com
`"refmap"` apontando para o refmap gerado pelo toolchain.

Fontes: [NeoForged mod files](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles).

---

### 4.2 `[[mixins]]` e `[[accessTransformers]]` no `neoforge.mods.toml`

**O que mudou.** A especificação do `mods.toml` virou `neoforge.mods.toml`
(desde NeoForge 1.20.5) e passou a ter seções de array de tabelas:

```toml
[[mods]]
modId = "mna"
...

[[accessTransformers]]
file = "accesstransformer.cfg"

[[mixins]]
config = "mna.mixins.json"
```

Também há `[[dependencies.<modid>]]`, `[modproperties.<modid>]`, e a chave
`enumExtensions` (veja §4.4).

Fontes: [NeoForged mod files](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles).

---

### 4.3 Access Transformer e refmap

**O que mudou.**
- ATs continuam suportados, declarados via `[[accessTransformers]]` com `file="..."`.
- Sob Fabric Mixin, o mapeamento de nomes oficiais→intermediary passa pelo
  **refmap** gerado em build. Mixins escritos com nomes oficiais precisam que o
  refmap esteja no jar e referenciado no `.mixins.json`.
- Mixins de terceiros que usavam nomes SRG/obfuscados diretamente quebram: muitos
  campos que eram acessados por SRG agora precisam de refmap ou de acesso via
  método/record.
- Se um mixin referencia campo que virou **record** (ex.: `BlockElementFace`,
  `ModelResourceLocation`) ou private, provavelmente precisa de AT ou de
  `@Shadow`/acesso via accessor MixinExtras.

Fontes: [NeoForged AT docs](https://docs.neoforged.net/docs/1.21.1/advanced/accesstransformers),
[NeoForged mod files](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles).

---

### 4.4 Enum extensions rework (NeoForge 21.0.10+)

**O que mudou.** Em vez de chamar método estático no enum para adicionar valores,
declara-se um **JSON de enum extensions** referenciado por `enumExtensions` no
`[[mods]]` do `neoforge.mods.toml`:

```json
{
  "entries": [
    {
      "enum": "net/minecraft/world/item/ItemDisplayContext",
      "name": "EXAMPLEMOD_STANDING",
      "constructor": "(ILjava/lang/String;Ljava/lang/String;)V",
      "parameters": [-1, "examplemod:standing", null]
    }
  ]
}
```

**Impacto.** Se o MNA (ou uma lib) usa `IExtensibleEnum`/`EnumHelper.addEnum`,
precisa migrar para o formato JSON.

Fontes: [NeoForge 21.0 release](https://neoforged.net/news/21.0release),
[NeoForged extensible enums](https://docs.neoforged.net/docs/1.21.1/advanced/extensibleenums).

---

## 5. GeckoLib 4.7 → 4.9 e Curios 5.x → 9.x

### 5.1 GeckoLib 4.7 → 4.9 — ⚠️ ATENÇÃO A UM EQUÍVOCO COMUM

**Fato importante:** o rename de pacote `software.bernie.geckolib` → `com.geckolib`
aconteceu no **GeckoLib 5.x**, **não** no 4.9. O branch `1.21.1` do GeckoLib ainda
usa `software.bernie.geckolib` e ainda é "GeckoLib 4". Os dois targets do seu port
estão corretos: `geckolib-forge-1.20.1-4.7` e `geckolib-neoforge-1.21.1-4.9.3`.
Se você portou para `com.geckolib.*`, você migrou para a API do 5.x e o alvo deve
ser o jar 5.x — cuidado.

**O que mudou de 4.7 (1.20.1) para 4.9 (1.21.1):**
- **`GeoModel` deixou de implementar `CoreGeoModel`** — a interface foi absorvida.
  Se o mod referencia `CoreGeoModel`/`setCustomAnimations` via essa interface,
  ajuste.
- **Cor em vez de floats:** `GeoRenderer.getRenderColor` passou a devolver
  `software.bernie.geckolib.util.Color` (era `software.bernie.geckolib.core.object.Color`)
  e os métodos de render passaram de `float red, green, blue, alpha` para
  **`int colour`** (ARGB). Vale para `defaultRender`, `actuallyRender`,
  `renderFinal`, `preRender`, `postRender`.
  Também: `Color.ofARGB(...)` — havia um bug histórico usando RGBA.
- **`DeferredGeoRenderProvider`** adicionado em 4.7 (fontes divididas) — útil para xplat.
- `DefaultedEntityGeoModel` passou a receber **nome de osso nullable** em vez de booleano.
- Suporte a UV rotation; `stopTriggeredAnimation`; easings catmull-rom nativos;
  correções de molang (precedência, `OR`/`AND` eager, `asin/acos/atan`,
  `DieRollIntegerFunction`, `LoopType.DEFAULT` serializável, `math.min_angle`);
  suporte a mais `BlockState` properties para `GeoBlock`.
- Melhorias de compatibilidade com EntityRenderState do NeoForge.
- 4.9 adicionou `AttachedAnimatableRenderLayer` (render de objetos GeckoLib em
  cima de modelos vanilla, tipo player) — útil se o MNA renderiza coisas em cima do player.

**Impacto no MNA.** `com/mna/interop/` (BookRenderer, CurioRenderer, HatRenderer,
StaffRenderer, WandRenderer) + 52 `.geo.json` + 42 `.animation.json`.
Assets permanecem em `assets/<modid>/geckolib/{animations,models}/geo/...`.

**Como corrigir.** Renomear os parâmetros de cor para `int`, migrar imports de
`Color`, remover referências a `CoreGeoModel` se houver. Caminhos de asset iguais
entre 4.7 e 4.9 (a reorganização de assets é do 5.x).

Fontes: [GeckoLib 4 changes wiki](https://github.com/bernie-g/geckolib/wiki/Geckolib-4-Changes),
[GeckoLib changelog 1.21.1](https://github.com/bernie-g/geckolib/blob/1.21.1/changelog.txt),
[GeckoLib 4.9 CF](https://www.curseforge.com/minecraft/mc-mods/geckolib/files/8299411),
[GeckoLib 4.7 CF](https://www.curseforge.com/minecraft/mc-mods/geckolib/files/5874016).

---

### 5.2 Curios 5.x → 9.x

**O que mudou (do changelog oficial):**

Entre 5.x e 9.x há muita coisa; as quebras diretas de API para consumidores:

**1.21.1 (9.2.0) — quebras de API**
- `ICurio#getLootingLevel(SlotContext, DamageSource, LivingEntity, int)` →
  `getLootingLevel(SlotContext, LootContext)`.
- **UUID → `ResourceLocation`** em *todas* as instâncias de attribute modifiers da API.
- Novos: `ICurio#getAttributesTooltip(List<Component>, Item.TooltipContext)`,
  `getDropRule(SlotContext, DamageSource, boolean)`,
  `getAttributeModifiers(SlotContext, ResourceLocation)`,
  `getSlotsTooltip(List<Component>, Item.TooltipContext)`.
- Deprecados (removidos depois): `getAttributesTooltip(List)`, `getDropRule(SlotContext, DamageSource, int, boolean)`,
  `getAttributeModifiers(SlotContext, UUID)`, `CuriosApi#getSlotIcon(String)`,
  `CurioCanEquipEvent(ItemStack, SlotContext)`.
- Tooltip passou a usar a API do NeoForge (`AddAttributeTooltipsEvent`,
  `GatherSkippedAttributeTooltipsEvent`) em 9.4.0.
- Registro de curios agora via `RegisterCapabilitiesEvent` (curios é uma capability NeoForge).
- Novidades úteis: `ICurioSlotExtension`, `CuriosTags`, `CuriosTooltip`,
  `ICuriosItemHandler#isSlotActive/setSlotActive` (9.5.0).
- Data fixers para itens do inventário curios.

**Remoções relevantes ao longo da linha 5→9:**
- `SlotTypeMessage` e `SlotTypePreset` (registro por IMC) → **registro por datapack**
  (`data/<ns>/curios/...`), desde 5.2.0/1.20.
- `ICuriosHelper`, `ISlotHelper`, `IIconHelper` **deprecados em 5.2.0** e removidos —
  use métodos equivalentes em `ICuriosItemHandler`.
- `CuriosApi.MODID` deprecado (removido em 1.21.x) → use `CuriosResources.MOD_ID`.
- `CuriosApi.registerCurio(Item, ICurioItem)` existe desde 5.3.0.

**Impacto no MNA.** 45 classes usam `top.theillusivec4.curios`, 3 slots custom
(`curios/` + 8 tags). A migração exige: datapacks de slot, e troca de qualquer
`AttributeModifier` UUID-string por `ResourceLocation`.

**Como corrigir.**
- Slots: mover IMC `SlotTypeMessage` → JSON em `data/mna/curios/`.
- Helpers removidos → `ICuriosItemHandler`.
- `getAttributeModifiers(SlotContext, ResourceLocation)`.
- `getLootingLevel(SlotContext, LootContext)`.
- Registro de curio item via `RegisterCapabilitiesEvent` no mod bus.

Fontes: [Curios changelog 1.21.1](https://github.com/TheIllusiveC4/Curios/blob/1.21.1/CHANGELOG.md),
[CuriosApi 1.21.1](https://github.com/TheIllusiveC4/Curios/blob/1.21.1/neoforge/src/main/java/top/theillusivec4/curios/api/CuriosApi.java),
[Illusive Soulworks docs](https://docs.illusivesoulworks.com/curios/porting/1211to1214).

---

## 6. Gotchas de migração da comunidade

Consolidados de ports reais (ModPorter, changelogs de mods, issues):

1. **Bus errado = listener silenciosamente morto.** Em Forge, `@Mod.EventBusSubscriber`
   sem `bus` vai para o **game bus**; `FMLCommonSetupEvent`/`RegisterEvent` só disparam
   no **mod bus**. Sem `bus = Mod`, registro de blocos/itens fica `null` → NPE mais tarde.
   *Relato real:* RIGR perdeu spawn de mobs e drops de Silk Touch por isso.
   → [Modrinth RIGR changelog](https://modrinth.com/mod/rigr/changelog)

2. **Pasta plural em pack 1.21 é ignorada sem erro.** Sem aliasing. Checar com
   `/reload` e inspecionar contagem de receitas.
   → [Datapack Codex](https://elkonia.com/datapack_codex/pack-format.html)

3. **`ItemStack.save(registries, tag)` não muta o `tag`.** Sempre capture o retorno.
   → [NeoForge 20.5 release](https://neoforged.net/news/20.5release/)

4. **`DeferredHolder` não é `ItemLike`.** `event.accept(holder)` não compila/não
   funciona — precisa `.get()`. Pega muito em `BuildCreativeModeTabContentsEvent`.

5. **`DamageSource.isIndirect()` virou `isDirect()` com lógica invertida.** Compila
   e roda com comportamento errado — não quebra, corrompe.

6. **`AABB(BlockPos, BlockPos)` removido.** Todos os hitboxes/vulume cheques precisam
   de `Vec3.atLowerCornerOf(...)`.

7. **NBT→componente não é migrável automaticamente** e não há downgrade. Só ida.
   Itens com dados salvos em mundos 1.20.1 **perdem** o NBT custom ao migrar.
   → [FTB Quests issue 2042](https://github.com/FTBTeam/FTB-Mods-Issues/issues/2042)

8. **`defineSynchedData` mudou para builder.** `this.entityData.define(...)` deixa
   de existir; tudo passa a `builder.define(...)`, incluindo `super.defineSynchedData(builder)`.
   Se uma entidade não define nada, o método ainda precisa existir com a nova assinatura.

9. **`MobType` removido.** Checagens `getMobType() == MobType.UNDEAD` viram
   `getType().is(EntityTypeTags.UNDEAD)`. Overrides de `getMobType()` devem ser removidos.

10. **`MinecraftServer.getServerDirectory()` virou `Path`.** Todo `new File(dir, x)` quebra.

11. **`PlantType`/`IPlantable` removidos** (NeoForge 21.0.39+). Migrar para
    `SpecialPlantable` + `canSustainPlant` + tag `neoforge:villager_farmlands`.

12. **`ResourceLocation` construtor privado** — qualquer `new ResourceLocation(...)` quebra.
    É espalhado por todo mod grande; use busca global.

13. **Tint alpha perdido em 1.21.** Em tinting de itens, a cor perdeu o canal alpha
    em alguns caminhos — itens ficaram invisíveis. *Relato real:* Extrabotany
    (Gaia hammer, mana lenses, brew items).
    → [Extrabotany commit dda6a0e](https://github.com/Lounode/Extrabotany/commit/dda6a0ee9505cbbdaa6704753554b9b3814d90be)

14. **`ModConfigSpec` e `defineList` ganhou parâmetro.** Em 1.21 a UI de config
    precisa de um valor default novo para adicionar entradas de lista (nullable).

15. **`IConfigScreenFactory#createScreen` mudou de parâmetros** (quebra pequena mas real).

16. **`MissingMappingsEvent` removido** → `DeferredRegister.addAlias()`. Mods que
    dependiam de renomear IDs antigos precisam de alias no registro.

17. **`FMLJavaModLoadingContext` removido.** Todo `FMLJavaModLoadingContext.get().getModEventBus()`
    como primeira linha do construtor deve morrer — use o `IEventBus` injetado.

18. **`DistExecutor` removido sem substituto.** Usar `FMLLoader.getDist()` ou entrypoints.

19. **`ItemEntityPickupEvent` (antes `EntityItemPickupEvent`) usa `TriState`** — resultado
    mudou de boolean/cancelable para `TriState`.

20. **Enum extensions exige JSON** — `EnumHelper.addEnum`/`IExtensibleEnum` no estilo antigo não funciona.

21. **GeckoLib 5.x ≠ 4.9.** Não confunda: `com.geckolib` é 5.x. Para 1.21.1 com o jar
    4.9.x, mantenha `software.bernie.geckolib`.

22. **Curios slots mudaram de IMC para datapack** desde 5.2.0 — registrar slot por
    `SlotTypeMessage` não funciona mais.

Fontes: consolidadas de [ModPorter README](https://github.com/crabsatellite/modporter),
[ModPorter mappings forge2neo](https://github.com/crabsatellite/modporter/tree/main/src/main/resources/mappings/forge2neo),
[Modrinth RIGR changelog](https://modrinth.com/mod/rigr/changelog),
[Extrabotany 1.21.1 port](https://github.com/Lounode/Extrabotany/commit/dda6a0ee9505cbbdaa6704753554b9b3814d90be),
[FTB-Mods-Issues 2042](https://github.com/FTBTeam/FTB-Mods-Issues/issues/2042).

---

## 7. Checklist de maior risco para o MNA

Ordenado por volume × dificuldade:

| # | Setor | Volume | Risco principal |
|---:|---|---:|---|
| 1 | `items/` NBT → data components | 219 itens | não automatizável; perda de dados em mundos antigos |
| 2 | `gui/` renderBg/quickMoveStack | 164 arquivos | recursão de `renderBackground`; assinatura `quickMoveStack(Player,int)` |
| 3 | `network/` SimpleChannel → payload | 88 arquivos | cada pacote vira `CustomPacketPayload` + `StreamCodec` + `PacketType` |
| 4 | `recipes/` fromJson → codec | 51 serializers | já endereçado empiricamente |
| 5 | `advancements/` triggers codec-driven | 29 triggers | reestruturação, não rename |
| 6 | `capabilities/` → attachments/components | 37 arquivos | decisão de design por capability |
| 7 | `effects/` `Holder<MobEffect>` + `AttributeModifier` RL | 70 arquivos | UUID-string quebra em todos os modifiers |
| 8 | `enchantments/` datapack | 21 arquivos | registro Java removido; JSON necessário |
| 9 | `entities/` GeckoLib + `defineSynchedData` | 300 arquivos | builder + cor ARGB |
| 10 | `particles/` vertex rewrite | 38 arquivos | `Tesselator.begin`, `addVertex`, `MeshData` |
| 11 | `mixins/` | 16 mixins | refmap; campos virados record/private |
| 12 | `interop/` Curios + GeckoLib | 45+5 classes | slot datapack, RL modifiers, cor ARGB |
| 13 | Datapack folders | 1796+393+88 | silencioso; já convertido |
| 14 | `loot/` functions renomeadas | 15 arquivos + 11 GLM | `CopyNbtFunction`, `NestedLootTable` |
| 15 | `rituals/` + `spells/` | 41 + 151 | depende de effects/items acima |

---

## 8. Fontes

**Oficiais (NeoForged / MinecraftForge)**
- [NeoForged primer 1.20.6 → 1.21](https://docs.neoforged.net/primer/docs/1.21/) — a fonte primária, mais completa
- [NeoForged primer 1.21 → 1.21.1](https://docs.neoforged.net/primer/docs/1.21.1/) — mínimo (vanilla)
- [NeoForged primer 1.20.4 → 1.20.5](https://docs.neoforged.net/primer/docs/1.20.5/) — data components, StreamCodec, MapCodec
- [NeoForge 21.0 release notes](https://neoforged.net/news/21.0release) — mudanças do loader: depluralização, damage pipeline, PlantType, ItemAbilities, enum extensions, config GUI
- [NeoForge 20.5 release notes](https://neoforged.net/news/20.5release/) — data components, DFU 7, HolderLookup
- [NeoForge 20.2 eventbus changes](https://neoforged.net/news/20.2eventbus-changes)
- [NeoForged docs: registries](https://docs.neoforged.net/docs/1.21.1/concepts/registries)
- [NeoForged docs: events](https://docs.neoforged.net/docs/1.21.1/concepts/events)
- [NeoForged docs: mod files (neoforge.mods.toml, mixins, ATs)](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles)
- [NeoForged docs: attachments](https://docs.neoforged.net/docs/1.21.1/datastorage/attachments)
- [NeoForged docs: data components](https://docs.neoforged.net/docs/1.21.1/items/datacomponents)
- [NeoForged docs: menus](https://docs.neoforged.net/docs/1.21.1/gui/menus) · [screens](https://docs.neoforged.net/docs/1.21.1/gui/screens)
- [NeoForged docs: networking payloads](https://docs.neoforged.net/docs/1.21.1/networking/payload)
- [NeoForged docs: biome modifiers](https://docs.neoforged.net/docs/1.21.1/worldgen/biomemodifier)
- [NeoForged docs: GLM](https://docs.neoforged.net/docs/1.21.1/resources/server/loottables/glm)
- [NeoForged docs: access transformers](https://docs.neoforged.net/docs/1.21.1/advanced/accesstransformers) · [extensible enums](https://docs.neoforged.net/docs/1.21.1/advanced/extensibleenums)
- [MinecraftForge porting](https://docs.minecraftforge.net/en/latest/legacy/porting/)

**Datapack / vanilla**
- [Datapack Wiki breaking changes](https://datapack.wiki/wiki/info/breaking-changes)
- [Datapack Codex pack_format history](https://elkonia.com/datapack_codex/pack-format.html)
- [Datapack Codex item modifiers](https://elkonia.com/datapack_codex/item-modifiers.html)
- [Minecraft Wiki data pack format](https://minecraft.wiki/w/Template:Data_pack_format)
- [Fabric 1.21 & 1.21.1 announcement](https://fabricmc.net/2024/05/31/121.html)
- [Misode version changelog](https://misode.github.io/versions/?id=1.21&tab=changelog)

**Bibliotecas**
- [GeckoLib wiki — GeckoLib 4 changes](https://github.com/bernie-g/geckolib/wiki/Geckolib-4-Changes)
- [GeckoLib branch 1.21.1](https://github.com/bernie-g/geckolib/tree/1.21.1) (pacote `software.bernie.geckolib`) · [changelog](https://github.com/bernie-g/geckolib/blob/1.21.1/changelog.txt)
- [GeckoLib branch 1.20.1](https://github.com/bernie-g/geckolib/tree/1.20.1)
- [Curios changelog 1.21.1](https://github.com/TheIllusiveC4/Curios/blob/1.21.1/CHANGELOG.md) · [CuriosApi 1.21.1](https://github.com/TheIllusiveC4/Curios/blob/1.21.1/neoforge/src/main/java/top/theillusivec4/curios/api/CuriosApi.java)
- [Illusive Soulworks Curios porting docs](https://docs.illusivesoulworks.com/curios/porting/1211to1214)

**Comunidade / ferramentas**
- [ModPorter (Forge 1.20.1 → NeoForge 1.21.1)](https://github.com/crabsatellite/modporter) — 449 regras de texto, renames de classe/método, ATs, resource renames
- [ModPorter mappings forge2neo](https://github.com/crabsatellite/modporter/tree/main/src/main/resources/mappings/forge2neo)
- [RIGR NeoForge→Forge port changelog](https://modrinth.com/mod/rigr/changelog) — gotcha do event bus
- [Extrabotany port 1.21.1](https://github.com/Lounode/Extrabotany/commit/dda6a0ee9505cbbdaa6704753554b9b3814d90be) — gotcha de tint alpha
- [FTB-Mods-Issues #2042](https://github.com/FTBTeam/FTB-Mods-Issues/issues/2042) — NBT→componente sem downgrade
