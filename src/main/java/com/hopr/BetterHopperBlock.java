package com.hopr;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BetterHopperBlock extends HopperBlock {

	public static AbstractBlock.Settings getDefaultSettings() {
		return FabricBlockSettings.copy(Blocks.HOPPER).mapColor(MapColor.GOLD).breakInstantly();
	}

	public BetterHopperBlock(AbstractBlock.Settings settings) {
		super(settings);

	}

	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity playerEntity,
			Hand hand, BlockHitResult blockHitResult) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if (blockEntity instanceof BetterHopperBlockEntity) {
			playerEntity.openHandledScreen((NamedScreenHandlerFactory) blockEntity);
			playerEntity.incrementStat(Stats.INSPECT_HOPPER);
		}

		return ActionResult.CONSUME;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return world.isClient ? null
				: BetterHopperBlock.checkType(type, BetterHopperService.getBlockEntityType(),
						(world1, pos, state1, be) -> BetterHopperBlockEntity.serverTick(world1, pos, state1, be));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BetterHopperBlockEntity(pos, state);
	}

}