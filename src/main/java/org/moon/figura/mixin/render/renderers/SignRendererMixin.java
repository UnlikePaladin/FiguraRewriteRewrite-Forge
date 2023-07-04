package org.moon.figura.mixin.render.renderers;

import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.Emojis;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(SignRenderer.class)
public class SignRendererMixin {

    @Dynamic
    @ModifyArg(method = "lambda$render$2", at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;"), require = 0)
    private FormattedText modifyText(FormattedText charSequence) {
        return Configs.EMOJIS.value > 0 && charSequence instanceof Component text ? Emojis.applyEmojis(text) : charSequence;
    }
}
