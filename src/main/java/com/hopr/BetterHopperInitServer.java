package com.hopr;

import static com.hopr.BetterHopperService.HOPR_BLOCK_ENTITY_TYPE_ID;
import static com.hopr.BetterHopperService.HOPR_BLOCK_ID;
import static com.hopr.BetterHopperService.HOPR_ITEM_ID;
import static com.hopr.BetterHopperService.HOPR_SCREEN_ID;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class BetterHopperInitServer implements ModInitializer {

	static {
		// WitherSkeletonEntity ent = Registries.ENTITY_TYPE.get(WITHER_SKELE_ID;
		final BetterHopperBlock bhBlock = new BetterHopperBlock(BetterHopperBlock.getDefaultSettings());
		//final EntityType<?> withereSkele = Registries.ENTITY_TYPE.get(new Identifier("minecraft:wither_skeleton"));

		final BetterHopperItem bhItem = new BetterHopperItem(bhBlock, new Item.Settings());
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(bhItem));
		bhItem.appendBlocks(Item.BLOCK_ITEMS, bhItem); // wat
		Registry.register(Registries.BLOCK_ENTITY_TYPE, HOPR_BLOCK_ENTITY_TYPE_ID,
				FabricBlockEntityTypeBuilder.create(BetterHopperBlockEntity::new, bhBlock).build(null));
		Registry.register(Registries.ITEM, HOPR_ITEM_ID, bhItem);
		Registry.register(Registries.BLOCK, HOPR_BLOCK_ID, bhBlock);
		Registry.register(Registries.SCREEN_HANDLER, HOPR_SCREEN_ID,
				new ScreenHandlerType<BetterHopperScreenHandler>(BetterHopperScreenHandler::new));
		

	}

	@Override
	public void onInitialize() {

		new BetterHopperService();

	}

}
