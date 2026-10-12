 
package com.mna.tools;

import com.mna.api.tools.MATags;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureUtils {
    public static boolean isPointInStructure(ServerLevel world, BlockPos pos) {
        return world.structureManager().hasAnyStructureAt(pos);
    }

    public static boolean isPointInStructure(ServerLevel world, BlockPos pos, ResourceLocation structureID, int pieceIndex) {
        Structure csf = MATags.getStructure(world, structureID);
        if (csf == null) {
            return false;
        }
        return world.structureManager().getAllStructuresAt(pos).entrySet().stream().filter(entry -> csf == entry.getKey()).anyMatch(entry -> {
            StructureStart start = world.structureManager().getStructureAt(pos, (Structure)entry.getKey());
            if (start == null) {
                return false;
            }
            if (start.getPieces().size() == 0) {
                return true;
            }
            if (pieceIndex < 0) {
                for (StructurePiece piece : start.getPieces()) {
                    if (!piece.getBoundingBox().isInside((Vec3i)pos)) continue;
                    return true;
                }
            } else if (pieceIndex < start.getPieces().size()) {
                return ((StructurePiece)start.getPieces().get(pieceIndex)).getBoundingBox().isInside((Vec3i)pos);
            }
            return false;
        });
    }

    public static boolean isPointInAnyStructure(ServerLevel world, BlockPos pos, ResourceLocation structureTagID) {
        List<Holder<Structure>> taggedStructures = MATags.getStructureContents(world, structureTagID);
        if (taggedStructures.size() == 0) {
            return false;
        }
        return world.structureManager().getAllStructuresAt(pos).entrySet().stream().filter(entry -> taggedStructures.stream().anyMatch(structure -> structure.value() == entry.getKey())).anyMatch(entry -> {
            StructureStart start = world.structureManager().getStructureAt(pos, (Structure)entry.getKey());
            if (start == null) {
                return false;
            }
            if (start.getPieces().size() == 0) {
                return true;
            }
            for (StructurePiece piece : start.getPieces()) {
                if (!piece.getBoundingBox().isInside((Vec3i)pos)) continue;
                return true;
            }
            return false;
        });
    }

    public static BoundingBox getStructureBoundingBoxAt(ServerLevel world, BlockPos pos, ResourceLocation structureID, int subPieceIndex) {
        Structure csf = MATags.getStructure(world, structureID);
        if (csf == null) {
            return null;
        }
        StructureStart start = world.structureManager().getStructureWithPieceAt(pos, csf);
        if (start == null || start.getPieces().size() == 0) {
            return null;
        }
        if (subPieceIndex > -1 && subPieceIndex < start.getPieces().size()) {
            return ((StructurePiece)start.getPieces().get(subPieceIndex)).getBoundingBox();
        }
        return start.getBoundingBox();
    }

    public static List<Holder<Structure>> getAllStructures(ServerLevel world) {
        ArrayList<Holder<Structure>> result = new ArrayList<Holder<Structure>>();
        Registry registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        registry.asHolderIdMap().forEach(holder -> result.add((Holder<Structure>)holder));
        return result;
    }

    public static List<ResourceLocation> getAllStructureIDs(ServerLevel world) {
        ArrayList<ResourceLocation> result = new ArrayList<ResourceLocation>();
        Registry registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        registry.entrySet().forEach(entry -> result.add(((ResourceKey)entry.getKey()).location()));
        return result;
    }

    @Nullable
    public static ResourceLocation getStructureID(ServerLevel world, Holder<Structure> structure) {
        if (!structure.isBound()) {
            return null;
        }
        Registry registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return registry.getKey(structure.value());
    }
}

