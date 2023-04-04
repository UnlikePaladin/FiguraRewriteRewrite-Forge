package org.moon.figura.config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.CacheAvatarLoader;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.config.ConfigType.*;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.permissions.PermissionManager;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Configs {

    //mod config version
    //only change this if you rename old configs
    public static final int CONFIG_VERSION = 1;

    //config update hashmap; <version number, <actual config, old config name>>
    public static final HashMap<Integer, HashMap<ConfigType<?>, String>> CONFIG_UPDATES = new HashMap<>();

    //code to run when the config is initialized
    public static void init() {
        //test for unused configs
        if (FiguraMod.DEBUG_MODE) {
            Category debug = new Category("debug");
            new ColorConfig("color_test", debug, 0xFF72AD);
            new StringConfig("string_test", debug, "text");
            new IntConfig("int_test", debug, 2147483647);
        }
    }


    // -- categories -- //


    public static final Category
            NAMEPLATE = new Category("nameplate"),
            SCRIPT = new Category("script"),
            RENDERING = new Category("rendering"),
            ACTION_WHEEL = new Category("action_wheel"),
            UI = new Category("ui"),
            PAPERDOLL = new Category("paperdoll"),
            MISC = new Category("misc"),
            DEV = new Category("dev") {{
                this.name = this.name.copy().withStyle(ChatFormatting.RED);
            }};


    // -- nameplate -- //


    public static final BoolConfig
            SELF_NAMEPLATE = new BoolConfig("self_nameplate", NAMEPLATE, false),
            PREVIEW_NAMEPLATE = new BoolConfig("preview_nameplate", NAMEPLATE, true);
    public static final EnumConfig
            NAMEPLATE_RENDER = new EnumConfig("nameplate_render", NAMEPLATE, 0, 3),
            CHAT_NAMEPLATE = new EnumConfig("chat_nameplate", NAMEPLATE, 2, 3) {{
                String path = "config.nameplate_level";
                this.enumTooltip = FiguraText.of(path + ".enum");
                this.enumList = List.of(
                        FiguraText.of(path + ".1"),
                        FiguraText.of(path + ".2"),
                        FiguraText.of(path + ".3")
                );
            }},
            ENTITY_NAMEPLATE = new EnumConfig("entity_nameplate", NAMEPLATE, 2, 3) {{
                String path = "config.nameplate_level";
                this.enumTooltip = FiguraText.of(path + ".enum");
                this.enumList = List.of(
                        FiguraText.of(path + ".1"),
                        FiguraText.of(path + ".2"),
                        FiguraText.of(path + ".3")
                );
            }},
            LIST_NAMEPLATE = new EnumConfig("list_nameplate", NAMEPLATE, 2, 3) {{
                String path = "config.nameplate_level";
                this.enumTooltip = FiguraText.of(path + ".enum");
                this.enumList = List.of(
                        FiguraText.of(path + ".1"),
                        FiguraText.of(path + ".2"),
                        FiguraText.of(path + ".3")
                );
            }};


    // -- script -- //

    public static final EnumConfig
            LOG_LOCATION = new EnumConfig("log_location", SCRIPT, 0, 2),
            FORMAT_SCRIPT = new EnumConfig("format_script", SCRIPT, 1, 3) {
                {
                    String tooltip = "config.format_script.tooltip.";
                    this.tooltip = FiguraText.of(tooltip + "1")
                            .append("\n")
                            .append(FiguraText.of(tooltip + "2").withStyle(ChatFormatting.RED));
                }

                @Override
                public void onChange() {
                    if (!AvatarManager.localUploaded)
                        AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());
                }
            };
    public static final PositiveIntConfig
            LOG_NUMBER_LENGTH = new PositiveIntConfig("log_number_length", SCRIPT, 5) {
                @Override
                public void onChange() {
                    super.onChange();
                    FiguraLuaPrinter.updateDecimalFormatting();
                }
            };


    // -- RENDERING -- //


    public static final EnumConfig
            IRIS_COMPATIBILITY_FIX = new EnumConfig("iris_compatibility_fix", RENDERING, 2, 3),
            RENDER_DEBUG_PARTS_PIVOT = new EnumConfig("render_debug_parts_pivot", RENDERING, 1, 5) {{
                    String tooltip = "config.render_debug_parts_pivot.tooltip";
                    this.tooltip = FiguraText.of(tooltip,
                            FiguraText.of(tooltip + ".cubes").setStyle(ColorUtils.Colors.FRAN_PINK.style),
                            FiguraText.of(tooltip + ".groups").setStyle(ColorUtils.Colors.MAYA_BLUE.style));
            }};
    public static final BoolConfig
            ALLOW_FP_HANDS = new BoolConfig("allow_fp_hands", RENDERING, false),
            FIRST_PERSON_MATRICES = new BoolConfig("first_person_matrices", RENDERING, true);


    // -- ACTION WHEEL -- //


    public static final KeybindConfig
            ACTION_WHEEL_BUTTON = new KeybindConfig("action_wheel_button", ACTION_WHEEL, "key.keyboard.b");
    public static final EnumConfig
            ACTION_WHEEL_MODE = new EnumConfig("action_wheel_mode", ACTION_WHEEL, 0, 4);
    public static final PositiveFloatConfig
            ACTION_WHEEL_SCALE = new PositiveFloatConfig("action_wheel_scale", ACTION_WHEEL, 1f);
    public static final EnumConfig
            ACTION_WHEEL_TITLE = new EnumConfig("action_wheel_title", ACTION_WHEEL, 0, 5),
            ACTION_WHEEL_SLOTS_INDICATOR = new EnumConfig("action_wheel_slots_indicator", ACTION_WHEEL, 0, 3);
    public static final BoolConfig
            ACTION_WHEEL_DECORATIONS = new BoolConfig("action_wheel_decorations", ACTION_WHEEL, true);


    // -- UI -- //


    public static final BoolConfig
            FIGURA_INVENTORY = new BoolConfig("figura_inventory", UI, true),
            PREVIEW_HEAD_ROTATION = new BoolConfig("preview_head_rotation", UI, false),
            AVATAR_PORTRAITS = new BoolConfig("avatar_portraits", UI, false) {{
                this.disabled = true;
            }},
            WARDROBE_FILE_NAMES = new BoolConfig("wardrobe_file_names", UI, false);
    public static final FloatConfig
            BACKGROUND_SCROLL_SPEED = new FloatConfig("background_scroll_speed", UI, 1f);
    public static final PositiveFloatConfig
            POPUP_SCALE = new PositiveFloatConfig("popup_scale", UI, 1f),
            POPUP_MIN_SIZE = new PositiveFloatConfig("popup_min_size", UI, 1f),
            POPUP_MAX_SIZE = new PositiveFloatConfig("popup_max_size", UI, 6f),
            TOAST_TIME = new PositiveFloatConfig("toast_time", UI, 5f),
            TOAST_TITLE_TIME = new PositiveFloatConfig("toast_title_time", UI, 2f),
            TEXT_SCROLL_SPEED = new PositiveFloatConfig("text_scroll_speed", UI, 1f);
    public static final PositiveIntConfig
            TEXT_SCROLL_DELAY = new PositiveIntConfig("text_scroll_delay", UI, 20);


    // -- PAPERDOLL -- //


    public static final BoolConfig
            HAS_PAPERDOLL = new BoolConfig("has_paperdoll", PAPERDOLL, false),
            PAPERDOLL_ALWAYS_ON = new BoolConfig("paperdoll_always_on", PAPERDOLL, false),
            FIRST_PERSON_PAPERDOLL = new BoolConfig("first_person_paperdoll", PAPERDOLL, true),
            PAPERDOLL_INVISIBLE = new BoolConfig("paperdoll_invisible", PAPERDOLL, false);
    public static final FloatConfig
            PAPERDOLL_SCALE = new FloatConfig("paperdoll_scale", PAPERDOLL, 1f),
            PAPERDOLL_X = new FloatConfig("paperdoll_x", PAPERDOLL, 0f),
            PAPERDOLL_Y = new FloatConfig("paperdoll_y", PAPERDOLL, 0f),
            PAPERDOLL_PITCH = new FloatConfig("paperdoll_pitch", PAPERDOLL, 0f),
            PAPERDOLL_YAW = new FloatConfig("paperdoll_yaw", PAPERDOLL, 20);


    // -- MISC -- //


    public static final KeybindConfig
            POPUP_BUTTON = new KeybindConfig("popup_button", MISC, "key.keyboard.r"),
            RELOAD_BUTTON = new KeybindConfig("reload_button", MISC, "key.keyboard.unknown"),
            PANIC_BUTTON = new KeybindConfig("panic_button", MISC, "key.keyboard.unknown"),
            WARDROBE_BUTTON = new KeybindConfig("wardrobe_button", MISC, "key.keyboard.unknown");
    public static final EnumConfig
            BUTTON_LOCATION = new EnumConfig("button_location", MISC, 0, 5),
            UPDATE_CHANNEL = new EnumConfig("update_channel", MISC, 1, 3) {
                @Override
                public void onChange() {
                    super.onChange();
                    NetworkStuff.checkVersion();
                }
            },
            DEFAULT_PERMISSION_LEVEL = new EnumConfig("default_permission_level", MISC, 2, Permissions.Category.values().length) {{
                List<Component> list = new ArrayList<>();
                Permissions.Category[] categories = Permissions.Category.values();
                for (Permissions.Category category : categories)
                    list.add(category.text.copy());
                this.enumList = list;
                this.enumTooltip = null;
            }
                @Override
                public void onChange() {
                    super.onChange();
                    PermissionManager.saveToDisk();
                }
            };
    public static final BoolConfig
            CHAT_EMOJIS = new BoolConfig("chat_emojis", MISC, false),
            EASTER_EGGS = new BoolConfig("easter_eggs", MISC, true);


    // -- DEV -- //


    public static final BoolConfig
            CONNECTION_TOASTS = new BoolConfig("connection_toasts", DEV, true),
            LOG_OTHERS = new BoolConfig("log_others", DEV, false);
    public static final EnumConfig
            LOG_PINGS = new EnumConfig("log_pings", DEV, 0, 3);
    public static final BoolConfig
            SYNC_PINGS = new BoolConfig("sync_pings", DEV, false) {{
                String tooltip = "config.sync_pings.tooltip.";
                this.tooltip = FiguraText.of(tooltip + "1")
                        .append("\n")
                        .append(FiguraText.of(tooltip + "2").withStyle(ChatFormatting.RED));
            }},
            CHAT_MESSAGES = new BoolConfig("chat_messages", DEV, false) {{
                this.name = this.name.copy().withStyle(ChatFormatting.RED);
                String tooltip = "config.chat_messages.tooltip.";
                this.tooltip = FiguraText.of(tooltip + "1")
                        .append("\n\n")
                        .append(FiguraText.of(tooltip + "2").withStyle(ChatFormatting.RED))
                        .append("\n\n")
                        .append(FiguraText.of(tooltip + "3").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }};
    public static final FolderConfig
            MAIN_DIR = new FolderConfig("main_dir", DEV, "");
    public static final IPConfig
            SERVER_IP = new IPConfig("server_ip", DEV, "figura.moonlight-devs.org:25565") {
                @Override
                public void onChange() {
                    super.onChange();
                    NetworkStuff.reAuth();
                }
            };
    public static final ButtonConfig
            CLEAR_CACHE = new ButtonConfig("clear_cache", DEV, () -> {
                CacheAvatarLoader.clearCache();
                IOUtils.deleteCacheFiles("avatars.nbt", "settings.nbt", "saved_texture.png");
            });
}
