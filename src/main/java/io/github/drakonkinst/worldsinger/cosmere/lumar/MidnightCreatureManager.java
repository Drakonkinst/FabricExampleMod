package io.github.drakonkinst.worldsinger.cosmere.lumar;

import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class MidnightCreatureManager {

    public static final int MIN_DRAIN_INTERVAL_TICKS = 20;
    public static final int MAX_DRAIN_INTERVAL_TICKS = 80;
    public static final int MIN_BRIBE_AMOUNT = 1;
    public static final int MAX_BRIBE_AMOUNT = 10;

    public static final double DEFAULT_MOVEMENT_SPEED = 0.25;
    public static final double DEFAULT_MAX_HEALTH = 20.0;
    private static final double MAX_HEALTH_SIZE_MULTIPLIER = 17;
    private static final double ATTACK_DAMAGE_SIZE_MULTIPLIER = 2.5;
    private static final float MIN_MAX_HEALTH = 8.0f;       // Same as Silverfish
    private static final float MAX_MAX_HEALTH = 100.0f;     // Same as Ravager
    private static final float MIN_ATTACK_DAMAGE = 3.0f;    // Same as Zombie
    private static final float MAX_ATTACK_DAMAGE = 12.0f;   // Same as Ravager
    private static final int DRAIN_INTERVAL_MULTIPLIER = -8;

    public static DefaultAttributeContainer.Builder createMidnightCreatureAttributes() {
        // Before transforming, should not be able to move or attack
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, DEFAULT_MAX_HEALTH);
    }

    public static double getMaxHealthForSize(float size) {
        double value = size * MAX_HEALTH_SIZE_MULTIPLIER;
        return MathHelper.clamp(Math.round(value), MIN_MAX_HEALTH, MAX_MAX_HEALTH);
    }

    public static double getAttackDamageForSize(float size) {
        double value = size * ATTACK_DAMAGE_SIZE_MULTIPLIER;
        return MathHelper.clamp(Math.round(value), MIN_ATTACK_DAMAGE, MAX_ATTACK_DAMAGE);
    }

    public static int getDrainIntervalForSize(float size) {
        int sizeStage = MathHelper.floor(size * 2.0f);
        return Math.max(MAX_DRAIN_INTERVAL_TICKS + DRAIN_INTERVAL_MULTIPLIER * sizeStage,
                MIN_DRAIN_INTERVAL_TICKS);
    }

    public static int getMinBribeForSize(float size) {
        return MathHelper.floor(size * 2.0f);
    }

    public static int getWaterAmountPerUnit(ItemStack stack) {
        if (stack.isOf(Items.POTION)) {
            return MidnightCreatureEntity.POTION_BRIBE;
        }
        if (stack.isOf(Items.WATER_BUCKET)) {
            return MidnightCreatureEntity.WATER_BUCKET_BRIBE;
        }
        return 0;
    }

    public static ItemStack getStackAfterDraining(ItemStack stack) {
        if (stack.isOf(Items.POTION)) {
            return Items.GLASS_BOTTLE.getDefaultStack();
        }
        return stack.getRecipeRemainder();
    }

    // Client-side only
    public static void addMidnightParticle(World world, Entity entity, Random random,
            double velocity) {
        Vec3d pos = EntityUtil.getRandomPointInBoundingBox(entity, random);
        double velocityX = velocity * random.nextGaussian();
        double velocityY = velocity * random.nextGaussian();
        double velocityZ = velocity * random.nextGaussian();
        world.addParticle(ModParticleTypes.MIDNIGHT_ESSENCE, pos.getX(), pos.getY(), pos.getZ(),
                velocityX, velocityY, velocityZ);
    }

    // Client-side only
    public static void addMidnightParticles(World world, Entity entity, Random random,
            double velocity, int count) {
        for (int i = 0; i < count; ++i) {
            MidnightCreatureManager.addMidnightParticle(world, entity, random, velocity);
        }
    }

    private MidnightCreatureManager() {}
}
