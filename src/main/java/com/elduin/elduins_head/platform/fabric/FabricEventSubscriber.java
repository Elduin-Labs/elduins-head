package com.elduin.elduins_head.platform.fabric;

//? fabric {

import com.elduin.elduins_head.content.ModBlocks;
import com.elduin.elduins_head.head.Head;
import com.elduin.elduins_head.portal.PortalFilling;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.item.CreativeModeTabs;
//? if >=26 {
/*import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
*///? } else {
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
//? }

public class FabricEventSubscriber {

	public static void registerEvents() {
		UseBlockCallback.EVENT.register(PortalFilling::onUseBlock);
		ServerTickEvents.END_SERVER_TICK.register(Head::tick);

		// Fabric API renamed "item groups" to "creative mode tabs" in 26.
		//? if >=26 {
		/*CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
			output.accept(ModBlocks.HEAD_WALL_ITEM);
			output.accept(ModBlocks.BRAIN_ITEM);
		});
		*///? } else {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			entries.accept(ModBlocks.HEAD_WALL_ITEM);
			entries.accept(ModBlocks.BRAIN_ITEM);
		});
		//? }
	}
}
//?}
