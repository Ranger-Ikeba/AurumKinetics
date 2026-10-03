#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Патч 78: зачарованные книги с реальными энчантами (разные уровни)."""

from pathlib import Path

ROOT = Path.cwd()
JAVA = ROOT / "src/main/java/com/aurumkinetics"


def write_file(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print(f"[OK] {path.relative_to(ROOT)}")


write_file(JAVA / "trade/PiglinTradeData.java", """package com.aurumkinetics.trade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PiglinTradeData {

    public static final String KEY = "AurumPiglinTrade";

    public int level = 0;
    public int xp = 0;
    public int tradesCount = 0;
    public boolean tamed = false;
    public boolean restockAvailable = false;
    public MerchantOffers offers = new MerchantOffers();

    public record TradeDef(ItemStack cost, ItemStack secondCost, ItemStack result,
                           int maxUses, int xp, int rarity) {}

    private static ItemStack g(int n) { return new ItemStack(Items.GOLD_INGOT, n); }
    private static ItemStack e() { return ItemStack.EMPTY; }

    /** Создать книгу с указанным энчантом и уровнем. */
    private static ItemStack book(Enchantment ench, int lvl) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK, 1);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(ench, lvl));
        return stack;
    }

    /** Случайная книга из пула с случайным уровнем 1..maxLvl. */
    private static ItemStack randomBook(RandomSource rng, int maxTotalLvl) {
        Enchantment[] pool = {
                Enchantments.SOUL_SPEED,       // скорость души (1..3)
                Enchantments.SHARPNESS,        // сила (1..5)
                Enchantments.FIRE_PROTECTION,  // огнеупорность (1..4)
                Enchantments.ALL_DAMAGE_PROTECTION, // защита (1..4)
                Enchantments.PROJECTILE_PROTECTION, // защита от снарядов (1..4)
                Enchantments.UNBREAKING,       // прочность (1..3)
        };
        Enchantment ench = pool[rng.nextInt(pool.length)];
        int max = ench.getMaxLevel();
        int lvl = 1 + rng.nextInt(Math.min(max, maxTotalLvl));
        return book(ench, lvl);
    }

    private static List<TradeDef> pool(int lvl, RandomSource rng) {
        List<TradeDef> p = new ArrayList<>();
        if (lvl == 1) {
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.ARROW, 6),         12, 2, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.STRING, 3),        12, 2, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.FEATHER, 2),       12, 2, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.LEATHER, 1),       12, 2, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.PORKCHOP, 2),      12, 2, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.CHAIN, 1),         12, 2, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.CRIMSON_ROOTS, 2), 12, 2, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.WARPED_ROOTS, 2),  12, 2, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.SOUL_SAND, 1),     10, 3, 0));
            p.add(new TradeDef(g(1), e(), new ItemStack(Items.GRAVEL, 4),        10, 3, 0));
            p.add(new TradeDef(g(1), new ItemStack(Items.FLINT, 1),
                    new ItemStack(Items.ARROW, 12), 12, 3, 0));
        }
        if (lvl == 2) {
            p.add(new TradeDef(g(2), e(), new ItemStack(Items.QUARTZ, 3),        10, 5, 0));
            p.add(new TradeDef(g(2), e(), new ItemStack(Items.NETHER_WART, 2),   10, 5, 0));
            p.add(new TradeDef(g(2), e(), new ItemStack(Items.NETHER_BRICK, 4),  10, 5, 0));
            p.add(new TradeDef(g(3), e(), new ItemStack(Items.COOKED_PORKCHOP, 3),10, 5, 0));
            p.add(new TradeDef(g(3), e(), new ItemStack(Items.SPECTRAL_ARROW, 4),10, 5, 0));
            p.add(new TradeDef(g(3), e(), new ItemStack(Items.BLACKSTONE, 4),    10, 5, 0));
            p.add(new TradeDef(g(3), e(), new ItemStack(Items.MAGMA_CREAM, 1),   8,  6, 0));
            p.add(new TradeDef(g(4), e(), new ItemStack(Items.GILDED_BLACKSTONE, 1), 6, 8, 1));
            p.add(new TradeDef(g(2), new ItemStack(Items.QUARTZ, 1),
                    new ItemStack(Items.EXPERIENCE_BOTTLE, 1), 8, 6, 0));
            // Простые книги 1 уровня
            p.add(new TradeDef(g(6), new ItemStack(Items.BOOK, 1),
                    book(Enchantments.UNBREAKING, 1), 4, 8, 1));
        }
        if (lvl == 3) {
            p.add(new TradeDef(g(4), e(), new ItemStack(Items.IRON_INGOT, 1),     8, 8, 0));
            p.add(new TradeDef(g(4), e(), new ItemStack(Items.BLAZE_POWDER, 2),   8, 8, 0));
            p.add(new TradeDef(g(5), e(), new ItemStack(Items.BLAZE_ROD, 1),      6, 10, 0));
            p.add(new TradeDef(g(5), e(), new ItemStack(Items.ENDER_PEARL, 1),    6, 10, 0));
            p.add(new TradeDef(g(6), e(), new ItemStack(Items.OBSIDIAN, 1),       6, 10, 0));
            p.add(new TradeDef(g(7), e(), new ItemStack(Items.CRYING_OBSIDIAN, 1),5, 12, 0));
            p.add(new TradeDef(g(8), e(), new ItemStack(Items.GHAST_TEAR, 1),     4, 14, 1));
            p.add(new TradeDef(g(3), new ItemStack(Items.LEATHER, 2),
                    new ItemStack(Items.BOOK, 1), 8, 6, 0));
            // Книги: защита от снарядов 1-2, огнеупорность 1-2
            p.add(new TradeDef(g(8), new ItemStack(Items.BOOK, 1),
                    book(Enchantments.PROJECTILE_PROTECTION, 1 + rng.nextInt(2)), 4, 12, 0));
            p.add(new TradeDef(g(8), new ItemStack(Items.BOOK, 1),
                    book(Enchantments.FIRE_PROTECTION, 1 + rng.nextInt(2)), 4, 12, 0));
        }
        if (lvl == 4) {
            p.add(new TradeDef(g(8),  e(), new ItemStack(Items.EXPERIENCE_BOTTLE, 4), 6, 12, 0));
            p.add(new TradeDef(g(10), e(), new ItemStack(Items.GOLDEN_CARROT, 3),     5, 14, 0));
            p.add(new TradeDef(g(12), e(), new ItemStack(Items.DIAMOND, 1),           4, 16, 0));
            p.add(new TradeDef(g(10), new ItemStack(Items.BOOK, 1),
                    book(Enchantments.ALL_DAMAGE_PROTECTION, 2 + rng.nextInt(3)), 3, 18, 0));
            p.add(new TradeDef(g(12), new ItemStack(Items.BOOK, 1),
                    book(Enchantments.SHARPNESS, 2 + rng.nextInt(3)), 3, 18, 0));
            p.add(new TradeDef(g(15), e(), new ItemStack(Items.GOLDEN_APPLE, 1), 3, 20, 1));
        }
        if (lvl == 5) {
            p.add(new TradeDef(g(15), e(), new ItemStack(Items.EXPERIENCE_BOTTLE, 8), 6, 15, 0));
            p.add(new TradeDef(g(18), e(), new ItemStack(Items.DIAMOND, 1),           3, 20, 0));
            p.add(new TradeDef(g(20), new ItemStack(Items.BOOK, 1),
                    book(Enchantments.SHARPNESS, 4 + rng.nextInt(2)), 3, 25, 0));
            p.add(new TradeDef(g(22), new ItemStack(Items.BOOK, 1),
                    book(Enchantments.ALL_DAMAGE_PROTECTION, 3 + rng.nextInt(2)), 2, 28, 0));
            p.add(new TradeDef(g(24), new ItemStack(Items.BOOK, 1),
                    book(Enchantments.SOUL_SPEED, 2 + rng.nextInt(2)), 2, 30, 1));
            p.add(new TradeDef(g(28), e(), new ItemStack(Items.EXPERIENCE_BOTTLE, 12), 2, 40, 2));
        }
        return p;
    }

    public static PiglinTradeData get(net.minecraft.world.entity.Mob piglin) {
        CompoundTag root = piglin.getPersistentData();
        PiglinTradeData data = new PiglinTradeData();
        if (root.contains(KEY)) data.load(root.getCompound(KEY));
        return data;
    }

    public static PiglinTradeData getOrCreate(net.minecraft.world.entity.Mob piglin, RandomSource rng) {
        CompoundTag root = piglin.getPersistentData();
        PiglinTradeData data = new PiglinTradeData();
        if (root.contains(KEY)) {
            data.load(root.getCompound(KEY));
        } else {
            data.level = 1;
            data.addOffersForLevel(rng, 1);
            data.save(piglin);
        }
        return data;
    }

    public void save(net.minecraft.world.entity.Mob piglin) {
        piglin.getPersistentData().put(KEY, write());
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Level", level);
        tag.putInt("Xp", xp);
        tag.putInt("Trades", tradesCount);
        tag.putBoolean("Tamed", tamed);
        tag.putBoolean("RestockAvailable", restockAvailable);
        ListTag list = new ListTag();
        for (MerchantOffer o : offers) list.add(o.createTag());
        tag.put("Offers", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        level = tag.getInt("Level");
        xp = tag.getInt("Xp");
        tradesCount = tag.getInt("Trades");
        tamed = tag.getBoolean("Tamed");
        restockAvailable = tag.getBoolean("RestockAvailable");
        offers = new MerchantOffers();
        if (tag.contains("Offers")) {
            ListTag list = tag.getList("Offers", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++)
                offers.add(new MerchantOffer(list.getCompound(i)));
        }
    }

    private Set<Item> currentResults() {
        Set<Item> set = new HashSet<>();
        for (MerchantOffer o : offers) {
            if (!o.getResult().isEmpty()) set.add(o.getResult().getItem());
        }
        return set;
    }

    public void addOffersForLevel(RandomSource rng, int lvl) {
        int count = 2;
        List<TradeDef> p = new ArrayList<>(pool(lvl, rng));
        for (int i = p.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            TradeDef t = p.get(i);
            p.set(i, p.get(j));
            p.set(j, t);
        }
        Set<Item> used = currentResults();
        int added = 0;
        for (TradeDef def : p) {
            if (added >= count) break;
            // Проверка уникальности по описанию (item + enchants)
            boolean dup = false;
            for (MerchantOffer existing : offers) {
                if (ItemStack.isSameItemSameTags(existing.getResult(), def.result())) {
                    dup = true;
                    break;
                }
            }
            if (dup) continue;
            offers.add(new MerchantOffer(def.cost(), def.secondCost(), def.result(),
                    def.maxUses(), def.xp(), 0.05f));
            added++;
        }
    }

    public void levelUp(RandomSource rng) {
        if (level >= 5) return;
        level++;
        addOffersForLevel(rng, level);
    }

    public boolean allOffersExhausted() {
        if (offers.isEmpty()) return false;
        for (MerchantOffer o : offers) {
            if (o.getUses() < o.getMaxUses()) return false;
        }
        return true;
    }

    public void restock() {
        for (MerchantOffer o : offers) {
            o.resetUses();
        }
        restockAvailable = false;
    }
}
""")

print("\nПатч 78 применён.")