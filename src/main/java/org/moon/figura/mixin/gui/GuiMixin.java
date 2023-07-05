package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.gui.PaperDoll;
import org.moon.figura.gui.PopupMenu;
import org.moon.figura.lua.api.RendererAPI;
import org.moon.figura.math.vector.FiguraVec2;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow @Final private Minecraft minecraft;
    @Unique private FiguraVec2 crosshairOffset;

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    private void renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        crosshairOffset = null;

        if (ActionWheel.isEnabled()) {
            ci.cancel();
            return;
        }

        Entity entity = this.minecraft.getCameraEntity();
        Avatar avatar;
        if (entity == null || (avatar = AvatarManager.getAvatar(entity)) == null || avatar.luaRuntime == null)
            return;

        RendererAPI renderer = avatar.luaRuntime.renderer;
        if (!renderer.renderCrosshair) {
            ci.cancel();
            return;
        }

        crosshairOffset = renderer.crosshairOffset;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"), method = "renderCrosshair")
    private void blitRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (crosshairOffset != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(crosshairOffset.x, crosshairOffset.y, 0d);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", shift = At.Shift.AFTER), method = "renderCrosshair")
    private void afterBlitRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (crosshairOffset != null)
            guiGraphics.pose().popPose();
    }
}
