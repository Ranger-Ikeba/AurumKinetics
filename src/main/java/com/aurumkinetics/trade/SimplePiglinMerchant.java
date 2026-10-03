package com.aurumkinetics.trade;

import com.aurumkinetics.AurumKinetics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Прокачка по опыту.
 * Пороги: 20, 45, 80, 130 (примерно 8-12 сделок на уровень).
 * XP за сделку = offer.getXp() (2..5 на ур.1, 5..8 на ур.2, 8..14 на ур.3, 12..20 на ур.4).
 */
public class SimplePiglinMerchant implements Merchant {

    /** Порог XP для переходов 1→2, 2→3, 3→4, 4→5. */
    private static final int[] XP_TO_NEXT_LEVEL = { 20, 45, 80, 130 };

    private final Mob piglin;
    private final PiglinTradeData data;
    private Player tradingPlayer;
    private MerchantOffer lastTrade = null;
    private long lastTradeTime = 0;

    public SimplePiglinMerchant(Mob piglin, PiglinTradeData data) {
        this.piglin = piglin;
        this.data = data;
    }

    public Mob getPiglin() { return piglin; }

    @Override public void setTradingPlayer(@Nullable Player p) { this.tradingPlayer = p; }
    @Nullable @Override public Player getTradingPlayer() { return tradingPlayer; }
    @Override public MerchantOffers getOffers() { return data.offers; }
    @Override public void overrideOffers(MerchantOffers o) { data.offers = o; }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        long now = System.currentTimeMillis();
        if (offer == lastTrade && now - lastTradeTime < 100) return;
        lastTrade = offer;
        lastTradeTime = now;

        if (offer.getUses() >= offer.getMaxUses()) return;

        int gained = Math.max(1, offer.getXp());
        offer.increaseUses();
        data.tradesCount++;
        data.xp += gained;

        if (tradingPlayer instanceof ServerPlayer sp && piglin.level() instanceof ServerLevel sl) {
            Vec3 pos = new Vec3(piglin.getX(), piglin.getY() + 0.5, piglin.getZ());
            ExperienceOrb.award(sl, pos, Math.max(1, gained / 4));
        }

        while (data.level < 5) {
            int threshold = XP_TO_NEXT_LEVEL[data.level - 1];
            if (data.xp < threshold) break;
            data.levelUp(piglin.level().random);
            AurumKinetics.LOGGER.info("[Aurum] Уровень вырос до {} (xp={})", data.level, data.xp);
        }

        if (data.allOffersExhausted() && !data.restockAvailable) {
            data.restockAvailable = true;
        }

        data.save(piglin);
    }

    @Override public void notifyTradeUpdated(ItemStack s) { }
    @Override public int getVillagerXp() { return data.xp; }
    @Override public void overrideXp(int xp) { this.data.xp = xp; }
    @Override public boolean showProgressBar() { return true; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.PIGLIN_ADMIRING_ITEM; }
    @Override public boolean isClientSide() { return piglin.level().isClientSide; }
}
