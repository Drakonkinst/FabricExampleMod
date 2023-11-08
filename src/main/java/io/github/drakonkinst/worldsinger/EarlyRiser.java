package io.github.drakonkinst.worldsinger;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class EarlyRiser implements Runnable {

    private static final String INTERMEDIARY = "intermediary";

    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

        String pathNodeTypeEnum = remapper.mapClassName(INTERMEDIARY, "net.minecraft.class_7");
        ClassTinkerers.enumBuilder(pathNodeTypeEnum, float.class).addEnum("AETHER_SPORE_SEA", -1.0f)
                .build();

        String skyTypeEnum = remapper.mapClassName(INTERMEDIARY,
                "net.minecraft.class_5294$class_5401");
        ClassTinkerers.enumBuilder(skyTypeEnum).addEnum("LUMAR").build();

        String cameraSubmersionTypeEnum = remapper.mapClassName(INTERMEDIARY,
                "net.minecraft.class_5636");
        ClassTinkerers.enumBuilder(cameraSubmersionTypeEnum).addEnum("SPORE_SEA").build();
    }
}
