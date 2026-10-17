# Inventário Quantitativo — Mana and Artifice 3.1.11 (1.20.1 → 1.21.1)

Documento base da dissecação. Contagens extraídas do fonte descompilado 1.20.1
(`decompiled/`) e do workspace do port 1.21.1 (`mna-port-workspace/`).

## 1. Código-fonte (Java) — 1.20.1

Total: **1871 arquivos `.java`** em `com/mna/`.

| Pacote | .java | Domínio |
|---|---:|---|
| entities | 300 | entidades, mobs, projecteis, constructs |
| api | 257 | API pública (capabilities, spells, tools, entities base) |
| items | 219 | itens (staves, wands, grimórios, runas, armor...) |
| blocks | 199 | blocos |
| gui | 164 | telas/menus/containers |
| spells | 151 | sistema de magia (shapes/components/modifiers, casting) |
| network | 88 | pacotes de rede |
| effects | 70 | mob effects |
| tools | 52 | utilitários/reflexão/fachadas |
| recipes | 51 | serializers de receita custom |
| rituals | 41 | sistema de rituais |
| particles | 38 | partículas |
| capabilities | 37 | capabilities (→ attachments no 1.21.1) |
| advancements | 33 | 29 triggers custom |
| guide | 28 | guia/livro de receitas |
| interop | 26 | integrações (Curios, GeckoLib, Lootr...) |
| enchantments | 21 | enchants custom |
| mixins | 16 | mixins (16 aplicando no port) |
| loot | 15 | loot functions/modifiers/conditions |
| apibridge | 13 | ponte de eventos |
| inventory | 12 | utilidades de inventário |
| events | 10 | handlers de evento |
| factions | 6 | facções |
| config | 6 | configs |
| commands | 4 | comandos |
| villagers | 3 | profissões/POI |
| sound | 3 | SFX/Music |
| cantrips | 2 | cantrips |
| brewing | 2 | poções custom |
| Registries | 1 | registries custom (SHAPES/SPELL_EFFECT/MODIFIER/RITUAL_EFFECT/FACTIONS/CONSTRUCT_TASK) |

## 2. Datapack — 1.21.1 (já convertido para SINGULAR)

| Pasta | Arquivos | Conteúdo |
|---|---:|---|
| recipe | 1796 | receitas (todas as custom + vanilla-typed convertidas p/ `{"id":...}`) |
| loot_table | 393 | loot tables |
| tags | 159 | tags (block/item/entity_type/damage_type/worldgen) |
| advancement | 88 | progressão (tier_1..4, boss, faction, misc, loot) |
| worldgen | 66 | features/biome modifiers |
| structure | 61 | estruturas de mundo |
| neoforge | 24 | biome modifiers (`neoforge:add_features`/`add_spawns`) |
| loot_modifiers | 11 | global loot modifiers (pasta PERMANECE plural) |
| damage_type | 7 | damage types custom |
| curios | 3(+8 tags) | tags de slot curios (back, belt, bracelet, charm, head, necklace, ring, spell) |
| weapon_attributes | 1 | atributos de arma |

### Receitas por subconjunto (recipe/)
manaweaving (530), decoration (365), construct_arm_switch (136), eldrin_altar (122),
components (110), progression (70), crushing (55), rituals (32), manaweave_cache_effects (29),
runescribing (26), runeforging (26), staves (21), shapes (21), wands (20), fume_filter (18),
stone_runes (17), manaweave_patterns (17), chimerite_crystals (16), ... (mais na análise setorial).

## 3. Assets — 1.21.1

| Tipo | Qtd |
|---|---:|
| .geo.json (modelos GeckoLib) | 52 |
| .animation.json (animações GeckoLib) | 42 |
| models json (blocos/itens) | 2478 |
| textures png | 1642 |
| shaders | 3 |

## 4. Paridade de mods de apoio (confirmado)

| | Source 1.20.1 | Port 1.21.1 |
|---|---|---|
| Curios | curios-forge-5.14.1+1.20.1 | curios-neoforge-9.5.1+1.21.1 |
| GeckoLib | geckolib-forge-1.20.1-4.7 | geckolib-neoforge-1.21.1-4.9.3 |
| MNA | mna-forge-1.20.1-3.1.11-all (32MB) | mna-neoforge-1.21.1-3.1.11 (32MB, port) |

Integração no port: `com/mna/interop/` = BookRenderer, CurioRenderer, CuriosInterop,
HatRenderer, StaffRenderer, WandRenderer (+ lootr bridge). 45 classes usam `top.theillusivec4.curios`.

## 5. Estado ATUAL do port 1.21.1 (verificado em jogo)

- Compila **0 erros**; **RODA em jogo** (mundo carrega, player spawna, advancements disparam).
- Último launch: **Loaded 3086 recipes** (0 parse errors — antes 718 quebradas), **1078 recipe IDs restaurados**,
  1407 advancements, player entrou 2x sem crash.
- Mixins (16) aplicando. Registries custom resolvem. Enchantments/receitas por key/holder corrigidos.
- Corrigido nesta sessão: datapack singular; recipe codec bridge (AMRecipeBaseSerializer.codec) +
  RecipeIdFixer; 718 receitas vanilla-typed convertidas p/ `{"id":...}`; FillerBlock offset null-guard.

> As análises setoriais detalhadas (spells, blocos/multiblocos, entidades/GeckoLib/Curios,
> progressão/GUI, mudanças de versão) estão em `_audit/01..05_*.md` (geradas por subagents).
