package com.elduin.elduins_head.portal;

import com.elduin.elduins_head.head.Head;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Where the head portal takes you. From anywhere it takes you inside Elduin's head; from inside the
 * head it takes you home to your bed (or to spawn if you haven't slept in one). Only players go
 * through; mobs and items stay where they are.
 */
public final class HeadTravel {

	private HeadTravel() {
	}

	public static @Nullable TeleportTransition destination(ServerLevel from, Entity entity) {
		if (!(entity instanceof ServerPlayer player)) return null;

		if (Head.isHead(from)) {
			// Exactly where you'd wake up after dying: your bed, or the world spawn.
			return player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.PLAY_PORTAL_SOUND);
		}

		ServerLevel head = from.getServer().getLevel(Head.LEVEL);
		if (head == null) return null;
		Head.buildIfNeeded(head);
		return new TeleportTransition(head, Head.ARRIVAL, Vec3.ZERO, Head.ARRIVAL_YAW, 0.0f,
				TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
	}
}
