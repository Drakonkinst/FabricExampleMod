package io.github.drakonkinst.worldsinger.client.fluid;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
// I'm pretty sure this is client-side code, but weirdly enough its superclass and siblings aren't.
public class StillFluidRenderHandler implements FluidRenderHandler {

    private static final float Z_FIGHTING_BUFFER = 0.001f;

    private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction,
            BlockState state) {
        if (state.isOpaque()) {
            VoxelShape voxelShape = VoxelShapes.fullCube();
            VoxelShape neighborVoxelShape = state.getCullingShape(world, pos.offset(direction));
            return VoxelShapes.isSideCovered(voxelShape, neighborVoxelShape, direction);
        }
        return false;
    }

    protected final Identifier texture;
    protected final Sprite[] sprites;
    protected final boolean shaded;

    public StillFluidRenderHandler(Identifier stillTexture, boolean shaded) {
        this.texture = Objects.requireNonNull(stillTexture, "texture");
        this.sprites = new Sprite[2];
        this.shaded = shaded;
    }

    @Override
    public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos,
            FluidState state) {
        return sprites;
    }

    @Override
    public void reloadTextures(SpriteAtlasTexture textureAtlas) {
        sprites[0] = textureAtlas.getSprite(texture);
        // Workaround since renderer expects sprite to have 2 entries
        sprites[1] = sprites[0];
    }

    @Override
    // Since these fluids only have one sprite and are always full blocks, we can skip a lot of rendering logic
    public void renderFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer,
            BlockState blockState, FluidState fluidState) {
        Sprite sprite = sprites[0];
        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();
        float minV = sprite.getMinV();
        float maxV = sprite.getMaxV();

        BlockState blockStateDown = world.getBlockState(pos.offset(Direction.DOWN));
        FluidState fluidStateDown = blockStateDown.getFluidState();
        BlockState blockStateUp = world.getBlockState(pos.offset(Direction.UP));
        FluidState fluidStateUp = blockStateUp.getFluidState();
        BlockState blockStateNorth = world.getBlockState(pos.offset(Direction.NORTH));
        FluidState fluidStateNorth = blockStateNorth.getFluidState();
        BlockState blockStateSouth = world.getBlockState(pos.offset(Direction.SOUTH));
        FluidState fluidStateSouth = blockStateSouth.getFluidState();
        BlockState blockStateWest = world.getBlockState(pos.offset(Direction.WEST));
        FluidState fluidStateWest = blockStateWest.getFluidState();
        BlockState blockStateEast = world.getBlockState(pos.offset(Direction.EAST));
        FluidState fluidStateEast = blockStateEast.getFluidState();
        boolean shouldRenderUp =
                FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.UP,
                        fluidStateUp) && !StillFluidRenderHandler.isSideCovered(world, pos,
                        Direction.UP, blockStateUp);
        boolean shouldRenderDown =
                FluidRenderer.shouldRenderSide(world, pos, fluidState, blockState, Direction.DOWN,
                        fluidStateDown) && !StillFluidRenderHandler.isSideCovered(world, pos,
                        Direction.DOWN, blockStateDown);
        boolean shouldRenderNorth = FluidRenderer.shouldRenderSide(world, pos, fluidState,
                blockState, Direction.NORTH, fluidStateNorth);
        boolean shouldRenderSouth = FluidRenderer.shouldRenderSide(world, pos, fluidState,
                blockState, Direction.SOUTH, fluidStateSouth);
        boolean shouldRenderWest = FluidRenderer.shouldRenderSide(world, pos, fluidState,
                blockState, Direction.WEST, fluidStateWest);
        boolean shouldRenderEast = FluidRenderer.shouldRenderSide(world, pos, fluidState,
                blockState, Direction.EAST, fluidStateEast);
        if (!(shouldRenderUp || shouldRenderDown || shouldRenderEast || shouldRenderWest
                || shouldRenderNorth || shouldRenderSouth)) {
            return;
        }
        float brightnessDown = world.getBrightness(Direction.DOWN, shaded);
        float brightnessUp = world.getBrightness(Direction.UP, shaded);
        float brightnessNorth = world.getBrightness(Direction.NORTH, shaded);
        float brightnessWest = world.getBrightness(Direction.WEST, shaded);
        double x = pos.getX() & 0xF;
        double y = pos.getY() & 0xF;
        double z = pos.getZ() & 0xF;

        if (shouldRenderUp) {
            float u1 = sprite.getMinU();
            float u2 = u1;
            float u3 = sprite.getMaxU();
            float u4 = u3;
            float midU = (u1 + u3) / 2.0f;

            float v1 = sprite.getMinV();
            float v2 = v1;
            float v3 = sprite.getMaxV();
            float v4 = v3;
            float midV = (v1 + v3) / 2.0f;

            float frameDelta = sprite.getAnimationFrameDelta();
            u1 = MathHelper.lerp(frameDelta, u1, midU);
            u2 = MathHelper.lerp(frameDelta, u2, midU);
            u3 = MathHelper.lerp(frameDelta, u3, midU);
            u4 = MathHelper.lerp(frameDelta, u4, midU);

            v1 = MathHelper.lerp(frameDelta, v1, midV);
            v2 = MathHelper.lerp(frameDelta, v2, midV);
            v3 = MathHelper.lerp(frameDelta, v3, midV);
            v4 = MathHelper.lerp(frameDelta, v4, midV);

            int light = this.getLight(world, pos);
            this.vertex(vertexConsumer, x, y + 1.0, z, brightnessUp, u1, v1, light);
            this.vertex(vertexConsumer, x, y + 1.0, z + 1.0, brightnessUp, u2, v3, light);
            this.vertex(vertexConsumer, x + 1.0, y + 1.0, z + 1.0, brightnessUp, u3, v4, light);
            this.vertex(vertexConsumer, x + 1.0, y + 1.0, z, brightnessUp, u4, v2, light);
            if (fluidState.canFlowTo(world, pos.up())) {
                // Renders the other side
                this.vertex(vertexConsumer, x, y + 1.0, z, brightnessUp, u1, v1, light);
                this.vertex(vertexConsumer, x + 1.0, y + 1.0, z, brightnessUp, u4, v2, light);
                this.vertex(vertexConsumer, x + 1.0, y + 1.0, z + 1.0, brightnessUp, u3, v4, light);
                this.vertex(vertexConsumer, x, y + 1.0, z + 1.0, brightnessUp, u2, v3, light);
            }
        }
        if (shouldRenderDown) {
            int lightDown = this.getLight(world, pos.down());
            this.vertex(vertexConsumer, x, y, z + 1.0, brightnessDown, minU, maxV, lightDown);
            this.vertex(vertexConsumer, x, y, z, brightnessDown, minU, minV, lightDown);
            this.vertex(vertexConsumer, x + 1.0, y, z, brightnessDown, maxU, minV, lightDown);
            this.vertex(vertexConsumer, x + 1.0, y, z + 1.0, brightnessDown, maxU, maxV, lightDown);

            if (fluidState.canFlowTo(world, pos.down())) {
                // Renders the other side
                this.vertex(vertexConsumer, x, y, z + 1.0, brightnessDown, minU, maxV, lightDown);
                this.vertex(vertexConsumer, x + 1.0, y, z + 1.0, brightnessDown, maxU, maxV,
                        lightDown);
                this.vertex(vertexConsumer, x + 1.0, y, z, brightnessDown, maxU, minV, lightDown);
                this.vertex(vertexConsumer, x, y, z, brightnessDown, minU, minV, lightDown);
            }
        }
        int light = this.getLight(world, pos);
        for (Direction direction : Direction.Type.HORIZONTAL) {
            double minX;
            double maxX;
            double minZ;
            double maxZ;
            if (!(switch (direction) {
                case NORTH -> {
                    minX = x;
                    maxX = x + 1.0;
                    minZ = z + Z_FIGHTING_BUFFER;
                    maxZ = z + Z_FIGHTING_BUFFER;
                    yield shouldRenderNorth;
                }
                case SOUTH -> {
                    minX = x + 1.0;
                    maxX = x;
                    minZ = z + 1.0 - Z_FIGHTING_BUFFER;
                    maxZ = z + 1.0 - Z_FIGHTING_BUFFER;
                    yield shouldRenderSouth;
                }
                case WEST -> {
                    minX = x + Z_FIGHTING_BUFFER;
                    maxX = x + Z_FIGHTING_BUFFER;
                    minZ = z + 1.0;
                    maxZ = z;
                    yield shouldRenderWest;
                }
                default -> {
                    minX = x + 1.0 - Z_FIGHTING_BUFFER;
                    maxX = x + 1.0 - Z_FIGHTING_BUFFER;
                    minZ = z;
                    maxZ = z + 1.0;
                    yield shouldRenderEast;
                }
            }) || StillFluidRenderHandler.isSideCovered(world, pos, direction,
                    world.getBlockState(pos.offset(direction)))) {
                continue;
            }

            float brightness =
                    brightnessUp * (direction.getAxis() == Direction.Axis.Z ? brightnessNorth
                            : brightnessWest);
            this.vertex(vertexConsumer, minX, y + 1.0, minZ, brightness, minU, minV, light);
            this.vertex(vertexConsumer, maxX, y + 1.0, maxZ, brightness, maxU, minV, light);
            this.vertex(vertexConsumer, maxX, y, maxZ, brightness, maxU, maxV, light);
            this.vertex(vertexConsumer, minX, y, minZ, brightness, minU, maxV, light);
            // Renders the other side
            this.vertex(vertexConsumer, minX, y, minZ, brightness, minU, maxV, light);
            this.vertex(vertexConsumer, maxX, y, maxZ, brightness, maxU, maxV, light);
            this.vertex(vertexConsumer, maxX, y + 1.0, maxZ, brightness, maxU, minV, light);
            this.vertex(vertexConsumer, minX, y + 1.0, minZ, brightness, minU, minV, light);
        }
    }

    private int getLight(BlockRenderView world, BlockPos pos) {
        int i = WorldRenderer.getLightmapCoordinates(world, pos);
        int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
        int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xF);
        return (k > l ? k : l) | (m > n ? m : n) << 16;
    }

    private void vertex(VertexConsumer vertexConsumer, double x, double y, double z,
            float brightness, float u, float v, int light) {
        if (shaded) {
            brightness = 1.0f;
        }
        vertexConsumer.vertex(x, y, z)
                .color(brightness, brightness, brightness, 1.0f)
                .texture(u, v)
                .light(light)
                .normal(0.0f, 1.0f, 0.0f)
                .next();
    }
}
