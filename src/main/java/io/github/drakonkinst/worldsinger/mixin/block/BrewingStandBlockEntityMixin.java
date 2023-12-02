package io.github.drakonkinst.worldsinger.mixin.block;

import io.github.drakonkinst.worldsinger.item.ModItemTags;
import io.github.drakonkinst.worldsinger.mixin.accessor.BrewingRecipeRegistryAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.BrewingStandBlockEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends LockableContainerBlockEntity {

    @Unique
    private static final int BREWING_FUEL_AMOUNT = 20;

    @Unique
    private static final int FUEL_SLOT = 4;

    @Unique
    private static final int INGREDIENT_SLOT = 3;

    @Inject(method = "tick", at = @At("HEAD"))
    private static void consumeCustomFuels(World world, BlockPos pos, BlockState state,
            BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        DefaultedList<ItemStack> slots = ((BrewingStandBlockEntityAccessor) blockEntity).worldsinger$getInventory();
        ItemStack itemStack = slots.get(FUEL_SLOT);
        if (((BrewingStandBlockEntityAccessor) blockEntity).worldsinger$getFuel() <= 0
                && itemStack.isIn(ModItemTags.BREWING_STAND_FUELS)) {
            ((BrewingStandBlockEntityAccessor) blockEntity).worldsinger$setFuel(
                    BREWING_FUEL_AMOUNT);

            Item remainderItem = itemStack.getItem().getRecipeRemainder();
            itemStack.decrement(1);
            if (remainderItem != null) {
                ItemStack remainderStack = remainderItem.getDefaultStack();
                if (itemStack.isEmpty()) {
                    itemStack = remainderStack;
                } else {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), remainderStack);
                }
            }
            slots.set(FUEL_SLOT, itemStack);
            BrewingStandBlockEntity.markDirty(world, pos, state);
        }
    }

    // BrewingStandBlockEntity#craft does not properly handle items with recipe remainders
    // (specifically when there's only 1 in the stack) so fixing this
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BrewingStandBlockEntity;craft(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/collection/DefaultedList;)V"))
    private static void craftCorrectly(World world, BlockPos pos, DefaultedList<ItemStack> slots) {
        ItemStack itemStack = slots.get(INGREDIENT_SLOT);
        for (int i = 0; i < 3; ++i) {
            slots.set(i, BrewingRecipeRegistry.craft(itemStack, slots.get(i)));
        }

        Item remainderItem = itemStack.getItem().getRecipeRemainder();
        itemStack.decrement(1);
        if (remainderItem != null) {
            ItemStack remainderStack = remainderItem.getDefaultStack();
            if (itemStack.isEmpty()) {
                itemStack = remainderStack;
            } else {
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), remainderStack);
            }
        }
        slots.set(INGREDIENT_SLOT, itemStack);
        world.syncWorldEvent(WorldEvents.BREWING_STAND_BREWS, pos, 0);
    }

    protected BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos,
            BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "isValid", at = @At("RETURN"), cancellable = true)
    private void allowCustomPotionsAndFuels(int slot, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir) {
        if (slot == 4) {
            cir.setReturnValue(cir.getReturnValue() || stack.isIn(ModItemTags.BREWING_STAND_FUELS));
            return;
        }
        if (slot == 3 || !this.getStack(slot).isEmpty()) {
            return;
        }
        for (Ingredient ingredient : BrewingRecipeRegistryAccessor.worldsinger$getPotionTypes()) {
            if (ingredient.test(stack)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Shadow
    public abstract ItemStack getStack(int slot);
}
