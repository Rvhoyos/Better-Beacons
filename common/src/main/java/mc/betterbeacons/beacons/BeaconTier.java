package mc.betterbeacons.beacons;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Enum representing the material tiers of a beacon pyramid.
 * Each tier corresponds to a specific material and its associated range.
 */
public enum BeaconTier {
    NONE(0, 0),
    IRON(1, 24), // 3x3 Chunks (~24 blocks radius)
    GOLD(1, 24), // Same as Iron
    EMERALD(2, 40), // 5x5 Chunks
    DIAMOND(3, 56), // 7x7 Chunks
    NETHERITE(4, 72); // 9x9 Chunks

    /** The vanilla level requirement for this tier. */
    public final int level;
    /** Approximate radius in blocks for range checking. */
    public final int rangeBlocks;

    BeaconTier(int level, int rangeBlocks) {
        this.level = level;
        this.rangeBlocks = rangeBlocks;
    }

    /**
     * Determines the tier based on the provided BlockState.
     * 
     * @param state The block state to check.
     * @return The corresponding BeaconTier, or NONE if not a valid beacon base.
     */
    public static BeaconTier fromBlock(BlockState state) {
        if (state.is(Blocks.NETHERITE_BLOCK))
            return NETHERITE;
        if (state.is(Blocks.DIAMOND_BLOCK))
            return DIAMOND;
        if (state.is(Blocks.EMERALD_BLOCK))
            return EMERALD;
        if (state.is(Blocks.GOLD_BLOCK))
            return GOLD;
        if (state.is(Blocks.IRON_BLOCK))
            return IRON;
        return NONE;
    }

    /**
     * Calculates the effective chunk radius for this tier based on mod configuration.
     * 
     * @return The radius in chunks.
     */
    public int getChunkRadius() {
        // Read from config. Default to hardcoded values if missing (safe fallback)
        int size = mc.betterbeacons.config.BeaconConfig.BEACON_BLOCK_SIZES.getOrDefault(this.name().toLowerCase(), 3);
        // Radius = (Size - 1) / 2. e.g. Size 3 -> Radius 1. Size 9 -> Radius 4.
        return Math.max(0, (size - 1) / 2);
    }
}
