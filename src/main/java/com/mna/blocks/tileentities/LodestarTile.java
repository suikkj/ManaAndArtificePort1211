 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructTask;
import com.mna.api.entities.construct.ai.IMutexManager;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.gui.containers.block.ContainerLodestar;
import com.mna.gui.widgets.lodestar.LodestarNodeErrorTypes;
import com.mna.network.ClientMessageDispatcher;
import com.mna.tools.NBTUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.mna.api.tools.MNAItemData;

public class LodestarTile
extends BlockEntity
implements Consumer<FriendlyByteBuf>,
MenuProvider,
GeoBlockEntity,
IMutexManager {
    public static final int MAX_BYTES_PER_PACKET = 30000;
    private CompoundTag logic;
    private ArrayList<ConstructAITask<?>> inflatedTasks;
    private List<Component> errors;
    private List<Component> warnings;
    private HashMap<String, LodestarNodeErrorTypes> misconfiguredIDs;
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private HashSet<Integer> messagesRemaining = new HashSet();
    private byte[] buffer = null;
    private HashMap<Class<?>, HashMap<IConstruct<?>, Long>> blockPositionMutex;
    private HashMap<Class<?>, HashMap<IConstruct<?>, Integer>> entityIDMutex;

    public LodestarTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.LODESTAR.get(), pos, state);
        this.logic = new CompoundTag();
        this.inflatedTasks = new ArrayList();
        this.errors = new ArrayList<Component>();
        this.warnings = new ArrayList<Component>();
        this.misconfiguredIDs = new HashMap();
        this.blockPositionMutex = new HashMap();
        this.entityIDMutex = new HashMap();
    }

    public Optional<ConstructAITask<?>> getCommand(String ID) {
        if (ID == null) {
            return this.inflatedTasks.stream().filter(t -> t.isStart()).findFirst();
        }
        return this.inflatedTasks.stream().filter(t -> t.getId().equals(ID)).findFirst();
    }

    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        compound.put("logic", (Tag)this.logic);
    }

    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        if (nbt.contains("logic")) {
            this.logic = nbt.getCompound("logic");
            this.inflateLogic();
        }
        super.loadAdditional(nbt, registries);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithFullMetadata(com.mna.api.tools.MNAItemData.provider());
    }

    @Override
    public void accept(FriendlyByteBuf t) {
        t.writeBlockPos(this.getBlockPos());
    }

    public LodestarTile readFrom(FriendlyByteBuf data) {
        return this;
    }

    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
        return new ContainerLodestar(id, playerInventory, this);
    }

    public Component getDisplayName() {
        return Component.literal((String)"");
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> {
            if (this.getLevel().hasNeighborSignal(this.getBlockPos())) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.lodestar_armature.inactive"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.lodestar_armature.active"));
        }).transitionLength(20)});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    private void initializeBuffer(int numMessages, int bufferSize) {
        this.buffer = new byte[bufferSize];
        this.messagesRemaining.clear();
        for (int i = 0; i < numMessages; ++i) {
            this.messagesRemaining.add(i);
        }
    }

    public void setLogicPartial(int index, int numMessages, int totalSize, byte[] buf, boolean sync) {
        if (numMessages == 1) {
            CompoundTag tag = NBTUtilities.fromBytes(buf);
            this.setLogic(tag, sync);
        } else {
            if (this.buffer != null) {
                if (this.buffer.length < totalSize) {
                    this.initializeBuffer(numMessages, totalSize);
                }
            } else {
                this.initializeBuffer(numMessages, totalSize);
            }
            this.messagesRemaining.remove(index);
            int startIndex = index * 30000;
            int bytesToCopy = Math.min(totalSize - startIndex, 30000);
            System.arraycopy(buf, 0, this.buffer, startIndex, bytesToCopy);
            if (this.messagesRemaining.size() == 0) {
                CompoundTag restored = NBTUtilities.fromBytes(this.buffer);
                this.buffer = null;
                this.setLogic(restored, sync);
            }
        }
    }

    public void setLogic(CompoundTag logic, boolean sync) {
        this.logic = logic;
        this.inflateLogic();
        if (this.getLevel().isClientSide() && sync) {
            ClientMessageDispatcher.sendLodestarLogicMessage(this.getBlockPos(), logic);
        } else {
            this.setChanged();
        }
    }

    public CompoundTag getLogic() {
        return this.logic;
    }

    private void inflateLogic() {
        this.inflatedTasks.clear();
        if (!this.logic.contains("commands")) {
            return;
        }
        ListTag data = this.logic.getList("commands", 10);
        data.forEach(tag -> this.inflateLogicNode((CompoundTag)tag));
        this.analyzeLogic();
    }

    private void inflateLogicNode(CompoundTag tag) {
        ResourceLocation taskID = ResourceLocation.parse(tag.getString("task"));
        ConstructTask action = (ConstructTask)Registries.ConstructTasks.getValue(taskID);
        if (action == null) {
            ManaAndArtifice.LOGGER.error("Failed to look up task (" + taskID.toString() + "); Node skipped.  This will likely break connections.");
            return;
        }
        if (tag.contains("id") && tag.contains("start")) {
            ConstructAITask<?> inst = action.instantiateTask(null);
            if (tag.contains("parameters", 9)) {
                inst.loadParameterData(tag.getList("parameters", 10));
            }
            if (tag.contains("connections", 9)) {
                inst.loadConnections(tag.getList("connections", 10));
            }
            inst.setIdAndIsStart(tag.getString("id"), tag.getBoolean("start"));
            inst.inflateParameters();
            this.inflatedTasks.add(inst);
        } else {
            ManaAndArtifice.LOGGER.error("Task (" + taskID.toString() + ") did not have the required parameters id and start from the Lodestar.  Skipping.");
        }
    }

    private void analyzeLogic() {
        this.errors.clear();
        this.warnings.clear();
        List visitedIDs = this.inflatedTasks.stream().map(task -> task.getConnectedIDs()).flatMap(Collection::stream).collect(Collectors.toList());
        boolean hasNoStart = true;
        boolean hasMisconfigured = false;
        boolean hasDisconnected = false;
        this.misconfiguredIDs = new HashMap();
        if (this.inflatedTasks.size() > 0) {
            for (ConstructAITask<?> task2 : this.inflatedTasks) {
                if (task2.isStart()) {
                    hasNoStart = false;
                }
                if (!visitedIDs.contains(task2.getId()) && !task2.isStart()) {
                    this.misconfiguredIDs.put(task2.getId(), LodestarNodeErrorTypes.Disconnected);
                    hasDisconnected = true;
                    continue;
                }
                if (task2.isFullyConfigured()) continue;
                this.misconfiguredIDs.put(task2.getId(), LodestarNodeErrorTypes.Misconfigured);
                hasMisconfigured = true;
            }
            if (hasNoStart) {
                this.errors.add((Component)Component.translatable((String)"gui.mna.lodestar.error.no_start"));
            }
            if (hasMisconfigured) {
                this.warnings.add((Component)Component.translatable((String)"gui.mna.lodestar.warning.misconfigured"));
            }
            if (hasDisconnected) {
                this.warnings.add((Component)Component.translatable((String)"gui.mna.lodestar.warning.unreachable"));
            }
        }
    }

    public List<Component> getErrors() {
        return this.errors;
    }

    public List<Component> getWarnings() {
        return this.warnings;
    }

    public HashMap<String, LodestarNodeErrorTypes> getNodeErrors() {
        return this.misconfiguredIDs;
    }

    public boolean isLogicDownloaded() {
        return this.buffer == null;
    }

    @Override
    public boolean claimMutex(BlockPos pos, IConstruct<?> construct, ConstructAITask<?> task) {
        long posLong = pos.asLong();
        HashMap mutex = this.blockPositionMutex.computeIfAbsent((task).getClass(), k -> new HashMap());
        if (mutex.containsValue(posLong)) {
            return mutex.containsKey(construct) && (Long)mutex.get(construct) == posLong;
        }
        mutex.put(construct, posLong);
        return true;
    }

    @Override
    public void releaseMutex(BlockPos pos, IConstruct<?> construct, ConstructAITask<?> task) {
        HashMap mutex = this.blockPositionMutex.computeIfAbsent((task).getClass(), k -> new HashMap());
        mutex.remove(construct);
    }

    @Override
    public boolean claimMutex(Entity entity, IConstruct<?> construct, ConstructAITask<?> task) {
        if (entity == null) {
            return false;
        }
        int entityID = entity.getId();
        HashMap mutex = this.entityIDMutex.computeIfAbsent((task).getClass(), k -> new HashMap());
        if (mutex.containsValue(entityID)) {
            return mutex.containsKey(construct) && (Integer)mutex.get(construct) == entityID;
        }
        mutex.put(construct, entityID);
        return true;
    }

    @Override
    public void releaseMutex(Entity entity, IConstruct<?> construct, ConstructAITask<?> task) {
        HashMap mutex = this.entityIDMutex.computeIfAbsent((task).getClass(), k -> new HashMap());
        mutex.remove(construct);
    }

    @Override
    public void releaseAllMutexes(IConstruct<?> construct, ConstructAITask<?> task) {
        this.blockPositionMutex.computeIfPresent((task).getClass(), (k, v) -> {
            v.remove(construct);
            return v;
        });
        this.entityIDMutex.computeIfPresent((task).getClass(), (k, v) -> {
            v.remove(construct);
            return v;
        });
    }
}

