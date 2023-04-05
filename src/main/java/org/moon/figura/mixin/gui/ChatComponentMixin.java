package org.moon.figura.mixin.gui;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.Badges;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.Emojis;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.EntityUtils;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V")
    private void addMessageEvent(Component message, int messageId, int timestamp, boolean refresh, CallbackInfo ci) {
        Avatar avatar;
        if (!refresh && (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) != null)
            avatar.chatReceivedMessageEvent(message);
    }

    @ModifyVariable(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", ordinal = 0, argsOnly = true)
    private Component addMessage(Component message, Component message2, int messageId, int timestamp, boolean refresh) {
        //get config
        int config = Configs.CHAT_NAMEPLATE.value;
        if (refresh || config == 0 || AvatarManager.panic)
            return message;

        message = TextUtils.parseLegacyFormatting(message);

        Map<String, UUID> players = EntityUtils.getPlayerList();
        String owner = null;

        String[] split = message.getString().split("\\W+");
        for (String s : split) {
            if (players.containsKey(s)) {
                owner = s;
                break;
            }
        }

        //iterate over ALL online players
        for (Map.Entry<String, UUID> entry : players.entrySet()) {
            String name = entry.getKey();
            UUID uuid = entry.getValue();

            Component playerName = new TextComponent(name);

            //apply customization
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            NameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.CHAT;

            Component replacement = custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1 ?
                    TextUtils.replaceInText(custom.getJson().copy(), "\n|\\\\n", " ") : playerName;

            //name
            replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", playerName);

            //sender badges
            if (config > 1 && name.equals(owner)) {
                //badges
                Component temp = Badges.appendBadges(replacement, uuid, true);
                //trim
                temp = TextUtils.trim(temp);
                //modify message, only first, also no need for ignore case, since it is already matched with proper case
                message = TextUtils.replaceInText(message, "\\b" + Pattern.quote(name) + "\\b", temp, (s, style) -> true, 1);
            }

            //badges
            replacement = Badges.appendBadges(replacement, uuid, config > 1 && owner == null);

            //trim
            replacement = TextUtils.trim(replacement);

            //modify message
            message = TextUtils.replaceInText(message, "(?i)\\b" + Pattern.quote(name) + "\\b", replacement);
        }

        return message;
    }

    @ModifyVariable(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", argsOnly = true)
    private Component addMessageEmojis(Component message) {
        return Configs.CHAT_EMOJIS.value ? Emojis.applyEmojis(message) : message;
    }
}
