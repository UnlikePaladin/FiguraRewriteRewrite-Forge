package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.SoundsList;
import org.moon.figura.utils.FiguraText;

public class SoundScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.sound");

    private final Screen sourcePanel;

    public SoundScreen(AbstractPanelScreen parentScreen) {
        super(parentScreen.parentScreen, TITLE, WardrobeScreen.class);
        sourcePanel = parentScreen;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    protected void init() {
        super.init();

        Avatar owner = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());

        //list
        int listWidth = Math.min(this.width - 8, 420);
        this.addRenderableWidget(new SoundsList((this.width - listWidth) / 2, 28, listWidth, height - 56, owner));

        //back
        addRenderableWidget(new TexturedButton(width / 2 - 60, height - 24, 120, 20, FiguraText.of("gui.done"), null,
                bx -> this.minecraft.setScreen(sourcePanel)
        ));
    }
}
