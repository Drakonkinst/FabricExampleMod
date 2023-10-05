package io.github.drakonkinst.worldsinger.mixin.fluid;

import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin {

    @Inject(method = "canFill", at = @At("HEAD"), cancellable = true)
    private void makeCanFillDataDriven(BlockView world, BlockPos pos, BlockState state, Fluid fluid,
            CallbackInfoReturnable<Boolean> cir) {
        if (state.isIn(ModBlockTags.FLUIDS_CANNOT_BREAK)) {
            cir.setReturnValue(false);
        }
    }
}
