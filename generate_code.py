#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Финал этапа: убираем воронки из ускоряемых, спавнер оставляем."""

from pathlib import Path

ROOT = Path.cwd()
JAVA = ROOT / "src/main/java/com/aurumkinetics"


def write_file(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print(f"[OK] {path.relative_to(ROOT)}")


# SpeedupEvents — убираем воронку из валидных
write_file(JAVA / "event/SpeedupEvents.java", """package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.item.SpeedupItem;
import com.aurumkinetics.network.ModNetwork;
import com.aurumkinetics.network.PacketSyncSpeedup;
import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class SpeedupEvents {

    public static final String SPEEDUP_KEY = "AurumSpeedup";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof SpeedupItem si)) return;
        if (event.getLevel().isClientSide()) return;

        Level level = event.getLevel();
        BlockEntity be = level.getBlockEntity(event.getPos());
        if (be == null) return;
        if (!isValidTarget(be)) return;

        CompoundTag data = be.getPersistentData();
        if (data.contains(SPEEDUP_KEY)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        data.putString(SPEEDUP_KEY, si.getSpeedupType().id);
        be.setChanged();

        AurumKinetics.LOGGER.info("[Aurum] Ускоритель {} вставлен в {}",
                si.getSpeedupType().id, event.getPos());

        if (!event.getEntity().getAbilities().instabuild) stack.shrink(1);

        if (event.getEntity() instanceof ServerPlayer sp) {
            int containerId = sp.containerMenu.containerId;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                    new PacketSyncSpeedup(containerId, si.getSpeedupType().id));
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    /** Печи, коптильни, плавильни, зельеварки, спавнеры. Воронок нет. */
    public static boolean isValidTarget(BlockEntity be) {
        return be instanceof AbstractFurnaceBlockEntity
                || be instanceof BrewingStandBlockEntity
                || be instanceof SpawnerBlockEntity;
    }

    public static SpeedupType getSpeedup(BlockEntity be) {
        if (be == null) return null;
        CompoundTag data = be.getPersistentData();
        if (!data.contains(SPEEDUP_KEY)) return null;
        return SpeedupType.byId(data.getString(SPEEDUP_KEY));
    }
}
""")

# SpeedupTickers — только спавнер
write_file(JAVA / "event/SpeedupTickers.java", """package com.aurumkinetics.event;

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
""")

print("""
Финал этапа ускорителей.

ЧТО РАБОТАЕТ:
  - Печи / коптильни / плавильни — ×1.5 / ×2 / ×3.
  - Зельеварки — ×1.5 / ×2 / ×3.
  - Спавнеры — чаще (extra раз serverTick за тик) + бонусный моб (10% для золотого).

ЧТО НЕ РАБОТАЕТ (убрано):
  - Воронки — ванильная логика не поддерживает ускорение без застревания.

Достижения:
  - «Скорость» (кварц), «Эффективность» (золото), «Продуктивность» (лазурь).
  - «Умелый зельевар» — 42 бутылки.
  - «Золотое рукопожатие» — приручить пиглина.
""")