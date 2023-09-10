package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.block.SporeEmitting;
import io.github.drakonkinst.worldsinger.world.lumar.AetherSporeType;
import io.github.drakonkinst.worldsinger.world.lumar.SporeParticleManager;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SporeBottleItem extends PotionItem implements SporeEmitting {

    private final AetherSporeType sporeType;

    public SporeBottleItem(AetherSporeType sporeType, Settings settings) {
        super(settings);
        this.sporeType = sporeType;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity) user : null;
        if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
        }
        if (!world.isClient) {
            StatusEffect statusEffect = sporeType.getStatusEffect();
            if (statusEffect == null) {
                // TODO: Deal flat damage to entity
            } else {
                SporeParticleManager.applySporeEffect(user, statusEffect,
                        SporeParticleManager.SPORE_EFFECT_DURATION_TICKS_DEFAULT);
            }
        }
        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!playerEntity.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        if (playerEntity == null || !playerEntity.getAbilities().creativeMode) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            if (playerEntity != null) {
                playerEntity.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        user.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    public AetherSporeType getSporeType() {
        return sporeType;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    // All code below used to overwrite potion behavior & hopefully avoid crashes.

    @Override
    public String getTranslationKey(ItemStack stack) {
        // Reset to default
        return this.getTranslationKey();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip,
            TooltipContext context) {
        // Reset to default
    }

    @Override
    public ItemStack getDefaultStack() {
        // Reset to default
        return new ItemStack(this);
    }
}
