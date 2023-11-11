package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SunlightFluid extends StillFluid {

    // Same fluid properties as lava
    public static final float HORIZONTAL_DRAG_MULTIPLIER = 0.5f;
    public static final float VERTICAL_DRAG_MULTIPLIER = 0.8f;

    @Override
    protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
        super.appendProperties(builder);
        builder.add(ModProperties.SUNLIGHT_LEVEL);
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return ModBlocks.SUNLIGHT.getDefaultState()
                .with(ModProperties.SUNLIGHT_LEVEL, state.get(ModProperties.SUNLIGHT_LEVEL));
    }

    @Override
    public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        BlockPos blockPos = pos.up();
        if (world.getBlockState(blockPos).isAir() && !world.getBlockState(blockPos)
                .isOpaqueFullCube(world, blockPos)) {
            if (random.nextInt(100) == 0) {
                double d = (double) pos.getX() + random.nextDouble();
                double e = (double) pos.getY() + 1.0;
                double f = (double) pos.getZ() + random.nextDouble();
                world.addParticle(ParticleTypes.LAVA, d, e, f, 0.0, 0.0, 0.0);
                world.playSound(d, e, f, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS,
                        0.2f + random.nextFloat() * 0.2f, 0.9f + random.nextFloat() * 0.15f, false);
            }
        }
    }

    @Override
    public void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            return;
        }

        int numFireAttempts = random.nextInt(3);
        if (numFireAttempts > 0) {
            BlockPos currentPos = pos;
            for (int i = 0; i < numFireAttempts; ++i) {
                currentPos = currentPos.add(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
                if (!world.canSetBlock(currentPos)) {
                    return;
                }
                BlockState blockState = world.getBlockState(currentPos);
                if (blockState.isAir()) {
                    if (!this.canLightFire(world, currentPos)) {
                        continue;
                    }
                    world.setBlockState(currentPos, AbstractFireBlock.getState(world, currentPos));
                    return;
                }
                if (!blockState.blocksMovement()) {
                    continue;
                }
                return;
            }
        } else {
            for (int i = 0; i < 3; ++i) {
                BlockPos blockPos2 = pos.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
                if (!world.canSetBlock(blockPos2)) {
                    return;
                }
                if (!world.isAir(blockPos2.up()) || !this.hasBurnableBlock(world, blockPos2)) {
                    continue;
                }
                world.setBlockState(blockPos2.up(), AbstractFireBlock.getState(world, blockPos2));
            }
        }
    }

    private boolean canLightFire(WorldView world, BlockPos pos) {
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            if (!this.hasBurnableBlock(world, pos.offset(direction))) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean hasBurnableBlock(WorldView world, BlockPos pos) {
        if (pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY()
                && !world.isChunkLoaded(pos)) {
            return false;
        }
        return world.getBlockState(pos).isBurnable();
    }
}
