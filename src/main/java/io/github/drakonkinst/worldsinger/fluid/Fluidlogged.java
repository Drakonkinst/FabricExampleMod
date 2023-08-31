package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.WorldsingerConfig;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

// Manages making blocks fluidloggable by fluids other than water.
public final class Fluidlogged {

    public static final List<Identifier> WATERLOGGABLE_FLUIDS = WorldsingerConfig.instance()
            .getFluidloggableFluids();
    private static final Map<Fluid, FluidBlock> fluidToFluidBlocks = new HashMap<>();

    public static Fluid getFluid(BlockState state) {
        if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
            return Fluids.WATER;
        }
        if (!state.contains(ModProperties.FLUIDLOGGED)) {
            return null;
        }
        int index = state.get(ModProperties.FLUIDLOGGED) - 1;
        if (index < 0) {
            return Fluids.EMPTY;
        }
        if (index >= Fluidlogged.WATERLOGGABLE_FLUIDS.size()) {
            return null;
        }
        Identifier id = Fluidlogged.WATERLOGGABLE_FLUIDS.get(index);
        if (id == null) {
            return null;
        }
        return Registries.FLUID.get(id);
    }

    public static void registerFluidBlockForFluid(Fluid fluid, FluidBlock fluidBlock) {
        fluidToFluidBlocks.put(fluid, fluidBlock);
    }

    public static FluidBlock getFluidBlockForFluid(Fluid fluid) {
        return fluidToFluidBlocks.get(fluid);
    }

    public static int getFluidIndex(Fluid fluid) {
        if (fluid.equals(Fluids.EMPTY)) {
            return 0;
        }
        return Fluidlogged.WATERLOGGABLE_FLUIDS.indexOf(Registries.FLUID.getId(fluid)) + 1;
    }

    public static void initialize() {}

    private Fluidlogged() {}
}
