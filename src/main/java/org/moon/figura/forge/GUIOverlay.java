package org.moon.figura.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.gui.PaperDoll;
import org.moon.figura.gui.PopupMenu;

public class GUIOverlay implements IIngameOverlay {

    @Override
    public void render(ForgeIngameGui gui, PoseStack stack, float partialTick, int screenWidth, int screenHeight) {
        if (AvatarManager.panic)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        FiguraMod.pushProfiler("popupMenu");
        PopupMenu.render(stack);

        FiguraMod.popPushProfiler("paperdoll");
        PaperDoll.render(stack);

        FiguraMod.popProfiler();

        //get avatar
        Entity entity = Minecraft.getInstance().getCameraEntity();
        Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);

        if (avatar != null) {
            //hud parent type
            avatar.hudRender(stack, Minecraft.getInstance().renderBuffers().bufferSource(), entity, partialTick);

            //hud hidden by script
            if (avatar.luaRuntime != null) {
                //render wheel
                FiguraMod.pushProfiler("actionWheel");
                ActionWheel.render(stack);
                FiguraMod.popProfiler();

                //cancel this method
                return;
            }
        }

        FiguraMod.popProfiler();
    }
}