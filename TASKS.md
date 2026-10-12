# TASKS — Mana and Artifice → NeoForge 1.21.1 (até o primeiro teste)

> Estado inicial desta fase: 8.815 erros de compilação (após remap SRG + registries).
> "Primeiro teste" = o mod **compila**, gera JAR que **carrega** sem crash no NeoForge 1.21.1.

## STATUS (retomada "até a barreira grande"; 5.118 → 5.086)
- **Barreira grande atingida:** C Capabilities→Attachments (~307 + 453 call-sites, ATOMICO) domina o restante.
- Confirmei na prática que os blocos secundários NÃO cedem a passe rápido: `load/saveAdditional` (BlockEntity provider-threading,
  net −2, revert limpo), `TickEvent`, `getEnchantments` — todos exigem migração dedicada completa.
- ✅ win limpo: `motion` (FeyArmorItem/EntityUtil) `Vec3 motion=null` perdido na descompilação → −32.
- Build: 5.086, network/ 0, limpo e verificado.
- PRÓXIMO (para retomar, passos dedicados por-bloco): C Capabilities→Attachments em primeiro; depois enchantment→data-driven,
  menus, provider-threading (BlockEntity load/save, ItemStack NBT, recipes), Tier, armaduras/atributos, DamageEventHandler.
- **Progresso total: 46.940 → 5.118 erros** (clean compile). **Sessão: 7.610 → 5.118 (−2.492).**
- ✅ **NBT residual:** PatterningPrism + ItemStack-receivers migrados (DataComponents) com cuidado;
  REVERTIDOS SpellRecipe/CantripRegistry/PractitionersPouch/ItemRuneMarking/RuneMarkingPair (receptores não-ItemStack → regressões eliminadas).
- ✅ **`defineSynchedData(SynchedEntityData.Builder)`** (raiz dos 174 defines!) — 56 entidades, `builder.define(...)`, −195.
- ✅ **`getEnchantments`:** novo helper `EnchantmentUtils.enchantmentMap` (ItemEnchantments→Map), 29 usos migrados.
- **PRÓXIMO:** C Capabilities→Attachments (topo, ~307 LazyOptional/Capability/Token/ForgeCapabilities, atômico);
  TierSorting/tier-rework (`Tier.getIncorrectBlocksForDrops`→TagKey, `getLevel` removido);
  menus (IForgeMenuType+openScreen-3arg); TickEvent.Phase/event.field; armor/atributos data-driven; threading Provider.
- **Progresso total: 46.940 → 5.381 erros** (clean compile). **Sessão: 7.610 → 5.381 (−2.229).**
- ✅ **E2 `DeferredHolder`** R correto (−~2.000), `onAddedToLevel/onRemovedFromLevel`, imports mortos,
  `@Cancelable` removido, `getAttribute(Holder)`, `getResultItem` revert, EntityInit builder-limpo.
- **REVERSÕES (padrão importante):** os blocos restantes NÃO cedem a fix mecânico de 1 passo —
  `getResultItem(Provider)`, `EntityType.Builder.of(X::new)`, `TickEvent`→`event.tick`, `getEnchantments(...).size()`
  todos precisaram de revert (ficavam piores). **Regra mantida: nunca deixar o build pior.**
- **PRÓXIMO (blocos atômicos/deep, exigem rodadas dedicadas por-bloco):**
  C Capabilities→Attachments (LazyOptional ~220 + 453 call-sites), enchantment→data-driven, menus
  (IForgeMenuType + openScreen-3arg), TickEvent.Phase/event.level (CommonEventHandler), armaduras
  `MAArmorMaterial`→`ArmorMaterial`/`AttributeModifier` ctor/`RecordItem`/`DyeableLeatherItem`,
  threading `HolderLookup.Provider` (ItemStack NBT save/of, BlockEntity, recipes `getResultItem(Provider)`/`getType`).
- **Progresso total: 46.940 → 5.479 erros** (clean compile). **Nesta sessão: 7.610 → 5.479 (−2.131).**
- ✅ **E2 genérico `DeferredHolder`** (fallout dos registries) — o R estava errado:
  `DeferredHolder<X,? extends X>` → `DeferredHolder<Item/Block/MobEffect/Enchantment, ? extends X>` (ItemInit 571, BlockInit 380, EffectInit 69, EnchantmentInit 22, ParticleInit 53);
  genéricos `EntityType<?>`/`BlockEntityType<?>`/`MenuType<?>` (3 arquivos); spells `(Object)new`→`new` (SpellsInit 152 → atual 0);
  EntityInit builder: removidos `fireImmune()/noSummon()/noSave()/setShouldReceiveVelocityUpdates()` (102) + `SpawnPlacementRegisterEvent`→`RegisterSpawnPlacementsEvent`.
- **PRÓXIMO:** EntityInit `EntityType.Builder.of()` factory (~60) e spawn-place register; ItemInit/BlockInit (RecordItem, armaduras/MAArmorMaterial data-driven, forward-ref, FlowerPotBlock);
  depois: C Capabilities→Attachments (LazyOptional 222), enchantment→data-driven, menus (IForgeMenuType), define(175), NBT residual, provider-threading.
- **Progresso total: 46.940 → 6.720 erros** (clean compile).
- ✅ **FASE B NBT parcial**: ItemStack `getTag`→`getOrDefault(CUSTOM_DATA).copyTag()`, `getOrCreateTag`, `hasTag→has(..)`,
  `setTag→set(..)`, `getOrCreateTagElement` migrados p/ ~80 arquivos limpos; REVERTIDOS os 45 arquivos com tile/custom
  receivers (tiles usam `getTag()` próprio). Residual: getTag 70, hasTag 25, getOrCreateTagElement 33.
- ✅ **H/F mecânicos**: ADDITION→ADD_VALUE, setSecondsOnFire→igniteForSeconds, ForgeEventFactory removido,
  setHoverName→set(ITEM_NAME), getDamageSource, RenderUtil (GeckoLib), Render/VertexConsumer (A4).
- **PRÓXIMO:** define(174, cascade SEnchedEntityData/Construct), C Capabilities→Attachments (LazyOptional 221, atomic),
  getId(143), enchantments (getEnchantments/Rarity/EnchantmentCategory/TierSortingRegistry ~175),
  NetworkHooks 3-arg(94)+IForgeMenuType(81)=sistema de menus.
- **Progresso total: 46.940 → 7.069 erros** (clean compile verificado).
- ✅ **FASE D** mensagens+canal+handlers+dispatchers; **openScreen 2-arg → openMenu** (7); **getEntitySpawningPacket ×47** removidos;
  **IEntityAdditionalSpawnData ×12** removido. Fica: openScreen 3-arg (14) + `IForgeMenuType` (81) = sistema de menus.
- ✅ **FASE A4 render**: `VertexConsumer` → `addVertex`/`setColor`/`setUv`/`setNormal`/`setLight`/`setOverlay`,
  `endVertex` removido (31 arquivos); corrigido `PoseStack.Pose`.normal()`.
- ✅ **FASE F parcial**: `NonNullLazy→Supplier`, `MobType` removido (`MATools.isUndead` novo), `Bludgeoning`,
  `AttributeModifier.Operation` ADDITION→ADD_VALUE, `setSecondsOnFire→igniteForSeconds`, `ForgeEventFactory` removido.
- ✅ **H parcial**: `setHoverName→set(ITEM_NAME,..)`, `event.getSource→getDamageSource`, `RenderUtils→RenderUtil` (GeckoLib 4).
- **PRÓXIMO (maiores blocos restantes):** B NBT→DataComponents (getTag/getOrCreateTag/hasTag ~570);
  C Capabilities→Attachments (LazyOptional ~300, atomic); H define(174)/getId(140);
  F enchantments (Rarity/getEnchantments/EnchantmentCategory/TierSortingRegistry ~174); menus (IForgeMenuType 81 + openScreen-3arg).
- **Progresso total: 46.940 → 7.610 erros**.
- ✅ **FASE A** concluída (EventBusSubscriber→top-level, -119; JEI/Lucent/Lootr; PathType; etc).
- ✅ **FASE E (registries custom)** — núcleo completo e verificado: `MnaRegistry` wrapper + `com.mna.Registries`
  (`DeferredRegister.create(ResourceKey)` + `makeRegistry`), 218 call-sites, `IForgeRegistry`→`MnaRegistry` sign.
  Recipe `itemRegistry`→`BuiltInRegistries.ITEM.get`, renames `NeoForgeRegistries.*`→`BuiltInRegistries.*`
  (MOB_EFFECTS→MOB_EFFECT, ENTITY_TYPES→ENTITY_TYPE, FLUIDS→FLUID, Keys.SOUND_EVENTS→Registries.SOUND_EVENT),
  `ForgeRegistry`/`IForgeRegistry` imports removidos (0 restantes).
- ✅ **FASE G (Advancements)** — 30 triggers + 3 predicates + registro para codecs 1.21.1. Build 7.958 → 7.610.
- ✅ **FASE D — ESTÁGIO 1 (mensagens) COMPLETO**: 76 mensagens → `CustomPacketPayload`
  (`TYPE`+`StreamCodec.of`+`type()`); `MAPacketHandler` → `PayloadRegistrar` (31 playToClient/45 playToServer,
  classificação por BASE — ex.: InscriptionTableCraftingUpdate é client); handlers → `IPayloadContext`;
  `ClientMessageDispatcher`/`ServerMessageDispatcher` → `PacketDistributor` (sendToServer / PDIST.send).
  Pacote `network/` compila com 0 erros; build 7.762 → 7.629 (pós-migração de envio).
- **FASE D — ESTÁGIO 2 (restante)**: D4 `NetworkHooks.openScreen` (~21, 2/3 args) → `player.openMenu`
  (+payload de dados de menu) e `NetworkHooks.getEntitySpawningPacket` ×47 → `getAddEntityPacket(ServerEntity)`;
  D5 `IEntityAdditionalSpawnData` ×12 → spawn-data do Entity (NeoForge). Bloqueador atual p/ D4/D5.
- **PRÓXIMO após D estágio 2:** C capabilities→attachments (serializers C0 prontos) ou F/H.

## FASE D — Networking antigo → payloads (NeoForge 1.21)
- [ ] D1. `NetworkHooks` (133): `openScreen` com payload (não FriendlyByteBuf writer)
- [ ] D2. `NetworkDirection` (49): remover/migrar
- [ ] D3. `IEntityAdditionalSpawnData`/`getEntityData` (24): reescrever
- [ ] D4. `MAPacketHandler` (network) + mensagens → payloads registrados (`IPayload`/`StreamCodec`)

## FASE A — Corrigir imports/API mecânicas (reduz rápido)
- [ ] A1. `@ObjectHolder` (415) — import correto no NeoForge 1.21 (`net.neoforged.neoforge.registries.ObjectHolder`)
- [ ] A2. `RegistryObject` remanescentes (225) — tipos genéricos complexos e usos (`RegistryObject.class`/returns) → `DeferredHolder`
- [ ] A3. `@Mod.EventBusSubscriber` / `EventBusSubscriber` (121) — imports multiline do anotação
- [ ] A4. `Renderer`: `vertex(...)`/`PoseStack`/`renderBackground`/`ImageButton`/`addRenderableWidget` remanescentes (render API 1.21)
- [ ] A5. `BlockPathTypes` (184) — import correto (pacote mudou no 1.21)
- [ ] A6. JEI (`mezz.jei.*`) (~150) — decidir: adicionar JEI dep ou remover/stub das classes de integração
- [ ] A7. Integrações 3rd-party opcionais: `com.legacy.lucent` (Lucent) e `noobanidus.mods.lootr` (Lootr) — guardar/remover
- [ ] A8. Nomes/pacotes menores: `ToolActions`→`ItemAbilities`, `Rarity`, `MobType`, `EnchantmentCategory`, `TierSortingRegistry`, `IForgeRegistry`, `NetworkDirection`, `RenderUtils`(geckolib)

## FASE B — NBT → Data Components (item data) (~400)
- [ ] B1. `ItemStack.getTag()`→`getComponents()`, `getOrCreateTag()`→`getComponents()`, `hasTag()`, `setTag()`
- [ ] B2. `CompoundTag` em items → `Component`/DataComponentPatch; adaptar leituras/escritas

## FASE C — Capabilities → Attachments (maior bloco, profundo)
> ⚠️ Executar em BLOCO ÚNICO (não deixar pela metade). Desenho validado (design_refs/CapabilitiesInit.attachments.reference.java).
> ✅ **C0 FEITO (verificado, sem regressão — build em 7.802):** 5 serializers criados e compilando limpos:
>   `PlayerMagicSerializer`, `PlayerProgressionSerializer`, `PlayerRoteSpellsSerializer`,
>   `WorldMagicSerializer`, `ChunkMagicSerializer` (uso `IAttachmentSerializer` com a lógica
>   movida dos providers; `ResourceLocation.parse`; `ContainerHelper` com provider;
>   `Pair`=com.mojang.datafixers.util.Pair; `ISpellComponent`=com.mna.api.spells.base).
> RESTANTE (atômico — getters+call-sites juntos; erro sobe antes de cair):
> - [ ] C1. Recriar `CapabilitiesInit` com `DeferredRegister.create(Registries.ATTACHMENT_TYPE,"mna")` (
>       referência: design_refs/) usando `builder(Concrete::new).serialize(new XSerializer())`
>       (+ `.copyOnDeath()` p/ player); registrar no bus do mod no construtor de `ManaAndArtifice`.
>       OBS: desbloquear `com.mna.Registries.Factions` (IForgeRegistry → IRegistryExtension) antes,
>       pois `PlayerProgression/WorldMagicSerializer` dependem disso.
> - [ ] C2. API `ManaAndArtificeMod`: `Capability<T>`→`Supplier<AttachmentType<Concreto>>`; getters retornam o
>       Supplier; remover `setXCapability` e os campos `__cap`.
> - [ ] C3. Call-sites (~253): `x.getCapability(g).orElse(null)`→`x.getData(g)`;
>       `getCapability(...).ifPresent(lam)`→`if (x.hasData(g)) lam.accept(x.getData(g))`;
>       remover arg `Direction` onde houver. `Level`→`getData`, `Chunk`→`getData`, `BlockEntity`→`getData`, `Entity`→`getData`.
> - [ ] C4. Remover: providers/factories antigos, `MACapabilityForgeEventHandlers`, `MACapabilityModEventHandlers`,
>       e o `@EventBusSubscriber` de `CapabilitiesInit` antigo.
> - [ ] C5. `CurioItemCapability` (api/capabilities) → converter p/ attachment em item caso ainda referencie old-API.



## FASE D — Networking antigo → payloads (NeoForge 1.21)
- [ ] D1. `NetworkHooks` (133): `openScreen` com payload (não FriendlyByteBuf writer)
- [ ] D2. `MAPacketHandler` (SimpleChannel) → `RegisterPayloadHandlersEvent`/`PayloadRegistrar`
- [ ] D3. 86 mensagens: adaptar encode/decode para CustomPacketPayload

## FASE E — Registries/RegistryObject/Holders (restante)
- [ ] E1. `IForgeRegistry`(43)/`IForgeMenuType`(41)/`RegistryObject.getId()`(95)
- [ ] E2. `DeferredRegister` restantes; `Registries.X` corretos
- [ ] E3. `RecipeType`/`RecipeSerializer` generics

## FASE F — MobEffect/Attribute/Enchantment → Holder (data-driven)
- [ ] F1. `MobEffect`↔`Holder<MobEffect>` (~230 + MobEffectInstance ctor)
- [ ] F2. `Attribute`↔`Holder<Attribute>` (32); `Enchantment`↔`Holder<Enchantment>` (38+15)
- [ ] F3. `EnchantmentCategory` removido → data-driven

## FASE G — Advancements/Triggers (1.21)
- [ ] G1. `AbstractCriterionTriggerInstance`/`DeserializationContext`/`SerializationContext` (58x3)
- [ ] G2. `CriteriaTriggers.register` (29) e triggers custom

## FASE H — GUI/Menus/Other (1.21)
- [ ] H1. `registerScreenFactory` privado (37) — usar `MenuConstructor`/`IContainerFactory`
- [ ] H2. misc: `getEntityData().define`(131), `getId`, `setHoverName`, `motion`, `y0`, `ADDITION`, `of`

## FASE I — Build do JAR de teste
- [ ] I1. Zerar erros de compilação → `gradle build` gera JAR
- [ ] I2. Copiar JAR para `mna-1.21.1-test/`
- [ ] I3. Smoke-test de carga (boot) num client NeoForge 1.21.1, se possível

---
Legenda: ✔ mecânico/seguro · ⭐ grande reescrita · 🔧 precisa decisão
