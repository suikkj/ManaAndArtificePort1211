# Mana and Artifice — NeoForge 1.21.1 port

Unofficial port of **Mana and Artifice 3.1.11** from Minecraft 1.20.1 (Forge) to Minecraft 1.21.1 (NeoForge).

The original mod is closed source. Its 1.20.1 jar was decompiled with CFR and migrated to the 1.21.1 loader; this repository is the migration work, not the original source code.

## Status

| Area | State |
|---|---|
| Build | `gradle build` completes and produces `build/libs/mna-3.1.11.jar` |
| Launch | NeoForge 21.1.248 on Java 21, zero crashes in the validation run |
| Content | 3,086 recipes and 1,078 registry ids load at runtime |
| Guidebook | recipe grid renders with items and related tabs |
| Pending | JEI integration, creative tab icons, Curios equip check, GeckoLib summon check, multiblock trigger, automation, save and reload, multiplayer, sound review |

The pending list is deliberately explicit. The port runs, but I do not claim parity with the original in systems I have not exercised in game.

## What the port involved

- **SRG to mojmap remap.** The 1.20.1 jar runs on SRG names, while NeoForge uses Mojang names. The mapping was generated from a 1.20.1 ForgeGradle `createSrgToMcp` task and applied as 59,104 substitutions across 1,420 files.
- **UTF-8 BOM removal in 488 Java files.** The BOM broke the javac parser and generated cascades of `class, interface, enum, or record expected`; removing it cut roughly 14,000 errors at once.
- **Loader migration.** 123 files moved from `net.minecraftforge.*` to `net.neoforged.*`, with the class renames that follow: `MinecraftForge` to `NeoForge`, `ForgeHooks` to `CommonHooks`, `ForgeConfigSpec` to `ModConfigSpec`, `ForgeRegistries` to `NeoForgeRegistries`.
- **GeckoLib relocation.** 68 files moved to the 4.9.x package layout, including `core.animation` to `animation` and the keyframe event package.
- **API drift.** Compile errors went from 47,062 to 9,318 after the remap, then through the real 1.20.1 to 1.21.1 changes: capabilities to attachments, `RegistryObject` to `DeferredHolder`, `ResourceLocation` constructors, `MobEffect` and `Attribute` holders, networking payloads, GUI widgets and `TickEvent`.
- **Runtime fixes found only in game.** 77 advancements that failed to parse, the FluidJug server crash, a `ManaweaveCacheTile` NPE on save, and empty villager trades.
- **Assets.** 807 models validated and 257 sounds decoded.

## Stack

| Item | Version |
|---|---|
| Java | 21 |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248 |
| Parchment | 2024.11.17 |
| moddev plugin | 2.0.71 |
| Curios | 9.5.1 |
| GeckoLib | 4.9.3 |

## Build

Requirements: JDK 21 and Gradle 8.x. The wrapper is not committed.

```bash
gradle build
```

Output: `build/libs/mna-3.1.11.jar`. Install it in a NeoForge 1.21.1 instance together with Curios and GeckoLib.

## Layout

```
src/main/java/com/mna      ported Java sources
src/main/resources         assets, data, mixins, neoforge.mods.toml
_audit                     inventory of the original mod, migration plan, subsystem reports
_snapshots/jar_prev        previous build artifact
libs                       local dependency jars used during development
```

## Legal

Mana and Artifice is copyright Mithion, all rights reserved. This repository is unofficial, is not affiliated with or endorsed by the author, and grants no license to the original code or assets. It exists as a personal migration exercise and technical study. Do not redistribute it as your own mod. If the author objects, the repository will be taken down.

## Credits

Original mod, assets and design: Mithion and the Mana and Artifice contributors. Curios and GeckoLib belong to their respective authors.
