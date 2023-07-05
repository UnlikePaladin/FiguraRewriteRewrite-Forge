package org.moon.figura.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.ActionWheel;

public class GUIActionWheelOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (AvatarManager.panic)
            return;

        //render wheel last, on top of everything
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler("actionWheel");

        ActionWheel.render(guiGraphics);

        FiguraMod.popProfiler(2);
    }
}
