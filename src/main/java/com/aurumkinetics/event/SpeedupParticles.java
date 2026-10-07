package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.Random;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class SpeedupParticles {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (level.getGameTime() % 8 != 0) return;

        for (Player player : level.players()) {
            BlockPos center = player.blockPosition();
            int radius = 32;

            for (int dx = -radius; dx <= radius; dx += 16) {
                for (int dz = -radius; dz <= radius; dz += 16) {
                    int cx = (center.getX() + dx) >> 4;
                    int cz = (center.getZ() + dz) >> 4;
                    LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (!SpeedupEvents.isValidTarget(be)) continue;
                        SpeedupType type = SpeedupEvents.getSpeedup(be);
                        if (type == null) continue;
                        if (RNG.nextFloat() > 0.6f) continue;

                        for (int i = 0; i < 2; i++) {
                            spawnParticleAround(level, be.getBlockPos(), type);
                        }
                    }
                }
            }
        }
    }

    private static void spawnParticleAround(ServerLevel level, BlockPos pos, SpeedupType type) {
        double x, y, z;
        int side = RNG.nextInt(6);
        double offset = 0.05;
        switch (side) {
            case 0 -> { x = pos.getX() + RNG.nextDouble(); y = pos.getY() + RNG.nextDouble(); z = pos.getZ() - offset; }
            case 1 -> { x = pos.getX() + RNG.nextDouble(); y = pos.getY() + RNG.nextDouble(); z = pos.getZ() + 1 + offset; }
            case 2 -> { x = pos.getX() - offset; y = pos.getY() + RNG.nextDouble(); z = pos.getZ() + RNG.nextDouble(); }
            case 3 -> { x = pos.getX() + 1 + offset; y = pos.getY() + RNG.nextDouble(); z = pos.getZ() + RNG.nextDouble(); }
            case 4 -> { x = pos.getX() + RNG.nextDouble(); y = pos.getY() + 1 + offset; z = pos.getZ() + RNG.nextDouble(); }
            default -> { x = pos.getX() + RNG.nextDouble(); y = pos.getY() - offset; z = pos.getZ() + RNG.nextDouble(); }
        }

        DustParticleOptions particle = new DustParticleOptions(
                new Vector3f(type.pr, type.pg, type.pb), 0.7f);
        level.sendParticles(particle, x, y, z, 1, 0, 0.02, 0, 0.0);
    }
}
