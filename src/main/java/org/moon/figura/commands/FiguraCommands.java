package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.backend2.BackendCommands;
import org.moon.figura.lua.docs.FiguraDocsManager;

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

        //load
        root.then(FiguraReloadCommand.getCommand());

        //debug
        root.then(FiguraDebugCommand.getCommand());

        if (FiguraMod.DEBUG_MODE) {
            //backend debug
            root.then(BackendCommands.getCommand());

            //set avatar command
            root.then(AvatarManager.getCommand());
        }
        event.getDispatcher().register(root);
    }
}
