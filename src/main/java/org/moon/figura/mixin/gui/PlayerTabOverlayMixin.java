package org.moon.figura.mixin.gui;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.Badges;
import org.moon.figura.config.Configs;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Unique private UUID uuid;

    @Inject(at = @At("RETURN"), method = "getNameForDisplay", cancellable = true)
    private void getPlayerName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        //get config
        int config = Configs.LIST_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic)
            return;

        //apply customization
        Component text = cir.getReturnValue();
        Component name = Component.literal(playerInfo.getProfile().getName());

        UUID uuid = playerInfo.getProfile().getId();
        Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
        NameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.LIST;

        Component replacement = custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1 ?
                TextUtils.replaceInText(custom.getJson().copy(), "\n|\\\\n", " ") : name;

        //name
        replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);

        //badges
        replacement = Badges.appendBadges(replacement, uuid, config > 1);

        //trim
        replacement = TextUtils.trim(replacement);

        text = TextUtils.replaceInText(text, "\\b" + Pattern.quote(playerInfo.getProfile().getName()) + "\\b", replacement);

        cir.setReturnValue(text);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPlayerByUUID(Ljava/util/UUID;)Lnet/minecraft/world/entity/player/Player;"), method = "render")
    private UUID render(UUID uuid) {
        this.uuid = uuid;
        return uuid;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lcom/mojang/blaze3d/vertex/PoseStack;IIIZZ)V"), index = 3)
    private int doNotDrawFace(PoseStack p_240133_, int p_240134_, int p_240135_, int p_240136_, boolean p_240137_, boolean p_240138_) {
        if (uuid != null) {
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            if (avatar != null && avatar.renderPortrait(p_240133_, p_240134_, p_240135_, p_240136_, 16, args.get(5)))
                return 0;
        }
        return p_240136_;
    }
}
