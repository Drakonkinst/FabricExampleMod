/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.datatables.DataTableRegistry;
import io.github.drakonkinst.worldsinger.cosmere.ThirstManager;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.registry.ModDataTables;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Difficulty;

// Similar to Hunger, but uses different names.
// Thirst is simpler, and has no saturation equivalent. It goes down faster than hunger by default.
public class PlayerThirstManager implements ThirstManager {

    public static final Codec<PlayerThirstManager> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("thirst_level").forGetter(PlayerThirstManager::get),
                            Codec.FLOAT.fieldOf("dehydration_level")
                                    .forGetter(playerThirstManager -> playerThirstManager.dehydration),
                            Codec.INT.fieldOf("dehydration_tick_timer")
                                    .forGetter(
                                            playerThirstManager -> playerThirstManager.dehydrationTickTimer))
                    .apply(instance, PlayerThirstManager::new));

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

    private int thirstLevel;
    private float dehydration;
    private int dehydrationTickTimer;

    public PlayerThirstManager() {
        this.thirstLevel = MAX_THIRST_LEVEL;
        this.dehydration = 0.0f;
        this.dehydrationTickTimer = 0;
    }

    public PlayerThirstManager(int thirstLevel, float dehydration, int dehydrationTickTimer) {
        this.thirstLevel = thirstLevel;
        this.dehydration = dehydration;
        this.dehydrationTickTimer = dehydrationTickTimer;
    }

    // Used for syncing only, should not be used by any other methods
    // For now, only thirst level needs to be synced -- the others do not produce any visual updates
    public void setThirstLevel(int thirstLevel) {
        this.thirstLevel = thirstLevel;
    }

    @Override
    public void update(LivingEntity entity) {
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
                DamageSource thirstDamageSource = ModDamageTypes.createSource(entity.getWorld(),
                        ModDamageTypes.THIRST);
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
    }

    @Override
    public void remove(int water) {
        thirstLevel = Math.max(0, thirstLevel - water);
    }

    @Override
    public void addDehydration(float dehydration) {
        this.dehydration = Math.min(this.dehydration + dehydration * DRAIN_MULTIPLIER,
                MAX_EXHAUSTION);
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
