package mc.betterbeacons.beacons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
        @Nullable Holder<MobEffect> primary,
        @Nullable Holder<MobEffect> secondary) {

    /**
     * Codec for serializing and deserializing BeaconInfo objects.
     */
    public static final Codec<BeaconInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(BeaconInfo::pos),
        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(BeaconInfo::dimension),
        Codec.INT.fieldOf("radius").forGetter(BeaconInfo::radius),
        Codec.STRING.fieldOf("weakestBlockId").forGetter(BeaconInfo::weakestBlockId),
        BuiltInRegistries.MOB_EFFECT.holderByNameCodec().optionalFieldOf("primary").forGetter(info -> Optional.ofNullable(info.primary())),
        BuiltInRegistries.MOB_EFFECT.holderByNameCodec().optionalFieldOf("secondary").forGetter(info -> Optional.ofNullable(info.secondary()))
    ).apply(instance, (pos, dim, rad, bid, p, s) -> new BeaconInfo(pos, dim, rad, bid, p.orElse(null), s.orElse(null))));
}
