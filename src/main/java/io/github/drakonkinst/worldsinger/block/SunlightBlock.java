package io.github.drakonkinst.worldsinger.block;

import com.mojang.serialization.MapCodec;
import java.util.function.ToIntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class SunlightBlock extends Block {

    public static final MapCodec<SunlightBlock> CODEC = createCodec(SunlightBlock::new);
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE = state -> state.get(
            Properties.LEVEL_15);

    public SunlightBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(Properties.LEVEL_15, 15));
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(Properties.LEVEL_15);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
            ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        // TODO: Decay state
    }

    public MapCodec<? extends SunlightBlock> getCodec() {
        return CODEC;
    }
}
