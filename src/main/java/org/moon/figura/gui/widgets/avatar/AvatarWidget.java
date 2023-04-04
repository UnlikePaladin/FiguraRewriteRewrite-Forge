package org.moon.figura.gui.widgets.avatar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FileTexture;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

public class AvatarWidget extends AbstractAvatarWidget {

    public static final ResourceLocation MISSING_ICON = new FiguraIdentifier("textures/gui/unknown_icon.png");

    public AvatarWidget(int depth, int width, LocalAvatarFetcher.AvatarPath avatar, AvatarList parent) {
        super(depth, width, 24, avatar, parent);

        AvatarWidget instance = this;
        this.button = new TexturedButton(x, y, width, 24, Component.empty(), null, button -> {
            AvatarManager.loadLocalAvatar(avatar == null ? null : avatar.getPath());
            AvatarList.selectedEntry = instance;
        }) {
            @Override
            public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
                super.renderButton(stack, mouseX, mouseY, delta);

                //selected border
                if (instance.equals(AvatarList.selectedEntry))
                    UIHelper.fillOutline(stack, x - 1, y - 1, width + 2, height + 2, 0xFFFFFFFF);
            }

            @Override
            protected void renderText(PoseStack stack) {
                //variables
                Font font = Minecraft.getInstance().font;

                int spacing = font.width(SPACING) * depth;
                int x = getX() + 2;
                int y = getY() + 2;

                //icon
                FileTexture texture = avatar.getIcon();
                ResourceLocation icon = texture == null ? MISSING_ICON : texture.getLocation();
                UIHelper.renderTexture(stack, x + spacing, y, 20, 20, icon);

                //name
                Component name = TextUtils.trimToWidthEllipsis(font, getMessage(), this.width - 26, TextUtils.ELLIPSIS.copy().withStyle(getMessage().getStyle()));
                font.drawShadow(stack, name, x + 22, y, 0xFFFFFFFF);

                //description
                Component description = Component.literal(avatar.getDescription());
                Component parsedDescription = TextUtils.trimToWidthEllipsis(font, description, this.width - 26, TextUtils.ELLIPSIS.copy().withStyle(description.getStyle()));
                font.drawShadow(stack, parsedDescription, x + 22, y + font.lineHeight + 1, ChatFormatting.GRAY.getColor());

                //tooltip
                if (name != getMessage() || parsedDescription != description) {
                    Component tooltip = instance.getName();
                    if (!description.getString().isBlank())
                        tooltip = tooltip.copy().append("\n\n").append(description);
                    setTooltip(tooltip);
                }
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
            }

            @Override
            public void setHovered(boolean hovered) {
                if (!hovered && UIHelper.getContext() == context && context.isVisible())
                    hovered = true;

                super.setHovered(hovered);
            }
        };

        this.button.shouldHaveBackground(false);
        children.add(this.button);
        updateName();
    }
}
