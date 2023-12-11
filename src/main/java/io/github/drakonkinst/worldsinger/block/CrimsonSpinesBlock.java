package io.github.drakonkinst.worldsinger.block;

import com.mojang.serialization.MapCodec;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.util.VoxelShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class CrimsonSpinesBlock extends Block implements Waterloggable, SporeGrowthBlock {

    public static final MapCodec<CrimsonSpinesBlock> CODEC = AbstractBlock.createCodec(
            CrimsonSpinesBlock::new);
    private static final VoxelShape[] SHAPES = VoxelShapeUtil.createDirectionAlignedShapes(2.0, 0.0,
            11.0);

    public CrimsonSpinesBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(Properties.PERSISTENT, false)
                .with(Properties.WATERLOGGED, false)
                .with(Properties.FACING, Direction.UP));
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // Spikes do not destroy items
        if (entity instanceof ItemEntity) {
            return;
        }

        Direction direction = state.get(Properties.FACING);
        VoxelShape blockShape = SHAPES[direction.ordinal()];
        VoxelShape entityShape = VoxelShapes.cuboid(
                entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ()));

        if (!VoxelShapes.matchesAnywhere(entityShape, blockShape, BooleanBiFunction.AND)) {
            return;
        }

        if (entity.fallDistance > entity.getSafeFallDistance()) {
            // Not safe to land
            entity.handleFallDamage(entity.fallDistance, 1.5f,
                    ModDamageTypes.createSource(world, ModDamageTypes.SPIKE_FALL));
            entity.onLanding();
        } else if (CrimsonSpikeBlock.isMoving(entity)) {
            // Slow and damage normally
            entity.slowMovement(state, new Vec3d(0.9, 0.9, 0.9));
            if (world.isClient()) {
                return;
            }
            entity.damage(ModDamageTypes.createSource(world, ModDamageTypes.SPIKE), 2.0f);
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity,
            float fallDistance) {
        entity.handleFallDamage(fallDistance, 1.5f,
                ModDamageTypes.createSource(world, ModDamageTypes.SPIKE_FALL));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(Properties.PERSISTENT, true)
                .with(Properties.WATERLOGGED, ctx.getWorld().isWater(ctx.getBlockPos()))
                .with(Properties.FACING, ctx.getSide());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
            ShapeContext context) {
        Direction direction = state.get(Properties.FACING);
        return SHAPES[direction.ordinal()];
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(Properties.FACING);
        BlockPos attachPos = pos.offset(direction.getOpposite());
        BlockState attachState = world.getBlockState(attachPos);
        return attachState.isSideSolidFullSquare(world, attachPos, direction);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false)
                : super.getFluidState(state);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return !state.get(Properties.PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        // Decay over time
        if (SporeGrowthBlock.canDecay(world, pos, state, random)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction == state.get(Properties.FACING).getOpposite() && !state.canPlaceAt(world,
                pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos,
                neighborPos);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.PERSISTENT, Properties.WATERLOGGED, Properties.FACING);
    }

    @Override
    protected MapCodec<? extends CrimsonSpinesBlock> getCodec() {
        return CODEC;
    }
}
