package com.aurumkinetics.trade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.Merchant;

public class PiglinMerchantMenuProvider implements MenuProvider {

    private final Merchant merchant;

    public PiglinMerchantMenuProvider(Merchant merchant) {
        this.merchant = merchant;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.minecraft.piglin");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new PiglinMerchantMenu(id, inv, merchant);
    }
}
