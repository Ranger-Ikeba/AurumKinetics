package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.trade.PiglinTradeData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Пиглин подбирает золото → отменяем ванильный подбор (без бартера) → приручаем.
 *
 * EntityItemPickupEvent срабатывает только для игроков (Player),
 * поэтому используем тик: находим лежащие ItemEntity с золотом рядом с пиглином
 * и «подбираем» их сами.
 */
@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class PiglinGoldPickupHandler {

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (level.getGameTime() % 5 != 0) return;

        // Ищем пиглинов вокруг игроков (радиус 48) — этого достаточно
        for (var player : level.players()) {
            AABB area = player.getBoundingBox().inflate(48);
            List<Mob> piglins = level.getEntitiesOfClass(Mob.class, area,
                    e -> (e instanceof Piglin || e instanceof PiglinBrute)
                            && !PiglinTradeData.get(e).tamed);

            for (Mob piglin : piglins) {
                List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
                        piglin.getBoundingBox().inflate(3.0));
                for (ItemEntity item : items) {
                    ItemStack stack = item.getItem();
                    if (!PiglinTradeEvents.isGold(stack)) continue;

                    // Забираем 1 предмет и приручаем
                    stack.shrink(1);
                    if (stack.isEmpty()) item.discard();
                    PiglinTradeEvents.tryTame(piglin, level);
                    break;
                }
            }
        }
    }
}
