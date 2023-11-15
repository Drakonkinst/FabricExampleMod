package io.github.drakonkinst.worldsinger.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

// Represents a block that can grow either up or down. Does not grow naturally.
public abstract class AbstractVerticalGrowthComponentBlock extends Block {

    public static final DirectionProperty VERTICAL_DIRECTION = Properties.VERTICAL_DIRECTION;
    private final VoxelShape outlineShape;

    public AbstractVerticalGrowthComponentBlock(Settings settings, VoxelShape outlineShape) {
        super(settings);
        this.outlineShape = outlineShape;
        this.setDefaultState(this.getDefaultState().with(VERTICAL_DIRECTION, Direction.UP));
    }

    public static Direction getGrowthDirection(BlockState state) {
        return state.get(VERTICAL_DIRECTION);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Can only grow up or down
        Direction verticalAimDirection = ctx.getVerticalPlayerLookDirection().getOpposite();
        World world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        Direction placeDirection = this.getDirectionToPlaceAt(world, blockPos,
                verticalAimDirection);
        if (placeDirection == null) {
            return null;
        }

        BlockPos attachedBlockPos = ctx.getBlockPos().offset(placeDirection);
        BlockState attachedBlockState = ctx.getWorld().getBlockState(attachedBlockPos);
        if (this.isSamePlant(attachedBlockState)) {
            // Is the outermost block, use the plant block
            return this.getStem().getDefaultState().with(VERTICAL_DIRECTION, placeDirection);
        }
        // Use the stem block (only one available as an item)
        return this.getDefaultState().with(VERTICAL_DIRECTION, placeDirection);
    }

    @Nullable
    private Direction getDirectionToPlaceAt(WorldView world, BlockPos pos,
            Direction direction) {
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return null;
        }

        Direction placeDirection = null;
        if (this.canPlaceAtWithDirection(world, pos, direction)) {
            placeDirection = direction;
        } else if (this.canPlaceAtWithDirection(world, pos, direction.getOpposite())) {
            placeDirection = direction.getOpposite();
        }
        return placeDirection;
    }

    private boolean canPlaceAtWithDirection(WorldView world, BlockPos pos,
            Direction direction) {
        BlockPos supportingBlockPos = pos.offset(direction.getOpposite());
        BlockState supportingBlockState = world.getBlockState(supportingBlockPos);
        return supportingBlockState.isSideSolidFullSquare(world, supportingBlockPos, direction)
                || this.isSamePlantWithDirection(supportingBlockState, direction);
    }

    protected boolean isSamePlantWithDirection(BlockState state, Direction direction) {
        return this.isSamePlant(state) && state.get(VERTICAL_DIRECTION) == direction;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction growthDirection = AbstractVerticalGrowthComponentBlock.getGrowthDirection(state);
        BlockPos attachedBlockPos = pos.offset(growthDirection.getOpposite());
        BlockState attachedBlockState = world.getBlockState(attachedBlockPos);

        // Can always place on a solid face
        if (attachedBlockState.isSideSolidFullSquare(world, attachedBlockPos,
                growthDirection)) {
            return true;
        }

        // Can only place on another of the same plant if in the same direction
        if (isSamePlant(attachedBlockState)) {
            return growthDirection == attachedBlockState.get(VERTICAL_DIRECTION);
        }

        return this.canAttachTo(state, attachedBlockState);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Break if no longer valid position
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
            ShapeContext context) {
        return this.outlineShape;
    }

    protected abstract Block getBud();

    protected abstract Block getStem();

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(VERTICAL_DIRECTION);
    }

    protected boolean isSamePlant(BlockState state) {
        return state.isOf(this.getBud()) || state.isOf(this.getStem());
    }

    protected boolean canAttachTo(BlockState state, BlockState attachCandidate) {
        return false;
    }
}
