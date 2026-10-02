package com.elduin.elduins_head.portal;

import com.elduin.elduins_head.block.HeadPortalBlock;
import com.elduin.elduins_head.content.ModBlocks;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Finds a glowstone frame around an empty spot, the same way Minecraft finds an obsidian frame for
 * a Nether portal: a rectangle of glowstone around an empty inside, at least 2 wide and 3 tall
 * inside (4 by 5 counting the frame). Corners don't matter. Same shape as the Elduin Portal's.
 */
public final class GlowstoneFrame {

	public static final int MIN_WIDTH = 2;
	public static final int MIN_HEIGHT = 3;
	public static final int MAX_SIZE = 21;

	private GlowstoneFrame() {
	}

	/** The inside of a frame: its bottom-left corner, which way it faces, and its size. */
	public record Inside(Direction.Axis axis, BlockPos bottomLeft, int width, int height) {

		public void fillWithPortal(Level level) {
			BlockState portal = ModBlocks.HEAD_PORTAL.defaultBlockState().setValue(HeadPortalBlock.AXIS, axis);
			Direction right = rightOf(axis);
			for (int w = 0; w < width; w++) {
				for (int h = 0; h < height; h++) {
					// UPDATE_KNOWN_SHAPE: don't let the half-built portal check its own neighbours yet.
					level.setBlock(bottomLeft.relative(right, w).above(h), portal,
							Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
				}
			}
		}
	}

	/** Look for a frame around {@code start} facing either way. */
	public static Optional<Inside> around(LevelReader level, BlockPos start) {
		Optional<Inside> found = around(level, start, Direction.Axis.X);
		return found.isPresent() ? found : around(level, start, Direction.Axis.Z);
	}

	public static Optional<Inside> around(LevelReader level, BlockPos start, Direction.Axis axis) {
		if (!isEmpty(level.getBlockState(start))) return Optional.empty();
		Direction right = rightOf(axis);
		Direction left = right.getOpposite();

		// Slide down to the floor, then left to the wall.
		BlockPos pos = start;
		for (int i = 0; i < MAX_SIZE && isEmpty(level.getBlockState(pos.below())); i++) pos = pos.below();
		if (!isFrame(level.getBlockState(pos.below()))) return Optional.empty();
		for (int i = 0; i < MAX_SIZE && isEmpty(level.getBlockState(pos.relative(left))); i++) pos = pos.relative(left);
		if (!isFrame(level.getBlockState(pos.relative(left)))) return Optional.empty();
		BlockPos bottomLeft = pos;

		// How wide: count empty blocks to the right wall, and check the floor under all of them.
		int width = 0;
		while (width <= MAX_SIZE && isEmpty(level.getBlockState(bottomLeft.relative(right, width)))) width++;
		if (width < MIN_WIDTH || width > MAX_SIZE) return Optional.empty();
		if (!isFrame(level.getBlockState(bottomLeft.relative(right, width)))) return Optional.empty();
		for (int w = 0; w < width; w++) {
			if (!isFrame(level.getBlockState(bottomLeft.relative(right, w).below()))) return Optional.empty();
		}

		// How tall: go up row by row until a whole row is glowstone (the roof).
		for (int height = 0; height <= MAX_SIZE; height++) {
			BlockPos row = bottomLeft.above(height);
			boolean allFrame = true;
			boolean allEmpty = true;
			for (int w = 0; w < width; w++) {
				BlockState state = level.getBlockState(row.relative(right, w));
				if (!isFrame(state)) allFrame = false;
				if (!isEmpty(state)) allEmpty = false;
			}
			if (allFrame && height >= MIN_HEIGHT) return Optional.of(new Inside(axis, bottomLeft, width, height));
			if (!allEmpty) return Optional.empty();
			if (!isFrame(level.getBlockState(row.relative(left)))) return Optional.empty();
			if (!isFrame(level.getBlockState(row.relative(right, width)))) return Optional.empty();
		}
		return Optional.empty();
	}

	public static Direction rightOf(Direction.Axis axis) {
		return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
	}

	private static boolean isFrame(BlockState state) {
		return state.is(Blocks.GLOWSTONE);
	}

	private static boolean isEmpty(BlockState state) {
		return state.isAir() || state.is(ModBlocks.HEAD_PORTAL);
	}
}
