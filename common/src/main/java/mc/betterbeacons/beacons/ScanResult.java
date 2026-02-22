package mc.betterbeacons.beacons;

/**
 * Helper record for scanning results.
 * @param radius The calculated radius.
 * @param weakestBlockId The block ID determining the radius.
 */
public record ScanResult(int radius, String weakestBlockId) {}
