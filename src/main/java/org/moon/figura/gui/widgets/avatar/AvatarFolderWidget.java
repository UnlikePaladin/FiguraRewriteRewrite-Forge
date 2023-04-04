package org.moon.figura.gui.widgets.avatar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.gui.widgets.ContainerButton;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AvatarFolderWidget extends AbstractAvatarWidget {

    private final HashMap<String, AbstractAvatarWidget> entries = new HashMap<>();
    private final ArrayList<AbstractAvatarWidget> sortedEntires = new ArrayList<>();

    public AvatarFolderWidget(int depth, int width, LocalAvatarFetcher.FolderPath avatar, AvatarList parent) {
        super(depth, width, 20, avatar, parent);

        AvatarFolderWidget instance = this;
        this.button = new ContainerButton(parent, x, y, width, 20, Component.empty(), null, button -> {
            toggleEntries(((ContainerButton) this.button).isToggled());
            parent.updateScroll();
        }) {
            @Override
            protected  void renderText(PoseStack stack) {
                super.renderText(stack);

                //fix tooltip
                if (getTooltip() == getMessage())
                    setTooltip(instance.getName());
            }

            @Override
            public void setHovered(boolean hovered) {
                if (!hovered && UIHelper.getContext() == context && context.isVisible())
                    hovered = true;

                super.setHovered(hovered);
            }
        };

        children.add(this.button);
        updateName();

        update(avatar, "");

        boolean expanded = avatar.isExpanded();
        ((ContainerButton) this.button).setToggled(expanded);
        this.button.shouldHaveBackground(false);

        toggleEntries(expanded);
        parent.updateScroll();
    }

    @Override
    public void update(LocalAvatarFetcher.AvatarPath path, String filter) {
        super.update(path, filter);

        if (!(path instanceof LocalAvatarFetcher.FolderPath avatar))
            return;

        for (AbstractAvatarWidget value : entries.values())
            value.filter = this.filter;

        //update children
        HashSet<String> missingPaths = new HashSet<>(entries.keySet());
        for (LocalAvatarFetcher.AvatarPath child : avatar.getChildren()) {
            String str = child.getPath() + child.getName();

            //skip unfiltered
            if (!child.search(filter))
                continue;

            //update children
            AbstractAvatarWidget childEntry = entries.get(str);
            if (childEntry != null)
                childEntry.update(child, filter);

            //remove from exclusion list
            missingPaths.remove(str);

            //add children
            this.entries.computeIfAbsent(str, s -> {
                AbstractAvatarWidget entry = child instanceof LocalAvatarFetcher.FolderPath folder ? new AvatarFolderWidget(depth + 1, width, folder, parent) : new AvatarWidget(depth + 1, width, child, parent);
                children.add(entry);
                entry.setVisible(((ContainerButton) this.button).isToggled());
                return entry;
            });
        }

        //remove missing avatars
        for (String str : missingPaths)
            children.remove(entries.remove(str));

        sortedEntires.clear();
        sortedEntires.addAll(entries.values());

        //sort children
        children.sort((children1, children2) -> {
            if (children1 instanceof AbstractAvatarWidget avatar1 && children2 instanceof AbstractAvatarWidget avatar2)
                return avatar1.compareTo(avatar2);
            return 0;
        });
        sortedEntires.sort(AbstractAvatarWidget::compareTo);

        //update height
        updateHeight();
    }

    public void toggleEntries(boolean toggle) {
        toggle = toggle && ((ContainerButton) this.button).isToggled();
        avatar.setExpanded(toggle);

        for (AbstractAvatarWidget widget : entries.values()) {
            widget.setVisible(toggle);

            if (widget instanceof AvatarFolderWidget folder)
                folder.toggleEntries(toggle);
        }

        updateHeight();
    }

    private void updateHeight() {
        this.height = 20;

        for (AbstractAvatarWidget entry : entries.values()) {
            if (entry instanceof AvatarFolderWidget folder)
                folder.updateHeight();

            if (entry.isVisible())
                this.height += entry.height + 2;
        }
    }

    @Override
    public void setPos(int x, int y) {
        super.setPos(x, y);

        y = 22;
        for (AbstractAvatarWidget widget : sortedEntires) {
            if (widget.isVisible()) {
                widget.setPos(x, this.y + y);
                y += widget.height + 2;
            }
        }
    }

    @Override
    public boolean filtered() {
        boolean result = super.filtered();

        for (AbstractAvatarWidget value : entries.values()) {
            if (result) break;
            result = value.filtered();
        }

        return result;
    }
}
