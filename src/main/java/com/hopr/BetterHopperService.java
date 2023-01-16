package com.hopr;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;


@SuppressWarnings("unchecked")
public class BetterHopperService {
	public static final Identifier HOPR_BLOCK_ID = new Identifier("hopr:hopperplus");
	public static final Identifier HOPR_ITEM_ID = new Identifier("hopr:hopperplus");
	public static final Identifier HOPR_SCREEN_ID = new Identifier("hopr:hopperplus");
	public static final Identifier HOPR_BLOCK_ENTITY_TYPE_ID = new Identifier("hopr:hopperplus");

	public static ScreenHandlerType<BetterHopperScreenHandler> getScreenHandlerType() {
		return (ScreenHandlerType<BetterHopperScreenHandler>) Registries.SCREEN_HANDLER.get(HOPR_SCREEN_ID);
	}

	public static BlockEntityType<BetterHopperBlockEntity> getBlockEntityType() {
		return (BlockEntityType<BetterHopperBlockEntity>) Registries.BLOCK_ENTITY_TYPE.get(HOPR_BLOCK_ENTITY_TYPE_ID);
	}

	public static Block getBlockType() {
		return Registries.BLOCK.get(HOPR_BLOCK_ID);
	}

	public static Item getBlockItem() {
		return Registries.ITEM.get(HOPR_ITEM_ID);
	}
}
