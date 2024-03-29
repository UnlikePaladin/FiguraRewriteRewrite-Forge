package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.moon.figura.ducks.SuggestionsListAccessor;
import org.moon.figura.gui.Emojis;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionsListMixin implements SuggestionsListAccessor {

    @Shadow @Final private Rect2i rect;

    @Unique private boolean figuraList;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFI)I"), index = 2)
    private float voidTextDraw(PoseStack stack, String text, float x, float y, int color) {
        if (!figuraList)
            return x;
        Font font = Minecraft.getInstance().font;

        //get emoji
        Component emoji = Emojis.applyEmojis(Component.literal(text));

        //dont render if no emoji was applied
        if (emoji.getString().equals(text))
            return x;

        //render emoji
        font.drawShadow(stack, emoji, x + 4 - (int) (font.width(emoji) / 2f), y, color);

        //change text x
        return (x + 8 + font.width(" "));
    }

    @Override
    @Intrinsic
    public void figura$setFiguraList(boolean bool) {
        figuraList = bool;
        if (bool) this.rect.setWidth(this.rect.getWidth() + 8 + Minecraft.getInstance().font.width(" "));
    }
}
