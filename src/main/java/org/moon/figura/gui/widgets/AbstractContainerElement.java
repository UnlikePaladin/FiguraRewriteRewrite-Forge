package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContainerElement extends AbstractContainerEventHandler implements FiguraTickable, FiguraWidget, NarratableEntry {

    public static final Component HOVERED_ARROW = Component.literal("•");

    protected final List<GuiEventListener> children = new ArrayList<>();

    public int x, y;
    public int width, height;

    private boolean hovered = false;
    private boolean visible = true;

    public AbstractContainerElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void tick() {
        for (GuiEventListener listener : this.children) {
            if (listener instanceof FiguraTickable tickable)
                tickable.tick();
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        for (GuiEventListener listener : this.children) {
            if (listener instanceof Renderable widget)
                widget.render(stack, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GuiEventListener widget = null;

        //update children focused
        for (GuiEventListener children : List.copyOf(this.children())) {
            boolean clicked = children.mouseClicked(mouseX, mouseY, button);
            children.changeFocus(clicked);
            if (clicked) widget = children;
        }

        //set this focused
        if (getFocused() != widget)
            setFocused(widget);

        if (widget != null) {
            if (button == 0) this.setDragging(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return UIHelper.isMouseOver(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        //yeet mouse 0 and isDragging check
        return this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //better check for mouse released when outside node's boundaries
        boolean bool = this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);

        //remove focused when clicking
        if (bool) setFocused(null);

        this.setDragging(false);
        return bool;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        //fix scrolling targeting only one child
        boolean ret = false;
        for (GuiEventListener child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY))
                ret = ret || child.mouseScrolled(mouseX, mouseY, amount);
        }
        return ret;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean isHovered() {
        return hovered;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;

        for (GuiEventListener listener : this.children) {
            if (listener instanceof FiguraWidget drawable)
                drawable.setVisible(visible);
            else if (listener instanceof AbstractWidget widget)
                widget.visible = visible;
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }
}
