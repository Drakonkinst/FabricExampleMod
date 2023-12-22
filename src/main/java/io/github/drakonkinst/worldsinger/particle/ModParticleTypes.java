package io.github.drakonkinst.worldsinger.particle;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@SuppressWarnings("UnqualifiedStaticUsage")
public class ModParticleTypes {

    public static final ParticleType<SporeDustParticleEffect> SPORE_DUST = register("spore_dust",
            true, SporeDustParticleEffect.PARAMETERS_FACTORY);

    public static final DefaultParticleType MIDNIGHT_ESSENCE = register("midnight_essence", false);
    public static final DefaultParticleType MIDNIGHT_TRAIL = register("midnight_trail", false);

    public static void initialize() {}

    private static DefaultParticleType register(String name, boolean alwaysShow) {
        return Registry.register(Registries.PARTICLE_TYPE, Worldsinger.id(name),
                FabricParticleTypes.simple(alwaysShow));
    }

    private static <T extends ParticleEffect> ParticleType<T> register(String name,
            boolean alwaysShow, ParticleEffect.Factory<T> factory) {

        return Registry.register(Registries.PARTICLE_TYPE, Worldsinger.id(name),
                FabricParticleTypes.complex(alwaysShow, factory));
    }
}
