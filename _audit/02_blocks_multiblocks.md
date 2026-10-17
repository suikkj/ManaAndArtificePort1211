# Auditoria 02 — Blocos, Multiblocos, Tile Entities e Receitas de Crafting

**Mod:** Mana and Artifice 3.1.11 (Forge 1.20.1) → port 1.21.1 (NeoForge)
**Fontes:** jar original `mna-forge-1.20.1-3.1.11-all.jar` + `decompiled/` (CFR) vs. `mna-port-workspace/src/main/`
**Data da análise:** port com `BUILD SUCCESSFUL` (compileJava 0 erros) e roda em jogo.

---

## 1. Blocos (`blocks/BlockInit.java`)

### Contagem e categorias

| Fonte | Total de blocos registrados |
|---|---|
| 1.20.1 (`BlockInit`) | **380** |
| Port 1.21.1 | **380** (diff de IDs = vazio, paridade perfeita) |

Registro idêntico: `DeferredRegister<Block>` com namespace `mna`, um `BLOCKS.register("id", ...)` por bloco. IDs idênticos 1:1 (verificado por `diff` dos nomes extraídos — nenhuma divergência).

Distribuição por pacote/classe de bloco (igual nas duas versões):

| Pacote | Arquivos de classe | Papel |
|---|---|---|
| `blocks/artifice` | 34 | Bancadas, altares, eldrin (matriz/conduíte/fumaça/prisma/fole), sentries, fluid jug, lodestar, etc. |
| `blocks/decoration` | 9 | Vidro rúnico (+16 cores +16 panes), estantes, braseiro, etc. |
| `blocks/manaweaving` | 3 | `ManaweavingAltar`, `ManaweaveProjector`, `ManaResevoir` |
| `blocks/pylon` | 6 | `PylonBlock` + 24 instâncias de pilar (freeze, haste, insight…) |
| `blocks/ritual` | 4 | Chalk rune, cristal chimerite, circle of power, teleporte ritual |
| `blocks/runeforging` | 4 | Pedestal, runeforge, runescribing table, bigorna rúnica |
| `blocks/sorcery` | 14 | Blocos espectrais (anvil/crafting/stonecutter/ladder/web/fence), magelight, ilusão, etc. |
| `blocks/utility` | 2 | `CloudBlock`, `InvisibleLight` |
| `blocks/worldgen` | 6 | Flores (aum, wakebloom, cerublossom, tarma root, desert nova), manaweave cache |

Observações de registro relevantes ao port:
- **Nomes com subpasta**: muitos blocos usam IDs com `/` (ex.: `decoration/arcane_stone`, `pylon/inert`). O port mantém esses paths literais nos `register("decoration/...")` — correto, pois são só IDs de registry, não paths de arquivo.
- **Variações por afinidade**: 6× `EldrinMatrixBlock`, 6× `RefractionLensBlock`, 6+6× `EldrinConduitBlock` (normal + lesser), 6× `ElementalSentryBlock`, 16× `ChimeriteCrystalBlock`. Todos presentes no port.
- **Stairs/Wall/Slab** apontam para `BlockState` do bloco-base via `.get().m_49966_()` (→ `defaultBlockState()` no port) — migrados.

### BlockItems (`BlockItemRegistration.java`)

Registro automático: para cada bloco que **não** seja `IDontCreateBlockItem` nem `FlowerPotBlock`, cria um `BlockItem` (`TieredBlockItem`, ou `OffsetPlacerItem` se `IOffsetPlace`). Flores (aum/cerublossom/tarma_root/desert_nova) recebem `BlockItem` vanilla explícito. Flower pots são ligados às plantas no `FMLCommonSetupEvent`.

**Port:** `BlockItemRegistration` migrado para NeoForge — `RegisterEvent` + `BuiltInRegistries.ITEM.key()`, mesmas interfaces (`IDontCreateBlockItem`, `IOffsetPlace`, `TieredBlockItem`, `OffsetPlacerItem`). Paridade mantida.

### Aba criativa
`BuildCreativeModeTabContentsEvent` (`FillCreativeTabs`) exclui ~36 blocos da tab `GENERAL` (círculos, filler, hellfire, espectrais, potes, sentries, etc.). Migrado.

---

## 2. Tile Entities (`TileEntityInit`)

| Fonte | Total |
|---|---|
| 1.20.1 | **56** |
| Port 1.21.1 | **56** (IDs idênticos, diff vazio) |

Padrão de registro: `BlockEntityType.Builder.of(Tile::new, blocos...).build(null)`. Vários tipos agrupam múltiplos blocos num só BE type:
- `ELEMENTAL_SENTRY` → 6 blocos de sentry
- `ELDRIN_CONDUIT_TILE` → 12 conduítes (6 normal + 6 lesser)
- `ELDRIN_MATRIX` → 6 matrizes; `REFRACTION_LENS` → 6 lentes
- `POTION_PYLON` → 17 blocos de pilar; `INTERDICTION_PYLON` → 2
- `RUNIC_LIGHTS` → `runic_light` + `runic_torch`
- `FLUID_JUG` → `fluid_jug` + infinite_water + infinite_lava
- `SIMPLE_SPECTRAL_TILE` → ladder/web/fence; `SPECTRAL_TILE` → crafting/anvil/stonecutter
- `PEDESTAL` → `pedestal` + `pedestal_with_sign`

Classes de BE por pacote: `wizard_lab` (16), `pylon` (5), resto em `tileentities/`.

### Tiles com GUI / menu (`ContainerInit`)

| Fonte | Menus registrados |
|---|---|
| 1.20.1 | **40** |
| Port 1.21.1 | **40** |

Menus de bloco (`of(BlockInit.X)`): INSCRIPTION_TABLE, SEER_STONE, RUNESCRIBING_TABLE, LODESTAR, SPECTRAL_CRAFTING_TABLE, SPECTRAL_ANVIL, SPECTRAL_STONECUTTER, MAGICIANS_WORKBENCH, PARTICLE_EMITTER, THESIS_DESK, STUDY_DESK, DISENCHANTER, ELDRIN_FUME, AFFINITY_TINKER, SPELL_SPECIALIZATION, TRANSCRIPTION_TABLE, ENSORCELLATION_STATION (altar_of_arcana), BOOKSHELF, SCROLLSHELF. Menus de item/puro (`IForgeMenuType.create`): ELDRIN_PERMISSIONS, PYLON, RITUAL_KIT, SPELL_BOOK, GRIMOIRE, ROTE_BOOK, MARK_BOOK, RUNIC_MALUS, ASTRO_BLADE, PHYLACTERY_STAFF, FILTER_ITEM, RIFT, GUIDE_BOOK, ENDER_DISC, SPELL_CUSTOMIZATION, OCCULUS, SPELL_RECIPE_LIST, CANTRIPS, SPELL_ADJUSTMENTS, AREA_ADJUSTMENTS, WANDERING_WIZARD. **Port: todos os 40 presentes, paridade total.**

### Tiles com capability/item handler (1.20.1 `getCapability`)

Tiles 1.20.1 que sobrescrevem `getCapability`:

| Tile | Capability |
|---|---|
| `FluidJugTile` | `ForgeCapabilities.FLUID_HANDLER` (implementa `IFluidHandler`, holder `LazyOptional`) |
| `ManaResevoirTile`, `ManaweaveProjectorTile`, `ManaweavingAltarTile`, `ArcanaAltarTile`, `EldrinMatrixTile`, `EldrinAltarTile` | item handler / capacidades M&A |
| `SpellSpecializationTile`, `StudyDeskTile`, `ThesisDeskTile` | item handler |
| `SanctumTile`, `InterdictionPylonTile`, `RefractionLensTile` | leem outras capabilities (progression etc.) |

**Ver Seção 5 — isto é o ponto fraco do port.**

---

## 3. Multiblocos

### Classe de definição
- Definição: **`com.mna.recipes.multiblock.MultiblockDefinition`** (implementa `IMultiblockDefinition`), com `MultiblockConfiguration` (estrutura + variações + matching) e `MultiblockRecipeSerializer`.
- Base de tile: **`com.mna.api.blocks.tile.MultiblockTile`** (abstrata, estende `TileEntityWithInventory`).

### Como são definidos no datapack
- **Receita**: `data/mna/recipe/multiblock/*.json` com `"type": "mna:multiblock"`. Campos: `structure` (ResourceLocation do `.nbt`), `replacements[]` (variações), `symmetrical`, `matchers[]` (`{block|offset, matcher}`), `tags[]`.
- **Estrutura**: `data/mna/structure/multiblock/*.nbt` (NBT), carregada server-side via `MultiblockConfiguration.loadStructure(registryAccess, structure, blockStates)`.
- **Matchers** (`block_matchers/`): `ExactBlockMatcher` (default), `StairsBlockMatcher`, `StatelessBlockMatcher`, `ChalkBlockMatcher`, `RefractionLensBlockMatcher`, `PedestalBlockMatcher` (+1 = 7 no port).

### Como são montados / verificados em jogo
- `MultiblockTile.match()` roda no tick (a cada `reMatchRate`: 20 ticks quando não-match, ~1000+ quando match), só se há jogador num raio de 32.
- Chama `definition.matchWithCount(world, origin, Rotation.NONE, matchOriginIsCenter())`. Origem centralizada: `origin + (size.x/2, 0, size.z/2)`.
- Matching por bloco via `matchBlockState` → escolhe matcher especial (por offset ou por bloco) ou default/stairs.
- Ao formar, dispara advancement `CompleteMultiblockTrigger` para jogadores da facção.
- **Cliente**: se não carregou, `MultiblockConfiguration.createDummyStructure()` + envia `MultiblockSyncRequestMessage`; servidor responde `StructureSyncMessage` com core blocks serializados em NBT.

### Quantos multiblocks existem
**6 estruturas + 1 de teste = 7 JSON** em `recipe/multiblock/`:

| JSON `recipe/multiblock/` | Estrutura `.nbt` | Quem usa |
|---|---|---|
| `eldrin_altar.json` | `structure/multiblock/eldrin_altar.nbt` | `EldrinAltarTile` (path fixo `multiblock/eldrin_altar`) |
| `council_circle.json` | `council_circle_of_power.nbt` | `SanctumTile` via facção (`getSanctumStructure()`) |
| `demon_circle.json` | `demon_circle_of_power.nbt` | idem |
| `fey_circle.json` | `fey_circle_of_power.nbt` | idem |
| `undead_circle.json` | `undead_circle_of_power.nbt` | idem |
| `wellspring_capture.json` | `wellspring_capture.nbt` | `RefractionLensTile` (path fixo `multiblock/wellspring_capture`) |
| `test_multiblock.json` | `fountain_test` (**.nbt AUSENTE** em ambas as versões) | receita de dev, nunca usada em jogo |

### Os "multiblocks" que o enunciado cita — esclarecimento

Só **3 tiles** estendem `MultiblockTile` (em 1.20.1 **e** no port, idêntico):

| Tile | Estrutura | Observação |
|---|---|---|
| `EldrinAltarTile` | `multiblock/eldrin_altar` | Altar eldrin + 8 pedestais (match por `PedestalBlockMatcher`). Exige estrutura com exatamente 8 pedestais (runtime check). |
| `RefractionLensTile` | `multiblock/wellspring_capture` | Captura de wellspring. |
| `SanctumTile` | dinâmica por facção | **passa `null` no construtor**; `getDefinition()` é sobrescrito para buscar a estrutura da facção alinhada. `getMatchOrigin()` = `pos.above()`. |

**Manaweaving altar, construct workbench, magicians workbench NÃO são multiblocks** em nenhuma versão:
- `ManaweavingAltarTile extends TileEntityWithInventory` ( altar de manaweaving = tile comum com inventário + GUI).
- `ConstructWorkbenchTile extends BlockEntity`.
- `MagiciansWorkbenchTile extends TileEntityWithInventory`.

O altar de **manaweaving** é validado por receita (`recipe/manaweaving/`, tipo `mna:manaweaving-recipe`), não por multiblock. O altar eldrin (`EldrinAltarTile`) é o multiblock de altar.

**Port:** os 3 tiles multiblock, `MultiblockDefinition`, `MultiblockConfiguration`, `MultiblockRecipeSerializer`, os 7 matchers, `MultiblockRenderer`, `StructureSyncMessage` e `MultiblockSyncRequestMessage` estão todos presentes. Paths fixos (`RLoc.create("multiblock/eldrin_altar")`, `multiblock/wellspring_capture`) e o `super(..., null)` do SanctumTile preservados. Datapack: 7 JSON em `recipe/multiblock/` + 6 `.nbt` em `structure/multiblock/`.

---

## 4. Receitas de crafting

Datapack: 1.20.1 = `data/mna/recipes/` (plural); port = `data/mna/recipe/` (singular, exigência 1.21.1). Port tem **1796 JSON** (contém todas as categorias). Receitas vanilla-typed convertidas para `result.id` (formato 1.21.1); receitas `mna:*` mantêm `"type": "mna:..."`.

| Subconjunto | Qtd (1.20.1) | TYPE | Serializer no port (`RecipeInit`) |
|---|---|---|---|
| `decoration` (crafting) | 172 | `minecraft:crafting_shaped` | vanilla (não precisa) |
| `decoration/stonecutting` | 192 | `minecraft:stonecutting` | vanilla |
| `construct_arm_switch` | 136 | `minecraft:crafting_shapeless` | vanilla |
| `eldrin_altar` | 122 | `mna:eldrin-altar` | **OK** `ELDRIN_ALTAR_RECIPE_SERIALIZER` = `serializer("eldrin-altar")` |
| `crushing` | 55 | `mna:crushing` | **OK** `CRUSHING_SERIALIZER` = `serializer("crushing")` |
| `rituals` | 32 | `mna:ritual` | **OK** `RITUAL_SERIALIZER` = `serializer("ritual")` |
| `manaweaving` (recipe) | 36 | `mna:manaweaving-recipe` | **OK** `MANAWEAVING_RECIPE_SERIALIZER` = `serializer("manaweaving-recipe")` |
| `fume_filter` | 18 | `mna:eldrin-fume` | **OK** `ELDRIN_FUME_RECIPE_SERIALIZER` = `serializer("eldrin-fume")` |
| `arcanefurnace` | 6 | `mna:arcane-furnace` | **OK** `ARCANE_FURNACE_RECIPE_SERIALIZER` = `serializer("arcane-furnace")` |
| `components` | 110 | `mna:component` | **OK** `COMPONENT_RECIPE_SERIALIZER` = `serializer("component")` |
| `progression` | 70 | `mna:progression-condition` | **OK** `PROGRESSION_SERIALIZER` = `serializer("progression-condition")` |
| `multiblock` | 7 | `mna:multiblock` | **OK** `MULTIBLOCK_RECIPE_SERIALIZER` = `serializer("multiblock")` |
| `transmutation` | 4 | `mna:transmutation` | **OK** `TRANSMUTATION_SERIALIZER` = `serializer("transmutation")` |
| `modifiers` | 9 | `mna:modifier` | **OK** `MODIFIER_RECIPE_SERIALIZER` = `serializer("modifier")` |
| `shapes` | 21 | `mna:shape` | **OK** `SHAPE_RECIPE_SERIALIZER` = `serializer("shape")` |
| `stone_runes` | 17 | `minecraft:crafting_shapeless` (16) + `crafting_shaped` (1) | vanilla |
| `manaweave_patterns` | 17 | `mna:manaweaving-pattern` | **OK** `MANAWEAVING_SERIALIZER` = `serializer("manaweaving-pattern")` |
| `manaweave_cache_effects` | 29 | `mna:manaweave-cache-effect` | **OK** `MANAWEAVE_CACHE_EFFECT_SERIALIZER` = `serializer("manaweave-cache-effect")` |
| `runeforging` | 14 | `mna:runeforging` (12) + crafting_shaped (2) | **OK** `RUNEFORGING_SERIALIZER` = `serializer("runeforging")` |
| `runescribing` | 14 | `mna:runescribing` (12) + crafting_shaped (2) | **OK** `RUNESCRIBING_SERIALIZER` = `serializer("runescribing")` |
| `staves` / `wands` / `wickerwood_constructs` / `chimerite_crystals` | 21/20/14/16 | vanilla shaped/shapeless | vanilla |

**RecipeType**: os 16 tipos (`RITUAL_TYPE`, `CRUSHING_TYPE`, `MULTIBLOCK_TYPE`, `ELDRIN_ALTAR_TYPE`, etc.) estão registrados no port via `type("...-type")`. O nome do tipo é `mna:<id>` (ex.: `mna:eldrin-altar-recipe-type`), casa com o `RecipeType` retornado por cada receita (`m_6671_()`/`getType()`).

**Nomenclatura dos serializers:** os IDs registrados no port (`eldrin-altar`, `crushing`, `manaweaving-recipe`, `eldrin-fume`, `progression-condition`, `manaweaving-cache-effect`, `multiblock`, etc.) coincidem exatamente com os `"type": "mna:<id>"` dos JSON — verificado. Todos os 16 serializers + 16 tipos presentes, sem falta.

**Nota:** `test_multiblock.json` aponta para `structure: mna:multiblock/fountain_test`, cujo `.nbt` não existe em nenhuma versão — mas é receita de dev (nunca instanciada por nenhum tile), sem impacto em jogo.

---

## 5. Capabilities dos tiles → attachments

### O que mudou de 1.20.1 → 1.21.1
- **1.20.1**: capabilities via `getCapability(Capability, Direction)` + `LazyOptional`. Itens via `ForgeCapabilities.ITEM_HANDLER` (SidedInvWrapper); fluidos via `ForgeCapabilities.FLUID_HANDLER`.
- **1.21.1 (NeoForge)**: capacidades de player/entity/chunk/world viraram **data attachments** (`AttachmentType`), registradas em `CapabilitiesInit`. As de item/fluido de bloco deveriam ser expostas via `RegisterCapabilitiesEvent` ou override de `getCapability`.

### CapabilitiesInit (port)
Registra **7 attachments**: `PLAYER_MAGIC`, `PLAYER_PROGRESSION`, `PLAYER_ROTE_SPELLS`, `WORLD_MAGIC`, `CHUNK_MAGIC`, `ENTITY_MAPFX`, `ENTITY_PARTICLE_AURA`. Cobrem as capabilities de player/world/chunk/entity do 1.20.1 — correto.

### Itens e fluidos de bloco — LACUNA
- **Não há nenhum override de `getCapability` em todo o port** (grep em toda a árvore = 0).
- **Não há nenhum `RegisterCapabilitiesEvent`** no port.
- `TileEntityWithInventory` cria `sidedHandlers` (`SidedInvWrapper` por `Direction`) mas **nunca os expõe** — o campo existe, `getOrCreateSidedHandler` existe, mas nada retorna `ForgeCapabilities.ITEM_HANDLER`.
- `FluidJugTile` **implementa `IFluidHandler`** (migrado, com `isInfinite`/`infiniteFluidType` corretos) mas **não expõe a capabilidade** de fluido.

Consequência: hoppers, pipes e automação de outros mods **não** conseguem inserir/extrair itens dos tiles M&A nem drenar/encher o fluid jug por capability. A lógica interna (containers M&A, GUIs, receitas) funciona porque usa os métodos do tile direto — por isso o mod "roda", mas a integração por capability está ausente.

**Nota:** `InfiniteFluidCapabilityHandler`/`InfiniteFluidJug` (1.20.1) são de **item** (fluid jug infinito como item), não do tile de bloco. O item no port usa `InfiniteFluidJug` — precisa de verificação de exposição à parte (fora do escopo estrito de blocos, mas mesmo padrão de lacuna pode se aplicar).

---

## Tabela final — O que precisa estar / como está no 1.21.1 / STATUS

| Item | Precisa estar (1.20.1) | Como está no port 1.21.1 | STATUS |
|---|---|---|---|
| Blocos registrados | 380 (`BlockInit`) | 380, IDs idênticos (diff vazio) | **OK** |
| Categorias de bloco | artifice/decoration/manaweaving/pylon/ritual/runeforging/sorcery/utility/worldgen | mesmos pacotes, mesmas contagens | **OK** |
| BlockItems auto (`TieredBlockItem`/`OffsetPlacerItem`, pots, flores) | `BlockItemRegistration` | migrado p/ `RegisterEvent` + `BuiltInRegistries`, mesma lógica | **OK** |
| Tile Entities | 56 (`TileEntityInit`) | 56, IDs idênticos, tipos agrupados iguais | **OK** |
| Menus/containers | 40 (`ContainerInit`) | 40, IDs idênticos | **OK** |
| Definição de multiblock | `MultiblockDefinition` + `MultiblockConfiguration` + matchers | todos presentes (7 matchers) | **OK** |
| Base de multiblock | `MultiblockTile` (`api/blocks/tile`) | presente, `match`/tick/advancement migrados | **OK** |
| Tiles multiblock | EldrinAltar, RefractionLens, Sanctum | mesmos 3, paths/`super(null)` preservados | **OK** |
| JSON multiblock | 7 em `recipe/multiblock/` | 7 presentes, `type: mna:multiblock` | **OK** |
| Estruturas multiblock `.nbt` | 6 em `structure/multiblock/` | 6 presentes | **OK** |
| Sync de estrutura cliente | `StructureSyncMessage` + `MultiblockSyncRequestMessage` | ambos presentes | **OK** |
| Renderizador de multiblock (ghost) | `MultiblockRenderer` | presente | **OK** |
| Serializers de receita `mna:*` | 16 tipos + 16 serializers | 16 + 16, IDs casam com os JSON | **OK** |
| Receita `mna:eldrin-altar` | 122 JSON | 122, type preservado | **OK** |
| Receita `mna:crushing` | 55 JSON | 55 | **OK** |
| Receita `mna:ritual` | 32 JSON | 32 | **OK** |
| Receita `mna:manaweaving-recipe` | 36 JSON | 36 | **OK** |
| decoration crafting / stonecutting | 172 / 192 (vanilla) | 172 / 192, `result.id` convertido | **OK** |
| construct_arm_switch | 136 (shapeless) | 136 | **OK** |
| Capabilities player/world/chunk/entity | attachments `CapabilitiesInit` | 7 attachments registrados | **OK** |
| **Capability de ITEM dos tiles** (`ITEM_HANDLER`, sided) | via `getCapability` | **sem `getCapability`, sem `RegisterCapabilitiesEvent`**; `sidedHandlers` criados mas não expostos | **FALTA** |
| **Capability de FLUIDO** (`FLUID_JUG`) | via `getCapability` | `FluidJugTile` implementa `IFluidHandler` mas **não expõe** a cap | **FALTA** |
| `test_multiblock` → `fountain_test.nbt` | ausente em 1.20.1 | ausente no port (receita de dev, sem uso) | **PARCIAL** (inerte) |

---

## Resumo do que está QUEBRADO / FALTANDO no port

1. **Exposição de capabilities de bloco (item handler + fluid handler) — FALTA.**
   - Nenhum `getCapability` override, nenhum `RegisterCapabilitiesEvent` no port inteiro.
   - `TileEntityWithInventory.sidedHandlers` (SidedInvWrapper) existe mas não é retornado por capability → hoppers/pipes não interagem.
   - `FluidJugTile` implementa `IFluidHandler` mas não registra/exponde `ForgeCapabilities.FLUID_HANDLER` → não drenável/enchível por automação.
   - **Correção:** registrar `RegisterCapabilitiesEvent` no mod bus — `event.registerBlockEntity(ForgeCapabilities.ITEM_HANDLER, TileEntityInit.X, (be, side) -> be.getOrCreateSidedHandler(side))` para os tiles com inventário, e `event.registerBlockEntity(ForgeCapabilities.FLUID_HANDLER, TileEntityInit.FLUID_JUG, (be, side) -> be)`. (NeoForge 1.21.1 aceita `registerBlockEntity`; alternativa: override `getCapability` nos tiles.)

2. **`test_multiblock` / `fountain_test` — PARCIAL/inerte.** Receita de dev sem `.nbt`; não afeta jogo. Opcional remover o JSON ou adicionar o `.nbt` só por completude.

3. **Não há divergências de registro** de blocos (380), tile entities (56), containers (40) nem de serializers/tipos de receita (16+16) — paridade total confirmada por diff de IDs.

**Nota sobre o `PORT_STATUS.md` do workspace:** está desatualizado (descreve 47k erros SRG). O build real atual é `BUILD SUCCESSFUL` (0 erros, 100 warnings de deprecation). O muro SRG já foi resolvido.
