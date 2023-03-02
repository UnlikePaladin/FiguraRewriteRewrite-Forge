package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.gui.widgets.*;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class KeybindList extends AbstractList {

    private final List<KeybindElement> keybinds = new ArrayList<>();
    private final Avatar owner;
    private final TexturedButton resetAllButton;

    private FiguraKeybind focusedKeybind;

    public KeybindList(int x, int y, int width, int height, Avatar owner, TexturedButton resetAllButton) {
        super(x, y, width, height);
        this.owner = owner;
        this.resetAllButton = resetAllButton;
        updateList();

        Label noOwner, noKeys;
        this.children.add(noOwner = new Label(FiguraText.of("gui.error.no_avatar").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, true, 0));
        this.children.add(noKeys = new Label(FiguraText.of("gui.error.no_keybinds").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, true, 0));

        noOwner.setVisible(owner == null);
        noKeys.setVisible(!noOwner.isVisible() && keybinds.isEmpty());
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        if (!keybinds.isEmpty())
            updateEntries();

        //children
        super.render(stack, mouseX, mouseY, delta);

        //reset scissor
        UIHelper.disableScissor();
    }

    private void updateEntries() {
        //scrollbar
        int totalHeight = -4;
        for (KeybindElement keybind : keybinds)
            totalHeight += keybind.height + 8;
        int entryHeight = keybinds.isEmpty() ? 0 : totalHeight / keybinds.size();

        scrollBar.visible = totalHeight > height;
        scrollBar.setScrollRatio(entryHeight, totalHeight - height);

        //render list
        int xOffset = scrollBar.visible ? 4 : 11;
        int yOffset = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -4, totalHeight - height)) : 4;
        for (KeybindElement keybind : keybinds) {
            keybind.setPos(x + xOffset, y + yOffset);
            yOffset += keybind.height + 8;
        }
    }

    private void updateList() {
        //clear old widgets
        keybinds.forEach(children::remove);

        //add new keybinds
        if (owner == null || owner.luaRuntime == null)
            return;

        for (FiguraKeybind keybind : owner.luaRuntime.keybinds.keyBindings) {
            KeybindElement element = new KeybindElement(width - 22, keybind, this);
            keybinds.add(element);
            children.add(element);
        }

        updateBindings();
    }

    public boolean updateKey(InputConstants.Key key) {
        if (focusedKeybind == null)
            return false;

        focusedKeybind.setKey(key);
        focusedKeybind = null;

        updateBindings();
        return true;
    }

    public void updateBindings() {
        boolean active = false;

        for (KeybindElement keybind : keybinds) {
            keybind.updateText();
            if (!active && !keybind.keybind.isDefault())
                active = true;
        }

        resetAllButton.active = active;
    }

    private static class KeybindElement extends AbstractContainerElement {

        private final KeybindWidgetHelper helper = new KeybindWidgetHelper();
        private final FiguraKeybind keybind;
        private final KeybindList parent;
        private final TexturedButton resetButton;
        private final TexturedButton keybindButton;

        public KeybindElement(int width, FiguraKeybind keybind, KeybindList parent) {
            super(0, 0, width, 20);
            this.keybind = keybind;
            this.parent = parent;

            //toggle button
            children.add(0, keybindButton = new ParentedButton(0, 0, 90, 20, keybind.getTranslatedKeyMessage(), this, button -> {
                parent.focusedKeybind = keybind;
                updateText();
            }));

            //reset button
            children.add(resetButton = new ParentedButton(0, 0, 60, 20, Component.translatable("controls.reset"), this, button -> {
                keybind.resetDefaultKey();
                parent.updateBindings();
            }));
        }

        @Override
        public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
            if (!this.isVisible()) return;

            helper.renderConflictBars(stack, keybindButton.getX() - 8, keybindButton.getY() + 2, 4, 16);

            //vars
            Font font = Minecraft.getInstance().font;
            int textY = y + height / 2 - font.lineHeight / 2;

            //hovered arrow
            setHovered(isMouseOver(mouseX, mouseY));
            if (isHovered()) {
                font.draw(stack, HOVERED_ARROW, x + 4, textY, 0xFFFFFF);
                if (keybindButton.isHoveredOrFocused())
                    helper.renderTooltip();
            }

            //render name
            font.draw(stack, this.keybind.getName(), x + 16, textY, 0xFFFFFF);

            //render children
            super.render(stack, mouseX, mouseY, delta);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }

        public void setPos(int x, int y) {
            this.x = x;
            this.y = y;

            resetButton.setX(x + width - 60);
            resetButton.setY(y);

            keybindButton.setX(x + width - 154);
            keybindButton.setY(y);
        }

        public void updateText() {
            //tooltip
            List<FiguraKeybind> temp = new ArrayList<>();
            for (KeybindElement keybind : parent.keybinds)
                temp.add(keybind.keybind);
            helper.setTooltip(this.keybind, temp);

            //reset enabled
            boolean isDefault = this.keybind.isDefault();
            this.resetButton.active = !isDefault;

            //text
            boolean selected = parent.focusedKeybind == this.keybind;
            Component text = helper.getText(isDefault, selected, this.keybind.getTranslatedKeyMessage());
            keybindButton.setMessage(text);
        }
    }
}
