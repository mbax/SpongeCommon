/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.processor.value.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFlowerPot;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;

import java.util.Optional;

public class FlowerPotValueProcessor extends AbstractSpongeValueProcessor<TileEntityFlowerPot, ItemStackSnapshot, Value<ItemStackSnapshot>> {

    public FlowerPotValueProcessor() {
        super(TileEntityFlowerPot.class, Keys.REPRESENTED_ITEM);
    }

    @Override
    protected Value<ItemStackSnapshot> constructValue(ItemStackSnapshot value) {
        return new SpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, value);
    }

    @Override
    protected boolean set(TileEntityFlowerPot flowerPot, ItemStackSnapshot value) {
        if (value == ItemStackSnapshot.NONE) {
            flowerPot.setFlowerPotData(null, 0);
        } else {
            Item item = (Item) value.getType();
            int meta = ((SpongeItemStackSnapshot) value).getDamageValue();
            if (!((BlockFlowerPot) Blocks.flower_pot).canNotContain(Block.getBlockFromItem(item), meta)) {
                return false;
            }
            flowerPot.setFlowerPotData(item, meta);
        }
        flowerPot.markDirty();
        flowerPot.getWorld().markBlockForUpdate(flowerPot.getPos());
        return true;
    }

    @Override
    protected Optional<ItemStackSnapshot> getVal(TileEntityFlowerPot flowerPot) {
        if (flowerPot.getFlowerPotItem() == null) {
            return Optional.empty();
        }
        ItemStack stack = new ItemStack(flowerPot.getFlowerPotItem(), 1, flowerPot.getFlowerPotData());
        return Optional.of(((org.spongepowered.api.item.inventory.ItemStack) stack).createSnapshot());
    }

    @Override
    protected ImmutableValue<ItemStackSnapshot> constructImmutableValue(ItemStackSnapshot value) {
        return new ImmutableSpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof TileEntityFlowerPot)) {
            return DataTransactionResult.failNoData();
        }
        Optional<ItemStackSnapshot> oldValue = getVal((TileEntityFlowerPot) container);
        if (!oldValue.isPresent()) {
            return DataTransactionResult.successNoData();
        }
        ((TileEntityFlowerPot) container).setFlowerPotData(null, 0);
        ((TileEntityFlowerPot) container).markDirty();
        return DataTransactionResult.successRemove(constructImmutableValue(oldValue.get()));
    }
}
