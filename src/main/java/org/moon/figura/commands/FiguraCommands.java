package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientCommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.backend2.BackendCommands;
import org.moon.figura.lua.FiguraLuaRuntime;
import org.moon.figura.lua.docs.FiguraDocsManager;
import org.moon.figura.model.rendering.AvatarRenderer;
import org.moon.figura.utils.FiguraText;

@Mod.EventBusSubscriber(modid = FiguraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FiguraCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        //root
        LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(FiguraMod.MOD_ID);

        //docs
        root.then(FiguraDocsManager.getCommand());
        root.then(FiguraDocsManager.getExportCommand());

        //links
        root.then(FiguraLinkCommand.getCommand());

        //run
        root.then(FiguraRunCommand.getCommand());

        //load
        root.then(FiguraLoadCommand.getCommand());

        //reload
        root.then(FiguraReloadCommand.getCommand());

        //debug
        root.then(FiguraDebugCommand.getCommand());

        //debug
        root.then(FiguraExportTextureCommand.getCommand());

        if (FiguraMod.DEBUG_MODE) {
            //backend debug
            root.then(BackendCommands.getCommand());

            //set avatar command
            root.then(AvatarManager.getCommand());
        }

        event.getDispatcher().register(root);
    }

    protected static Avatar checkAvatar(CommandContext<CommandSourceStack> context) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null) {
            context.getSource().sendFailure(FiguraText.of("command.no_avatar_error"));
            return null;
        }
        return avatar;
    }

    protected static FiguraLuaRuntime getRuntime(CommandContext<CommandSourceStack> context) {
        Avatar avatar = checkAvatar(context);
        if (avatar == null)
            return null;
        if (avatar.luaRuntime == null || avatar.scriptError) {
            context.getSource().sendFailure(FiguraText.of("command.no_script_error"));
            return null;
        }
        return avatar.luaRuntime;
    }

    protected static AvatarRenderer getRenderer(CommandContext<CommandSourceStack> context) {
        Avatar avatar = checkAvatar(context);
        if (avatar == null)
            return null;
        if (avatar.renderer == null) {
            context.getSource().sendFailure(FiguraText.of("command.no_renderer_error"));
            return null;
        }
        return avatar.renderer;
    }
}
