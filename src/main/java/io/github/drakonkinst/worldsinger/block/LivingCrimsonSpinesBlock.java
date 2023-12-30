package io.github.drakonkinst.worldsinger.block;

import com.mojang.serialization.MapCodec;
import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingManager;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class LivingCrimsonSpinesBlock extends CrimsonSpinesBlock implements LivingSporeGrowthBlock {

    public static final MapCodec<LivingCrimsonSpinesBlock> CODEC = AbstractBlock.createCodec(
            LivingCrimsonSpinesBlock::new);
    public static final int RECATALYZE_VALUE = 30;

    public LivingCrimsonSpinesBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(ModProperties.CATALYZED, false));
    }

    /* Start of code common to all LivingSporeGrowthBlocks */
    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ModProperties.CATALYZED);
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
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (this.canReactToWater(pos, state) && world instanceof World realWorld) {
            BlockPos waterNeighborPos = LivingVerdantVineBlock.getWaterNeighborPos(world, pos);
            if (waterNeighborPos != null) {
                WaterReactionManager.catalyzeAroundWater(realWorld, waterNeighborPos);
                state = state.with(ModProperties.CATALYZED, true);
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos,
                neighborPos);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return super.hasRandomTicks(state) || !state.get(ModProperties.CATALYZED);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (!state.get(ModProperties.CATALYZED) && world.hasRain(pos.up())) {
            this.reactToWater(world, pos, state, Integer.MAX_VALUE, random);
        }
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos,
            PlayerEntity player) {
        SporeKillingManager.killSporeGrowthUsingTool(world, this, state, pos, player,
                Hand.MAIN_HAND);
    }

    @Override
    public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world,
            BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (SporeKillingManager.killSporeGrowthUsingTool(world, this, state, pos, player, hand)) {
            return ItemActionResult.success(true);
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /* End of code common to all LivingSporeGrowthBlocks */

    @Override
    public boolean reactToWater(World world, BlockPos pos, BlockState state, int waterAmount,
            Random random) {
        if (!this.canReactToWater(pos, state)) {
            return false;
        }

        world.setBlockState(pos, state.with(ModProperties.CATALYZED, true));
        Direction facing = state.get(Properties.FACING);
        Int3 dir = new Int3(facing.getOffsetX(), facing.getOffsetY(), facing.getOffsetZ());
        CrimsonSpores.getInstance()
                .spawnSporeGrowth(world, pos.toCenterPos(), RECATALYZE_VALUE, waterAmount, false,
                        true, false, dir);
        return true;
    }

    @Override
    public Block getDeadSporeBlock() {
        return ModBlocks.DEAD_CRIMSON_SPINES;
    }

    // Catalyze when waterlogged, common to all LivingSporeGrowthBlocks that implement Waterloggable
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState,
            boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!state.isOf(newState.getBlock())) {
            return;
        }
        if (!newState.get(ModProperties.CATALYZED) && newState.get(Properties.WATERLOGGED)) {
            WaterReactionManager.catalyzeAroundWater(world, pos);
        }
    }

    @Override
    public Type getReactiveType() {
        return Type.CRIMSON_SPORES;
    }

    @Override
    protected MapCodec<? extends LivingCrimsonSpinesBlock> getCodec() {
        return CODEC;
    }
}
