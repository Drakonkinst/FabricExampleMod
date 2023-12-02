package io.github.drakonkinst.worldsinger.cosmere.lumar;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.LivingAetherSporeBlock;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.BoxUtil;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class ZephyrSpores extends AetherSpores {

    public static final String NAME = "zephyr";
    public static final int ID = 4;

    private static final ZephyrSpores INSTANCE = new ZephyrSpores();
    private static final int COLOR = 0x4b9bb7;
    private static final int PARTICLE_COLOR = 0x4b9bb7;

    private static final float SPORE_TO_POWER_MULTIPLIER = 0.15f;
    private static final float KNOCKBACK_MULTIPLIER = 2.0f;
    private static final int MAX_PLAYER_AFFECTED_DISTANCE = 64;
    private static final int MAX_BLOCK_AFFECTED_DISTANCE = 8;

    public static ZephyrSpores getInstance() {
        return INSTANCE;
    }

    private ZephyrSpores() {}

    @Override
    public void onDeathFromStatusEffect(World world, LivingEntity entity, BlockPos pos, int water) {
        Vec3d startPos = this.getTopmostSeaPosForEntity(world, entity, ModFluidTags.ZEPHYR_SPORES);
        Vec3d adjustedStartPos = new Vec3d(startPos.getX(), Math.ceil(startPos.getY()),
                startPos.getZ());
        this.doReaction(world, adjustedStartPos, LivingAetherSporeBlock.CATALYZE_VALUE, water,
                world.getRandom());
    }

    @Override
    public void doReaction(World world, Vec3d pos, int spores, int water, Random random) {
        float power = Math.min(spores, water) * SPORE_TO_POWER_MULTIPLIER + random.nextFloat();
        // Push the explosion to the top of the block, so that it is not obstructed by itself
        Vec3d centerPos = new Vec3d(pos.getX(), Math.ceil(pos.getY()), pos.getZ());
        ZephyrSpores.explode(world, centerPos, power, KNOCKBACK_MULTIPLIER);
    }

    // Too inefficient to use actual explosion logic since they spend a lot of time calculating blocks,
    // so we'll need our own solution
    public static void explode(World world, Vec3d centerPos, float radius,
            float globalKnockbackMultiplier) {
        world.emitGameEvent(null, GameEvent.EXPLODE, centerPos);
        Explosion explosion = new Explosion(world, null, null, WindChargeEntity.EXPLOSION_BEHAVIOR,
                centerPos.getX(), centerPos.getY(), centerPos.getZ(), radius * 0.5f, false,
                Explosion.DestructionType.TRIGGER_BLOCK, ParticleTypes.GUST,
                ParticleTypes.GUST_EMITTER, ModSoundEvents.BLOCK_ZEPHYR_SEA_CATALYZE);
        ZephyrSpores.collectBlocks(world, centerPos, radius, explosion);
        ZephyrSpores.affectEntities(world, centerPos, radius, explosion, globalKnockbackMultiplier);
        ZephyrSpores.affectBlocks(world, explosion);
        ZephyrSpores.sendToPlayers(world, explosion);
    }

    private static void affectBlocks(World world, Explosion explosion) {
        // TODO: Will particles still appear on dedicated server?
        world.getProfiler().push("explosion_blocks");
        boolean first = true;
        for (BlockPos blockPos : explosion.getAffectedBlocks()) {
            if (first) {
                Worldsinger.LOGGER.info(blockPos.toString());
                first = false;
            }
            world.getBlockState(blockPos)
                    .onExploded(world, blockPos, explosion, (stack, pos) -> {});
        }
        world.getProfiler().pop();
    }

    // Instead of doing a full raycast, just collect all blocks in the radius
    private static void collectBlocks(World world, Vec3d centerPos, float radius,
            Explosion explosion) {
        BlockPos centerBlockPos = BlockPosUtil.toBlockPos(centerPos);
        int blockRadius = Math.min(MAX_BLOCK_AFFECTED_DISTANCE, MathHelper.floor(radius));
        List<BlockPos> affectedBlocks = explosion.getAffectedBlocks();
        if (blockRadius > 0) {
            for (BlockPos currPos : BlockPos.iterate(centerBlockPos.getX() - blockRadius,
                    centerBlockPos.getY() - blockRadius, centerBlockPos.getZ() - blockRadius,
                    centerBlockPos.getX() + blockRadius, centerBlockPos.getY() + blockRadius,
                    centerBlockPos.getZ() + blockRadius)) {
                if (!world.getBlockState(currPos).isAir()) {
                    affectedBlocks.add(new BlockPos(currPos));
                }
            }
        }
    }

    private static void affectEntities(World world, Vec3d centerPos, float radius,
            Explosion explosion, float globalKnockbackMultiplier) {
        Box box = BoxUtil.createBoxAroundPos(centerPos, radius);
        List<Entity> entities = world.getEntitiesByClass(Entity.class, box,
                EntityPredicates.EXCEPT_SPECTATOR);
        Map<PlayerEntity, Vec3d> affectedPlayers = explosion.getAffectedPlayers();
        for (Entity entity : entities) {
            if (entity.isImmuneToExplosion(null)) {
                continue;
            }

            double sqrDistance = entity.getPos().squaredDistanceTo(centerPos);
            if (sqrDistance > radius * radius) {
                continue;
            }
            double distanceMultiplier = Math.sqrt(sqrDistance) / radius;
            double forceX = entity.getX() - centerPos.getX();
            double forceY = entity.getY() - centerPos.getY();
            double forceZ = entity.getZ() - centerPos.getZ();
            double delta = Math.sqrt(forceX * forceX + forceY * forceY + forceZ * forceZ);
            if (delta <= 0.0) {
                continue;
            }

            double knockbackMultiplier =
                    (1.0 - distanceMultiplier) * Explosion.getExposure(centerPos, entity)
                            * globalKnockbackMultiplier;
            if (entity instanceof LivingEntity livingEntity) {
                knockbackMultiplier = ProtectionEnchantment.transformExplosionKnockback(
                        livingEntity, knockbackMultiplier);
            }

            double forceMultiplier = knockbackMultiplier / delta;
            forceX = forceX * forceMultiplier;
            forceY = forceY * forceMultiplier;
            forceZ = forceZ * forceMultiplier;
            Vec3d forceVector = new Vec3d(forceX, forceY, forceZ);
            entity.setVelocity(entity.getVelocity().add(forceVector));

            if (entity instanceof PlayerEntity playerEntity && !playerEntity.isSpectator() && (
                    !playerEntity.isCreative() || !playerEntity.getAbilities().flying)) {
                affectedPlayers.put(playerEntity, forceVector);
            }
        }
    }

    // Send packet to players
    // Required for knockback to register properly
    private static void sendToPlayers(World world, Explosion explosion) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }
        Vec3d centerPos = explosion.getPosition();
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            if (player.squaredDistanceTo(centerPos)
                    < MAX_PLAYER_AFFECTED_DISTANCE * MAX_PLAYER_AFFECTED_DISTANCE) {
                player.networkHandler.sendPacket(
                        new ExplosionS2CPacket(centerPos.getX(), centerPos.getY(), centerPos.getZ(),
                                explosion.getPower(), explosion.getAffectedBlocks(),
                                explosion.getAffectedPlayers().get(player),
                                explosion.getDestructionType(), explosion.getParticle(),
                                explosion.getEmitterParticle(), explosion.getSoundEvent()));
            }
        }
    }

    @Override
    public Item getBottledItem() {
        return ModItems.ZEPHYR_SPORES_BOTTLE;
    }

    @Override
    public Item getBucketItem() {
        return ModItems.ZEPHYR_SPORES_BUCKET;
    }

    @Override
    public Block getFluidBlock() {
        return ModBlocks.ZEPHYR_SPORE_SEA;
    }

    @Override
    public Block getSolidBlock() {
        return ModBlocks.ZEPHYR_SPORE_BLOCK;
    }

    @Override
    public FlowableFluid getFluid() {
        return ModFluids.ZEPHYR_SPORES;
    }

    @Override
    public StatusEffect getStatusEffect() {
        return ModStatusEffects.ZEPHYR_SPORES;
    }

    @Override
    public int getColor() {
        return COLOR;
    }

    @Override
    public int getParticleColor() {
        return PARTICLE_COLOR;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public @Nullable BlockState getFluidCollisionState() {
        return Blocks.AIR.getDefaultState();
    }
}
