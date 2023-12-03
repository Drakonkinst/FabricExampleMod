package io.github.drakonkinst.worldsinger.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.cauldron.CauldronBehavior.CauldronBehaviorMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class LivingSporeCauldronBlock extends SporeCauldronBlock implements SporeKillable,
        WaterReactiveBlock {

    // Unused Codec
    public static final MapCodec<LivingSporeCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(LeveledCauldronBlock.createSettingsCodec(),
                            CauldronBehavior.CODEC.fieldOf("interactions")
                                    .forGetter(block -> block.behaviorMap),
                            AetherSpores.CODEC.fieldOf("sporeType")
                                    .forGetter(LivingSporeCauldronBlock::getSporeType))
                    .apply(instance, LivingSporeCauldronBlock::new));
    private static final int CATALYZE_VALUE_PER_LEVEL = 80;

    public LivingSporeCauldronBlock(Settings settings, CauldronBehaviorMap behaviorMap,
            AetherSpores sporeType) {
        super(settings, behaviorMap, sporeType);
    }

    @Override
    public Block getDeadSporeBlock() {
        return ModBlocks.DEAD_SPORE_CAULDRON;
    }

    @Override
    public boolean canReactToWater(BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (world.hasRain(pos.up())) {
            this.reactToWater(world, pos, state, Integer.MAX_VALUE, random);
        }
    }

    @Override
    public boolean reactToWater(World world, BlockPos pos, BlockState state, int waterAmount,
            Random random) {
        BlockPos posAbove = pos.up();
        BlockState stateAbove = world.getBlockState(posAbove);
        if (!stateAbove.isIn(ModBlockTags.SPORES_CAN_GROW) && !stateAbove.isIn(
                ModBlockTags.SPORES_CAN_BREAK)) {
            return false;
        }
        world.setBlockState(pos, Blocks.CAULDRON.getStateWithProperties(state));
        int catalyzeValue = CATALYZE_VALUE_PER_LEVEL * state.get(LEVEL);
        sporeType.doReactionFromFluidContainer(world, pos, catalyzeValue, waterAmount, random);
        return true;
    }

    @Override
    public WaterReactiveType getReactiveType() {
        return AetherSpores.getReactiveTypeFromSpore(sporeType);
    }
}
