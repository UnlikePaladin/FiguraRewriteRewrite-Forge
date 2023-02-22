package org.moon.figura.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.moon.figura.FiguraMod;
import org.moon.figura.animation.Animation;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.config.Config;
import org.moon.figura.trust.Trust;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.MathUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class FiguraDebugCommand {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        LiteralArgumentBuilder<CommandSourceStack> debug = LiteralArgumentBuilder.literal("debug");
        debug.executes(FiguraDebugCommand::commandAction);
        return debug;
    }

    private static int commandAction(CommandContext<CommandSourceStack> context) {
        try {
            //get path
            Path targetPath = FiguraMod.getFiguraDirectory().resolve("debug_data.json");

            //create file
            if (!Files.exists(targetPath))
                Files.createFile(targetPath);

            //write file
            FileOutputStream fs = new FileOutputStream(targetPath.toFile());
            fs.write(fetchStatus(AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())).getBytes());
            fs.close();

            //feedback
            context.getSource().sendSystemMessage(
                    new FiguraText("command.debug.success")
                            .append(" ")
                            .append(new FiguraText("command.click_to_open")
                                    .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, targetPath.toFile().toString())).withUnderlined(true))
                            )
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(new FiguraText("command.debug.error"));
            FiguraMod.LOGGER.error("Failed to save " + FiguraMod.MOD_NAME + " debug data!", e);
            return 0;
        }
    }

    public static String fetchStatus(Avatar avatar) {
        //root
        JsonObject root = new JsonObject();

        //mod meta
        JsonObject meta = new JsonObject();

        meta.addProperty("version", FiguraMod.VERSION.toString());
        meta.addProperty("localUUID", FiguraMod.getLocalPlayerUUID().toString());
        meta.addProperty("ticks", FiguraMod.ticks);
        meta.addProperty("figuraDirectory", FiguraMod.getFiguraDirectory().toString());
        meta.addProperty("figuraCacheDirectory", FiguraMod.getCacheDirectory().toString());
        meta.addProperty("backendStatus", NetworkStuff.backendStatus);
        meta.addProperty("backendConnected", NetworkStuff.isConnected());
        meta.addProperty("backendDisconnectedReason", NetworkStuff.disconnectedReason);
        meta.addProperty("uploaded", AvatarManager.localUploaded);
        meta.addProperty("panicMode", AvatarManager.panic);

        root.add("meta", meta);

        //config
        JsonObject config = new JsonObject();

        for (Config value : Config.values())
            if (value.value != null)
                config.addProperty(value.name(), value.value.toString());

        root.add("config", config);

        //trust groups
        JsonObject trust = new JsonObject();

        for (TrustContainer.GroupContainer group : TrustManager.GROUPS.values()) {
            JsonObject allTrust = new JsonObject();

            JsonObject standard = new JsonObject();
            for (Map.Entry<Trust, Integer> entry : group.getTrustSettings().entrySet())
                standard.addProperty(entry.getKey().name, entry.getValue());

            allTrust.add("standard", standard);

            JsonObject customTrust = new JsonObject();
            for (Map.Entry<String, Map<Trust, Integer>> entry : group.getCustomTrusts().entrySet()) {
                JsonObject obj = new JsonObject();
                for (Map.Entry<Trust, Integer> entry1 : entry.getValue().entrySet())
                    obj.addProperty(entry1.getKey().name, entry1.getValue());

                customTrust.add(entry.getKey(), obj);
            }

            allTrust.add("custom", customTrust);

            trust.add(group.name, allTrust);
        }

        root.add("trust", trust);

        //avatars
        LocalAvatarFetcher.load();
        root.add("avatars", getAvatarsPaths(LocalAvatarFetcher.ALL_AVATARS));


        // -- avatar -- //


        if (avatar == null)
            return GSON.toJson(root);

        JsonObject a = new JsonObject();

        //trust
        JsonObject aTrust = new JsonObject();

        aTrust.addProperty("parentTrust", avatar.trust.parent.name);

        JsonObject standard = new JsonObject();
        for (Map.Entry<Trust, Integer> entry : avatar.trust.getTrustSettings().entrySet())
            standard.addProperty(entry.getKey().name, entry.getValue());

        aTrust.add("standard", standard);

        JsonObject customTrust = new JsonObject();
        for (Map.Entry<String, Map<Trust, Integer>> entry : avatar.trust.getCustomTrusts().entrySet()) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<Trust, Integer> entry1 : entry.getValue().entrySet())
                obj.addProperty(entry1.getKey().name, entry1.getValue());

            customTrust.add(entry.getKey(), obj);
        }

        aTrust.add("custom", customTrust);

        a.add("trust", aTrust);

        //avatar metadata
        JsonObject aMeta = new JsonObject();

        aMeta.addProperty("version", avatar.version.toString());
        aMeta.addProperty("versionStatus", avatar.versionStatus);
        aMeta.addProperty("color", avatar.color);
        aMeta.addProperty("authors", avatar.authors);
        aMeta.addProperty("name", avatar.name);
        aMeta.addProperty("entityName", avatar.entityName);
        aMeta.addProperty("fileSize", avatar.fileSize);
        aMeta.addProperty("isHost", avatar.isHost);
        aMeta.addProperty("loaded", avatar.loaded);
        aMeta.addProperty("owner", avatar.owner.toString());
        aMeta.addProperty("scriptError", avatar.scriptError);
        aMeta.addProperty("hasTexture", avatar.hasTexture);
        aMeta.addProperty("hasLuaRuntime", avatar.luaRuntime != null);
        aMeta.addProperty("hasRenderer", avatar.renderer != null);
        aMeta.addProperty("hasData", avatar.nbt != null);

        a.add("meta", aMeta);

        //avatar complexity
        JsonObject inst = new JsonObject();

        inst.addProperty("animationComplexity", avatar.animationComplexity);
        inst.addProperty("complexity", avatar.complexity.pre);
        inst.addProperty("entityInitInstructions", avatar.init.post);
        inst.addProperty("entityRenderInstructions", avatar.render.pre);
        inst.addProperty("entityTickInstructions", avatar.tick.pre);
        inst.addProperty("initInstructions", avatar.init.pre);
        inst.addProperty("postEntityRenderInstructions", avatar.render.post);
        inst.addProperty("postWorldRenderInstructions", avatar.worldRender.post);
        inst.addProperty("worldRenderInstructions", avatar.worldRender.pre);
        inst.addProperty("worldTickInstructions", avatar.worldTick.pre);
        inst.addProperty("particlesRemaining", avatar.particlesRemaining.peek());
        inst.addProperty("soundsRemaining", avatar.soundsRemaining.peek());

        a.add("instructions", inst);

        //sounds
        JsonArray sounds = new JsonArray();

        for (String s : avatar.customSounds.keySet())
            sounds.add(s);

        a.add("sounds", sounds);

        //animations
        JsonArray animations = new JsonArray();

        for (Animation animation : avatar.animations.values())
            animations.add(animation.modelName + "/" + animation.name);

        a.add("animations", animations);

        //sizes
        if (avatar.nbt != null)
            a.add("sizes", parseNbtSizes(avatar.nbt));

        //return as string
        root.add("avatar", a);
        return GSON.toJson(root);
    }

    private static JsonObject getAvatarsPaths(List<LocalAvatarFetcher.AvatarPath> list) {
        JsonObject avatar = new JsonObject();

        for (LocalAvatarFetcher.AvatarPath path : list) {
            String name = path.getPath().getFileName().toString();

            if (path instanceof LocalAvatarFetcher.FolderPath folder)
                avatar.add(name, getAvatarsPaths(folder.getChildren()));
            else
                avatar.addProperty(name, path.getName());
        }

        return avatar;
    }

    private static JsonObject parseNbtSizes(CompoundTag nbt) {
        JsonObject sizes = new JsonObject();

        //metadata
        sizes.addProperty("metadata", getBytesFromNbt(nbt.getCompound("metadata")));

        //models
        JsonObject models = new JsonObject();

        CompoundTag modelsNbt = nbt.getCompound("models");
        ListTag childrenNbt = modelsNbt.getList("chld", Tag.TAG_COMPOUND);

        for (Tag tag : childrenNbt) {
            CompoundTag compound = (CompoundTag) tag;
            models.addProperty(compound.getString("name"), getBytesFromNbt(compound));
        }

        sizes.add("models", models);
        sizes.addProperty("models_total", getBytesFromNbt(modelsNbt));

        //scripts
        JsonObject scripts = new JsonObject();

        CompoundTag scriptsNbt = nbt.getCompound("scripts");
        for (String key : scriptsNbt.getAllKeys())
            scripts.addProperty(key, getBytesFromNbt(scriptsNbt.get(key)));

        sizes.add("scripts", scripts);
        sizes.addProperty("scripts_total", getBytesFromNbt(scriptsNbt));

        //sounds
        JsonObject sounds = new JsonObject();

        CompoundTag soundsNbt = nbt.getCompound("sounds");
        for (String key : soundsNbt.getAllKeys())
            sounds.addProperty(key, getBytesFromNbt(soundsNbt.get(key)));

        sizes.add("sounds", sounds);
        sizes.addProperty("sounds_total", getBytesFromNbt(soundsNbt));

        //textures
        JsonObject textures = new JsonObject();
        CompoundTag texturesNbt = nbt.getCompound("textures");

        CompoundTag textureSrc = texturesNbt.getCompound("src");
        for (String key : textureSrc.getAllKeys())
            textures.addProperty(key, getBytesFromNbt(textureSrc.get(key)));

        sizes.add("textures", textures);
        sizes.addProperty("textures_total", getBytesFromNbt(texturesNbt));

        //animations
        JsonObject animations = new JsonObject();
        ListTag animationsNbt = nbt.getList("animations", Tag.TAG_COMPOUND);

        for (Tag tag : animationsNbt) {
            CompoundTag compound = (CompoundTag) tag;
            animations.addProperty(compound.getString("mdl") + "." + compound.getString("name"), getBytesFromNbt(compound));
        }

        sizes.add("animations", animations);
        sizes.addProperty("animations_total", getBytesFromNbt(animationsNbt));

        //total
        sizes.addProperty("total", getBytesFromNbt(nbt));
        return sizes;
    }

    private static String getBytesFromNbt(Tag nbt) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(baos)));
            NbtIo.writeUnnamedTag(nbt, dos);
            dos.close();

            int size = baos.size();
            baos.close();

            return size < 1000 ? size + "b" : MathUtils.asFileSize(size) + " (" + size + "b)";
        } catch (Exception ignored) {
            return "?";
        }
    }
}
