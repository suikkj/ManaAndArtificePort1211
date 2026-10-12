package com.mna.capabilities;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.capabilities.chunkdata.ChunkMagic;
import com.mna.capabilities.entity.MAPFX;
import com.mna.capabilities.particles.ParticleAura;
import com.mna.capabilities.playerdata.magic.PlayerMagic;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpells;
import com.mna.capabilities.worlddata.WorldMagic;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro das capabilities do M&A como AttachmentTypes do NeoForge 1.21.1.
 * Registrado no event bus do mod a partir de ManaAndArtifice.
 */
public class CapabilitiesInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(Registries.ATTACHMENT_TYPE, "mna");

    // Player data
    public static final Supplier<AttachmentType<IPlayerMagic>> PLAYER_MAGIC =
        ATTACHMENT_TYPES.register("player_magic", () -> AttachmentType.serializable(PlayerMagic::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<IPlayerProgression>> PLAYER_PROGRESSION =
        ATTACHMENT_TYPES.register("player_progression", () -> AttachmentType.serializable(PlayerProgression::new).copyOnDeath().build());
    public static final Supplier<AttachmentType<IPlayerRoteSpells>> PLAYER_ROTE_SPELLS =
        ATTACHMENT_TYPES.register("player_rote_spells", () -> AttachmentType.serializable(PlayerRoteSpells::new).copyOnDeath().build());

    // World data
    public static final Supplier<AttachmentType<IWorldMagic>> WORLD_MAGIC =
        ATTACHMENT_TYPES.register("world_magic", () -> AttachmentType.serializable(WorldMagic::new).build());

    // Chunk data
    public static final Supplier<AttachmentType<IChunkMagic>> CHUNK_MAGIC =
        ATTACHMENT_TYPES.register("chunk_magic", () -> AttachmentType.serializable(ChunkMagic::new).build());

    // Entity/transient data
    public static final Supplier<AttachmentType<MAPFX>> ENTITY_MAPFX =
        ATTACHMENT_TYPES.register("entity_mapfx", () -> AttachmentType.serializable(MAPFX::new).build());
    public static final Supplier<AttachmentType<ParticleAura>> ENTITY_PARTICLE_AURA =
        ATTACHMENT_TYPES.register("entity_particle_aura", () -> AttachmentType.serializable(ParticleAura::new).build());

    public static void register() {
        ATTACHMENT_TYPES.register(ManaAndArtifice.MOD_BUS);
        ManaAndArtifice.LOGGER.info("M&A -> Attachment Types Registered");
    }
}
