package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class SpeedupTickers {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

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
                        SpeedupType type = SpeedupEvents.getSpeedup(be);
                        if (type == null) continue;

                        if (be instanceof SpawnerBlockEntity spawner) {
                            tickSpawnerExtra(level, spawner, type);
                        }
                    }
                }
            }
        }
    }

    private static void tickSpawnerExtra(ServerLevel level, SpawnerBlockEntity spawner, SpeedupType type) {
        BlockPos pos = spawner.getBlockPos();
        AABB area = new AABB(pos).inflate(4);
        List<Mob> before = level.getEntitiesOfClass(Mob.class, area);
        int countBefore = before.size();

        int extra = (int) type.multiplier - 1;
        for (int i = 0; i < extra; i++) {
            spawner.getSpawner().serverTick(level, pos);
        }

        if (type == SpeedupType.GOLD && RNG.nextFloat() < 0.10f) {
            List<Mob> after = level.getEntitiesOfClass(Mob.class, area);
            if (after.size() > countBefore) {
                Mob newMob = after.get(after.size() - 1);
                EntityType<?> entityType = newMob.getType();
                Entity spawned = entityType.create(level);
                if (spawned != null) {
                    spawned.moveTo(pos.getX() + 0.5 + (RNG.nextDouble() - 0.5) * 2,
                            pos.getY(),
                            pos.getZ() + 0.5 + (RNG.nextDouble() - 0.5) * 2,
                            RNG.nextFloat() * 360f, 0);
                    level.addFreshEntity(spawned);
                }
            }
        }
    }
}
