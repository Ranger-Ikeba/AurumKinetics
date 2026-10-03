package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.trade.PiglinMerchantMenu;
import com.aurumkinetics.trade.PiglinMerchantMenuProvider;
import com.aurumkinetics.trade.PiglinTradeData;
import com.aurumkinetics.trade.SimplePiglinMerchant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class PiglinTradeEvents {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;

        Entity target = event.getTarget();
        if (!(target instanceof Piglin || target instanceof PiglinBrute)) return;

        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!(target.level() instanceof ServerLevel level)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        Mob piglin = (Mob) target;
        PiglinTradeData data = PiglinTradeData.get(piglin);
        if (!data.tamed) return;
        if (sp.containerMenu instanceof PiglinMerchantMenu) return;

        if (data.offers.isEmpty()) {
            data.addOffersForLevel(level.random, Math.max(1, data.level));
        }

        SimplePiglinMerchant merchant = new SimplePiglinMerchant(piglin, data);
        merchant.setTradingPlayer(sp);

        sp.openMenu(new PiglinMerchantMenuProvider(merchant));

        if (sp.containerMenu instanceof net.minecraft.world.inventory.MerchantMenu menu) {
            sp.connection.send(new ClientboundMerchantOffersPacket(
                    menu.containerId,
                    data.offers,
                    Math.max(1, data.level),
                    data.xp,
                    true,
                    true));
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Piglin || event.getEntity() instanceof PiglinBrute)) return;
        Mob piglin = (Mob) event.getEntity();
        if (!(event.getSource().getEntity() instanceof Player)) return;

        PiglinTradeData data = PiglinTradeData.get(piglin);
        if (data.tamed) {
            data.tamed = false;
            data.save(piglin);
            piglin.setTarget((Player) event.getSource().getEntity());
        }
    }

    public static boolean isGold(ItemStack s) {
        return s.is(Items.GOLD_INGOT) || s.is(Items.GOLD_NUGGET) || s.is(Items.GOLD_BLOCK);
    }

    public static void tryTame(Mob piglin, ServerLevel level) {
        PiglinTradeData data = PiglinTradeData.getOrCreate(piglin, level.random);
        if (data.tamed) return;
        data.tamed = true;
        data.save(piglin);

        level.sendParticles(ParticleTypes.HEART,
                piglin.getX(), piglin.getY() + 1.8, piglin.getZ(),
                7, 0.5, 0.5, 0.5, 0.0);
        level.playSound(null, piglin.blockPosition(), SoundEvents.PIGLIN_CELEBRATE,
                piglin.getSoundSource(), 1.0f, 1.0f);
        piglin.setTarget(null);
        piglin.setLastHurtByMob(null);
    }
}
