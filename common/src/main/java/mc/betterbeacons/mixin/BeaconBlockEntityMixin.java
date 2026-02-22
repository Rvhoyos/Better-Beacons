package mc.betterbeacons.mixin;

import mc.betterbeacons.beacons.BeaconManager;
import mc.betterbeacons.config.BeaconConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin to intercept vanilla beacon behavior and inject custom effect
 * management and rendering logic.
 * Hardened to avoid early registry access which causes NeoForge bootstrap issues.
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {

    @Shadow
    int levels;
    @Shadow
    @Nullable
    Holder<MobEffect> primaryPower;
    @Shadow
    @Nullable
    Holder<MobEffect> secondaryPower;

    /**
     * Hides the beacon beam if a carpet is placed directly above it.
     */
    @Inject(method = "getBeamSections", at = @At("HEAD"), cancellable = true)
    private void hideBeamWithCarpet(CallbackInfoReturnable<List<BeaconBeamOwner.Section>> cir) {
        BlockEntity self = (BlockEntity) (Object) this;
        Level world = self.getLevel();
        if (BeaconConfig.HIDE_BEAM_WITH_CARPET && world != null) {
            // Identifier is safe here, Registries might be risky but usually ready by the time BlockEntity ticks.
            TagKey<Block> carpets = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "wool_carpets"));
            BlockState above = world.getBlockState(self.getBlockPos().above());
            if (above.is(carpets)) {
                cir.setReturnValue(Collections.emptyList());
            }
        }
    }

    /**
     * Cancels vanilla effect application when custom beacon logic is enabled.
     */
    @Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
    private static void cancelVanillaEffects(Level level, BlockPos pos, int levels, @Nullable Holder<MobEffect> primary,
            @Nullable Holder<MobEffect> secondary, CallbackInfo ci) {
        if (BeaconConfig.ENABLE_CUSTOM_BEACONS) {
            ci.cancel();
        }
    }

    /**
     * Periodic tick injection to update beacon registration in the {@link BeaconManager}.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private static void tick(Level level, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity,
            CallbackInfo ci) {
        if (!BeaconConfig.ENABLE_CUSTOM_BEACONS || level.isClientSide())
            return;

        if (level.getGameTime() % 80L == 0L) {
            BeaconBlockEntityMixin self = (BeaconBlockEntityMixin) (Object) blockEntity;
            // Delegate all complex logic to BeaconManager to avoid Mixin class-loading issues
            BeaconManager.get().updateBeaconRegistration(level, pos, self.levels, self.primaryPower, self.secondaryPower);
        }
    }
}
