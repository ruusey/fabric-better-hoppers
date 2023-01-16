package com.hopr;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class BetterHopperBlockEntity extends LootableContainerBlockEntity implements Hopper, NamedScreenHandlerFactory {
	public static final int TRANSFER_COOLDOWN = -1;
	private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
	public static final int INVENTORY_SIZE = 5;
	private int transferCooldown = -1;
	private long lastTickTime;

	public BetterHopperBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BetterHopperService.getBlockEntityType(), blockPos, blockState);
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound test = createNbt();
		test.putInt("TransferCooldown", -1);
		return test;
	}

	@Override
	public void readNbt(NbtCompound nbtCompound) {
		super.readNbt(nbtCompound);

		this.inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY);
		if (!deserializeLootTable(nbtCompound)) {
			Inventories.readNbt(nbtCompound, this.inventory);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbtCompound) {
		super.writeNbt(nbtCompound);

		if (!serializeLootTable(nbtCompound)) {
			Inventories.writeNbt(nbtCompound, this.inventory);
		}

		nbtCompound.putInt("TransferCooldown", this.transferCooldown);
	}

	@Override
	public int size() {
		return this.inventory.size();
	}

	@Override
	public ItemStack removeStack(int i, int j) {
		checkLootInteraction((PlayerEntity) null);

		return Inventories.splitStack(getInvStackList(), i, j);
	}

	@Override
	public void setStack(int i, ItemStack itemStack) {
		checkLootInteraction((PlayerEntity) null);
		getInvStackList().set(i, itemStack);
		if (itemStack.getCount() > getMaxCountPerStack()) {
			itemStack.setCount(getMaxCountPerStack());
		}
	}
	
	public long getLastTickTime() {
		return this.lastTickTime;
	}

	@Override
	protected Text getContainerName() {
		return Text.translatable("container.hopr.hopperplus");
	}

	public static void serverTick(World world, BlockPos blockPos, BlockState blockState,
			BetterHopperBlockEntity hopperBlockEntity) {
		hopperBlockEntity.transferCooldown = -1;
		hopperBlockEntity.lastTickTime = world.getTime();

		if (!hopperBlockEntity.needsCooldown()) {
			// hopperBlockEntity.setTransferCooldown(0);
			insertAndExtract(world, blockPos, blockState, hopperBlockEntity, () -> extract(world, hopperBlockEntity));
		}
	}

	private static boolean insertAndExtract(World world, BlockPos blockPos, BlockState blockState,
			BetterHopperBlockEntity hopperBlockEntity, BooleanSupplier booleanSupplier) {
		if (world.isClient) {
			return false;
		}

		if (!hopperBlockEntity.needsCooldown()
				&& ((Boolean) blockState.get((Property<?>) HopperBlock.ENABLED)).booleanValue()) {
			boolean bl = false;

			if (!hopperBlockEntity.isEmpty()) {
				bl = insert(world, blockPos, blockState, hopperBlockEntity);
			}
			if (!hopperBlockEntity.isFull()) {
				bl |= booleanSupplier.getAsBoolean();
			}

			if (bl) {
				hopperBlockEntity.setTransferCooldown(8);
				markDirty(world, blockPos, blockState);
				return true;
			}
		}
		return false;
	}

	private boolean isFull() {
		for (ItemStack itemStack : this.inventory) {
			if (itemStack.isEmpty() || itemStack.getCount() != itemStack.getMaxCount()) {
				return false;
			}
		}
		return true;
	}

	private static boolean insert(World world, BlockPos blockPos, BlockState blockState, Inventory inventory) {
		Inventory inventory2 = getOutputInventory(world, blockPos, blockState);
		if (inventory2 == null) {
			return false;
		}

		Direction direction = ((Direction) blockState.get((Property<?>) HopperBlock.FACING)).getOpposite();
		if (isInventoryFull(inventory2, direction)) {
			return false;
		}

		for (int i = 0; i < inventory.size(); i++) {
			if (!inventory.getStack(i).isEmpty()) {

				ItemStack itemStack = inventory.getStack(i).copy();
				ItemStack itemStack2 = transfer(inventory, inventory2, inventory.removeStack(i, 1), direction);

				if (itemStack2.isEmpty()) {
					inventory2.markDirty();
					return true;
				}
				inventory.setStack(i, itemStack);
			}
		}

		return false;
	}

	private static IntStream getAvailableSlots(Inventory inventory, Direction direction) {
		if (inventory instanceof SidedInventory) {
			return IntStream.of(((SidedInventory) inventory).getAvailableSlots(direction));
		}

		return IntStream.range(0, inventory.size());
	}

	private static boolean isInventoryFull(Inventory inventory, Direction direction) {
		return getAvailableSlots(inventory, direction).allMatch(i -> {
			ItemStack itemStack = inventory.getStack(i);
			return (itemStack.getCount() >= itemStack.getMaxCount());
		});
	}

	private static boolean isInventoryEmpty(Inventory inventory, Direction direction) {
		return getAvailableSlots(inventory, direction).allMatch(i -> inventory.getStack(i).isEmpty());
	}

	public static boolean extract(World world, Hopper hopper) {
		Inventory inventory = getInputInventory(world, hopper);

		if (inventory != null) {
			Direction direction = Direction.DOWN;
			if (isInventoryEmpty(inventory, direction)) {
				return false;
			}

			return getAvailableSlots(inventory, direction).anyMatch(i -> extract(hopper, inventory, i, direction));
		}
		for (ItemEntity itemEntity : getInputItemEntities(world, hopper)) {
			if (extract(hopper, itemEntity)) {
				return true;
			}
		}

		return false;
	}

	private static boolean extract(Hopper hopper, Inventory inventory, int i, Direction direction) {
		ItemStack itemStack = inventory.getStack(i);

		if (!itemStack.isEmpty() && canExtract(inventory, itemStack, i, direction)) {
			ItemStack itemStack2 = itemStack.copy();
			ItemStack itemStack3 = transfer(inventory, hopper, inventory.removeStack(i, 1), (Direction) null);

			if (itemStack3.isEmpty()) {
				inventory.markDirty();
				return true;
			}
			inventory.setStack(i, itemStack2);
		}

		return false;
	}

	public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
		boolean bl = false;

		ItemStack itemStack = itemEntity.getStack().copy();
		ItemStack itemStack2 = transfer((Inventory) null, inventory, itemStack, (Direction) null);

		if (itemStack2.isEmpty()) {
			bl = true;

			itemEntity.discard();
		} else {
			itemEntity.setStack(itemStack2);
		}

		return bl;
	}

	public static ItemStack transfer(@Nullable Inventory inventory, Inventory inventory2, ItemStack itemStack,
			@Nullable Direction direction) {
		if (inventory2 instanceof SidedInventory && direction != null) {
			SidedInventory sidedInventory = (SidedInventory) inventory2;
			int[] is = sidedInventory.getAvailableSlots(direction);

			for (int i = 0; i < is.length && !itemStack.isEmpty(); i++) {
				itemStack = transfer(inventory, inventory2, itemStack, is[i], direction);
			}
		} else {
			int j = inventory2.size();
			for (int k = 0; k < j && !itemStack.isEmpty(); k++) {
				itemStack = transfer(inventory, inventory2, itemStack, k, direction);
			}
		}

		return itemStack;
	}

	private static boolean canInsert(Inventory inventory, ItemStack itemStack, int i, @Nullable Direction direction) {
		if (!inventory.isValid(i, itemStack)) {
			return false;
		}
		if (inventory instanceof SidedInventory && !((SidedInventory) inventory).canInsert(i, itemStack, direction)) {
			return false;
		}
		return true;
	}

	private static boolean canExtract(Inventory inventory, ItemStack itemStack, int i, Direction direction) {
		if (inventory instanceof SidedInventory && !((SidedInventory) inventory).canExtract(i, itemStack, direction)) {
			return false;
		}
		return true;
	}

	private static ItemStack transfer(@Nullable Inventory inventory, Inventory inventory2, ItemStack itemStack, int i,
			@Nullable Direction direction) {
		ItemStack itemStack2 = inventory2.getStack(i);

		if (canInsert(inventory2, itemStack, i, direction)) {
			boolean bl = false;
			if (itemStack2.isEmpty()) {
				inventory2.setStack(i, itemStack);
				itemStack = ItemStack.EMPTY;
				bl = true;
			} else if (canMergeItems(itemStack2, itemStack)) {
				int j = itemStack.getMaxCount() - itemStack2.getCount();
				int k = Math.min(itemStack.getCount(), j);

				itemStack.decrement(k);
				itemStack2.increment(k);
				bl = (k > 0);
			}
			if (bl) {
				inventory2.markDirty();
			}
		}
		return itemStack;
	}

	@Nullable
	private static Inventory getOutputInventory(World world, BlockPos blockPos, BlockState blockState) {
		Direction direction = (Direction) blockState.get((Property<?>) HopperBlock.FACING);
		return getInventoryAt(world, blockPos.offset(direction));
	}

	@Nullable
	private static Inventory getInputInventory(World world, Hopper hopper) {
		return getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY() + 1.0D, hopper.getHopperZ());
	}

	public static List<ItemEntity> getInputItemEntities(World world, Hopper hopper) {
		return hopper.getInputAreaShape().getBoundingBoxes().stream()
				.flatMap(
						box -> world.getEntitiesByClass(ItemEntity.class,
								box.offset(hopper.getHopperX() - 0.5D, hopper.getHopperY() - 0.5D,
										hopper.getHopperZ() - 0.5D),
								EntityPredicates.VALID_ENTITY).stream())
				.collect(Collectors.toList());
	}

	@Nullable
	public static Inventory getInventoryAt(World world, BlockPos blockPos) {
		return getInventoryAt(world, blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
	}

	@Nullable
	private static Inventory getInventoryAt(World world, double d, double e, double f) {
		Inventory inventory = null;
		BlockPos blockPos = new BlockPos(d, e, f);

		BlockState blockState = world.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block instanceof InventoryProvider) {
			SidedInventory sidedInventory = ((InventoryProvider) block).getInventory(blockState, world, blockPos);
			inventory = sidedInventory;
		} else if (blockState.hasBlockEntity()) {
			BlockEntity blockEntity = world.getBlockEntity(blockPos);

			if (blockEntity instanceof Inventory) {
				inventory = (Inventory) blockEntity;

				if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
					inventory = ChestBlock.getInventory((ChestBlock) block, blockState, world, blockPos, true);
				}
			}
		}

		if (inventory == null) {
			List<Entity> list = world.getOtherEntities((Entity) null,
					new Box(d - 0.5D, e - 0.5D, f - 0.5D, d + 0.5D, e + 0.5D, f + 0.5D),
					EntityPredicates.VALID_INVENTORIES);

			if (!list.isEmpty()) {
				inventory = (Inventory) list.get(world.random.nextInt(list.size()));
			}
		}

		return inventory;
	}

	private static boolean canMergeItems(ItemStack itemStack, ItemStack itemStack2) {
		if (!itemStack.isOf(itemStack2.getItem())) {
			return false;
		}
		if (itemStack.getDamage() != itemStack2.getDamage()) {
			return false;
		}
		if (itemStack.getCount() > itemStack.getMaxCount()) {
			return false;
		}
		if (!ItemStack.areNbtEqual(itemStack, itemStack2)) {
			return false;
		}
		return true;
	}

	@Override
	public double getHopperX() {
		return this.pos.getX() + 0.5D;
	}

	@Override
	public double getHopperY() {
		return this.pos.getY() + 0.5D;
	}

	@Override
	public double getHopperZ() {
		return this.pos.getZ() + 0.5D;
	}

	private void setTransferCooldown(int i) {
		this.transferCooldown = -1;
	}

	private boolean needsCooldown() {
		return false;
	}

	@Override
	protected DefaultedList<ItemStack> getInvStackList() {
		return this.inventory;
	}

	@Override
	protected void setInvStackList(DefaultedList<ItemStack> defaultedList) {
		this.inventory = defaultedList;
	}

	public static void onEntityCollided(World world, BlockPos blockPos, BlockState blockState, Entity entity,
			BetterHopperBlockEntity hopperBlockEntity) {
		if (entity instanceof ItemEntity && VoxelShapes.matchesAnywhere(
				VoxelShapes
						.cuboid(entity.getBoundingBox().offset(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ())),
				hopperBlockEntity.getInputAreaShape(), BooleanBiFunction.AND)) {
			insertAndExtract(world, blockPos, blockState, hopperBlockEntity,
					() -> extract(hopperBlockEntity, (ItemEntity) entity));
		}
	}

	@Override
	public ScreenHandler createScreenHandler(int i, PlayerInventory playerInventory) {
		return new BetterHopperScreenHandler(i, playerInventory, this);
	}

}
