package com.elduin.elduins_head.head;

import com.elduin.elduins_head.ElduinsHead;
import com.elduin.elduins_head.block.HeadPortalBlock;
import com.elduin.elduins_head.content.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Elduin's head: an empty dimension with one big room built in it the first time anyone visits.
 * Pink walls, two eyes on the front wall you can look out of, a giant brain floating in the
 * middle on its brain stem, and a portal home by the door.
 *
 * <pre>
 *   room inside      x -20..20, y 64..100, z -20..20   (walls one block further out)
 *   eyes             on the north wall (z = -21), the "face"
 *   brain            centred at (0, 82, 3)
 *   portal home      glowstone frame x -1..2, y 64..68, z = -14
 *   you arrive at    (0.5, 64, -11.5), looking at the brain
 * </pre>
 */
public final class Head {

	public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, ElduinsHead.id("head"));

	public static final Vec3 ARRIVAL = new Vec3(0.5, 64.0, -11.5);
	/** Facing south, at the brain. */
	public static final float ARRIVAL_YAW = 0.0f;

	private static final int HALF = 20;
	private static final int FLOOR = 63;
	private static final int CEILING = 101;

	private static final BlockPos BRAIN_CENTRE = new BlockPos(0, 82, 3);
	private static final double BRAIN_X = 10.5;
	private static final double BRAIN_Y = 7.5;
	private static final double BRAIN_Z = 12.5;

	// 26 folded the sixteen colours of each block into one field.
	//? if >=26 {
	/*private static final BlockState PUPIL = Blocks.STAINED_GLASS.black().defaultBlockState();
	private static final BlockState IRIS = Blocks.CONCRETE.lightBlue().defaultBlockState();
	private static final BlockState EYE_WHITE = Blocks.CONCRETE.white().defaultBlockState();
	*///? } else {
	private static final BlockState PUPIL = Blocks.BLACK_STAINED_GLASS.defaultBlockState();
	private static final BlockState IRIS = Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
	private static final BlockState EYE_WHITE = Blocks.WHITE_CONCRETE.defaultBlockState();
	//? }

	private Head() {
	}

	public static boolean isHead(Level level) {
		return level.dimension().equals(LEVEL);
	}

	/** The room is built once; after that the brain is already there. */
	public static void buildIfNeeded(ServerLevel level) {
		if (level.getBlockState(BRAIN_CENTRE).is(ModBlocks.BRAIN)) return;
		ElduinsHead.LOGGER.info("Building the inside of Elduin's head");
		build(level);
	}

	private static void build(ServerLevel level) {
		BlockState wall = ModBlocks.HEAD_WALL.defaultBlockState();
		BlockState brain = ModBlocks.BRAIN.defaultBlockState();

		// The skull: floor, ceiling, and four walls.
		for (int x = -HALF - 1; x <= HALF + 1; x++) {
			for (int z = -HALF - 1; z <= HALF + 1; z++) {
				set(level, x, FLOOR, z, wall);
				set(level, x, CEILING, z, wall);
			}
		}
		for (int y = FLOOR; y <= CEILING; y++) {
			for (int i = -HALF - 1; i <= HALF + 1; i++) {
				set(level, i, y, -HALF - 1, wall);
				set(level, i, y, HALF + 1, wall);
				set(level, -HALF - 1, y, i, wall);
				set(level, HALF + 1, y, i, wall);
			}
		}

		// Two eyes in the face wall. The black pupils are glass, so you can see out.
		eye(level, -9, 88);
		eye(level, 9, 88);

		// The brain: a big squishy egg shape, with a dip down the middle between the two halves.
		for (int x = -11; x <= 11; x++) {
			for (int y = -8; y <= 8; y++) {
				for (int z = -13; z <= 13; z++) {
					double nx = x / BRAIN_X;
					double ny = y / BRAIN_Y;
					double nz = z / BRAIN_Z;
					if (nx * nx + ny * ny + nz * nz > 1.0) continue;
					boolean groove = x == 0 && y > -2;
					if (groove) continue;
					set(level, BRAIN_CENTRE.getX() + x, BRAIN_CENTRE.getY() + y, BRAIN_CENTRE.getZ() + z, brain);
				}
			}
		}
		// The middle of the groove, so the brain is all one piece (and so buildIfNeeded sees it).
		level.setBlock(BRAIN_CENTRE, brain, Block.UPDATE_ALL);

		// The brain stem, down to the floor.
		for (int y = FLOOR + 1; y < BRAIN_CENTRE.getY() - 5; y++) {
			for (int x = -1; x <= 1; x++) {
				for (int z = 6; z <= 8; z++) {
					if (Math.abs(x) + Math.abs(z - 7) <= 1) set(level, x, y, z, brain);
				}
			}
		}

		// The way home: a lit glowstone portal by where you arrive.
		BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
		BlockState portal = ModBlocks.HEAD_PORTAL.defaultBlockState().setValue(HeadPortalBlock.AXIS, Direction.Axis.X);
		for (int x = -1; x <= 2; x++) {
			for (int y = FLOOR + 1; y <= FLOOR + 5; y++) {
				boolean edge = x == -1 || x == 2 || y == FLOOR + 1 || y == FLOOR + 5;
				if (edge) set(level, x, y, -14, glowstone);
			}
		}
		for (int x = 0; x <= 1; x++) {
			for (int y = FLOOR + 2; y <= FLOOR + 4; y++) {
				level.setBlock(new BlockPos(x, y, -14), portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
		}
	}

	private static void eye(ServerLevel level, int cx, int cy) {
		int z = -HALF - 1;
		for (int x = cx - 6; x <= cx + 6; x++) {
			for (int y = cy - 6; y <= cy + 6; y++) {
				double r = Math.hypot(x - cx, y - cy);
				BlockState state;
				if (r <= 1.6) {
					state = PUPIL;
				} else if (r <= 3.5) {
					state = IRIS;
				} else if (r <= 5.5) {
					state = EYE_WHITE;
				} else {
					continue;
				}
				set(level, x, y, z, state);
			}
		}
	}

	private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
		level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
	}

	/** The brain is always thinking: little sparks jump around on it while someone is visiting. */
	public static void tick(MinecraftServer server) {
		if (server.getTickCount() % 5 != 0) return;
		ServerLevel level = server.getLevel(LEVEL);
		if (level == null || level.players().isEmpty()) return;
		RandomSource random = level.getRandom();
		for (int i = 0; i < 6; i++) {
			// A random spot on the brain's surface.
			double theta = random.nextDouble() * Math.PI * 2.0;
			double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
			double x = BRAIN_CENTRE.getX() + 0.5 + BRAIN_X * 1.05 * Math.sin(phi) * Math.cos(theta);
			double y = BRAIN_CENTRE.getY() + 0.5 + BRAIN_Y * 1.05 * Math.cos(phi);
			double z = BRAIN_CENTRE.getZ() + 0.5 + BRAIN_Z * 1.05 * Math.sin(phi) * Math.sin(theta);
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 3, 0.2, 0.2, 0.2, 0.05);
		}
	}
}
