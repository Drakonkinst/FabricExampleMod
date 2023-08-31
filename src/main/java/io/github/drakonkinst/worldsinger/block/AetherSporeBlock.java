package io.github.drakonkinst.worldsinger.block;

import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.util.Constants;
import io.github.drakonkinst.worldsinger.world.lumar.AetherSporeType;
import io.github.drakonkinst.worldsinger.world.lumar.SporeParticles;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;

public class AetherSporeBlock extends FallingBlock implements FluidDrainable, SporeEmitting {

    private final AetherSporeType aetherSporeType;
    private final Block fluidizedBlock;

    public AetherSporeBlock(AetherSporeType sporeType, Block fluidizedBlock, Settings settings) {
        super(settings);
        this.aetherSporeType = sporeType;
        this.fluidizedBlock = fluidizedBlock;

        if (this.fluidizedBlock instanceof AetherSporeFluidBlock aetherSporeFluidBlock) {
            aetherSporeFluidBlock.setSolidBlock(this);
        } else {
            Constants.LOGGER.error("Expected fluidized block for " + this.getClass().getName() +
                    " to be an instance of AetherSporeFluidBlock");
        }
    }

    @Override
    protected void configureFallingBlockEntity(FallingBlockEntity entity) {
        entity.dropItem = false;
    }

    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingBlockState,
            BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {

        if (AetherSporeFluidBlock.shouldFluidize(world.getBlockState(pos.down()))) {
            // If it should immediately become a liquid, do not spawn particles
            world.setBlockState(pos, this.fluidizedBlock.getDefaultState(), Block.NOTIFY_ALL);
        } else if (world instanceof ServerWorld serverWorld) {
            // Spawn particles based on fall distance
            int fallDistance = fallingBlockEntity.getFallingBlockPos().getY() - pos.getY();
            if (fallDistance >= 4) {
                SporeParticles.spawnBlockParticles(serverWorld, aetherSporeType, pos, 1.5, 0.45);
            } else {
                SporeParticles.spawnBlockParticles(serverWorld, aetherSporeType, pos, 0.75, 0.5);
            }
        }
    }

    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity entity) {
        if (world instanceof ServerWorld serverWorld) {
            SporeParticles.spawnBlockParticles(serverWorld, aetherSporeType, pos, 2.5, 0.45);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        BlockState fluidizedSource = blockView.getBlockState(blockPos.down());
        if (AetherSporeFluidBlock.shouldFluidize(fluidizedSource)) {
            // Become a fluid immediately if it should be fluidized
            return this.fluidizedBlock.getDefaultState();
        }
        return super.getPlacementState(ctx);
    }

    @Override
    public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(),
                Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        if (!world.isClient()) {
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
        }
        return new ItemStack(ModItems.VERDANT_SPORES_BUCKET);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity,
            float fallDistance) {
        // Spawn splash particles on landing
        if (fallDistance > 0.25f && world instanceof ServerWorld serverWorld
                && !(entity instanceof FallingBlockEntity)) {
            SporeParticles.spawnSplashParticles(serverWorld, aetherSporeType, entity,
                    fallDistance, false);
        }
        super.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit,
            ProjectileEntity projectile) {
        // Spawn projectile particles on hit
        if (world instanceof ServerWorld serverWorld) {
            Vec3d pos = projectile.getPos();
            SporeParticles.spawnProjectileParticles(serverWorld, aetherSporeType, pos);
        }
        super.onProjectileHit(world, state, hit, projectile);
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        // Spawn breaking particles on break
        if (world instanceof ServerWorld serverWorld) {
            SporeParticles.spawnBlockParticles(serverWorld, aetherSporeType, pos, 0.6, 1.0);
        }
        super.onBroken(world, pos, state);
    }

    public Block getFluidizedBlock() {
        return fluidizedBlock;
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        // TODO: Change to unique sound
        return Optional.of(SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW);
    }

    @Override
    public int getColor(BlockState state, BlockView world, BlockPos pos) {
        return aetherSporeType.getParticleColor();
    }

    @Override
    public AetherSporeType getSporeType() {
        return aetherSporeType;
    }
}
