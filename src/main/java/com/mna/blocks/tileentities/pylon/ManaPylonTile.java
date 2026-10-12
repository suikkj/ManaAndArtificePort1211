 
package com.mna.blocks.tileentities.pylon;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.items.ChargeableItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.tools.RLoc;
import com.mna.blocks.runeforging.PedestalBlock;
import com.mna.blocks.tileentities.PedestalTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.constructs.animated.Construct;
import com.mna.network.ServerMessageDispatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

public class ManaPylonTile
extends PylonTileBase {
    private static final ResourceLocation icon = RLoc.create("textures/gui/pylon/mana.png");
    private ArrayList<Long> knownPedestalLocations = new ArrayList();

    public ManaPylonTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ManaPylonTile(BlockPos pPos, BlockState pBlockState) {
        this((BlockEntityType)TileEntityInit.MANA_PYLON.get(), pPos, pBlockState);
    }

    @Override
    protected HashMap<Affinity, Float> getPowerConsumption() {
        return this.getBlock().getPowerConsumption();
    }

    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, ManaPylonTile tile) {
        if (tile.tick()) {
            int n = tile.powerUpTicks;
            Objects.requireNonNull(tile);
            if (n == 60) {
                if (level.getGameTime() % 80L == 0L) {
                    tile.addEffectsToPlayers();
                    tile.rechargeConstructs();
                    tile.scanForPedestals();
                }
                if (level.getGameTime() % 5L == 0L) {
                    tile.chargeItems();
                }
                if (level.getGameTime() % 100L == 0L) {
                    tile.chargeSigils();
                }
            }
        }
    }

    private void addEffectsToPlayers() {
        this.forAllPlayers(false, p -> p.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_REGEN.get()), 100, 4, true, false, true)));
    }

    private void rechargeConstructs() {
        Vec3 myPos = Vec3.atCenterOf((Vec3i)this.getBlockPos());
        this.forAllEntities(Construct.class, false, (T construct) -> {
            if (construct.getManaPct() == 1.0f || !construct.getConstructData().isCapabilityEnabled(ConstructCapability.STORE_MANA)) {
                return;
            }
            double dist = myPos.distanceTo(construct.position());
            int magnitudeByDist = 4 - (int)Math.floor(dist / 4.0);
            float manaRestored = magnitudeByDist * 50;
            construct.adjustMana(manaRestored);
            ServerMessageDispatcher.sendParticleSpawn((double)this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 2, (double)this.worldPosition.getZ() + 0.5, construct.getX(), construct.getY() + 0.8, construct.getZ(), 0, 32.0f, (ResourceKey<Level>)this.level.dimension(), (MAParticleType)((ParticleInit.LIGHTNING_BOLT.get())));
        });
    }

    private void chargeItems() {
        if (this.knownPedestalLocations.size() == 0) {
            return;
        }
        int i = (int)Math.floor(Math.random() * (double)this.knownPedestalLocations.size());
        BlockPos curPos = BlockPos.of((long)this.knownPedestalLocations.get(i));
        if (!this.getLevel().isLoaded(curPos)) {
            return;
        }
        BlockState state = this.getLevel().getBlockState(curPos);
        if (state.getBlock() instanceof PedestalBlock) {
            ChargeableItem item;
            PedestalTile pedestal;
            ItemStack stack;
            BlockEntity te = this.level.getBlockEntity(curPos);
            if (te instanceof PedestalTile && (stack = (pedestal = (PedestalTile)te).getItem(0)).getItem() instanceof ChargeableItem && (item = (ChargeableItem)stack.getItem()).getMana(stack) < item.getMaxMana()) {
                item.refundMana(pedestal.getItem(0), 50.0f, null);
                ServerMessageDispatcher.sendParticleSpawn((double)this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 2, (double)this.worldPosition.getZ() + 0.5, (double)curPos.getX() + 0.5, (double)curPos.getY() + 0.9, (double)curPos.getZ() + 0.5, 0, 32.0f, (ResourceKey<Level>)this.level.dimension(), (MAParticleType)(ParticleInit.LIGHTNING_BOLT.get()));
            }
        } else {
            this.knownPedestalLocations.remove(i--);
        }
    }

    private void chargeSigils() {
        this.forAllEntities((EntityTypeTest)EntityInit.SPELL_RUNE.get(), false, (T e) -> {
            if (e.isPermanent()) {
                e.addCharge();
            }
        });
    }

    private void scanForPedestals() {
        int SCAN_RADIUS = 8;
        for (int i = -SCAN_RADIUS; i <= SCAN_RADIUS; ++i) {
            for (int j = -SCAN_RADIUS; j <= SCAN_RADIUS; ++j) {
                for (int k = -SCAN_RADIUS; k <= SCAN_RADIUS; ++k) {
                    long posLong;
                    BlockState state;
                    BlockPos curPos = this.worldPosition.offset(i, j, k);
                    if (!this.getLevel().isLoaded(curPos) || !((state = this.getLevel().getBlockState(curPos)).getBlock() instanceof PedestalBlock) || this.knownPedestalLocations.contains(posLong = curPos.asLong())) continue;
                    this.knownPedestalLocations.add(posLong);
                }
            }
        }
    }

     
    public boolean isActiveAndPoweredUp() {
        if (!this.active) return false;
        Objects.requireNonNull(this);
        if (this.powerUpTicks != 60) return false;
        return true;
    }

    public float consumePower(float amount) {
        return this.consume(this.getOwnershipData(), this.level, this.worldPosition, Vec3.atCenterOf((Vec3i)this.worldPosition), Affinity.ARCANE, amount);
    }
}

