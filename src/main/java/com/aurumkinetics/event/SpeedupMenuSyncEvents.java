package com.aurumkinetics.event;

import com.aurumkinetics.network.ModNetwork;
import com.aurumkinetics.network.PacketSyncSpeedup;
import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "aurum_kinetics")
public class SpeedupMenuSyncEvents {

    @SubscribeEvent
    public static void onOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;

        AbstractContainerMenu menu = event.getContainer();
        BlockEntity be = findFurnaceBE(menu);
        String speedupId = "";
        if (be != null) {
            SpeedupType type = SpeedupEvents.getSpeedup(be);
            if (type != null) speedupId = type.id;
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                new PacketSyncSpeedup(menu.containerId, speedupId));
    }

    private static BlockEntity findFurnaceBE(AbstractContainerMenu menu) {
        for (int i = 0; i < menu.slots.size(); i++) {
            var c = menu.slots.get(i).container;
            if (c instanceof BlockEntity be
                    && (be instanceof AbstractFurnaceBlockEntity || be instanceof BrewingStandBlockEntity)) {
                return be;
            }
        }
        return null;
    }
}
