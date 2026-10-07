package com.aurumkinetics.network;

import com.aurumkinetics.speedup.ClientSpeedupCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Сервер → клиент: сообщает, какой ускоритель в блоке, открытом в конкретном меню.
 * speedupId = "" → нет ускорителя.
 */
public class PacketSyncSpeedup {

    public final int containerId;
    public final String speedupId;

    public PacketSyncSpeedup(int containerId, String speedupId) {
        this.containerId = containerId;
        this.speedupId = speedupId;
    }

    public static void encode(PacketSyncSpeedup msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.containerId);
        buf.writeUtf(msg.speedupId);
    }

    public static PacketSyncSpeedup decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        String s = buf.readUtf();
        return new PacketSyncSpeedup(id, s);
    }

    public static void handle(PacketSyncSpeedup msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientSpeedupCache.put(msg.containerId, msg.speedupId);
        });
        ctx.get().setPacketHandled(true);
    }
}
