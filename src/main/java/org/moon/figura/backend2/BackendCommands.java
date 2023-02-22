package org.moon.figura.backend2;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.moon.figura.FiguraMod;

public class BackendCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        //root
        LiteralArgumentBuilder<CommandSourceStack> backend = LiteralArgumentBuilder.literal("backend2");

        //force backend connection
        LiteralArgumentBuilder<CommandSourceStack> connect = LiteralArgumentBuilder.literal("connect");
        connect.executes(context -> {
            NetworkStuff.reAuth();
            return 1;
        });

        backend.then(connect);

        //run
        LiteralArgumentBuilder<CommandSourceStack> run = LiteralArgumentBuilder.literal("run");
        run.executes(context -> runRequest(context, ""));

        RequiredArgumentBuilder<CommandSourceStack, String> request = RequiredArgumentBuilder.argument("request", StringArgumentType.greedyString());
        request.executes(context -> runRequest(context, StringArgumentType.getString(context, "request")));

        run.then(request);
        backend.then(run);

        //debug mode
        LiteralArgumentBuilder<CommandSourceStack> debug = LiteralArgumentBuilder.literal("debug");
        debug.executes(context -> {
            NetworkStuff.debug = !NetworkStuff.debug;
            FiguraMod.sendChatMessage(Component.literal("Backend Debug Mode set to: " + NetworkStuff.debug).withStyle(NetworkStuff.debug ? ChatFormatting.GREEN : ChatFormatting.RED));
            return 1;
        });

        backend.then(debug);

        //check resources
        LiteralArgumentBuilder<CommandSourceStack> resources = LiteralArgumentBuilder.literal("checkResources");
        resources.executes(context -> {
            context.getSource().sendSuccess(Component.literal("Checking for resources..."), false);
//            FiguraRuntimeResources.init();
            return 1;
        });

        backend.then(resources);

        //return
        return backend;
    }

    private static int runRequest(CommandContext<CommandSourceStack> context, String request) {
        try {
            NetworkStuff.api.runString(
                    NetworkStuff.api.header(request).build(),
                    (code, data) -> FiguraMod.sendChatMessage(Component.literal(data))
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
