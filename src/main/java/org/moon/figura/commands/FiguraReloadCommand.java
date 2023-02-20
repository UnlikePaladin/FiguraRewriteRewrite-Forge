package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;

public class FiguraReloadCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> cmd = LiteralArgumentBuilder.literal("reload");
        cmd.executes(context -> {
            AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());
            FiguraToast.sendToast(new FiguraText("toast.reload"));
            return 1;
        });
        return cmd;
    }

}
