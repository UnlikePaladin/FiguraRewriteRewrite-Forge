package org.moon.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.PopupMenu;
import org.moon.figura.lua.api.vanilla_model.VanillaPart;
import org.moon.figura.model.rendering.PartFilterScheme;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Unique
    private Avatar currentAvatar;

    @Final
    @Shadow
    protected List<RenderLayer<T, M>> layers;

    @Shadow protected abstract boolean isBodyVisible(T livingEntity);
    @Shadow
    public static int getOverlayCoords(LivingEntity entity, float whiteOverlayProgress) {
        return 0;
    }
    @Shadow protected abstract float getWhiteOverlayProgress(T entity, float tickDelta);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V", shift = At.Shift.AFTER), method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true)
    private void preRender(T entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        currentAvatar = AvatarManager.getAvatar(entity);

        if (Avatar.firstPerson) {
            if (currentAvatar != null)
                currentAvatar.updateMatrices((LivingEntityRenderer<?, ?>) (Object) this, matrices);

            matrices.popPose();
            ci.cancel();
            return;
        }

        if (currentAvatar == null)
            return;

        if (currentAvatar.luaRuntime != null) {
            VanillaPart part = currentAvatar.luaRuntime.vanilla_model.PLAYER;
            EntityModel<?> model = getModel();
            part.save(model);
            if (currentAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1)
                part.preTransform(model);
        }

        boolean showBody = this.isBodyVisible(entity);
        boolean translucent = !showBody && Minecraft.getInstance().player != null && !entity.isInvisibleTo(Minecraft.getInstance().player);
        boolean glowing = !showBody && Minecraft.getInstance().shouldEntityAppearGlowing(entity);
        boolean invisible = !translucent && !showBody && !glowing;

        //When viewed 3rd person, render all non-world parts.
        PartFilterScheme filter = invisible ? PartFilterScheme.PIVOTS : PartFilterScheme.MODEL;
        int overlay = getOverlayCoords(entity, getWhiteOverlayProgress(entity, delta));

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(currentAvatar);

        FiguraMod.pushProfiler("renderEvent");
        currentAvatar.renderEvent(delta);

        FiguraMod.popPushProfiler("render");
        currentAvatar.render(entity, yaw, delta, translucent ? 0.15f : 1f, matrices, bufferSource, light, overlay, (LivingEntityRenderer<?, ?>) (Object) this, filter, translucent, glowing);

        FiguraMod.popPushProfiler("postRenderEvent");
        currentAvatar.postRenderEvent(delta);

        FiguraMod.popProfiler(3);

        if (currentAvatar.luaRuntime != null && currentAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1)
            currentAvatar.luaRuntime.vanilla_model.PLAYER.posTransform(getModel());
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"), method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void endRender(T entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        if (currentAvatar == null)
            return;

        //Render avatar with params
        if (currentAvatar.luaRuntime != null && currentAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1)
            currentAvatar.luaRuntime.vanilla_model.PLAYER.restore(getModel());

        currentAvatar = null;
    }

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    public void shouldShowName(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (UIHelper.paperdoll)
            cir.setReturnValue(Configs.PREVIEW_NAMEPLATE.value);
        else if (!Minecraft.renderNames() || livingEntity.getUUID().equals(PopupMenu.getEntityId()))
            cir.setReturnValue(false);
        else if (!AvatarManager.panic) {
            if (Configs.SELF_NAMEPLATE.value && livingEntity == Minecraft.getInstance().player)
                cir.setReturnValue(true);
            else if (Configs.NAMEPLATE_RENDER.value == 2 || (Configs.NAMEPLATE_RENDER.value == 1 && livingEntity != FiguraMod.extendedPickEntity))
                cir.setReturnValue(false);
        }
    }
}
