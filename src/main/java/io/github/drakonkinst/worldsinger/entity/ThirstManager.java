package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.datatables.DataTableRegistry;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.ThirstManagerComponent;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.registry.ModDataTables;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.Difficulty;

// Similar to Hunger, but uses different names.
// Thirst is simpler, and has no saturation equivalent. It goes down faster than hunger by default.
public class ThirstManager implements ThirstManagerComponent {

    // NBT Keys
    private static final String KEY_THIRST_LEVEL = "ThirstLevel";
    private static final String KEY_DEHYDRATION_LEVEL = "DehydrationLevel";
    private static final String KEY_DEHYDRATION_TICK_TIMER = "DehydrationTickTimer";

    // Constants that probably won't change
    private static final int MAX_THIRST_LEVEL = 20;
    private static final float MAX_EXHAUSTION = 40.0f;
    private static final float DEHYDRATION_PER_THIRST_LEVEL = 4.0f;
    private static final int DAMAGE_TICK_INTERVAL = 80;
    private static final float MIN_HEALTH_ON_EASY = 10.0f;
    private static final float MIN_HEALTH_ON_NORMAL = 1.0f;

    // Constants that can possibly change for balancing
    private static final int MIN_NATURAL_THIRST = 6;
    private static final float DRAIN_MULTIPLIER = 0.75f;
    private static final float DAMAGE_FROM_THIRST = 1.0f;

    private final LivingEntity entity;
    private final DamageSource thirstDamageSource;
    private int thirstLevel = MAX_THIRST_LEVEL;
    private int dehydrationTickTimer;
    private float dehydration;

    public ThirstManager(LivingEntity entity) {
        this.entity = entity;
        thirstDamageSource = ModDamageTypes.createSource(entity.getWorld(), ModDamageTypes.THIRST);
    }

    @Override
    public void serverTick() {
        if (dehydration > DEHYDRATION_PER_THIRST_LEVEL) {
            dehydration -= DEHYDRATION_PER_THIRST_LEVEL;
            if (thirstLevel > MIN_NATURAL_THIRST) {
                remove(1);
            }
        }

        // Use MIN_NATURAL_THIRST as the threshold after which you get negative effects from dehydration.
        // TODO: Might replace with a "Dehydrated" status effect later
        if (thirstLevel < MIN_NATURAL_THIRST && entity instanceof PlayerEntity player
                && !player.isCreative()) {
            player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 60, 0, false, false,
                            false));
            player.addStatusEffect(
                    new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, false, false, false));
        }

        // Start taking damage
        if (thirstLevel <= 0) {
            ++dehydrationTickTimer;
            if (dehydrationTickTimer >= DAMAGE_TICK_INTERVAL) {
                if (entity instanceof PlayerEntity) {
                    // Respect difficulty settings
                    Difficulty difficulty = entity.getWorld().getDifficulty();
                    float health = entity.getHealth();
                    if (difficulty != Difficulty.PEACEFUL && (difficulty != Difficulty.EASY
                            || health > MIN_HEALTH_ON_EASY) && (difficulty != Difficulty.NORMAL
                            || health > MIN_HEALTH_ON_NORMAL)) {
                        entity.damage(thirstDamageSource, DAMAGE_FROM_THIRST);
                    }
                } else {
                    // Just kill them
                    entity.damage(thirstDamageSource, DAMAGE_FROM_THIRST);
                }
                dehydrationTickTimer = 0;
            }
        }
    }

    @Override
    public void add(int water) {
        thirstLevel = Math.min(thirstLevel + water, MAX_THIRST_LEVEL);
        ModComponents.THIRST_MANAGER.sync(entity);
    }

    @Override
    public void remove(int water) {
        thirstLevel = Math.max(0, thirstLevel - water);
        ModComponents.THIRST_MANAGER.sync(entity);
    }

    @Override
    public void addDehydration(float dehydration) {
        this.dehydration = Math.min(this.dehydration + dehydration * DRAIN_MULTIPLIER,
                MAX_EXHAUSTION);
        ModComponents.THIRST_MANAGER.sync(entity);
    }

    public void drink(Item item, ItemStack stack) {
        int water = DataTableRegistry.INSTANCE.get(ModDataTables.CONSUMABLE_HYDRATION)
                .getIntForItem(item);
        if (water == 0) {
            return;
        }

        if (water > 0) {
            add(water);
        } else {
            remove(-water);
        }
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        thirstLevel = nbt.getInt(KEY_THIRST_LEVEL);
        dehydration = nbt.getFloat(KEY_DEHYDRATION_LEVEL);
        dehydrationTickTimer = nbt.getInt(KEY_DEHYDRATION_TICK_TIMER);
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putInt(KEY_THIRST_LEVEL, thirstLevel);
        nbt.putFloat(KEY_DEHYDRATION_LEVEL, dehydration);
        nbt.putInt(KEY_DEHYDRATION_TICK_TIMER, dehydrationTickTimer);
    }

    @Override
    public int get() {
        return thirstLevel;
    }

    @Override
    public boolean isFull() {
        return thirstLevel >= MAX_THIRST_LEVEL;
    }

    @Override
    public boolean isCritical() {
        return thirstLevel < MIN_NATURAL_THIRST;
    }
}
