package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.block.SporeKillable;
import io.github.drakonkinst.worldsinger.world.lumar.AetherSporeType;
import io.github.drakonkinst.worldsinger.world.lumar.LumarSeethe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class LivingAetherSporeFluid extends AetherSporeFluid implements
        WaterReactiveFluid {

    private static final int NUM_RANDOM_SPREAD_PER_RANDOM_TICK = 2;
    public static final int CATALYZE_VALUE_STILL = 250;
    public static final int CATALYZE_VALUE_FLOWING = 25;

    public LivingAetherSporeFluid(AetherSporeType aetherSporeType) {
        super(aetherSporeType);
    }

    protected abstract void doWaterReaction(World world, BlockPos pos, FluidState state,
            int sporeAmount, int waterAmount, Random random);

    @Override
    protected boolean hasRandomTicks() {
        return true;
    }

    @Override
    protected void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        super.onRandomTick(world, pos, state, random);

        if (world.hasRain(pos.up())) {
            this.reactToWater(world, pos, state, Integer.MAX_VALUE, random);
        }

        if (!LumarSeethe.areSporesFluidized(world)) {
            return;
        }

        // Spread to nearby dead spore sea blocks, regenerating them
        BlockState blockState = this.toBlockState(state);
        for (int i = 0; i < NUM_RANDOM_SPREAD_PER_RANDOM_TICK; ++i) {
            int offsetX = random.nextInt(3) - 1;
            int offsetY = random.nextInt(3) - 1;
            int offsetZ = random.nextInt(3) - 1;

            BlockPos blockPos = pos.add(offsetX, offsetY, offsetZ);
            if (world.getBlockState(blockPos).isOf(ModBlocks.DEAD_SPORE_SEA_BLOCK)
                    && world.getFluidState(blockPos).isStill()
                    && !SporeKillable.isSporeKillingBlockNearby(world, blockPos)) {
                world.setBlockState(blockPos, blockState);
            }
        }
    }

    @Override
    public boolean reactToWater(World world, BlockPos pos, FluidState fluidState, int waterAmount,
            Random random) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        int sporeAmount = this.isStill(fluidState) ? CATALYZE_VALUE_STILL : CATALYZE_VALUE_FLOWING;

        if (block instanceof FluidDrainable fluidDrainable) {
            ItemStack itemStack = fluidDrainable.tryDrainFluid(world, pos, blockState);
            if (itemStack.isEmpty() && block instanceof FluidBlock) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }

        this.doWaterReaction(world, pos, fluidState, sporeAmount, waterAmount, random);
        return true;
    }
}
