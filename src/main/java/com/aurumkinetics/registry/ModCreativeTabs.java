package com.aurumkinetics.registry;

import com.aurumkinetics.AurumKinetics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AurumKinetics.MOD_ID);

    public static final RegistryObject<CreativeModeTab> AURUM_TAB = TABS.register("aurum_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.aurum_kinetics"))
                    .icon(() -> new ItemStack(ModItems.GOLD_COATING.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.GOLD_COATING.get());
                        output.accept(ModItems.QUARTZ_COATING.get());
                        output.accept(ModItems.BLUE_COATING.get());
                        output.accept(ModItems.GILDED_REDSTONE.get());
                        output.accept(ModItems.GOLD_SPEEDUP.get());
                        output.accept(ModItems.QUARTZ_SPEEDUP.get());
                        output.accept(ModItems.AZURE_SPEEDUP.get());
                    })
                    .build());

    public static void register(IEventBus bus) { TABS.register(bus); }
}
