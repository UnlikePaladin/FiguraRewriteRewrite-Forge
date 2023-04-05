package org.moon.figura.lua.api.keybind;

import net.minecraft.client.KeyMapping;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.mixin.input.KeyMappingAccessor;

import java.util.ArrayList;
import java.util.HashMap;

@LuaWhitelist
@LuaTypeDoc(
        name = "KeybindAPI",
        value = "keybinds"
)
public class KeybindAPI {

    public final ArrayList<FiguraKeybind> keyBindings = new ArrayList<>();
    public final Avatar owner;

    public KeybindAPI(Avatar owner) {
        this.owner = owner;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "name"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"name", "key"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class, Boolean.class},
                            argumentNames = {"name", "key", "gui"}
                    )
            },
            aliases = "of",
            value = "keybinds.new_keybind"
    )
    public FiguraKeybind newKeybind(@LuaNotNil String name, String key, boolean gui) {
        if (key == null) key = "key.keyboard.unknown";
        FiguraKeybind binding = new FiguraKeybind(this.owner, name, FiguraKeybind.parseStringKey(key)).gui(gui);
        this.keyBindings.add(binding);
        return binding;
    }

    @LuaWhitelist
    public FiguraKeybind of(@LuaNotNil String name, String key, boolean gui) {
        return newKeybind(name, key, gui);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "id"
            ),
            value = "keybinds.from_vanilla"
    )
    public FiguraKeybind fromVanilla(@LuaNotNil String id) {
        KeyMapping key = KeyMappingAccessor.getAll().get(id);
        if (key == null)
            throw new LuaError("Failed to find key: \"" + id + "\"");

        return newKeybind("[Vanilla] " + key.getTranslatedKeyMessage().getString(), key.saveString(), false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "id"
            ),
            value = "keybinds.get_vanilla_key"
    )
    public String getVanillaKey(@LuaNotNil String id) {
        KeyMapping key = KeyMappingAccessor.getAll().get(id);
        return key == null ? null : key.saveString();
    }

    @LuaWhitelist
    @LuaMethodDoc("keybinds.get_keybinds")
    public HashMap<String, FiguraKeybind> getKeybinds() {
        HashMap<String, FiguraKeybind> map = new HashMap<>();
        for (FiguraKeybind keyBinding : keyBindings)
            map.put(keyBinding.getName(), keyBinding);
        return map;
    }

    @Override
    public String toString() {
        return "KeybindAPI";
    }
}
