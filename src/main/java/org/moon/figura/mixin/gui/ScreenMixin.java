package org.moon.figura.mixin.gui;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(
            method = "handleComponentClicked",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/network/chat/ClickEvent;getAction()Lnet/minecraft/network/chat/ClickEvent$Action;",
                    ordinal = 0
            ),
            cancellable = true
    )
    void a(Style style, CallbackInfoReturnable<Boolean> cir) {
        ClickEvent event = style.getClickEvent();
        // already null-checked before this injection
        assert event != null;
        if (event.getAction() == ClickEvent.Action.getByName("script_event")) {
            cir.setReturnValue(true);
            Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
            if (avatar == null || avatar.luaRuntime == null)
                return;
            avatar.chatComponentClickEvent(event.getValue());
        }
    }
}
