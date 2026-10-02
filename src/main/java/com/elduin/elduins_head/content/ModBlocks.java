package com.elduin.elduins_head.content;

import com.elduin.elduins_head.ElduinsHead;
import com.elduin.elduins_head.block.HeadPortalBlock;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {

	/** The inside of the head: walls, floor and ceiling. */
	public static final Block HEAD_WALL = register("head_wall", Block::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(1.5f).sound(SoundType.WOOL));

	/** The brain. Squishy, and it glows a little because it's always thinking. */
	public static final Block BRAIN = register("brain", Block::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.8f).sound(SoundType.SLIME_BLOCK)
					.lightLevel(state -> 8));

	/** The swirly pink inside of a glowstone frame that had water poured in it. */
	public static final Block HEAD_PORTAL = register("head_portal", HeadPortalBlock::new,
			BlockBehaviour.Properties.of().noCollision().noOcclusion().strength(-1.0f).sound(SoundType.GLASS)
					.lightLevel(state -> 11).pushReaction(PushReaction.BLOCK).noLootTable());

	public static final Item HEAD_WALL_ITEM = registerBlockItem("head_wall", HEAD_WALL);
	public static final Item BRAIN_ITEM = registerBlockItem("brain", BRAIN);

	private ModBlocks() {
	}

	/** Loads this class, which registers everything above. */
	public static void init() {
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ElduinsHead.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	private static Item registerBlockItem(String name, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ElduinsHead.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key,
				new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
	}
}
