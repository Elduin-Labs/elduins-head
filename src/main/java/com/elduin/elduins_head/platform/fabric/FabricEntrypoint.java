package com.elduin.elduins_head.platform.fabric;

//? fabric {

import com.elduin.elduins_head.ElduinsHead;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		ElduinsHead.onInitialize();
		FabricEventSubscriber.registerEvents();
	}
}
//?}
