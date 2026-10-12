# Port Mana and Artifice 1.20.1 (Forge) → 1.21.1 (NeoForge) — Status

> Documento vivo do port. Gerado em 20/08/2026. O mod é **closed-source** ("All rights
> reserved") e o JAR `mna-forge-1.20.1-3.1.11-all.jar` foi **descompilado com CFR** (sem
> acesso ao repositório-fonte).

## 1. O que já está funcionando (verificado por compilação real)

| Etapa | Resultado |
|---|---|
| Pipeline de build NeoForge 1.21.1 | **OK** — `gradle compileJava` roda e compila até o passo de compilação do mod |
| `build.gradle` (plugin moddev 2.0.71) | Corrigido: versão `2.0.74-beta` não existia; a partir de `2.0.71` são estáveis |
| `accesstransformer.cfg` | Corrigido: linha 10 tinha `LootContext;` — o parser do NeoForge 1.21.1 rejeita o `;` final. Removido |
| **BOM UTF-8** em 488 arquivos .java | **Removeu ~14.000 erros de uma vez** (14.583 → ~6.000). BOM (`\ufeff`) quebrava o parser do javac gerando cascatas de `class, interface, enum, or record expected` |
| 7 arquivos com quebra de sintaxe do CFR | Corrigidos: 6× artefato `LambdaMetafactory.metafactory(...)` reconstruído como lambda real (`ComponentSpectral{Anvil,CraftingTable,Fence,Ladder,Stonecutter,Web}`) + `ThreatTable` (parâmetros `? super T` inválidos → `Integer`/`Float`) |
| Importações `net.minecraftforge.*` | Migradas para `net.neoforged.*` / `net.neoforged.neoforge.*` (123 arquivos); renames de classe: `MinecraftForge→NeoForge`, `ForgeHooks→CommonHooks`, `ForgeConfigSpec→ModConfigSpec`, `ForgeMod→NeoForgeMod`, `ForgeRegistries→NeoForgeRegistries` |
| Importações GeckoLib (`core.*`) | Relocadas para o layout do GeckoLib 4.8.4 (68 arquivos): `core.animation→animation`, `core.animatable→animatable`, `core.keyframe.event→event`, `core.object.PlayState→animation`, `core.object.Color→util`, `CoreGeoBone→cache.object.GeoBone` |

## 2. Estado atual da compilação

```
47.062 erros  (javac com -Xmaxerrs 100000)
├─ 44.913  cannot find symbol   ← ~95% — nomes SRG (m_XXXX_, f_XXXX_)
├─   196   ResourceLocation(String,String) private access
├─   140   ResourceLocation ↔ tipo incompatível
├─   130   package ForgeRegistries does not exist  (já renomeado p/ NeoForgeRegistries)
├─    55   Object cannot be converted to Tag
├─    46   MobEffect ↔ Holder
├─    45   Object cannot be converted to ItemStack
└─   (~1.100) JEI, capabilities→attachments, DeferredRegister/RegistryObject,
             TickEvent, ImageButton, MobEffectInstance, etc.
```

**O número subiu de 6.375 para 47.062** porque, com os imports corrigidos, o javac
agora consegue penetrar fundo nos arquivos e relata **cada** referência SRG — antes a
análise era truncada nos erros de import. 95% dos erros são o **muro SRG**.

## 3. O muro SRG — e por que é o ponto central

O JAR 1.20.1 roda em **namespaces SRG** (o runtime do Forge usa SRG), então o CFR
descompilou para nomes SRG: `stack.m_41609_()`, `level.m_5776_()`, `living.f_20883_`,
`Mth.m_14109_()`, etc.

O **NeoForge 1.21.1 usa Mojang (mojmap)** — esses nomes SRG **não existem** e geram os
~45.000 `cannot find symbol`.

**Solução (o maior alavanca possível):** remapear SRG → mojmap (1.20.1) em lote sobre
toda a árvore `src/main/java`. Muitos nomes mojmap de 1.20.1 são idênticos em 1.21.1
(ex.: `clip()`, `getType()`, `level`, `dimensions`), então isso eliminaria a grande
maioria dos 45k de uma vez, deixando só o drift real de API 1.20.1→1.21.1.

**Blocker:** precisa do arquivo de mapeamento `obf→srg` do Forge 1.20.1 (para compor
`client_mappings.txt` (obf→mojmap) + srg → `SRG→mojmap`). Não foi possível obter por
Maven nesta sessão (artefatos legados do Forge não estão acessíveis de forma confiável).

**Como obter (próximo passo):**
1. Com um MDK Forge 1.20.1 (`build.gradle` com `mappings channel: 'official'` e
   `-P...`), rodar `gradle` (ForgeGradle) — ele baixa o `srg`/`mojmap` para o cache:
   `%USERPROFILE%\.gradle\caches\fml_loader` / `forge_gradle\minecraft` ou
   `moddev` → `~/.gradle/caches/neoformruntime` da versão 1.20.1.
2. Alternativa: usar `net.minecraftforge:forgesrg`/`specialsource` com o `joined.tsrg`.
3. Uma vez com `srg→mojmap` em TSRG, o script `remap_srg_to_mojmap.py` (ver §6)
   aplica em toda a árvore.

## 4. Trabalho restante (fora do SRG)

Após o remap SRG→mojmap, restam as migrações reais de API (ordem de prioridade):

1. **Capabilities → Attachments** (sistema reescrito; ~273 classes no scan original).
   `LazyOptional`/`CapabilityManager`/`CapabilityToken`/`ICapabilitySerializable`
   **não existem** em 1.21.1; `getCapability()` virou `getData(AttachmentType)`.
   `ForgeCapabilities→Capabilities`, `ICapabilityProvider` moveu para
   `net.neoforged.neoforge.capabilities`.
2. **Networking** — `MAPacketHandler` usa `NetworkRegistry.newSimpleChannel` +
   `SimpleChannel` (remoção). Reescrever para o sistema **payloads**
   (`RegisterPayloadHandlersEvent`/`PayloadRegistrar`). 86 mensagens.
3. **Registros/DeferredRegister** — em 1.21.1 `DeferredRegister.register` retorna
   `DeferredHolder` (não `RegistryObject`); `IForgeRegistry→IRegistryExtension`,
   `RegistryObject→DeferredHolder`.
4. **JEI interop** (`mezz.jei.*`) — JEI não é dependência; optar por adicionar JEI ou
   guardar/remover as classes de integração.
5. **Misc Minecraft 1.21.1** — `ResourceLocation(String,String)` é privado →
   `ResourceLocation.fromNamespaceAndPath`; `Enchantment`/`Ingredient` finais;
   `TickEvent` dividido; `ImageButton`; `MobEffect↔Holder`; advancement triggers;
   `ToolActions→ItemAbilities`; etc.

## 5. Dependências usadas no projeto

- `neoforge` 21.1.53 (NeoForge 1.21.1)
- `curios-neoforge-9.5.1+1.21.1.jar` (lib, em `libs/`)
- `geckolib-neoforge-1.21.1-4.8.4.jar` (lib, em `libs/`)
- Java 21, Gradle 8.10.2 (wrapper ausente — usar binário em
  `C:\Users\lucas\AppData\Local\hermes\tools\gradle-8.10.2\bin\gradle`)

## 6. Scripts/artefatos criados

- `mna-port-workspace/portdata/mojmap_client.txt` — mapeamento oficial Mojang
  1.20.1 (obf↔mojmap), baixado.
- **`remap_srg_to_mojmap.py`** — a criar: aplica um TSRG SRG→mojmap sobre todos os
  `.java` (próximo passo quando o mapping estiver disponível).
- `mna-port-workspace/build.gradle`, `gradle.properties`, `settings.gradle` — projeto
  NeoForge 1.21.1 funcional até a compilação do mod.

## 7. Conclusão honesta

O **pipeline de build do port está pronto e validado** e uma grande classe de erros
mecânicos foi eliminada. Porém o **port completo de um mod closed-source de ~2.400
classes não é concluível numa única sessão**: exige o mapeamento SRG→mojmap (o
alavanca de ~45k erros) e depois a migração de vários sistemas (capabilities,
networking, registres, JEI). Estimativa realista (já calculada anteriormente):
**500–800 horas** de trabalho. Este documento é o mapa exato para continuar.

---

## ATUALIZAÇÃO (20/08) — MARCO: muro SRG derrubado

### Mapeamento SRG→mojmap obtido e aplicado
- Gerado via MDK Forge 1.20.1 (ForgeGradle 6): tarefa **`gradlew createSrgToMcp`** →
  `build/createSrgToMcp/output.srg` (formato `CL:/FD:/MD:`, SRG→MCP/official/mojmap).
- Números SRG (m_XXXXX_/f_XXXXX_) são **globalmente únicos** → substituição global
  por token segura.
- **Remap aplicado em 1.420 arquivos · 59.104 substituições** (todas as referências SRG).

### Progresso da compilação (evidência real)
```
antes  do remap:  46.940 erros  (44.967 = cannot find symbol SRG)
depois do remap:   9.560 erros  (SRG sumiu; restam drift real de API)
+fix ResourceLocation ctor:  9.318 erros
++fix addRenderableWidget ctor:  9.176 erros
++migração registries (RegistryObject→DeferredHolder, DeferredRegister.create→Registries):  8.815 erros
++Fase A: ObjectHolder(removido), Bus.GAME, PathType, JEI/Lucent(Lootr), NeoForgeRegistries, ToolActions:  7.802 erros
```

### Erros restantes (9.318) — drift real 1.20.1→1.21.1, por categoria
- `MobEffect↔Holder<MobEffect>` (~240) · `Attribute↔Holder` (~32) · `Enchantment↔Holder`
- `ResourceLocation(String,String)` privado → `fromNamespaceAndPath`/`parse` (quase todo)
- capabilities→attachments: `getCapability()`→`getData(AttachmentType)`, `LazyOptional`
  removido, `net.neoforged.neoforge.common.capabilities`→`capabilities`
- registries: `DeferredRegister`→`DeferredHolder`, `RegistryObject`→`DeferredHolder`
- rede: `SimpleChannel`→payloads · GUI: `addRenderableWidget`, `ImageButton`,
  `registerScreenFactory` · JEI (mezz) sem dependência · `TickEvent` dividido

### Artefatos novos
- `mna-port-workspace/portdata/srg_to_mcp.srg` — tabela SRG→MCP (reutilizável)
- `mna-port-workspace/portdata/obf_to_srg.tsrg`, `mojmap_client.txt`
- `srg-gen/` — projeto MDK Forge 1.20.1 usado para gerar o mapping
- Procedimento documentado no skill `forge-to-neoforge-port`
