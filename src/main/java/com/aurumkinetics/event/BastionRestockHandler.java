package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.trade.PiglinTradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class BastionRestockHandler {

    /** 5 минут. */
    private static final long RESTOCK_TIME_MS = 5 * 60 * 1000L;

    private static final ResourceKey<Structure> BASTION_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    new ResourceLocation("minecraft", "bastion_remnant"));

    private static final Map<UUID, Long> piglinEnteredAt = new HashMap<>();

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (level.dimension() != Level.NETHER) return;
        if (level.getGameTime() % 20 != 0) return;

        long now = System.currentTimeMillis();

        AABB world = new AABB(-30000000, -256, -30000000, 30000000, 256, 30000000);
        List<Mob> piglins = level.getEntitiesOfClass(Mob.class, world,
                e -> e instanceof Piglin || e instanceof PiglinBrute);

        for (Mob piglin : piglins) {
            PiglinTradeData data = PiglinTradeData.get(piglin);
            if (!data.tamed) continue;

            BlockPos pos = piglin.blockPosition();
            boolean inBastion = isInsideBastion(level, pos);

            if (!inBastion) {
                piglinEnteredAt.remove(piglin.getUUID());
                continue;
            }

            Long entered = piglinEnteredAt.get(piglin.getUUID());
            if (entered == null) {
                piglinEnteredAt.put(piglin.getUUID(), now);
                AurumKinetics.LOGGER.info("[Aurum] Пиглин {} вошёл в бастион", piglin.getUUID());
                continue;
            }

            if (now - entered < RESTOCK_TIME_MS) continue;

            data.restock();
            data.save(piglin);
            piglinEnteredAt.remove(piglin.getUUID());

            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    piglin.getX(), piglin.getY() + 1.5, piglin.getZ(),
                    10, 0.5, 0.5, 0.5, 0.0);

            AurumKinetics.LOGGER.info("[Aurum] Торговля восстановлена для пиглина {}", piglin.getUUID());
        }
    }

    private static boolean isInsideBastion(ServerLevel level, BlockPos pos) {
        for (int dx = -12; dx <= 12; dx += 4) {
            for (int dz = -12; dz <= 12; dz += 4) {
                BlockPos check = pos.offset(dx, 0, dz);
                StructureStart start = level.structureManager()
                        .getStructureWithPieceAt(check, BASTION_KEY);
                if (start != null && start.isValid()) return true;
            }
        }
        return false;
    }
}
