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

package io.github.drakonkinst.worldsinger.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.LunagreeData;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeManager.LunagreeLocation;
import io.github.drakonkinst.worldsinger.entity.LunagreeDataAccess;
import io.github.drakonkinst.worldsinger.mixin.client.accessor.WorldRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.SkyRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class LumarSkyRenderer implements SkyRenderer {

    private static final Identifier SUN = new Identifier("textures/environment/sun.png");
    private static final Identifier LUMAR_MOON = Worldsinger.id(
            "textures/environment/lumar_moon.png");
    private static final int MOON_TEXTURE_SECTIONS_Y = 2;
    private static final int MOON_TEXTURE_SECTIONS_X = 4;

    private static final float SUN_RADIUS = 30.0f;
    private static final float SUN_HEIGHT = 100.0f;
    private static final int[] SPORE_ID_TO_MOON_INDEX = { -1, 0, 1, 2, 4, 5, 6 };

    private final VertexBuffer starsBuffer;
    private final VertexBuffer lightSkyBuffer;
    private final VertexBuffer darkSkyBuffer;

    public LumarSkyRenderer() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        // Stars
        this.starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.BuiltBuffer starsBuffer = ((WorldRendererAccessor) MinecraftClient.getInstance().worldRenderer).worldsinger$renderStars(
                bufferBuilder);
        this.starsBuffer.bind();
        this.starsBuffer.upload(starsBuffer);
        VertexBuffer.unbind();

        // Light Sky
        this.lightSkyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.BuiltBuffer lightSkyBuffer = WorldRendererAccessor.worldsinger$renderSky(
                bufferBuilder, 16.0F);
        this.lightSkyBuffer.bind();
        this.lightSkyBuffer.upload(lightSkyBuffer);
        VertexBuffer.unbind();

        // Dark Sky
        this.darkSkyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.BuiltBuffer darkSkyBuffer = WorldRendererAccessor.worldsinger$renderSky(
                bufferBuilder, -16.0F);
        this.darkSkyBuffer.bind();
        this.darkSkyBuffer.upload(darkSkyBuffer);
        VertexBuffer.unbind();
    }

    @Override
    public void render(WorldRenderContext context) {
        final MatrixStack matrices = context.matrixStack();
        final Matrix4f projectionMatrix = context.projectionMatrix();
        final float tickDelta = context.tickDelta();
        final Camera camera = context.camera();
        final GameRenderer gameRenderer = context.gameRenderer();
        final ClientWorld world = context.world();
        final ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert (player != null);

        Vec3d skyColor = world.getSkyColor(gameRenderer.getCamera().getPos(), tickDelta);
        float red = (float) skyColor.x;
        float green = (float) skyColor.y;
        float blue = (float) skyColor.z;

        BackgroundRenderer.applyFogColor();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(red, green, blue, 1.0f);
        ShaderProgram shaderProgram = RenderSystem.getShader();

        // Draw light sky
        this.drawLightSky(bufferBuilder, matrices, projectionMatrix, shaderProgram, world,
                tickDelta);

        // Draw things in the sky
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);
        matrices.push();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
        matrices.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(world.getSkyAngle(tickDelta) * 360.0f));
        this.drawSun(bufferBuilder, matrices);
        this.drawStars(matrices, projectionMatrix, world, gameRenderer, camera, tickDelta);
        matrices.pop();
        this.drawMoons(bufferBuilder, matrices, player, tickDelta);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        // Draw dark sky
        this.drawDarkSky(matrices, projectionMatrix, shaderProgram, world, tickDelta, player);

        RenderSystem.depthMask(true);
    }

    private void drawSun(BufferBuilder bufferBuilder, MatrixStack matrices) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SUN);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(positionMatrix, -SUN_RADIUS, SUN_HEIGHT, -SUN_RADIUS)
                .texture(0.0f, 0.0f)
                .next();
        bufferBuilder.vertex(positionMatrix, SUN_RADIUS, SUN_HEIGHT, -SUN_RADIUS)
                .texture(1.0f, 0.0f)
                .next();
        bufferBuilder.vertex(positionMatrix, SUN_RADIUS, SUN_HEIGHT, SUN_RADIUS)
                .texture(1.0f, 1.0f)
                .next();
        bufferBuilder.vertex(positionMatrix, -SUN_RADIUS, SUN_HEIGHT, SUN_RADIUS)
                .texture(0.0f, 1.0f)
                .next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private void drawMoons(BufferBuilder bufferBuilder, MatrixStack matrices,
            @NotNull ClientPlayerEntity player, float tickDelta) {
        RenderSystem.setShaderTexture(0, LUMAR_MOON);
        matrices.push();

        final LunagreeData lunagreeData = ((LunagreeDataAccess) player).worldsinger$getLunagreeData();
        final Vec3d playerPos = player.getCameraPosVec(tickDelta);
        for (LunagreeLocation location : lunagreeData.getKnownLunagreeLocations()) {
            final double distSq = location.distSqTo(playerPos.getX(), playerPos.getZ());
            if (distSq > LunagreeManager.TRAVEL_DISTANCE * LunagreeManager.TRAVEL_DISTANCE) {
                continue;
            }
            // Render moon
            drawMoonAtLocation(bufferBuilder, matrices, location, playerPos, distSq);
        }

        Vec3d moonPos = Vec3d.ZERO;
        double deltaX = playerPos.getX() - moonPos.getX();
        double deltaZ = playerPos.getZ() - moonPos.getZ();
        double distSq = deltaX * deltaX + deltaZ * deltaZ;
        float distance = MathHelper.sqrt((float) distSq);
        float multiplier = distance / LunagreeManager.TRAVEL_DISTANCE;

        float radius = 300.0f;
        // float moonHeight = 200.0f;  // Can go from 100.0 to 200.0f
        float moonHeight = MathHelper.lerp(multiplier, 100.0f, 500.0f);

        // For some reason we need to flip the Z coordinate here
        double angleRadians = MathHelper.atan2(playerPos.getZ() - moonPos.getZ(),
                moonPos.getX() - playerPos.getX());
        float horizontalAngle = (float) angleRadians * MathHelper.DEGREES_PER_RADIAN;

        float verticalAngle = MathHelper.lerp(multiplier, 180.0f, 45.0f);

        drawMoon(bufferBuilder, matrices, 0, radius, moonHeight, horizontalAngle, verticalAngle);
        // drawMoon(bufferBuilder, matrices, 1, radius, moonHeight, 45.0f + 180.0f, 70.0f);
        // Worldsinger.LOGGER.info(
        //         multiplier + " " + moonHeight + " " + horizontalAngle + " " + verticalAngle);

        matrices.pop();
    }

    private void drawMoonAtLocation(BufferBuilder bufferBuilder, MatrixStack matrices,
            LunagreeLocation lunagreeLocation, Vec3d playerPos, double distSq) {
        final int sporeId = lunagreeLocation.sporeId();
        if (sporeId < 0 || sporeId >= SPORE_ID_TO_MOON_INDEX.length) {
            Worldsinger.LOGGER.warn("Cannot render lunagree with unknown spore ID " + sporeId);
            return;
        }
        int moonIndex = SPORE_ID_TO_MOON_INDEX[sporeId];

        // Calculate shrink factor
        // TODO
        float radius = 300.0f;
        float moonHeight = 100.0f;

        // Calculate vertical angle
        // TODO
        float verticalAngle = 0.0f;

        // Calculate horizontal angle
        double angleRadians = MathHelper.atan2(playerPos.getZ() - lunagreeLocation.blockZ(),
                playerPos.getX() - lunagreeLocation.blockX());
        float horizontalAngle = (float) angleRadians * MathHelper.DEGREES_PER_RADIAN;

        drawMoon(bufferBuilder, matrices, moonIndex, radius, moonHeight, horizontalAngle,
                verticalAngle);
    }

    private void drawMoon(BufferBuilder bufferBuilder, MatrixStack matrices, int moonIndex,
            float radius, float height, float horizontalAngle, float verticalAngle) {
        int xIndex = moonIndex % MOON_TEXTURE_SECTIONS_X;
        int yIndex = moonIndex / MOON_TEXTURE_SECTIONS_X % MOON_TEXTURE_SECTIONS_Y;
        float x1 = (float) xIndex / MOON_TEXTURE_SECTIONS_X;
        float y1 = (float) yIndex / MOON_TEXTURE_SECTIONS_Y;
        float x2 = (float) (xIndex + 1) / MOON_TEXTURE_SECTIONS_X;
        float y2 = (float) (yIndex + 1) / MOON_TEXTURE_SECTIONS_Y;

        matrices.push();

        // Position the moon
        // Horizontal position. 0 = West (-X) direction, then goes clockwise.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(horizontalAngle));
        // matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(verticalAngle));
        // Vertical position. 180 = Directly upwards (+Y), 90 = Directly horizontal
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(verticalAngle));

        // Draw moon
        Matrix4f moonPosition = matrices.peek().getPositionMatrix();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        // Height needs to be inverted for some reason, don't know why.
        bufferBuilder.vertex(moonPosition, -radius, -height, radius).texture(x2, y2).next();
        bufferBuilder.vertex(moonPosition, radius, -height, radius).texture(x1, y2).next();
        bufferBuilder.vertex(moonPosition, radius, -height, -radius).texture(x1, y1).next();
        bufferBuilder.vertex(moonPosition, -radius, -height, -radius).texture(x2, y1).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        matrices.pop();
    }

    private int getMoonTextureIndex() {
        return 0;
    }

    private void drawStars(MatrixStack matrices, Matrix4f projectionMatrix, ClientWorld world,
            GameRenderer gameRenderer, Camera camera, float tickDelta) {
        float starBrightness = world.method_23787(tickDelta);
        if (starBrightness > 0.0f) {
            RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness,
                    starBrightness);
            BackgroundRenderer.clearFog();
            this.starsBuffer.bind();
            this.starsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix,
                    GameRenderer.getPositionProgram());
            VertexBuffer.unbind();

            // Render Fog
            this.renderFog(world, gameRenderer, camera, tickDelta);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderFog(ClientWorld world, GameRenderer gameRenderer, Camera camera,
            float tickDelta) {
        float viewDistance = gameRenderer.getViewDistance();
        Vec3d cameraPos = camera.getPos();
        double cameraX = cameraPos.getX();
        double cameraY = cameraPos.getY();
        boolean useThickFog = world.getDimensionEffects()
                .useThickFog(MathHelper.floor(cameraX), MathHelper.floor(cameraY))
                || MinecraftClient.getInstance().inGameHud.getBossBarHud().shouldThickenFog();
        BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_SKY, viewDistance,
                useThickFog, tickDelta);
    }

    private void drawLightSky(BufferBuilder bufferBuilder, MatrixStack matrices,
            Matrix4f projectionMatrix, ShaderProgram shaderProgram, ClientWorld world,
            float tickDelta) {
        this.lightSkyBuffer.bind();
        this.lightSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix,
                shaderProgram);
        VertexBuffer.unbind();
        RenderSystem.enableBlend();
        float[] fogRgba = world.getDimensionEffects()
                .getFogColorOverride(world.getSkyAngle(tickDelta), tickDelta);
        if (fogRgba != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            float i = MathHelper.sin(world.getSkyAngleRadians(tickDelta)) < 0.0f ? 180.0f : 0.0f;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f, 0.0f, 100.0f, 0.0f)
                    .color(fogRgba[0], fogRgba[1], fogRgba[2], fogRgba[3])
                    .next();
            for (int n = 0; n <= 16; ++n) {
                float o = (float) n * ((float) Math.PI * 2) / 16.0f;
                float p = MathHelper.sin(o);
                float q = MathHelper.cos(o);
                bufferBuilder.vertex(matrix4f, p * 120.0f, q * 120.0f, -q * 40.0f * fogRgba[3])
                        .color(fogRgba[0], fogRgba[1], fogRgba[2], 0.0f)
                        .next();
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            matrices.pop();
        }
    }

    private void drawDarkSky(MatrixStack matrices, Matrix4f projectionMatrix,
            ShaderProgram shaderProgram, ClientWorld world, float tickDelta,
            @NotNull ClientPlayerEntity player) {
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
        double skyDarknessHeight = player.getCameraPosVec(tickDelta).y - world.getLevelProperties()
                .getSkyDarknessHeight(world);
        if (skyDarknessHeight < 0.0) {
            matrices.push();
            matrices.translate(0.0f, 12.0f, 0.0f);
            this.darkSkyBuffer.bind();
            this.darkSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix,
                    shaderProgram);
            VertexBuffer.unbind();
            matrices.pop();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
