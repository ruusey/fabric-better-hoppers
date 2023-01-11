package com.hopr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

public class BetterHopperInitClient implements ClientModInitializer {
	
	@SuppressWarnings("deprecation")
	@Override
	public void onInitializeClient() {

		registerSyncPacketReceiver(RegistrySyncManager.DIRECT_PACKET_HANDLER);
		registerSyncPacketReceiver(RegistrySyncManager.NBT_PACKET_HANDLER);
		HandledScreens.register(BetterHopperService.getScreenHandlerType(), BetterHopperScreen::new);
		BlockRenderLayerMap.INSTANCE.putBlock(BetterHopperService.getBlockType(), RenderLayer.getSolid());
		//BlockRenderLayerMap.INSTANCE.putItem(BetterHopperService.getBlockItem(),
		// RenderLayer.getEntitySolid(HOPR_ITEM_ID));
		// RegistrySyncManager.bootstrapRegistries();

	}

	private static void registerSyncPacketReceiver(RegistryPacketHandler packetHandler) {
		ClientPlayNetworking.registerGlobalReceiver(packetHandler.getPacketId(),
				(client, handler, buf, responseSender) -> packetHandler.receivePacket(buf));

	}

}
