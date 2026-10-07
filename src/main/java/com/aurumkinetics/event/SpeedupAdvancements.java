package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.item.SpeedupItem;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class SpeedupAdvancements {

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        ItemStack stack = event.getCrafting();
        if (!(stack.getItem() instanceof SpeedupItem si)) return;

        String advId = si.getSpeedupType().advancementId;
        grant(sp, advId);
    }

    public static void grant(ServerPlayer player, String advancementId) {
        ResourceLocation id = new ResourceLocation(AurumKinetics.MOD_ID, advancementId);
        Advancement adv = player.server.getAdvancements().getAdvancement(id);
        if (adv != null) {
            player.getAdvancements().award(adv, "impossible");
        }
    }
}
