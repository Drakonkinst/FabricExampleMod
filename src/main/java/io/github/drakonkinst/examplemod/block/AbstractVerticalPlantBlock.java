package io.github.drakonkinst.examplemod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public abstract class AbstractVerticalPlantBlock extends AbstractVerticalPlantPartBlock {

    public AbstractVerticalPlantBlock(Settings settings,
            VoxelShape outlineShape) {
        super(settings, outlineShape);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        Direction growthDirection = AbstractVerticalPlantPartBlock.getGrowthDirection(state);
        if (direction == growthDirection.getOpposite() && !state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }
        if (direction == growthDirection && !neighborState.isOf(this) && !neighborState.isOf(
                this.getStem())) {
            return this.getStem().getDefaultState().with(GROWTH_DIRECTION, growthDirection);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos,
                neighborPos);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(this.getStem());
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        boolean superCanReplace = super.canReplace(state, context);
        if (superCanReplace && context.getStack().isOf(this.getStem().asItem())) {
            return false;
        }
        return superCanReplace;
    }

    @Override
    protected Block getPlant() {
        return this;
    }
}
