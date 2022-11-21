package org.moon.figura.mixin;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.moon.figura.ducks.StyleSerializerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Style.Serializer.class)
public class StyleSerializerMixin {
    @Inject(
            method = "getClickEvent",
            at = @At(
                    value = "RETURN",
                    ordinal = 0
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    static private void disableScriptEvents(JsonObject root, CallbackInfoReturnable<ClickEvent> cir, JsonObject a, String s, ClickEvent.Action action) {
        if (action == ClickEvent.Action.getByName("script_event") && !StyleSerializerAccessor.allowScriptEvents) {
            cir.setReturnValue(null);
        }
    }
}
