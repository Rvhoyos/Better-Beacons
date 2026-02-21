package mc.betterbeacons.mixin;

import mc.betterbeacons.config.BeaconConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconBlock.class)
public abstract class BeaconBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void allowCarpetPlacement(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (BeaconConfig.HIDE_BEAM_WITH_CARPET
                && player.getItemInHand(player.getUsedItemHand()).is(ItemTags.WOOL_CARPETS)) {
            // By returning PASS, we skip opening the beacon menu and let vanilla item
            // placement take over.
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
