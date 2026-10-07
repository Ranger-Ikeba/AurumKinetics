package com.aurumkinetics.network;

import com.aurumkinetics.AurumKinetics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AurumKinetics.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, PacketSyncSpeedup.class,
                PacketSyncSpeedup::encode,
                PacketSyncSpeedup::decode,
                PacketSyncSpeedup::handle);
    }
}
