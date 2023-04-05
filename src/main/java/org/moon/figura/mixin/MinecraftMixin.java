package org.moon.figura.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.PopupMenu;
import org.moon.figura.gui.screens.WardrobeScreen;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow @Final public MouseHandler mouseHandler;
    @Shadow @Final public Options options;
    @Shadow public LocalPlayer player;
    @Shadow public Entity cameraEntity;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Unique
    private boolean scriptMouseUnlock = false;

    @Inject(at = @At("RETURN"), method = "handleKeybinds")
    private void handleKeybinds(CallbackInfo ci) {
        //dont handle keybinds on panic
        if (AvatarManager.panic)
            return;

        //reload avatar button
        if (Configs.RELOAD_BUTTON.keyBind.consumeClick()) {
            AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());
            FiguraToast.sendToast(new FiguraText("toast.reload"));
        }

        //reload avatar button
        if (Configs.WARDROBE_BUTTON.keyBind.consumeClick())
            this.setScreen(new WardrobeScreen(null));

        //action wheel button
        Boolean wheel = null;
        if (Configs.ACTION_WHEEL_MODE.value % 2 == 1) {
            if (Configs.ACTION_WHEEL_BUTTON.keyBind.consumeClick())
                wheel = !ActionWheel.isEnabled();
        } else if (Configs.ACTION_WHEEL_BUTTON.keyBind.isDown()) {
            wheel = true;
        } else if (ActionWheel.isEnabled()) {
            wheel = false;
        }
        if (wheel != null) {
            if (wheel) {
                ActionWheel.setEnabled(true);
                this.mouseHandler.releaseMouse();
            } else {
                if (Configs.ACTION_WHEEL_MODE.value >= 2)
                    ActionWheel.execute(ActionWheel.getSelected(), true);
                ActionWheel.setEnabled(false);
                this.mouseHandler.grabMouse();
            }
        }

        //popup menu button
        if (Configs.POPUP_BUTTON.keyBind.isDown()) {
            PopupMenu.setEnabled(true);

            if (!PopupMenu.hasEntity()) {
                Entity target = FiguraMod.extendedPickEntity;
                if (this.player != null && target instanceof Player && !target.isInvisibleTo(this.player)) {
                    PopupMenu.setEntity(target);
                } else if (!this.options.getCameraType().isFirstPerson()) {
                    PopupMenu.setEntity(this.cameraEntity);
                }
            }

            for (int i = this.options.keyHotbarSlots.length - 1; i >= 0; i--) {
                if (this.options.keyHotbarSlots[i].isDown()) {
                    PopupMenu.hotbarKeyPressed(i);
                    break;
                }
            }
        } else if (PopupMenu.isEnabled()) {
            PopupMenu.run();
        }

        //unlock cursor :p
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.luaRuntime != null && avatar.luaRuntime.host.unlockCursor) {
            this.mouseHandler.releaseMouse();
            scriptMouseUnlock = true;
        } else if (scriptMouseUnlock) {
            this.mouseHandler.grabMouse();
            scriptMouseUnlock = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "setScreen")
    private void setScreen(Screen screen, CallbackInfo ci) {
        if (ActionWheel.isEnabled())
            ActionWheel.setEnabled(false);

        if (PopupMenu.isEnabled())
            PopupMenu.run();
    }

    @Inject(at = @At("RETURN"), method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V")
    private void clearLevel(Screen screen, CallbackInfo ci) {
        AvatarManager.clearAllAvatars();
        FiguraLuaPrinter.clearPrintQueue();
        NetworkStuff.unsubscribeAll();
    }

    @Inject(at = @At("RETURN"), method = "setLevel")
    private void setLevel(ClientLevel world, CallbackInfo ci) {
        NetworkStuff.auth();
    }

    @Inject(at = @At("HEAD"), method = "runTick")
    private void preTick(boolean tick, CallbackInfo ci) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler("applyBBAnimations");
        AvatarManager.applyAnimations();
        FiguraMod.popProfiler(2);
    }

    @Inject(at = @At("RETURN"), method = "runTick")
    private void afterTick(boolean tick, CallbackInfo ci) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler("clearBBAnimations");
        AvatarManager.clearAnimations();
        FiguraMod.popProfiler(2);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void startTick(CallbackInfo ci) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.tick();
        FiguraMod.popProfiler();
    }
}
