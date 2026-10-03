package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class CraftEvents {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getCrafting();
        if (stack.is(ModItems.GOLD_COATING.get())) {
            ResourceLocation id = new ResourceLocation(AurumKinetics.MOD_ID, "bling");
            Advancement adv = player.server.getAdvancements().getAdvancement(id);
            if (adv != null) player.getAdvancements().award(adv, "impossible");
        }
    }
}
