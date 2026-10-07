package com.aurumkinetics.mixin.client;

import com.aurumkinetics.speedup.ClientSpeedupCache;
import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Стрелка прогресса печи: x = 79, y = 34, размер 24×16.
 * Центр: (91, 42). Рисуем «×N» чуть выше — y = 26.
 */
@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void aurum$renderSpeedup(GuiGraphics g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractFurnaceScreen<?> self = (AbstractFurnaceScreen<?>) (Object) this;
        int containerId = self.getMenu().containerId;

        SpeedupType type = ClientSpeedupCache.get(containerId);
        if (type == null) return;

        String text = type.displayName();
        int tw = self.getMinecraft().font.width(text);
        int x = self.getGuiLeft() + 91 - tw / 2;
        int y = self.getGuiTop() + 26;

        g.drawString(self.getMinecraft().font, text, x, y, type.color, false);
    }
}
