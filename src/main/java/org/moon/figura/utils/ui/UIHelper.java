package org.moon.figura.utils.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.gui.screens.AbstractPanelScreen;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.rendering.EntityRenderMode;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.TextUtils;

import java.util.List;
import java.util.Stack;

public class UIHelper extends GuiComponent {

    // -- Variables -- //

    public static final ResourceLocation OUTLINE = new FiguraIdentifier("textures/gui/outline.png");
    public static final ResourceLocation TOOLTIP = new FiguraIdentifier("textures/gui/tooltip.png");
    public static final ResourceLocation UI_FONT = new FiguraIdentifier("ui");
    public static final ResourceLocation SPECIAL_FONT = new FiguraIdentifier("special");

    public static final Component UP_ARROW = Component.literal("^").withStyle(Style.EMPTY.withFont(UI_FONT));
    public static final Component DOWN_ARROW = Component.literal("V").withStyle(Style.EMPTY.withFont(UI_FONT));

    //Used for GUI rendering
    private static final CustomFramebuffer FIGURA_FRAMEBUFFER = new CustomFramebuffer();
    private static int previousFBO = -1;
    public static boolean paperdoll = false;
    public static float fireRot = 0f;
    public static float dollScale = 1f;
    private static final Stack<FiguraVec4> SCISSORS_STACK = new Stack<>();

    // -- Functions -- //

    public static void useFiguraGuiFramebuffer() {
        previousFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        int width = Minecraft.getInstance().getWindow().getWidth();
        int height = Minecraft.getInstance().getWindow().getHeight();
        FIGURA_FRAMEBUFFER.setSize(width, height);

        //Enable stencil buffer during this phase of rendering
        GL30.glEnable(GL30.GL_STENCIL_TEST);
        GlStateManager._stencilMask(0xFF);
        //Bind custom GUI framebuffer to be used for rendering
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, FIGURA_FRAMEBUFFER.getFbo());

        //Clear GUI framebuffer
        GlStateManager._clearStencil(0);
        GlStateManager._clearColor(0f, 0f, 0f, 1f);
        GlStateManager._clearDepth(1);
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL30.GL_STENCIL_BUFFER_BIT, false);

        Matrix4f mf = RenderSystem.getProjectionMatrix();
        Minecraft.getInstance().getMainRenderTarget().blitToScreen(width, height, false);
        RenderSystem.setProjectionMatrix(mf);
    }

    public static void useVanillaFramebuffer() {
        //Reset state before we go back to normal rendering
        GlStateManager._enableDepthTest();
        //Set a sensible default for stencil buffer operations
        GlStateManager._stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        GL30.glDisable(GL30.GL_STENCIL_TEST);

        //Bind vanilla framebuffer again
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousFBO);

        RenderSystem.disableBlend();
        //Draw GUI framebuffer -> vanilla framebuffer
        int windowWidth = Minecraft.getInstance().getWindow().getWidth();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();

        Matrix4f mf = RenderSystem.getProjectionMatrix();
        FIGURA_FRAMEBUFFER.drawToScreen(windowWidth, windowHeight);
        RenderSystem.setProjectionMatrix(mf);
        RenderSystem.enableBlend();
    }

    public static void drawEntity(float x, float y, float scale, float pitch, float yaw, LivingEntity entity, PoseStack stack, EntityRenderMode renderMode) {
        //backup entity variables
        float headX = entity.getXRot();
        float headY = entity.yHeadRot;
        boolean invisible = entity.isInvisible();

        float bodyY = entity.yBodyRot; //not truly a backup
        if (entity.getVehicle() instanceof LivingEntity l) {
            //drawEntity(x, y, scale, pitch, yaw, l, stack, renderMode);
            bodyY = l.yBodyRot;
        }

        //setup rendering properties
        float xRot, yRot;
        double xPos = 0d;
        double yPos = 0d;

        switch (renderMode) {
            case PAPERDOLL -> {
                //rotations
                xRot = pitch;
                yRot = yaw + bodyY + 180;

                //positions
                yPos--;

                if (entity.isFallFlying())
                    xPos += Mth.triangleWave((float) Math.toRadians(270), Mth.TWO_PI);

                if (entity.isAutoSpinAttack() || entity.isVisuallySwimming() || entity.isFallFlying()) {
                    yPos++;
                    entity.setXRot(0f);
                }

                //lightning
                Lighting.setupForEntityInInventory();

                //invisibility
                if (Config.PAPERDOLL_INVISIBLE.asBool())
                    entity.setInvisible(false);
            }
            case FIGURA_GUI -> {
                //rotations
                xRot = pitch;
                yRot = yaw + bodyY + 180;

                if (!Config.PREVIEW_HEAD_ROTATION.asBool()) {
                    entity.setXRot(0f);
                    entity.yHeadRot = bodyY;
                }

                //positions
                yPos--;

                //set up lighting
                Lighting.setupForFlatItems();
                RenderSystem.setShaderLights(Util.make(new Vector3f(-0.2f, -1f, -1f), Vector3f::normalize), Util.make(new Vector3f(-0.2f, 0.4f, -0.3f), Vector3f::normalize));

                //invisibility
                entity.setInvisible(false);
            }
            default -> {
                //rotations
                float rot = (float) Math.atan(pitch / 40f) * 20f;

                xRot = (float) Math.atan(yaw / 40f) * 20f;
                yRot = -rot + bodyY + 180;

                entity.setXRot(-xRot);
                entity.yHeadRot = rot + bodyY;

                //lightning
                Lighting.setupForEntityInInventory();
            }
        }

        //apply matrix transformers
        stack.pushPose();
        stack.translate(x, y, renderMode == EntityRenderMode.MINECRAFT_GUI ? 250d : -250d);
        stack.scale(scale, scale, scale);
        stack.last().pose().mul(new Matrix4f().scale(1f, 1f, -1f)); //Scale ONLY THE POSITIONS! Inverted normals don't work for whatever reason

        //apply rotations
        Quaternionf quaternion = Axis.ZP.rotationDegrees(180f);
        Quaternionf quaternion2 = Axis.YP.rotationDegrees(yRot);
        Quaternionf quaternion3 = Axis.XP.rotationDegrees(xRot);
        quaternion3.mul(quaternion2);
        quaternion.mul(quaternion3);
        stack.mulPose(quaternion);
        quaternion3.conjugate();

        //setup entity renderer
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        boolean renderHitboxes = dispatcher.shouldRenderHitBoxes();
        dispatcher.setRenderHitBoxes(false);
        dispatcher.setRenderShadow(false);
        dispatcher.overrideCameraOrientation(quaternion3);
        MultiBufferSource.BufferSource immediate = minecraft.renderBuffers().bufferSource();

        //render
        paperdoll = true;
        fireRot = -yRot;
        dollScale = scale;

        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar != null) avatar.renderMode = renderMode;

        double finalXPos = xPos;
        double finalYPos = yPos;
        //noinspection deprecation
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, finalXPos, finalYPos, 0d, 0f, 1f, stack, immediate, LightTexture.FULL_BRIGHT));
        immediate.endBatch();

        paperdoll = false;

        //restore entity rendering data
        dispatcher.setRenderHitBoxes(renderHitboxes);
        dispatcher.setRenderShadow(true);

        //pop matrix
        stack.popPose();
        Lighting.setupFor3DItems();

        //restore entity data
        entity.setXRot(headX);
        entity.yHeadRot = headY;
        entity.setInvisible(invisible);
    }

    public static void setupTexture(ResourceLocation texture) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void renderTexture(PoseStack stack, int x, int y, int width, int height, ResourceLocation texture) {
        setupTexture(texture);
        blit(stack, x, y, width, height, 0f, 0f, 1, 1, 1, 1);
    }

    public static void renderAnimatedBackground(ResourceLocation texture, double x, double y, float width, float height, float textureWidth, float textureHeight, double speed, float delta) {
        if (speed != 0) {
            double d = (FiguraMod.ticks + delta) / speed;
            x -= d % textureWidth;
            y -= d % textureHeight;
        }

        width += textureWidth;
        height += textureHeight;

        if (speed < 0) {
            x -= textureWidth;
            y -= textureHeight;
        }

        renderBackgroundTexture(texture, x, y, width, height, textureWidth, textureHeight);
    }

    public static void renderBackgroundTexture(ResourceLocation texture, double x, double y, float width, float height, float textureWidth, float textureHeight) {
        setupTexture(texture);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float z = -999f;
        bufferBuilder.vertex(x, y + height, z).uv(0f, height / textureHeight).endVertex();
        bufferBuilder.vertex(x + width, y + height, z).uv(width / textureWidth, height / textureHeight).endVertex();
        bufferBuilder.vertex(x + width, y, z).uv(width / textureWidth, 0f).endVertex();
        bufferBuilder.vertex(x, y, z).uv(0f, 0f).endVertex();

        tessellator.end();
    }

    public static void fillRounded(PoseStack stack, int x, int y, int width, int height, int color) {
        fill(stack, x + 1, y, x + width - 1, y + 1, color);
        fill(stack, x, y + 1, x + width, y + height - 1, color);
        fill(stack, x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    public static void fillOutline(PoseStack stack, int x, int y, int width, int height, int color) {
        fill(stack, x + 1, y, x + width - 1, y + 1, color);
        fill(stack, x, y + 1, x + 1, y + height - 1, color);
        fill(stack, x + width - 1, y + 1, x + width, y + height - 1, color);
        fill(stack, x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    public static void renderSliced(PoseStack stack, int x, int y, int width, int height, ResourceLocation texture) {
        renderSliced(stack, x, y, width, height, 0f, 0f, 15, 15, 15, 15, texture);
    }

    public static void renderSliced(PoseStack stack, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, ResourceLocation texture) {
        setupTexture(texture);

        Matrix4f pose = stack.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float rWidthThird = regionWidth / 3f;
        float rHeightThird = regionHeight / 3f;

        //top left
        sliceVertex(pose, buffer, x, y, rWidthThird, rHeightThird, u, v, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //top middle
        sliceVertex(pose, buffer, x + rWidthThird, y, width - rWidthThird * 2, rHeightThird, u + rWidthThird, v, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //top right
        sliceVertex(pose, buffer, x + width - rWidthThird, y, rWidthThird, rHeightThird, u + rWidthThird * 2, v, rWidthThird, rHeightThird, textureWidth, textureHeight);

        //middle left
        sliceVertex(pose, buffer, x, y + rHeightThird, rWidthThird, height - rHeightThird * 2, u, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //middle middle
        sliceVertex(pose, buffer, x + rWidthThird, y + rHeightThird, width - rWidthThird * 2, height - rHeightThird * 2, u + rWidthThird, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //middle right
        sliceVertex(pose, buffer, x + width - rWidthThird, y + rHeightThird, rWidthThird, height - rHeightThird * 2, u + rWidthThird * 2, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);

        //bottom left
        sliceVertex(pose, buffer, x, y + height - rHeightThird, rWidthThird, rHeightThird, u, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //bottom middle
        sliceVertex(pose, buffer, x + rWidthThird, y + height - rHeightThird, width - rWidthThird * 2, rHeightThird, u + rWidthThird, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //bottom right
        sliceVertex(pose, buffer, x + width - rWidthThird, y + height - rHeightThird, rWidthThird, rHeightThird, u + rWidthThird * 2, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);

        tessellator.end();
    }

    private static void sliceVertex(Matrix4f matrix, BufferBuilder bufferBuilder, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        float x1 = x + width;
        float y1 = y + height;

        float u0 = u / textureWidth;
        float v0 = v / textureHeight;
        float u1 = (u + regionWidth) / textureWidth;
        float v1 = (v + regionHeight) / textureHeight;

        bufferBuilder.vertex(matrix, x, y1, 0f).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix, x1, y1, 0f).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix, x1, y, 0f).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0f).uv(u0, v0).endVertex();
    }

    public static void setupScissor(int x, int y, int width, int height) {
        FiguraVec4 vec = FiguraVec4.of(x, y, width, height);
        if (!SCISSORS_STACK.isEmpty()) {
            FiguraVec4 old = SCISSORS_STACK.peek();
            double newX = Math.max(x, old.x());
            double newY = Math.max(y, old.y());
            double newWidth = Math.min(x + width, old.x() + old.z()) - newX;
            double newHeight = Math.min(y + height, old.y() + old.w()) - newY;
            vec.set(newX, newY, newWidth, newHeight);
        }

        SCISSORS_STACK.push(vec);
        setupScissor(vec);
    }

    private static void setupScissor(FiguraVec4 dimensions) {
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        int screenY = Minecraft.getInstance().getWindow().getHeight();

        int scaledWidth = (int) Math.max(dimensions.z * scale, 0);
        int scaledHeight = (int) Math.max(dimensions.w * scale, 0);
        RenderSystem.enableScissor((int) (dimensions.x * scale), (int) (screenY - dimensions.y * scale - scaledHeight), scaledWidth, scaledHeight);
    }

    public static void disableScissor() {
        SCISSORS_STACK.pop();
        if (!SCISSORS_STACK.isEmpty()) {
            setupScissor(SCISSORS_STACK.peek());
        } else {
            RenderSystem.disableScissor();
        }
    }

    public static void highlight(PoseStack stack, Object component, Component text) {
        //object
        int x, y, width, height;
        if (component instanceof AbstractWidget w) {
            x = w.getX(); y = w.getY();
            width = w.getWidth();
            height = w.getHeight();
        } else if (component instanceof AbstractContainerElement c) {
            x = c.x; y = c.y;
            width = c.width;
            height = c.height;
        } else {
            return;
        }

        //screen
        int screenW, screenH;
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panel) {
            screenW = panel.width;
            screenH = panel.height;

            if (text != null)
                panel.tooltip = text;
        } else {
            return;
        }

        //draw

        //left
        fill(stack, 0, 0, x, y + height, 0xBB000000);
        //right
        fill(stack, x + width, y, screenW, screenH, 0xBB000000);
        //up
        fill(stack, x, 0, screenW, y, 0xBB000000);
        //down
        fill(stack, 0, y + height, x + width, screenH, 0xBB000000);

        //outline
        fillOutline(stack, Math.max(x - 1, 0), Math.max(y - 1, 0), Math.min(width + 2, screenW), Math.min(height + 2, screenH), 0xFFFFFFFF);
    }

    //widget.isMouseOver() returns false if the widget is disabled or invisible
    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
        return isMouseOver(x, y, width, height, mouseX, mouseY, false);
    }

    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY, boolean force) {
        ContextMenu context = force ? null : getContext();
        return (context == null || !context.isVisible()) && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void renderOutlineText(PoseStack stack, Font textRenderer, Component text, int x, int y, int color, int outline) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        textRenderer.drawInBatch8xOutline(text.getVisualOrderText(), x, y, color, outline, stack.last().pose(), bufferSource, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();
    }

    public static void renderTooltip(PoseStack stack, Component tooltip, int mouseX, int mouseY, boolean background) {
        Minecraft minecraft = Minecraft.getInstance();

        //window
        double screenX = minecraft.getWindow().getGuiScaledWidth();
        double screenY = minecraft.getWindow().getGuiScaledHeight();

        //prepare text
        Font font = minecraft.font;
        List<FormattedCharSequence> text = TextUtils.wrapTooltip(tooltip, font, mouseX, (int) screenX);
        int height = font.lineHeight * text.size();

        //calculate pos
        int x = mouseX + 12;
        int y = (int) Math.min(Math.max(mouseY - 12, 0), screenY - height);

        int width = TextUtils.getWidth(text, font);
        if (x + width > screenX)
            x = Math.max(x - 24 - width, 0);

        //render
        stack.pushPose();
        stack.translate(0d, 0d, 999d);

        if (background)
            renderSliced(stack, x - 4, y - 4, width + 8, height + 8, TOOLTIP);

        for (int i = 0; i < text.size(); i++) {
            FormattedCharSequence charSequence = text.get(i);
            font.drawShadow(stack, charSequence, x, y + font.lineHeight * i, 0xFFFFFF);
        }

        stack.popPose();
    }

    public static void renderScrollingText(PoseStack stack, Component text, int x, int y, int width, int height, int color) {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int textX = x + width / 2;
        int textY = y + height / 2 - font.lineHeight / 2;

        //the text fit :D
        if (textWidth <= width - 2) {
            drawCenteredString(stack, font, text, textX, textY, color);
            return;
        }

        //oh, no it doesn't fit

        float speed = Config.TEXT_SCROLL_SPEED.asFloat();
        int scrollLen = textWidth - (width - 4);
        int startingOffset = scrollLen / 2;
        int stopDelay = (int) (Config.TEXT_SCROLL_DELAY.asInt() * speed);
        int time = scrollLen + stopDelay;
        int totalTime = time * 2;
        int ticks = (int) (FiguraMod.ticks * speed);
        int currentTime = ticks % time;
        int dir = (ticks % totalTime) > time - 1 ? 1 : -1;

        int clamp = Math.min(Math.max(currentTime - stopDelay, 0), scrollLen);
        textX += (startingOffset - clamp) * dir;

        setupScissor(x + 1, y, width - 2, height);
        drawCenteredString(stack, font, text, textX, textY, color);
        disableScissor();
    }

    public static void setContext(ContextMenu context) {
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panelScreen)
            panelScreen.contextMenu = context;
    }

    public static ContextMenu getContext() {
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panelScreen)
            return panelScreen.contextMenu;
        return null;
    }

    public static void setTooltip(Component text) {
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panelScreen)
            panelScreen.tooltip = text;
    }

    public static void setTooltip(Style style) {
        if (style == null || style.getHoverEvent() == null)
            return;

        Component text = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
        if (text != null)
            setTooltip(text);
    }
}
