package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;

public class FiguraLinkCommand {

    public enum LINK {
        WIKI("[Wiki]", "https://github.com/Kingdom-Of-The-Moon/FiguraRewriteRewrite/wiki/", ColorUtils.Colors.FRAN_PINK.style),
        KOFI("[Ko-fi]", "https://ko-fi.com/francy_chan", 0x27AAE0),
        Space1,
        DISCORD("[Discord]", "https://discord.gg/ekHGHcH8Af", ColorUtils.Colors.DISCORD.style),
        REDDIT("[Reddit]", "https://www.reddit.com/r/Figura", 0xFF4400),
        Space2,
        MODRINTH("[Modrinth]", "https://modrinth.com/mod/figura/", 0x1BD96A),
        CURSEFORGE("[Curseforge]", "https://www.curseforge.com/minecraft/mc-mods/figura", 0xF16436);

        public final String name;
        public final String url;
        public final Style style;
        public final boolean isSpace;

        LINK(String name, String url, int color) {
            this(name, url, Style.EMPTY.withColor(color));
        }
        LINK(String name, String url, Style style) {
            this.name = name;
            this.url = url;
            this.style = style;
            this.isSpace = name == null;
        }
        LINK() {
            this(null, null, null);
        }
    }

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        //get links
        LiteralArgumentBuilder<CommandSourceStack> links = LiteralArgumentBuilder.literal("links");
        links.executes(context -> {
            //header
            MutableComponent message = Component.empty().withStyle(ColorUtils.Colors.FRAN_PINK.style)
                    .append(Component.literal("•*+•* ")
                            .append(FiguraText.of())
                            .append(" Links *•+*•").withStyle(ChatFormatting.UNDERLINE))
                    .append("\n");

            //add links
            for (LINK link : LINK.values()) {
                message.append("\n");

                if (link.isSpace)
                    continue;

                message.append(Component.literal("• " + link.name)
                        .withStyle(link.style)
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.url)))
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(link.url)))));
            }

            FiguraMod.sendChatMessage(message);
            return 1;
        });

        return links;
    }
}
