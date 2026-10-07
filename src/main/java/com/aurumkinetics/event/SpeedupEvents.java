package com.aurumkinetics.event;

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
