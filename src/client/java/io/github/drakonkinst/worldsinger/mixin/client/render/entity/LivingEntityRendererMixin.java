package io.github.drakonkinst.worldsinger.mixin.client.render.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.entity.MidnightOverlayAccess;
import io.github.drakonkinst.worldsinger.entity.render.MidnightCreatureEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends
        EntityRenderer<T> {

    protected LivingEntityRendererMixin(Context ctx) {
        super(ctx);
    }

    @ModifyReturnValue(method = "getOverlay", at = @At("RETURN"))
    private static int renderModelWithMidnightOverlay(int original, LivingEntity entity,
            float whiteOverlayProgress) {
        boolean hasMidnightOverlay = ((MidnightOverlayAccess) entity).worldsinger$hasMidnightOverlay();
        if (hasMidnightOverlay) {
            boolean shouldFlashRed = entity.hurtTime > 0 || entity.deathTime > 0;
            if (shouldFlashRed) {
                return MidnightCreatureEntityRenderer.MIDNIGHT_OVERLAY_HURT_UV;
            }
            return MidnightCreatureEntityRenderer.MIDNIGHT_OVERLAY_UV;
        }
        return original;
    }
}
