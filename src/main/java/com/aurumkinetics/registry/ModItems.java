package com.aurumkinetics.registry;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.item.GoldCoatingItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AurumKinetics.MOD_ID);

    public static final RegistryObject<Item> GOLD_COATING = ITEMS.register(
            "gold_coating",
            () -> new GoldCoatingItem(new Item.Properties().stacksTo(16), GoldCoatingItem.Type.GOLD));

    public static final RegistryObject<Item> QUARTZ_COATING = ITEMS.register(
            "quartz_coating",
            () -> new GoldCoatingItem(new Item.Properties().stacksTo(16), GoldCoatingItem.Type.QUARTZ));

    public static final RegistryObject<Item> BLUE_COATING = ITEMS.register(
            "blue_coating",
            () -> new GoldCoatingItem(new Item.Properties().stacksTo(16), GoldCoatingItem.Type.BLUE));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
