package com.aurumkinetics.mixin;

import com.aurumkinetics.event.SpeedupEvents;
import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {

    @Shadow private int brewTime;

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void aurum$speedupBrew(Level level, BlockPos pos, BlockState state,
                                          BrewingStandBlockEntity be, CallbackInfo ci) {
        SpeedupType type = SpeedupEvents.getSpeedup(be);
        if (type == null) return;
        int extra = (int) type.multiplier - 1;
        if (extra <= 0) return;
        BrewingStandBlockEntityMixin self = (BrewingStandBlockEntityMixin) (Object) be;
        if (self.brewTime > 0) {
            self.brewTime = Math.max(0, self.brewTime - extra);

            // Если brewTime только что стал 0 — засчитываем 3 бутылки
            if (self.brewTime == 0 && level instanceof ServerLevel sl) {
                var data = be.getPersistentData();
                int count = data.getInt("AurumBrewedBottles") + 3;
                data.putInt("AurumBrewedBottles", count);

                if (count >= 42) {
                    // Выдать достижение игроку, который в радиусе 8 блоков
                    for (ServerPlayer sp : sl.getEntitiesOfClass(ServerPlayer.class,
                            new net.minecraft.world.phys.AABB(pos).inflate(8))) {
                        com.aurumkinetics.event.SpeedupAdvancements.grant(sp, "skilled_brewer");
                    }
                    data.putInt("AurumBrewedBottles", 0);  // сброс
                }
            }
        }
    }
}
