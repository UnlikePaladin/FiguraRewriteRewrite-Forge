package org.moon.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.command.ForgeCommand;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.utils.FiguraText;

import java.nio.file.Path;

public class FiguraLoadCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> load = LiteralArgumentBuilder.literal("load");

        RequiredArgumentBuilder<CommandSourceStack, String> path = RequiredArgumentBuilder.argument("path", StringArgumentType.greedyString());
        path.executes(FiguraLoadCommand::loadAvatar);

        return load.then(path);
    }

    private static int loadAvatar(CommandContext<CommandSourceStack> context) {
        String str = StringArgumentType.getString(context, "path");
        try {
            //parse path
            Path p = LocalAvatarFetcher.getLocalAvatarDirectory().resolve(Path.of(str));

            //try to load avatar
            AvatarManager.loadLocalAvatar(p);
            context.getSource().sendSuccess(new FiguraText("command.load.loading"),false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(new FiguraText("command.load.invalid", str));
        }

        return 0;
    }
}
