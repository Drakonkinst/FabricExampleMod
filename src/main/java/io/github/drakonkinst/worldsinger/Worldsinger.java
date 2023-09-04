package io.github.drakonkinst.worldsinger;

import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.command.ModCommands;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.registry.ModRegistries;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import net.fabricmc.api.ModInitializer;

public class Worldsinger implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstants.LOGGER.info("Initializing Worldsinger...");

        ModApi.initialize();
        ModRegistries.register();
        ModProperties.initialize();

        ModParticleTypes.initialize();
        ModFluids.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        ModEntityTypes.initialize();
        ModCommands.initialize();

        Fluidlogged.initialize();
    }
}