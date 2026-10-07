package com.aurumkinetics.mixin;

import com.aurumkinetics.event.SpeedupEvents;
import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    /** Ускорение: делим время готовки. */
    @Inject(method = "getTotalCookTime", at = @At("RETURN"), cancellable = true)
    private static void aurum$speedupCookTime(Level level, AbstractFurnaceBlockEntity be,
                                              CallbackInfoReturnable<Integer> cir) {
        SpeedupType type = SpeedupEvents.getSpeedup(be);
        if (type == null) return;
        int original = cir.getReturnValue();
        cir.setReturnValue(Math.max(1, (int)(original / type.multiplier)));
    }

    /**
     * Золотой ускоритель: 10% шанс двойного результата.
     * Вызывается ПОСЛЕ того, как ванила положила результат в слот (в cookTimeLogic).
     * Мы инжектимся в метод, который называется в 1.20.1 "m_5830_" (cookTimeLogic) — не можем
     * без SRG. Используем хук на setItem в слоте результата.
     *
     * Обход: инжектимся в уже существующий метод getTotalCookTime — нет эффекта.
     * Альтернатива: инжект в "tick" (method = "m_7498_") — не можем без SRG.
     *
     * Поэтому: делаем обработку двойного выхода через событие на сервере (см. FurnaceOutputEvents).
     */
}
