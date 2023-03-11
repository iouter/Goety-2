package com.Polarice3.Goety.common.items.handler;

import com.Polarice3.Goety.common.items.magic.MagicFocus;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class SoulUsingItemHandler extends ItemStackHandler {
    private final ItemStack itemStack;
    private int slot;

    public SoulUsingItemHandler(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack extractItem() {
        return extractItem(slot, 1, false);
    }

    public ItemStack insertItem(ItemStack insert) {
        return insertItem(slot, insert, false);
    }

    public ItemStack getSlot() {
        return getStackInSlot(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return stack.getItem() instanceof MagicFocus;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public NonNullList<ItemStack> getContents(){
        return stacks;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.putInt("slot", slot);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTags = tagList.getCompound(i);
            if (nbt.contains("slot")) {
                slot = nbt.getInt("slot");
                stacks.set(slot, ItemStack.of(itemTags));
            }
        }
        onLoad();

    }

    @Override
    protected void onContentsChanged(int slot) {
        CompoundTag nbt = itemStack.getOrCreateTag();
        nbt.putBoolean("goety-dirty", !nbt.getBoolean("goety-dirty"));
    }

    public static SoulUsingItemHandler orNull(ItemStack stack) {
        return (SoulUsingItemHandler) stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    }

    public static LazyOptional<SoulUsingItemHandler> getOptional(ItemStack stack) {
        LazyOptional<IItemHandler> itemHandlerOpt = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (itemHandlerOpt.isPresent()) {
            IItemHandler handler = itemHandlerOpt.orElse(null);
            if (handler instanceof SoulUsingItemHandler)
                return LazyOptional.of(() -> (SoulUsingItemHandler) handler);
        }
        return LazyOptional.empty();
    }

    public static SoulUsingItemHandler get(ItemStack stack) {
        IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElseThrow(() -> new IllegalArgumentException("ItemStack is missing item capability"));
        return (SoulUsingItemHandler) handler;
    }
}
