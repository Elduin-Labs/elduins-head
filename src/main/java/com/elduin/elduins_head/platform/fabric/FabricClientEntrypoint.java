package com.elduin.elduins_head.platform.fabric;

//? fabric {

import com.elduin.elduins_head.ElduinsHead;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ElduinsHead.onInitializeClient();
	}

}
//?}
