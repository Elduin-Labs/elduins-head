package com.elduin.elduins_head.block;

import com.elduin.elduins_head.portal.HeadTravel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** The pink inside of a watered glowstone frame. Stand in it to go into Elduin's head, or back home. */
public class HeadPortalBlock extends Block implements Portal {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	private static final VoxelShape X_SHAPE = Block.box(0, 0, 6, 16, 16, 10);
	private static final VoxelShape Z_SHAPE = Block.box(6, 0, 0, 10, 16, 16);

	/** How long a player stands in the portal before it takes them, in ticks. Same as a Nether portal. */
	private static final int PLAYER_WAIT = 80;

	private static final DustParticleOptions PINK = new DustParticleOptions(0xFF8FB8, 1.0f);

	public HeadPortalBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
	}

	/** Break the frame and the portal goes out, one block at a time, like a Nether portal. */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		Direction.Axis side = direction.getAxis();
		boolean inThePortalsPlane = side == Direction.Axis.Y || side == state.getValue(AXIS);
		if (inThePortalsPlane && !neighborState.is(this) && !neighborState.is(Blocks.GLOWSTONE)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier effects, boolean isPrecise) {
		if (entity instanceof Player && entity.canUsePortal(false)) {
			entity.setAsInsidePortal(this, pos);
		}
	}

	@Override
	public int getPortalTransitionTime(ServerLevel level, Entity entity) {
		if (entity instanceof Player player) {
			return player.getAbilities().invulnerable ? 1 : PLAYER_WAIT;
		}
		return 0;
	}

	@Override
	public @Nullable TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
		return HeadTravel.destination(level, entity);
	}

	@Override
	public Transition getLocalTransition() {
		return Transition.CONFUSION;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		for (int i = 0; i < 2; i++) {
			level.addParticle(PINK, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
					pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
		}
	}
}
