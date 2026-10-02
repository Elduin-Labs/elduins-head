package com.elduin.elduins_head.portal;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Pouring a water bucket into a glowstone frame turns it into a portal into Elduin's head, instead
 * of spilling the water. Anywhere else the bucket works like normal.
 */
public final class PortalFilling {

	private PortalFilling() {
	}

	public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(Items.WATER_BUCKET) || player.isSpectator()) return InteractionResult.PASS;
		if (!level.getBlockState(hit.getBlockPos()).is(Blocks.GLOWSTONE)) return InteractionResult.PASS;

		BlockPos inside = hit.getBlockPos().relative(hit.getDirection());
		Optional<GlowstoneFrame.Inside> frame = GlowstoneFrame.around(level, inside);
		if (frame.isEmpty()) return InteractionResult.PASS;

		if (!level.isClientSide()) {
			frame.get().fillWithPortal(level);
			level.playSound(null, inside, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
			level.playSound(null, inside, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.4f, 1.4f);
			if (!player.hasInfiniteMaterials()) {
				player.setItemInHand(hand, new ItemStack(Items.BUCKET));
			}
		}
		return InteractionResult.SUCCESS;
	}
}
