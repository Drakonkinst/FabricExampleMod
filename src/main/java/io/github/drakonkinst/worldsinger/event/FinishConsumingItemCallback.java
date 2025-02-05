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
package io.github.drakonkinst.worldsinger.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

// Fabric hasn't made one of these yet, so guess it's up to me
// Fired whenever an item is successfully eaten, so it cannot cancel the consumption.
@FunctionalInterface
public interface FinishConsumingItemCallback {

    Event<FinishConsumingItemCallback> EVENT = EventFactory.createArrayBacked(
            FinishConsumingItemCallback.class, (listeners) -> (entity, item, foodComponent) -> {
                for (FinishConsumingItemCallback listener : listeners) {
                    listener.onConsume(entity, item, foodComponent);
                }
            });

    void onConsume(LivingEntity entity, ItemStack stack, FoodComponent foodComponent);
}
