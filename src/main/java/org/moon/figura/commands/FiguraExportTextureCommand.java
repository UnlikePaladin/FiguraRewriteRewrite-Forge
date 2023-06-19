package org.moon.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import org.moon.figura.FiguraMod;
import org.moon.figura.model.rendering.AvatarRenderer;
import org.moon.figura.model.rendering.texture.FiguraTexture;
import org.moon.figura.utils.FiguraText;

public class FiguraExportTextureCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> run = LiteralArgumentBuilder.literal("export_texture");

        RequiredArgumentBuilder<CommandSourceStack, String> arg = RequiredArgumentBuilder.argument("texture", StringArgumentType.word());
        arg.executes(FiguraExportTextureCommand::run);

        RequiredArgumentBuilder<CommandSourceStack, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.greedyString());
        name.executes(context -> run(context, StringArgumentType.getString(context, "name")));
        arg.then(name);

        run.then(arg);
        return run;
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        return run(context, "exported_texture");
    }

    private static int run(CommandContext<CommandSourceStack> context, String name) {
        String textureName = StringArgumentType.getString(context, "texture");
        AvatarRenderer renderer = FiguraCommands.getRenderer(context);
        if (renderer == null)
            return 0;

        try {
            FiguraTexture texture = renderer.getTexture(textureName);
            if (texture == null)
                throw new Exception();

            texture.writeTexture(FiguraMod.getFiguraDirectory().resolve(name + ".png"));

            context.getSource().sendSuccess(FiguraText.of("command.export_texture.success"), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(FiguraText.of("command.export_texture.error"));
            return 0;
        }
    }
}
