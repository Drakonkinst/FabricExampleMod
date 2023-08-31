package io.github.drakonkinst.worldsinger.mixin.item;

import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    // When placing into a fluid block, allow waterlogged
    @Inject(method = "getPlacementState", at = @At(value = "RETURN"), cancellable = true)
    private void injectCustomFluidPlacementState(ItemPlacementContext context,
            CallbackInfoReturnable<BlockState> cir) {
        BlockState placementState = this.getBlock().getPlacementState(context);
        if (placementState == null || !placementState.contains(ModProperties.FLUIDLOGGED)) {
            return;
        }
        // Remove the fluid if double slabbed
        if (placementState.getBlock() instanceof SlabBlock
                && placementState.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
            return;
        }

        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        int index = Fluidlogged.getFluidIndex(fluidState.getFluid());
        if (index > -1) {
            cir.setReturnValue(placementState.with(ModProperties.FLUIDLOGGED, index));
        }
    }

    @Shadow
    public abstract Block getBlock();
}
