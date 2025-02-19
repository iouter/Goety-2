package com.Polarice3.Goety.client.inventory.container;

import com.Polarice3.Goety.Goety;
import com.Polarice3.Goety.common.items.handler.BrewBagItemHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BrewBagContainer extends AbstractContainerMenu {
    private final ItemStack stack;

    public static BrewBagContainer createContainerClientSide(int id, Inventory inventory, FriendlyByteBuf buffer) {
        return new BrewBagContainer(id, inventory, new BrewBagItemHandler(ItemStack.EMPTY), ItemStack.EMPTY);
    }

    public BrewBagContainer(int id, Inventory playerInventory, BrewBagItemHandler handler, ItemStack stack) {
        super(ModContainerType.BREW_BAG.get(), id);
        this.stack = stack;
        for (int i = 0; i < 5; i++) {
            addSlot(new SlotItemHandler(handler, i + 1, 62 - 18 + i * 18, 25){
                public @NotNull Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, Goety.location("item/empty_slot_brew"));
                }
            });
        }
        for (int i = 0; i < 5; i++) {
            addSlot(new SlotItemHandler(handler,6 + i, 62 - 18 + i * 18, 43){
                public @NotNull Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, Goety.location("item/empty_slot_brew"));
                }
            });
        }

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pIndex < 11) {
                if (!this.moveItemStackTo(itemstack1, 11, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 11, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return !stack.isEmpty();
    }
}
