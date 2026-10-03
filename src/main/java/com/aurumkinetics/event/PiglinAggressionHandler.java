package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.trade.PiglinTradeData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class PiglinAggressionHandler {

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Piglin || event.getEntity() instanceof PiglinBrute)) return;
        if (!(event.getNewTarget() instanceof Player)) return;

        Mob piglin = (Mob) event.getEntity();
        PiglinTradeData data = PiglinTradeData.get(piglin);
        if (data.tamed) {
            event.setCanceled(true);
        }
    }
}
