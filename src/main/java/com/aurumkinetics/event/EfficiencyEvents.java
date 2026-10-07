package com.aurumkinetics.event;

import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Лазуритовый ускоритель: 20% шанс вернуть топливо.
 * Простая реализация: раз в 20 тиков, если печь с azure горит и в слоте топлива пусто,
 * с шансом 20% добавляем обратно 1 уголь (или другой предмет, который недавно жгли).
 */
@Mod.EventBusSubscriber(modid = "aurum_kinetics")
public class EfficiencyEvents {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (level.getGameTime() % 100 != 0) return;  // раз в 5 сек

        for (var player : level.players()) {
            var center = player.blockPosition();
            for (int dx = -32; dx <= 32; dx += 16) {
                for (int dz = -32; dz <= 32; dz += 16) {
                    int cx = (center.getX() + dx) >> 4;
                    int cz = (center.getZ() + dz) >> 4;
                    var chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;

                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (!(be instanceof AbstractFurnaceBlockEntity furnace)) continue;
                        SpeedupType type = SpeedupEvents.getSpeedup(furnace);
                        if (type != SpeedupType.AZURE) continue;

                        ItemStack fuel = furnace.getItem(1);
                        // Если топливо на исходе и с шансом 20%
                        if (fuel.isEmpty() && RNG.nextFloat() < 0.2f) {
                            // Дадим 1 уголь в качестве бонуса
                            furnace.setItem(1, new ItemStack(net.minecraft.world.item.Items.COAL, 1));
                            furnace.setChanged();
                        }
                    }
                }
            }
        }
    }
}
