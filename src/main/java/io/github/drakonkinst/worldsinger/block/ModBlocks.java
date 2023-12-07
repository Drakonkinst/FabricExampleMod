package io.github.drakonkinst.worldsinger.block;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.DeadSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RoseiteSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.VerdantSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ZephyrSpores;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.registry.ModSoundGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Offsetter;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public final class ModBlocks {

    // Dead Spores
    public static final Block DEAD_SPORE_SEA = register("dead_spore_sea",
            new AetherSporeFluidBlock(DeadSpores.getInstance(),
                    createSporeFluidSettings(false, MapColor.GRAY)), false);
    public static final Block DEAD_SPORE_BLOCK = register("dead_spore_block",
            new AetherSporeBlock(DeadSpores.getInstance(), FabricBlockSettings.create()
                    // Same as sand
                    .strength(0.5f).mapColor(MapColor.GRAY).sounds(ModSoundGroups.SPORES)), false);
    public static final Block DEAD_SPORE_CAULDRON = register("dead_spore_cauldron",
            new SporeCauldronBlock(FabricBlockSettings.copy(Blocks.CAULDRON),
                    ModCauldronBehaviors.DEAD_SPORE_CAULDRON_BEHAVIOR, DeadSpores.getInstance()),
            false);
    public static final Block DEAD_VERDANT_VINE_BLOCK = register("dead_verdant_vine_block",
            new VerdantVineBlock(FabricBlockSettings.create()
                    // 0.5x strength of living version
                    .strength(1.0f)
                    .requiresTool()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.VERDANT_VINE_BRANCH)), true);
    public static final Block DEAD_VERDANT_VINE_BRANCH = register("dead_verdant_vine_branch",
            new VerdantVineBranchBlock(FabricBlockSettings.create()
                    // 0.5x strength of living version
                    .strength(0.4f)
                    .solid()
                    .requiresTool()
                    .nonOpaque()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.VERDANT_VINE_BRANCH)), true);
    public static final Block DEAD_VERDANT_VINE_SNARE = register("dead_verdant_vine_snare",
            new VerdantVineSnareBlock(
                    FabricBlockSettings.create()
                            .solid()
                            .noCollision()
                            .requiresTool()
                            // 0.5x strength of living version
                            .strength(0.4f)
                            .ticksRandomly()
                            .mapColor(MapColor.LIGHT_GRAY)
                            .sounds(ModSoundGroups.VERDANT_VINE_SNARE)
                            .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_TWISTING_VERDANT_VINES = register("dead_twisting_verdant_vines",
            new TwistingVerdantVineBlock(FabricBlockSettings.create()
                    // 0.5x strength of living version
                    .strength(0.2f)
                    .noCollision()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.TWISTING_VERDANT_VINES)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_TWISTING_VERDANT_VINES_PLANT = register(
            "dead_twisting_verdant_vines_plant",
            new TwistingVerdantVineStemBlock(FabricBlockSettings.create()
                    // 0.5x strength of living version
                    .strength(0.2f)
                    .noCollision()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.TWISTING_VERDANT_VINES)
                    .pistonBehavior(PistonBehavior.DESTROY)), false);
    public static final Block DEAD_CRIMSON_GROWTH = register("dead_crimson_growth",
            new CrimsonGrowthBlock(FabricBlockSettings.create()
                    // Same strength as coral block
                    .strength(1.5f, 6.0f)
                    .requiresTool()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.CRIMSON_GROWTH)), true);
    public static final Block DEAD_CRIMSON_SPIKE = register("dead_crimson_spike",
            new CrimsonSpikeBlock(
                    ModBlocks.createSettingsWithCustomOffsetter(CrimsonSpikeBlock.getOffsetter())
                            // Same strength as Pointed Dripstone
                            .strength(1.5f, 3.0f)
                            .solid()
                            .ticksRandomly()
                            .dynamicBounds()
                            .requiresTool()
                            .solidBlock(Blocks::never)
                            .mapColor(MapColor.LIGHT_GRAY)
                            .sounds(ModSoundGroups.CRIMSON_SPINE)
                            .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_CRIMSON_SNARE = register("dead_crimson_snare",
            new CrimsonSnareBlock(FabricBlockSettings.create()
                    // Same as Amethyst Bud
                    .strength(1.5f)
                    .solid()
                    .requiresTool()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.CRIMSON_SPINE)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_TALL_CRIMSON_SPINES = register("dead_tall_crimson_spines",
            new TallCrimsonSpinesBlock(FabricBlockSettings.create()
                    // Same as Amethyst Bud
                    .strength(1.5f)
                    .solid()
                    .ticksRandomly()
                    .dynamicBounds()
                    .offset(AbstractBlock.OffsetType.XZ)
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.CRIMSON_SPINE)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);
    public static final Block DEAD_CRIMSON_SPINES = register("dead_crimson_spines",
            new LivingCrimsonSpinesBlock(FabricBlockSettings.create()
                    // Same as Amethyst Bud
                    .strength(1.5f)
                    .solid()
                    .noCollision()
                    .ticksRandomly()
                    .mapColor(MapColor.LIGHT_GRAY)
                    .sounds(ModSoundGroups.CRIMSON_SPINE)
                    .pistonBehavior(PistonBehavior.DESTROY)), true);

    // Verdant Spores
    public static final Block VERDANT_SPORE_SEA = register("verdant_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.VERDANT_SPORES, VerdantSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.DARK_GREEN)), false);
    public static final Block VERDANT_SPORE_BLOCK = register("verdant_spore_block",
            new LivingAetherSporeBlock(VerdantSpores.getInstance(), ModBlocks.VERDANT_SPORE_SEA,
                    FabricBlockSettings.copyOf(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.DARK_GREEN)), false);
    public static final Block VERDANT_SPORE_CAULDRON = register("verdant_spore_cauldron",
            new LivingSporeCauldronBlock(FabricBlockSettings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.VERDANT_SPORE_CAULDRON_BEHAVIOR,
                    VerdantSpores.getInstance()), false);
    public static final Block VERDANT_VINE_BLOCK = register("verdant_vine_block",
            new LivingVerdantVineBlock(FabricBlockSettings.copyOf(ModBlocks.DEAD_VERDANT_VINE_BLOCK)
                    // Same strength as log
                    .strength(2.0f).mapColor(MapColor.GREEN)), true);
    public static final Block VERDANT_VINE_BRANCH = register("verdant_vine_branch",
            new LivingVerdantVineBranchBlock(FabricBlockSettings.copyOf(DEAD_VERDANT_VINE_BRANCH)
                    // 2x the strength of Chorus Plant
                    .strength(0.8f).mapColor(MapColor.GREEN)), true);
    public static final Block VERDANT_VINE_SNARE = register("verdant_vine_snare",
            new LivingVerdantVineSnareBlock(FabricBlockSettings.copyOf(DEAD_VERDANT_VINE_SNARE)
                    // Same strength as branch
                    .strength(0.8f).mapColor(MapColor.GREEN)), true);
    public static final Block TWISTING_VERDANT_VINES = register("twisting_verdant_vines",
            new LivingTwistingVerdantVineBlock(
                    FabricBlockSettings.copyOf(DEAD_TWISTING_VERDANT_VINES)
                            // Same strength as Ladder
                            .strength(0.4f).mapColor(MapColor.GREEN)), true);
    public static final Block TWISTING_VERDANT_VINES_PLANT = register(
            "twisting_verdant_vines_plant", new LivingTwistingVerdantVineStemBlock(
                    FabricBlockSettings.copyOf(ModBlocks.DEAD_TWISTING_VERDANT_VINES_PLANT)
                            // Same strength as Twisting Verdant Vines
                            .strength(0.4f).mapColor(MapColor.GREEN)), false);

    // Crimson Spores
    public static final Block CRIMSON_SPORE_SEA = register("crimson_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.CRIMSON_SPORES, CrimsonSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.DARK_RED)), false);
    public static final Block CRIMSON_SPORE_BLOCK = register("crimson_spore_block",
            new LivingAetherSporeBlock(CrimsonSpores.getInstance(), ModBlocks.CRIMSON_SPORE_SEA,
                    FabricBlockSettings.copyOf(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.DARK_RED)), false);
    public static final Block CRIMSON_SPORE_CAULDRON = register("crimson_spore_cauldron",
            new SporeCauldronBlock(FabricBlockSettings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.CRIMSON_SPORE_CAULDRON_BEHAVIOR,
                    CrimsonSpores.getInstance()), false);
    public static final Block CRIMSON_GROWTH = register("crimson_growth",
            new LivingCrimsonGrowthBlock(FabricBlockSettings.copyOf(ModBlocks.DEAD_CRIMSON_GROWTH)
                    // Double hardness, same resistance as dead version
                    .strength(3.0f, 6.0f).mapColor(MapColor.RED)), true);
    public static final Block CRIMSON_SPIKE = register("crimson_spike",
            new LivingCrimsonSpikeBlock(FabricBlockSettings.copyOf(ModBlocks.DEAD_CRIMSON_SPIKE)
                    // Double hardness, same resistance as dead version
                    .strength(3.0f, 3.0f).mapColor(MapColor.RED)), true);
    public static final Block CRIMSON_SNARE = register("crimson_snare",
            new LivingCrimsonSnareBlock(FabricBlockSettings.copyOf(ModBlocks.DEAD_CRIMSON_SNARE)
                    // Slightly stronger than dead version
                    .strength(2.0f).mapColor(MapColor.RED)), true);
    public static final Block TALL_CRIMSON_SPINES = register("tall_crimson_spines",
            new LivingTallCrimsonSpinesBlock(
                    FabricBlockSettings.copyOf(ModBlocks.DEAD_TALL_CRIMSON_SPINES)
                            // Slightly stronger than dead version
                            .strength(2.0f).mapColor(MapColor.RED)), true);
    public static final Block CRIMSON_SPINES = register("crimson_spines",
            new LivingCrimsonSpinesBlock(FabricBlockSettings.copyOf(ModBlocks.DEAD_CRIMSON_SPINES)
                    // Slightly stronger than dead version
                    .strength(2.0f).mapColor(MapColor.RED)), true);

    // Zephyr Spores
    public static final Block ZEPHYR_SPORE_SEA = register("zephyr_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.ZEPHYR_SPORES, ZephyrSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.LIGHT_BLUE)), false);
    public static final Block ZEPHYR_SPORE_BLOCK = register("zephyr_spore_block",
            new LivingAetherSporeBlock(ZephyrSpores.getInstance(), ModBlocks.ZEPHYR_SPORE_SEA,
                    FabricBlockSettings.copyOf(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.LIGHT_BLUE)), false);
    public static final Block ZEPHYR_SPORE_CAULDRON = register("zephyr_spore_cauldron",
            new LivingSporeCauldronBlock(FabricBlockSettings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.ZEPHYR_SPORE_CAULDRON_BEHAVIOR,
                    ZephyrSpores.getInstance()), false);

    // Sunlight Spores
    public static final Block SUNLIGHT_SPORE_SEA = register("sunlight_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.SUNLIGHT_SPORES, SunlightSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.TERRACOTTA_YELLOW)), false);
    public static final Block SUNLIGHT_SPORE_BLOCK = register("sunlight_spore_block",
            new LivingAetherSporeBlock(SunlightSpores.getInstance(), ModBlocks.SUNLIGHT_SPORE_SEA,
                    FabricBlockSettings.copyOf(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.TERRACOTTA_YELLOW)), false);
    public static final Block SUNLIGHT_SPORE_CAULDRON = register("sunlight_spore_cauldron",
            new LivingSporeCauldronBlock(FabricBlockSettings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.SUNLIGHT_SPORE_CAULDRON_BEHAVIOR,
                    SunlightSpores.getInstance()), false);
    public static final Block SUNLIGHT = register("sunlight", new SunlightBlock(
            FabricBlockSettings.create()
                    .strength(100.0f)
                    .noCollision()
                    .dropsNothing()
                    .liquid()
                    .replaceable()
                    .ticksRandomly()
                    .luminance(SunlightBlock.STATE_TO_LUMINANCE)
                    .mapColor(MapColor.YELLOW)
                    .pistonBehavior(PistonBehavior.DESTROY)), false);

    // Roseite Spores
    public static final Block ROSEITE_SPORE_SEA = register("roseite_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.ROSEITE_SPORES, RoseiteSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.PINK)), false);
    public static final Block ROSEITE_SPORE_BLOCK = register("roseite_spore_block",
            new LivingAetherSporeBlock(RoseiteSpores.getInstance(), ModBlocks.ROSEITE_SPORE_SEA,
                    FabricBlockSettings.copyOf(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.PINK)), false);
    public static final Block ROSEITE_SPORE_CAULDRON = register("roseite_spore_cauldron",
            new LivingSporeCauldronBlock(FabricBlockSettings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.ROSEITE_SPORE_CAULDRON_BEHAVIOR,
                    RoseiteSpores.getInstance()), false);
    public static final Block ROSEITE_BLOCK = register("roseite_block",
            new RoseiteBlock(FabricBlockSettings.create()
                    // TODO
                    .nonOpaque()
                    // TODO: Balance durability values
                    .strength(3.0F, 6.0F)
                    .ticksRandomly()
                    .sounds(ModSoundGroups.ROSEITE)
                    .mapColor(MapColor.TERRACOTTA_PINK)), true);
    public static final Block ROSEITE_STAIRS = register("roseite_stairs",
            new RoseiteStairsBlock(ROSEITE_BLOCK.getDefaultState(),
                    FabricBlockSettings.copyOf(ROSEITE_BLOCK)), true);
    public static final Block ROSEITE_SLAB = register("roseite_slab",
            new RoseiteSlabBlock(FabricBlockSettings.copyOf(ROSEITE_BLOCK)), true);

    // Midnight Spores
    public static final Block MIDNIGHT_SPORE_SEA = register("midnight_spore_sea",
            new LivingAetherSporeFluidBlock(ModFluids.MIDNIGHT_SPORES, MidnightSpores.getInstance(),
                    createSporeFluidSettings(true, MapColor.BLACK)), false);
    public static final Block MIDNIGHT_SPORE_BLOCK = register("midnight_spore_block",
            new LivingAetherSporeBlock(MidnightSpores.getInstance(), ModBlocks.MIDNIGHT_SPORE_SEA,
                    FabricBlockSettings.copyOf(ModBlocks.DEAD_SPORE_BLOCK)
                            .ticksRandomly()
                            .mapColor(MapColor.BLACK)), false);
    public static final Block MIDNIGHT_SPORE_CAULDRON = register("midnight_spore_cauldron",
            new LivingSporeCauldronBlock(FabricBlockSettings.copy(Blocks.CAULDRON).ticksRandomly(),
                    ModCauldronBehaviors.MIDNIGHT_SPORE_CAULDRON_BEHAVIOR,
                    MidnightSpores.getInstance()), false);

    // Other
    public static final Block MAGMA_VENT = register("magma_vent",
            new VentBlock(FabricBlockSettings.create()
                    // Same strength as copper deepslate ore
                    .strength(4.5f, 3.0f).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY)), true);
    public static final Block SALTSTONE = register("saltstone",
            new Block(FabricBlockSettings.create().requiresTool()
                    // Slightly harder to break than Netherrack
                    .strength(0.5f, 6.0f).sounds(ModSoundGroups.SALTSTONE)), true);
    public static final Block SALTSTONE_SALT_ORE = register("saltstone_salt_ore",
            new ExperienceDroppingBlock(UniformIntProvider.create(0, 2),
                    FabricBlockSettings.create().requiresTool()
                            // Slightly easier to break than Nether Gold Ore
                            .strength(2.5f, 3.0f).sounds(ModSoundGroups.SALTSTONE)), true);
    public static final Block SALT_BLOCK = register("salt_block",
            new Block(FabricBlockSettings.create().requiresTool()
                    // Same strength as Calcite
                    .strength(0.75f).sounds(ModSoundGroups.SALT)), true);
    public static final Block SILVER_ORE = register("silver_ore",
            new ExperienceDroppingBlock(ConstantIntProvider.create(0),
                    FabricBlockSettings.create().requiresTool()
                            // Equal to Gold Ore
                            .strength(3.0f, 3.0f)), true);
    public static final Block DEEPSLATE_SILVER_ORE = register("deepslate_silver_ore",
            new ExperienceDroppingBlock(ConstantIntProvider.create(0),
                    FabricBlockSettings.create().requiresTool()
                            // Equal to Deepslate Gold Ore
                            .strength(4.5f, 3.0f).sounds(BlockSoundGroup.DEEPSLATE)), true);
    public static final Block SILVER_BLOCK = register("silver_block",
            new Block(FabricBlockSettings.create().requiresTool()
                    // Same strength as Gold Block
                    .strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL)), true);
    public static final Block RAW_SILVER_BLOCK = register("raw_silver_block",
            new Block(FabricBlockSettings.create().requiresTool()
                    // Same strength as Raw Gold Block
                    .strength(5.0f, 6.0f)), true);

    // Steel
    public static final Block STEEL_BLOCK = register("steel_block",
            new Block(FabricBlockSettings.create().requiresTool()
                    // +1 strength of Iron Block, same blast resistance as End Stone
                    .strength(6.0f, 9.0f).sounds(BlockSoundGroup.METAL)), true);
    public static final Block STEEL_ANVIL = register("steel_anvil",
            new SteelAnvilBlock(FabricBlockSettings.create()
                    .requiresTool()
                    // +1 strength of Iron Anvil, same blast resistance as Anvil
                    .strength(6.0f, 1200.0f)
                    .sounds(BlockSoundGroup.ANVIL)
                    .pistonBehavior(PistonBehavior.BLOCK)), true);
    public static final Block CHIPPED_STEEL_ANVIL = register("chipped_steel_anvil",
            new SteelAnvilBlock(FabricBlockSettings.create()
                    .requiresTool()
                    // Same settings as Steel Anvil
                    .strength(6.0f, 1200.0f)
                    .sounds(BlockSoundGroup.ANVIL)
                    .pistonBehavior(PistonBehavior.BLOCK)), true);
    public static final Block DAMAGED_STEEL_ANVIL = register("damaged_steel_anvil",
            new SteelAnvilBlock(FabricBlockSettings.create()
                    .requiresTool()
                    // Same settings as Steel Anvil
                    .strength(6.0f, 1200.0f)
                    .sounds(BlockSoundGroup.ANVIL)
                    .pistonBehavior(PistonBehavior.BLOCK)), true);

    public static <T extends Block> T register(String id, T block, boolean shouldRegisterItem) {
        Identifier blockId = Worldsinger.id(id);

        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new FabricItemSettings());
            Registry.register(Registries.ITEM, blockId, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockId, block);
    }

    private static FabricBlockSettings createSettingsWithCustomOffsetter(Offsetter offsetter) {
        FabricBlockSettings settings = FabricBlockSettings.create();
        ((CustomBlockOffsetterAccess) settings).worldsinger$setCustomOffsetter(offsetter);
        return settings;
    }

    private static FabricBlockSettings createSporeFluidSettings(boolean ticksRandomly,
            MapColor color) {
        FabricBlockSettings settings = FabricBlockSettings.create()
                .strength(100.0f)
                .replaceable()
                .noCollision()
                .dropsNothing()
                .liquid()
                .mapColor(color)
                .pistonBehavior(PistonBehavior.DESTROY)
                .sounds(BlockSoundGroup.SAND);
        if (ticksRandomly) {
            settings = settings.ticksRandomly();
        }
        return settings;
    }

    public static void initialize() {}

    private ModBlocks() {}
}
