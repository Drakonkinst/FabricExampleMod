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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModEntityTypeTags {

    public static final TagKey<EntityType<?>> SPORES_ALWAYS_AFFECT = ModEntityTypeTags.ofCommon(
            "spores_always_affect");
    public static final TagKey<EntityType<?>> SPORES_NEVER_AFFECT = ModEntityTypeTags.ofCommon(
            "spores_never_affect");
    public static final TagKey<EntityType<?>> SPORES_NEVER_SUFFOCATE = ModEntityTypeTags.ofCommon(
            "spores_never_suffocate");
    public static TagKey<EntityType<?>> HAS_STEEL = ModEntityTypeTags.ofCommon("has_steel");
    public static TagKey<EntityType<?>> HAS_IRON = ModEntityTypeTags.ofCommon("has_iron");
    public static TagKey<EntityType<?>> MIDNIGHT_CREATURES_CANNOT_IMITATE = ModEntityTypeTags.ofCommon(
            "midnight_creatures_cannot_imitate");

    private static TagKey<EntityType<?>> of(String id) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, Worldsinger.id(id));
    }

    private static TagKey<EntityType<?>> ofCommon(String id) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(ModConstants.COMMON_ID, id));
    }

    private ModEntityTypeTags() {}
}
