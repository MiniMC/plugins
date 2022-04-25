/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2022 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.rammelkast.anticheatreloaded.check.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.Backend;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.config.providers.Checks;
import com.rammelkast.anticheatreloaded.util.Utilities;
import com.rammelkast.anticheatreloaded.util.VersionUtil;

public final class NoFallCheck {

	public static final Map<UUID, Integer> VIOLATIONS = new HashMap<>();
	
	private static final CheckResult PASS = new CheckResult(CheckResult.Result.PASSED);
	
	public static CheckResult performCheck(final Player player, final double motionY) {
		final UUID uuid = player.getUniqueId();
		final Backend backend = AntiCheatReloaded.getManager().getBackend();
		final Checks checksConfig = AntiCheatReloaded.getManager().getConfiguration().getChecks();
		if (player.getGameMode() != GameMode.CREATIVE && !player.isInsideVehicle() && !player.isSleeping()
				&& !backend.isMovingExempt(player) && !backend.justPlaced(player) && !Utilities.isNearWater(player)
				&& !Utilities.isInWeb(player) && !player.getLocation().getBlock().getType().name().endsWith("TRAPDOOR")
				&& !VersionUtil.isSlowFalling(player)
				&& !Utilities
						.isNearShulkerBox(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation())
				&& !Utilities.isNearClimbable(player)
				&& !Utilities.isNearWater(player.getLocation().clone().subtract(0, 1.5, 0))) {
			if (player.getFallDistance() == 0.0) {
				if (VIOLATIONS.get(uuid) == null) {
					VIOLATIONS.put(uuid, 1);
				} else {
					VIOLATIONS.put(uuid, VIOLATIONS.get(player.getUniqueId()) + 1);
				}

				final int violations = VIOLATIONS.get(uuid);
				final int vlBeforeFlag = checksConfig.getInteger(CheckType.NOFALL, "vlBeforeFlag");
				if (violations >= vlBeforeFlag) {
					VIOLATIONS.put(player.getUniqueId(), 1);
					return new CheckResult(CheckResult.Result.FAILED,
							"tried to avoid fall damage (" + violations + " times in a row, max=" + vlBeforeFlag + ")");
				} else {
					return PASS;
				}
			} else {
				VIOLATIONS.put(uuid, 0);
				return PASS;
			}
		}
		return PASS;
	}
	
}
