package com.aurumkinetics;

import com.aurumkinetics.network.ModNetwork;
import com.aurumkinetics.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AurumKinetics.MOD_ID)
public class AurumKinetics {
    public static final String MOD_ID = "aurum_kinetics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AurumKinetics() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(bus);
        ModBlocks.register(bus);
        ModEntities.register(bus);
        ModCreativeTabs.register(bus);

        ModNetwork.register();

        LOGGER.info("Aurum Kinetics loaded.");
    }
}
