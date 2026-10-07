package com.aurumkinetics.mixin.client;

import com.aurumkinetics.speedup.ClientSpeedupCache;
import com.aurumkinetics.speedup.SpeedupType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Стрелка зельеварки: x = 97, y = 16, размер 9×28.
 * Центр: (101, 30). Рисуем «×N» справа — x = 108.
 */
@Mixin(BrewingStandScreen.class)
public abstract class BrewingStandScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void aurum$renderSpeedup(GuiGraphics g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        BrewingStandScreen self = (BrewingStandScreen) (Object) this;
        int containerId = self.getMenu().containerId;

        SpeedupType type = ClientSpeedupCache.get(containerId);
        if (type == null) return;

        String text = type.displayName();
        int x = self.getGuiLeft() + 108;
        int y = self.getGuiTop() + 26;

        g.drawString(self.getMinecraft().font, text, x, y, type.color, false);
    }
}
