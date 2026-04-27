package mc.betterbeacons.beacons;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

/**
 * Data record representing the state of an active beacon.
 * This information is used by {@link BeaconManager} to apply effects to players within range.
 *
 * @param pos            The world position of the beacon block.
 * @param dimension      The dimension key where the beacon is located.
 * @param radius         The effective chunk radius (diameter).
 * @param weakestBlockId The block ID that determined this radius (used for dynamic configuration lookups).
 * @param primary        The primary mob effect applied by the beacon.
 * @param secondary      The secondary mob effect applied by the beacon.
 */
public record BeaconInfo(
        BlockPos pos,
        ResourceKey<Level> dimension,
        int radius,
        String weakestBlockId,
        @Nullable MobEffect primary,
        @Nullable MobEffect secondary) {
}
