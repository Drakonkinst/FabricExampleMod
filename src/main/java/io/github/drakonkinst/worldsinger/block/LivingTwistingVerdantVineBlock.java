package io.github.drakonkinst.worldsinger.block;

import io.github.drakonkinst.worldsinger.entity.AbstractSporeGrowthEntity;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LivingTwistingVerdantVineBlock extends TwistingVerdantVineBlock implements
        SporeKillable, WaterReactiveBlock {

    private static final int MAX_DEPTH = 3;

    // Works for both bud and stem version
    public static void growInSameDirection(World world, BlockPos pos, BlockState state,
            Random random) {
        if (!state.isIn(ModBlockTags.TWISTING_VERDANT_VINES)) {
            return;
        }
        AbstractVerticalGrowthComponentBlock block = (AbstractVerticalGrowthComponentBlock) state.getBlock();
        world.setBlockState(pos, state.with(ModProperties.CATALYZED, true));

        Direction direction = state.get(Properties.VERTICAL_DIRECTION);
        BlockPos.Mutable outermostPos = pos.mutableCopy();
        BlockState outermostState;

        int depth = 0;
        while (true) {
            do {
                outermostPos.move(direction);
                outermostState = world.getBlockState(outermostPos);
            } while (block.isSamePlant(outermostState));

            if (outermostState.isAir()) {
                BlockState newState = ModBlocks.TWISTING_VERDANT_VINES.getDefaultState()
                        .with(Properties.VERTICAL_DIRECTION, direction)
                        .with(ModProperties.CATALYZED, true);
                world.setBlockState(outermostPos, newState);
                AbstractSporeGrowthEntity.playPlaceSoundEffect(world, outermostPos, newState);

                if (++depth < MAX_DEPTH && random.nextInt(3) > 0) {
                    continue;
                }
            }
            break;
        }
    }

    public LivingTwistingVerdantVineBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(ModProperties.CATALYZED, false));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(ModProperties.CATALYZED);
        super.appendProperties(builder);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState != null) {
            placementState = placementState.with(ModProperties.CATALYZED, true);
        }
        return placementState;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (!state.get(ModProperties.CATALYZED) && world.hasRain(pos.up())) {
            this.reactToWater(world, pos, state, Integer.MAX_VALUE, random);
        }
    }

    @Override
    public void reactToWater(World world, BlockPos pos, BlockState state, int waterAmount,
            Random random) {
        LivingTwistingVerdantVineBlock.growInSameDirection(world, pos, state, random);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected Block getStem() {
        return ModBlocks.TWISTING_VERDANT_VINES_PLANT;
    }

    @Override
    public Block getDeadSporeBlock() {
        return ModBlocks.DEAD_TWISTING_VERDANT_VINES;
    }
}
