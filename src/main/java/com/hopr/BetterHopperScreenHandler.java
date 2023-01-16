package com.hopr;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class BetterHopperScreenHandler extends HopperScreenHandler {

	public BetterHopperScreenHandler(int syncId, PlayerInventory playerInventory) {
		super(syncId, playerInventory);
	}

	public BetterHopperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(syncId, playerInventory, inventory);
	}

	@Override
	public ScreenHandlerType<?> getType() {
		return BetterHopperService.getScreenHandlerType();
	}
}