package io.github.drakonkinst.worldsinger.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.drakonkinst.worldsinger.CameraPosAccess;
import io.github.drakonkinst.worldsinger.ModClientEnums;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.util.ColorUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BackgroundRenderer.FogData;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @Shadow
    private static float red;
    @Shadow
    private static float green;
    @Shadow
    private static float blue;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;", ordinal = 0))
    private static CameraSubmersionType skipExpensiveCalculation(CameraSubmersionType original) {
        // Pretend to be lava, skipping the expensive default "else" calculation
        if (original == ModClientEnums.CameraSubmersionType.SPORE_SEA) {
            return CameraSubmersionType.LAVA;
        }
        return original;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"))
    private static void correctCustomFluidColors(Camera camera, float tickDelta, ClientWorld world,
            int viewDistance, float skyDarkness, CallbackInfo ci) {
        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();

        if (cameraSubmersionType != ModClientEnums.CameraSubmersionType.SPORE_SEA) {
            return;
        }

        // Set color based on the specific spore fluid
        CameraPosAccess cameraPos = (CameraPosAccess) camera;
        BlockState blockState = cameraPos.worldsinger$getBlockState();

        if (blockState.isOf(ModBlocks.SUNLIGHT)) {
            // Use Sunlight Spore colors for Sunlight blocks
            int color = SunlightSpores.getInstance().getColor();
            red = ColorUtil.getNormalizedRed(color);
            green = ColorUtil.getNormalizedGreen(color);
            blue = ColorUtil.getNormalizedBlue(color);
            return;
        }

        FluidState fluidState = ((CameraPosAccess) camera).worldsinger$getSubmersedFluidState();
        if (fluidState.getFluid() instanceof AetherSporeFluid aetherSporeFluid) {
            red = aetherSporeFluid.getFogRed();
            green = aetherSporeFluid.getFogGreen();
            blue = aetherSporeFluid.getFogBlue();
        } else {
            Worldsinger.LOGGER.error(
                    "Expected fluid to be an instance of AetherSporeFluid since Spore Sea submersion type is being used");
        }
    }

    @Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"), cancellable = true)
    private static void injectCustomFluidFogSettings(Camera camera, FogType fogType,
            float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (camera.getSubmersionType() == ModClientEnums.CameraSubmersionType.SPORE_SEA) {
            FogData fogData = new FogData(fogType);
            Entity entity = camera.getFocusedEntity();
            if (entity.isSpectator()) {
                // Match spectator mode settings for other fluids
                fogData.fogStart = -8.0f;
                fogData.fogEnd = viewDistance * 0.5f;
            } else {
                fogData.fogStart = AetherSporeFluid.FOG_START;
                fogData.fogEnd = AetherSporeFluid.FOG_END;
            }

            // Call the end of the method
            RenderSystem.setShaderFogStart(fogData.fogStart);
            RenderSystem.setShaderFogEnd(fogData.fogEnd);
            RenderSystem.setShaderFogShape(fogData.fogShape);
            ci.cancel();
        }
    }
}
