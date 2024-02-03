/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.mixin.client.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {

    @Inject(method = "isSameFluid", at = @At("HEAD"), cancellable = true)
    private static void makeSporeFluidsIdentical(FluidState a, FluidState b,
            CallbackInfoReturnable<Boolean> cir) {
        if (a.isIn(ModFluidTags.AETHER_SPORES) && b.isIn(ModFluidTags.AETHER_SPORES)) {
            cir.setReturnValue(a.isStill() == b.isStill());
        }
    }

    @WrapOperation(method = "getFluidHeight(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/fluid/Fluid;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/Fluid;matchesType(Lnet/minecraft/fluid/Fluid;)Z"))
    private boolean makeSporeFluidsIdentical2(Fluid instance, Fluid fluid,
            Operation<Boolean> original) {
        if (instance.getDefaultState().isIn(ModFluidTags.AETHER_SPORES) && fluid.getDefaultState()
                .isIn(ModFluidTags.AETHER_SPORES)) {
            return true;
        }
        // Default behavior
        return original.call(instance, fluid);
    }

    @WrapOperation(method = "getFluidHeight(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/fluid/Fluid;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isSolid()Z"))
    private boolean fixSunlightBlockRendering(BlockState instance, Operation<Boolean> original) {
        if (instance.isOf(ModBlocks.SUNLIGHT)) {
            return true;
        }
        return original.call(instance);
    }
}
