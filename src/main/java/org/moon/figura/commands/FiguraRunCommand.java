package org.moon.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.ClientCommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.lua.FiguraLuaRuntime;
import org.moon.figura.utils.FiguraText;

public class FiguraRunCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> run = LiteralArgumentBuilder.literal("run");
        RequiredArgumentBuilder<CommandSourceStack, String> arg = RequiredArgumentBuilder.argument("code", StringArgumentType.greedyString());
        arg.executes(FiguraRunCommand::executeCode);
        run.then(arg);
        return run;
    }

    private static int executeCode(CommandContext<CommandSourceStack> context) {
        String lua = StringArgumentType.getString(context, "code");
        Avatar localAvatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (localAvatar == null) {
            context.getSource().sendFailure(FiguraText.of("command.run.not_local_error"));
            return 0;
        }
        if (localAvatar.luaRuntime == null || localAvatar.scriptError) {
            context.getSource().sendFailure(FiguraText.of("command.run.no_script_error"));
            return 0;
        }

        try {
            localAvatar.luaRuntime.load("runCommand", lua).call();
            return 1;
        } catch (Exception | StackOverflowError e) {
            FiguraLuaPrinter.sendLuaError(FiguraLuaRuntime.parseError(e), localAvatar);
            return 0;
        }
    }
}
