package com.aurumkinetics.event;

import com.aurumkinetics.AurumKinetics;
import com.aurumkinetics.entity.GoldCoatingEntity;
import com.aurumkinetics.item.GoldCoatingItem;
import com.aurumkinetics.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = AurumKinetics.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack stack = event.getItemStack();

        GoldCoatingEntity coating = findCoatingAt(level, player, event.getHitVec());
        if (coating == null) return;

        // Shift + ПКМ → снять
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ItemStack drop = new ItemStack(switch (coating.getCoatingType()) {
                    case QUARTZ -> ModItems.QUARTZ_COATING.get();
                    case BLUE -> ModItems.BLUE_COATING.get();
                    default -> ModItems.GOLD_COATING.get();
                });
                if (!player.getInventory().add(drop)) player.drop(drop, false);
                coating.discard();
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        // Улучшение: кварц / лазурит, только из GOLD
        if (coating.getCoatingType() == GoldCoatingItem.Type.GOLD) {
            GoldCoatingItem.Type newType = null;
            String advName = null;
            if (stack.is(Items.QUARTZ)) { newType = GoldCoatingItem.Type.QUARTZ; advName = "moon_glow"; }
            else if (stack.is(Items.LAPIS_LAZULI)) { newType = GoldCoatingItem.Type.BLUE; advName = "enchantment"; }

            if (newType != null) {
                if (!level.isClientSide) {
                    BlockPos pos = coating.getAttachedPos();
                    var face = coating.getFacing();
                    coating.discard();
                    GoldCoatingEntity upgraded = new GoldCoatingEntity(
                            com.aurumkinetics.registry.ModEntities.GOLD_COATING.get(), level);
                    upgraded.attachTo(pos, face, newType);
                    level.addFreshEntity(upgraded);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    player.displayClientMessage(
                            Component.translatable("message.aurum_kinetics.coating_enhanced"), true);
                    if (player instanceof ServerPlayer sp) {
                        grantAdvancement(sp, advName);
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    private static void grantAdvancement(ServerPlayer player, String name) {
        if (name == null) return;
        ResourceLocation id = new ResourceLocation(AurumKinetics.MOD_ID, name);
        Advancement adv = player.server.getAdvancements().getAdvancement(id);
        if (adv != null) {
            player.getAdvancements().award(adv, "impossible");
        }
    }

    private static GoldCoatingEntity findCoatingAt(Level level, Player player, BlockHitResult hit) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f).scale(4.0);
        Vec3 end = eye.add(look);
        AABB box = player.getBoundingBox().expandTowards(look).inflate(1.0);
        EntityHitResult ehr = ProjectileUtil.getEntityHitResult(
                player, eye, end, box,
                e -> e instanceof GoldCoatingEntity, 16.0f);
        if (ehr != null && ehr.getEntity() instanceof GoldCoatingEntity gce) return gce;
        return null;
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (level.getGameTime() % 10 != 0) return;

        for (Player player : level.players()) {
            AABB area = player.getBoundingBox().inflate(32);
            List<GoldCoatingEntity> coatings = level.getEntitiesOfClass(GoldCoatingEntity.class, area);
            if (coatings.isEmpty()) continue;

            boolean isDay = level.isDay();
            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, area);

            for (Mob mob : mobs) {
                for (GoldCoatingEntity c : coatings) {
                    if (mob.distanceToSqr(c.position()) > 12 * 12) continue;

                    GoldCoatingItem.Type type = c.getCoatingType();
                    if (type == GoldCoatingItem.Type.BLUE) {
                        if (isAttractable(mob)) moveToward(mob, c);
                        break;
                    }

                    if (!isHostile(mob)) continue;

                    boolean scare = false;
                    if (type == GoldCoatingItem.Type.QUARTZ) {
                        scare = true;
                    } else if (type == GoldCoatingItem.Type.GOLD) {
                        scare = isDay && isUndeadOrArthropod(mob);
                    }

                    if (scare) { scareAway(mob, c); break; }
                }
            }
        }
    }

    private static boolean isHostile(Mob mob) {
        return mob instanceof Enemy;
    }

    private static boolean isAttractable(Mob mob) {
        if (mob instanceof Enemy) return true;
        if (mob instanceof Piglin) return true;
        if (mob instanceof PiglinBrute) return true;
        if (mob instanceof Villager) return true;
        if (mob instanceof Vindicator) return true;
        if (mob instanceof Pillager) return true;
        return false;
    }

    private static boolean isUndeadOrArthropod(Mob mob) {
        if (mob.getMobType() == MobType.UNDEAD) return true;
        if (mob.getMobType() == MobType.ARTHROPOD) return true;
        if (mob instanceof Creeper) return true;
        return false;
    }

    private static void scareAway(Mob mob, GoldCoatingEntity coating) {
        if (mob.isDeadOrDying()) return;
        Vec3 away = mob.position().subtract(coating.position());
        if (away.lengthSqr() < 0.001) away = new Vec3(1, 0, 0);
        away = away.normalize().scale(8);
        Vec3 target = mob.position().add(away);
        mob.getNavigation().moveTo(target.x, target.y, target.z, 1.4);
    }

    private static void moveToward(Mob mob, GoldCoatingEntity coating) {
        if (mob.isDeadOrDying()) return;
        Vec3 target = coating.position();
        mob.getNavigation().moveTo(target.x, target.y, target.z, 1.0);
    }
}
