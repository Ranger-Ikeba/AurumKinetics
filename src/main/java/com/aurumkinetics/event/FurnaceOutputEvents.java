package com.aurumkinetics.event;

import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Золотой ускоритель: 10% шанс двойного результата.
 * Обход миксинов — раз в 20 тиков проверяем все печи с золотым ускорителем
 * и, если в слоте результата что-то есть, с шансом 10% клонируем 1 шт.
 */
@Mod.EventBusSubscriber(modid = "aurum_kinetics")
public class FurnaceOutputEvents {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (level.getGameTime() % 40 != 0) return;  // раз в 2 сек

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
                        if (type != SpeedupType.GOLD) continue;

                        // Слот результата — индекс 2
                        ItemStack result = furnace.getItem(2);
                        if (result.isEmpty()) continue;
                        if (RNG.nextFloat() > 0.10f) continue;

                        ItemStack extra = result.copy();
                        extra.setCount(1);
                        // Пытаемся добавить в слот результата
                        ItemStack current = furnace.getItem(2);
                        if (current.getCount() + 1 <= current.getMaxStackSize()) {
                            current.grow(1);
                            furnace.setChanged();
                        }
                    }
                }
            }
        }
    }
}
