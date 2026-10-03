package com.aurumkinetics.trade;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;

public class PiglinMerchantMenu extends MerchantMenu {

    private final Merchant trader;

    public PiglinMerchantMenu(int id, Inventory inv, Merchant merchant) {
        super(id, inv, merchant);
        this.trader = merchant;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();

        if (index == 2) {
            if (!this.moveItemStackTo(stackInSlot, 3, 39, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stackInSlot, result);
        } else if (index == 0 || index == 1) {
            if (!this.moveItemStackTo(stackInSlot, 3, 39, true)) return ItemStack.EMPTY;
        } else if (index >= 3 && index < 39) {
            if (!this.moveItemStackTo(stackInSlot, 0, 2, false)) {
                if (index < 30) {
                    if (!this.moveItemStackTo(stackInSlot, 30, 39, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.moveItemStackTo(stackInSlot, 3, 30, false)) return ItemStack.EMPTY;
                }
            }
        }

        if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stackInSlot.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stackInSlot);
        return result;
    }
}
