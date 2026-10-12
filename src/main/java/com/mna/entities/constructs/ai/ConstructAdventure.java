 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.tools.RLoc;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.math.MathUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class ConstructAdventure
extends ConstructAITask<ConstructAdventure> {
    private static final ResourceLocation ADVENTURE_LOOT_OVERWORLD = RLoc.create("entities/construct/adventure_minecraft_overworld");
    private static final ConstructCapability[] required_capabilities = new ConstructCapability[]{ConstructCapability.CARRY};
    protected BlockPos blockPos;
    protected Vec3 targetLook;
    private static final int WANDER_DIST = 5;
    protected int waitCount = 0;
    protected boolean isWaiting = false;
    protected int waitTime = 100;

    public ConstructAdventure(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isWaiting) {
            this.setMoveTarget(this.blockPos);
            if (this.doMove()) {
                this.construct.setAdventuring(true);
                this.waitCount = 0;
                this.targetLook = this.construct.asEntity().getForward().normalize().scale(-10.0);
                this.isWaiting = true;
            }
        } else {
            ++this.waitCount;
            if (this.waitCount > 40) {
                this.faceBlockPos(this.blockPos.offset((int)this.targetLook.x, (int)this.targetLook.y, (int)this.targetLook.z));
            }
            if (this.waitCount >= this.getWaitTime()) {
                this.construct.resetActions();
                this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.adventure_success", new Object[0]));
                this.exitCode = 0;
                return;
            }
            if (this.waitCount == this.getWaitTime() - 30) {
                this.construct.setHappy(100);
                this.construct.setAdventuring(false);
                this.rollLoot();
            }
        }
    }

    @Override
    public void onTaskSet() {
        int count;
        super.onTaskSet();
        this.waitCount = 0;
        this.isWaiting = false;
        AbstractGolem c = this.getConstructAsEntity();
        int rX = (int)c.getRandomX(5.0);
        int rY = (int)c.getRandomY();
        int rZ = (int)c.getRandomZ(5.0);
        this.blockPos = new BlockPos(rX, rY, rZ);
        BlockState state = c.level().getBlockState(this.blockPos);
        for (count = 0; !state.isAir() && count < 10; ++count) {
            this.blockPos = this.blockPos.offset(0, 1, 0);
            state = c.level().getBlockState(this.blockPos);
        }
        for (count = 0; state.isAir() && count < 10; ++count) {
            this.blockPos = this.blockPos.offset(0, -1, 0);
            state = c.level().getBlockState(this.blockPos);
        }
        InteractionHand[] hands = this.construct.getCarryingHands();
        for (int i = 0; i < hands.length; ++i) {
            ItemStack held = c.getItemInHand(hands[i]);
            BlockPos dropPos = c.blockPosition();
            ItemEntity item = new ItemEntity(c.level(), (double)dropPos.getX() + 0.5, (double)dropPos.getY() + 0.5, (double)dropPos.getZ() + 0.5, held.copy());
            item.setDeltaMovement(0.0, 0.25, 0.0);
            item.setDefaultPickUpDelay();
            c.level().addFreshEntity((Entity)item);
            c.setItemInHand(hands[i], ItemStack.EMPTY);
        }
    }

    private void rollLoot() {
        int num_items = Math.max(this.construct.getEmptyHands().length, 1);
        if (this.construct.getConstructData().isCapabilityEnabled(ConstructCapability.ITEM_STORAGE)) {
            num_items += 1 + MathUtils.weightedRandomNumber(15);
        }
        ServerLevel serverWorld = (ServerLevel)this.construct.asEntity().level();
        ResourceLocation lootTableId = RLoc.create("entities/construct/adventure_" + serverWorld.dimension().location().toString().replace(':', '_'));
        LootTable loottable = serverWorld.getServer().getLootData().getLootTable(lootTableId);
        if (loottable == LootTable.EMPTY) {
            loottable = serverWorld.getServer().getLootData().getLootTable(ADVENTURE_LOOT_OVERWORLD);
        }
        if (loottable == null) {
            return;
        }
        LootParams lootparams = new LootParams.Builder((ServerLevel)this.construct.asEntity().level()).withParameter(LootContextParams.THIS_ENTITY, this.construct.asEntity()).withParameter(LootContextParams.ORIGIN, this.construct.asEntity().position()).withParameter(LootContextParams.DAMAGE_SOURCE, this.construct.asEntity().damageSources().generic()).create(LootContextParamSets.ENTITY);
        ObjectArrayList items = loottable.getRandomItems(lootparams);
        for (int i = 0; i < Math.min(num_items, items.size()); ++i) {
            this.insertOrDropItem((ItemStack)items.get(i));
        }
    }

    private int getWaitTime() {
        if (!this.construct.getConstructData().areCapabilitiesEnabled(ConstructCapability.ITEM_STORAGE)) {
            return this.getInteractTime(ConstructCapability.ADVENTURE, 9600);
        }
        return this.getInteractTime(ConstructCapability.ADVENTURE, 4800);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.ADVENTURE);
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public boolean isFullyConfigured() {
        return true;
    }

    @Override
    public void inflateParameters() {
    }

    @Override
    public ConstructAdventure copyFrom(ConstructAITask<?> other) {
        return this;
    }

    @Override
    public ConstructAdventure duplicate() {
        return new ConstructAdventure(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return required_capabilities;
    }

    @Override
    public int getRequiredIntelligence() {
        return 16;
    }
}

