package org.moon.figura.mixin;

import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

// courtesy of https://github.com/SpongePowered/Mixin/issues/387
@Mixin(ClickEvent.Action.class)
public class ClickEventActionMixin {
    @Shadow
    @Final
    @Mutable
    private static ClickEvent.Action[] $VALUES;

    @Shadow
    @Final
    private static Map<String, ClickEvent.Action> LOOKUP;

    static {
        LOOKUP.put("script_event", figura$addVariant("SCRIPT_EVENT", "script_event", true));
    }

    @Invoker("<init>")
    public static ClickEvent.Action figura$invokeInit(String internalName, int internalId, String name, boolean user) {
        throw new AssertionError();
    }

    @SuppressWarnings({"SameParameterValue"}) // technically right, but it's ugly to hardcode values here
    private static ClickEvent.Action figura$addVariant(String internalName, String name, boolean user) {
        assert $VALUES != null;
        ArrayList<ClickEvent.Action> variants = new ArrayList<>(Arrays.asList($VALUES));
        ClickEvent.Action instrument = figura$invokeInit(internalName, variants.get(variants.size() - 1).ordinal() + 1, name, user);
        variants.add(instrument);
        $VALUES = variants.toArray(new ClickEvent.Action[0]);
        return instrument;
    }
}
