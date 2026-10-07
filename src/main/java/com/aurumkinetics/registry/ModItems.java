package com.aurumkinetics.registry;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.item.GoldCoatingItem;
import com.aurumkinetics.item.SpeedupItem;
import com.aurumkinetics.speedup.SpeedupType;
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

    public static final RegistryObject<Item> GILDED_REDSTONE = ITEMS.register(
            "gilded_redstone",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLD_SPEEDUP = ITEMS.register(
            "gold_speedup",
            () -> new SpeedupItem(new Item.Properties().stacksTo(16), SpeedupType.GOLD));

    public static final RegistryObject<Item> QUARTZ_SPEEDUP = ITEMS.register(
            "quartz_speedup",
            () -> new SpeedupItem(new Item.Properties().stacksTo(16), SpeedupType.QUARTZ));

    public static final RegistryObject<Item> AZURE_SPEEDUP = ITEMS.register(
            "azure_speedup",
            () -> new SpeedupItem(new Item.Properties().stacksTo(16), SpeedupType.AZURE));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
