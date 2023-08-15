package io.github.drakonkinst.examplemod.block;

import io.github.drakonkinst.examplemod.Constants;
import io.github.drakonkinst.examplemod.fluid.ModFluids;
import io.github.drakonkinst.examplemod.fluid.VerdantSporeFluid;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class ModBlocks {

    private ModBlocks() {
    }

    public static final Block DISCORD_BLOCK =
            register(new Block(FabricBlockSettings.create().strength(4.0f)), "discord_block", true);
    public static final Block VERDANT_SPORE_SEA_BLOCK =
            register(
                    new AetherSporeFluidBlock(ModFluids.VERDANT_SPORES, FabricBlockSettings.create()
                            .strength(100.0f)
                            .mapColor(MapColor.DARK_GREEN)
                            .replaceable()
                            .noCollision()
                            .pistonBehavior(PistonBehavior.DESTROY)
                            .dropsNothing()
                            .liquid()
                            .sounds(BlockSoundGroup.INTENTIONALLY_EMPTY)
                    ), "verdant_spore_sea_block", false);
    public static final Block VERDANT_SPORE_BLOCK =
            register(new AetherSporeBlock(ModBlocks.VERDANT_SPORE_SEA_BLOCK,
                    VerdantSporeFluid.PARTICLE_COLOR,
                    FabricBlockSettings.create()
                            .strength(0.5f)
                            .mapColor(MapColor.DARK_GREEN)
                            .sounds(BlockSoundGroup.SAND)
            ), "verdant_spore_block", true);
    public static final Block VERDANT_VINE_BLOCK = register(new VerdantVineBlock(
                    FabricBlockSettings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD)),
            "verdant_vine_block", true);
    public static final Block VERDANT_VINE_BRANCH = register(
            new VerdantVineBranch(FabricBlockSettings.create().strength(0.4f).nonOpaque()),
            "verdant_vine_branch", true);
    public static final Block VERDANT_VINE_SNARE = register(new VerdantVineSnare(
            FabricBlockSettings.create().solid().noCollision().requiresTool().strength(4.0f)
                    .pistonBehavior(PistonBehavior.DESTROY)), "verdant_vine_snare", true);

    public static <T extends Block> T register(T block, String id, boolean shouldRegisterItem) {
        Identifier blockId = new Identifier(Constants.MOD_ID, id);

        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new FabricItemSettings());
            Registry.register(Registries.ITEM, blockId, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockId, block);
    }

    public static void initialize() {
    }

}
