package org.moon.figura.gui.widgets.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.ConfigType;
import org.moon.figura.gui.widgets.KeybindWidgetHelper;
import org.moon.figura.gui.widgets.ParentedButton;
import org.moon.figura.gui.widgets.lists.ConfigList;

public class KeybindElement extends AbstractConfigElement {

    private final KeybindWidgetHelper helper = new KeybindWidgetHelper();
    private final KeyMapping binding;
    private final ParentedButton button;

    public KeybindElement(int width, ConfigType.KeybindConfig config, ConfigList parent) {
        super(width, config, parent);
        this.binding = config.keyBind;

        //toggle button
        children.add(0, button = new ParentedButton(0, 0, 90, 20, this.binding.getTranslatedKeyMessage(), this, button -> {
            parent.focusedBinding = binding;
            updateText();
        }));
        button.active = FiguraMod.DEBUG_MODE || !config.disabled;

        //overwrite reset button to update the keybind
        children.remove(resetButton);
        children.add(resetButton = new ParentedButton(x + width - 60, y, 60, 20, Component.translatable("controls.reset"), this, button -> {
            binding.setKey(binding.getDefaultKey());
            parent.updateKeybinds();
        }));

        updateText();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible()) return;

        //reset enabled
        helper.renderConflictBars(stack, button.getX() - 8, button.getY() + 2, 4, 16);

        //super render
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean bool = super.isMouseOver(mouseX, mouseY);
        if (bool && button.isMouseOver(mouseX, mouseY))
            helper.renderTooltip();
        return bool;
    }

    @Override
    public void setPos(int x, int y) {
        super.setPos(x, y);

        this.button.setX(x + width - 154);
        this.button.setY(y);
    }

    @Override
    public boolean isDefault() {
        return this.binding.isDefault();
    }

    @Override
    public boolean isChanged() {
        return false;
    }

    public void updateText() {
        //tooltip
        helper.setTooltip(binding);

        //reset button
        boolean isDefault = isDefault();
        this.resetButton.active = !isDefault;

        //text
        boolean selected = parent.focusedBinding == binding;
        Component text = helper.getText(isDefault, selected, binding.getTranslatedKeyMessage());
        button.setMessage(text);
    }
}
