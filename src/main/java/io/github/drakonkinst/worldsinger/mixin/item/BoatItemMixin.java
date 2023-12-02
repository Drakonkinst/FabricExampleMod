package io.github.drakonkinst.worldsinger.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.SilverLinedComponent;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.entity.SilverLinedBoatEntityData;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BoatItem.class)
public abstract class BoatItemMixin extends Item {

    @Unique
    private static final int SILVER_METER_COLOR = 0xC0C0C0;

    @Unique
    private static final int SILVER_TEXT_COLOR = 0xC0C0C0;

    @Unique
    private static final Style SILVER_TEXT_STYLE = Style.EMPTY.withColor(
            TextColor.fromRgb(SILVER_TEXT_COLOR));

    @Unique
    private static final int MAX_METER_STEPS = 13;

    public BoatItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isNbtSynced() {
        return super.isNbtSynced();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip,
            TooltipContext context) {
        SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(stack, null);
        if (silverItemData == null) {
            return;
        }
        int silverDurability = silverItemData.getSilverDurability();
        if (silverDurability <= 0) {
            return;
        }

        if (context.isAdvanced()) {
            tooltip.add(Text.translatable("item.silver_durability", silverDurability,
                    silverItemData.getMaxSilverDurability()).setStyle(SILVER_TEXT_STYLE));
        } else {
            tooltip.add(Text.translatable("item.silver_lined").setStyle(SILVER_TEXT_STYLE));
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        int silverDurability = this.getSilverDurability(stack);
        return super.isItemBarVisible(stack) || silverDurability > 0;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int silverDurability = this.getSilverDurability(stack);
        if (silverDurability <= 0) {
            return super.getItemBarColor(stack);
        }
        return SILVER_METER_COLOR;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int silverDurability = this.getSilverDurability(stack);
        if (silverDurability <= 0) {
            return super.getItemBarStep(stack);
        }
        int step = Math.min(Math.round((float) silverDurability * MAX_METER_STEPS
                / SilverLinedBoatEntityData.MAX_DURABILITY), MAX_METER_STEPS);
        return step;
    }

    @ModifyVariable(method = "use", at = @At(value = "STORE"))
    private BoatEntity addDataToEntity(BoatEntity entity, @Local PlayerEntity user,
            @Local Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        this.copySilverDataFromItemToEntity(itemStack, entity);
        return entity;
    }

    @Unique
    private void copySilverDataFromItemToEntity(ItemStack itemStack, BoatEntity entity) {
        int silverDurability = this.getSilverDurability(itemStack);
        if (silverDurability <= 0) {
            return;
        }
        SilverLinedComponent silverEntityData = ModComponents.SILVER_LINED.get(entity);
        silverEntityData.setSilverDurability(silverDurability);
    }

    @Unique
    private int getSilverDurability(ItemStack stack) {
        SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(stack, null);
        if (silverItemData == null) {
            Worldsinger.LOGGER.error(
                    "Expected to find silver data for boat item (testing " + stack.getItem()
                            .toString() + ")");
            return 0;
        }
        return silverItemData.getSilverDurability();
    }
}
