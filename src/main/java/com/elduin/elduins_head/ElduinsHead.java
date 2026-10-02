package com.elduin.elduins_head;

import com.elduin.elduins_head.content.ModBlocks;
import com.elduin.elduins_head.platform.Platform;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elduin.elduins_head.platform.fabric.FabricPlatform;

@SuppressWarnings("LoggingSimilarMessage")
public class ElduinsHead {

	public static final String MOD_ID = /*$ mod_id*/ "elduins_head";
	public static final String MOD_VERSION = /*$ mod_version*/ "1.0.0";
	public static final String MOD_FRIENDLY_NAME = /*$ mod_name*/ "Elduin's Head";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Platform PLATFORM = createPlatformInstance();

	public static void onInitialize() {
		LOGGER.info("Initializing {} on {}", MOD_ID, ElduinsHead.xplat().loader());
		LOGGER.debug("{}: { version: {}; friendly_name: {} }", MOD_ID, MOD_VERSION, MOD_FRIENDLY_NAME);
		ModBlocks.init();
	}

	public static void onInitializeClient() {
		LOGGER.info("Initializing {} Client on {}", MOD_ID, ElduinsHead.xplat().loader());
		LOGGER.debug("{}: { version: {}; friendly_name: {} }", MOD_ID, MOD_VERSION, MOD_FRIENDLY_NAME);
	}

	static Platform xplat() {
		return PLATFORM;
	}

	private static Platform createPlatformInstance() {
		return new FabricPlatform();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private static Identifier id(String namespace, String path) {
		return Identifier.fromNamespaceAndPath(namespace, path);
	}
}
