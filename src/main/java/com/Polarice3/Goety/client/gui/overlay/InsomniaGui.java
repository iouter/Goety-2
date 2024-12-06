package com.Polarice3.Goety.client.gui.overlay;

import com.Polarice3.Goety.Goety;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class InsomniaGui {
    public static final IGuiOverlay OVERLAY = InsomniaGui::drawHUD;
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static boolean shouldDisplayBar(){
//        return minecraft.player != null && (minecraft.gameMode != null && minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR);
        return false;
    }

    public static void drawHUD(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
        if (minecraft.player == null){
            return;
        }
        if(!shouldDisplayBar()) {
            return;
        }

        StatsCounter stats = minecraft.player.getStats();
        int i = Mth.clamp(stats.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
        int xOffset = 10;
        int yOffset = 10;
        int bossBars = gui.getBossOverlay().events.size();
        if (bossBars > 0) {
            yOffset += Math.min(screenHeight / 3, 12 + 19 * bossBars);
        }
        int potionsActive = 0;

        for (MobEffectInstance mobEffectInstance : minecraft.player.getActiveEffects()) {
            if (mobEffectInstance.showIcon()) {
                ++potionsActive;
            }
        }

        yOffset += Math.min(potionsActive, 2) * 24;

        int eye = 0;
        if (i >= 72000){
            eye = 32;
        } else if (i >= 24000){
            eye = 16;
        }
        guiGraphics.blit(Goety.location("textures/gui/insomnia.png"), xOffset, yOffset, eye, 0, 16, 16, 48, 16);

    }
}
