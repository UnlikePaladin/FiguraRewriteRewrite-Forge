package org.moon.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.ClientCommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.lua.FiguraLuaRuntime;

public class FiguraRunCommand {

    public static boolean canRun = false;

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> run = LiteralArgumentBuilder.literal("run");
        RequiredArgumentBuilder<CommandSourceStack, String> arg = RequiredArgumentBuilder.argument("code", StringArgumentType.greedyString());
        arg.executes(FiguraRunCommand::executeCode);
        run.then(arg);
        return run;
    }

    private static int executeCode(CommandContext<CommandSourceStack> context) {
        if (!canRun)
            return 0;

        String lua = StringArgumentType.getString(context, "code");
        FiguraLuaRuntime luaRuntime = FiguraCommands.getRuntime(context);
        if (luaRuntime == null)
            return 0;

        try {
            luaRuntime.load("runCommand", lua).call();
            return 1;
        } catch (Exception | StackOverflowError e) {
            FiguraLuaPrinter.sendLuaError(FiguraLuaRuntime.parseError(e), luaRuntime.owner);
            return 0;
        }
    }
}
