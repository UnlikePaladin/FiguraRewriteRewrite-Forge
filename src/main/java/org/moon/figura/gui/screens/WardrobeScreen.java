package org.moon.figura.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.avatar.local.LocalAvatarLoader;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.commands.FiguraLinkCommand;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.widgets.*;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;

public class WardrobeScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.wardrobe");

    private LoadingErrorWidget loadingErrorWidget;
    private StatusWidget statusWidget;
    private AvatarInfoWidget avatarInfo;
    private Label panic;

    private TexturedButton upload, delete;

    public WardrobeScreen(Screen parentScreen) {
        super(parentScreen, TITLE, WardrobeScreen.class);
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    protected void init() {
        super.init();

        //screen
        Minecraft minecraft = Minecraft.getInstance();
        int middle = width / 2;
        int panels = Math.min(width / 3, 256) - 8;

        int modelBgSize = Math.min(width - panels * 2 - 16, height - 96);
        panels = Math.max((width - modelBgSize) / 2 - 8, panels);

        // -- left -- //

        AvatarList avatarList = new AvatarList(4, 28, panels, height - 32, this);
        addRenderableWidget(avatarList);

        // -- middle -- //

        //model
        int entitySize = 11 * modelBgSize / 29;
        int entityX = middle - modelBgSize / 2;
        int entityY = this.height / 2 - modelBgSize / 2;

        EntityPreview entity = new EntityPreview(entityX, entityY, modelBgSize, modelBgSize, entitySize, -15f, 30f, minecraft.player, this);
        addRenderableWidget(entity);

        int buttX = entity.x + entity.width / 2;
        int buttY = entity.y + entity.height + 4;

        //upload
        addRenderableWidget(upload = new TexturedButton(buttX - 48, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/upload.png"), 72, 24, new FiguraText("gui.wardrobe.upload.tooltip"), button -> {
            Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
            try {
                LocalAvatarLoader.loadAvatar(null, null);
            } catch (Exception ignored) {}
            NetworkStuff.uploadAvatar(avatar);
            AvatarList.selectedEntry = null;
        }));
        upload.active = false;

        //reload
        addRenderableWidget(new TexturedButton(buttX - 12, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/reload.png"), 72, 24, new FiguraText("gui.wardrobe.reload.tooltip"), button -> {
            AvatarManager.clearAvatars(FiguraMod.getLocalPlayerUUID());
            try {
                LocalAvatarLoader.loadAvatar(null, null);
            } catch (Exception ignored) {}
            AvatarManager.localUploaded = true;
            NetworkStuff.auth();
            AvatarList.selectedEntry = null;
        }));

        //delete
        addRenderableWidget(delete = new TexturedButton(buttX + 24, buttY, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/delete.png"), 72, 24, new FiguraText("gui.wardrobe.delete.tooltip"), button ->
                NetworkStuff.deleteAvatar(null))
        );

        statusWidget = new StatusWidget(entity.x + entity.width - 64, 0, 64);
        statusWidget.y = entity.y - statusWidget.height - 4;
        addRenderableOnly(statusWidget);

        addRenderableOnly(loadingErrorWidget = new LoadingErrorWidget(statusWidget.x - 18, statusWidget.y, 14));

        // -- bottom -- //

        //version
        MutableComponent versionText = new FiguraText().append(" " + FiguraMod.VERSION.noBuildString()).withStyle(ChatFormatting.ITALIC);
        boolean oldVersion = NetworkStuff.latestVersion != null && NetworkStuff.latestVersion.compareTo(FiguraMod.VERSION) > 0;
        if (oldVersion) {
            versionText
                    .append(new TextComponent(" =")
                            .withStyle(Style.EMPTY
                                    .withFont(UIHelper.UI_FONT)
                                    .withItalic(false)
                                    .applyLegacyFormat(ChatFormatting.WHITE)
                            ))
                    .withStyle(Style.EMPTY
                            .applyFormat(ChatFormatting.AQUA)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new FiguraText("gui.new_version.tooltip", NetworkStuff.latestVersion)))
                    );
        }

        Label version = new Label(versionText, middle, this.height, TextUtils.Alignment.CENTER);
        addRenderableWidget(version);
        if (!oldVersion) version.alpha = 0x33;
        version.y -= version.getHeight() / 2 + 1;

        int rightSide = Math.min(panels, 134);

        //back
        TexturedButton back = new TexturedButton(width - rightSide - 4, height - 24, rightSide, 20, new FiguraText("gui.done"), null,
                bx -> this.minecraft.setScreen(parentScreen)
        );
        addRenderableWidget(back);

        // -- right side -- //

        rightSide = panels / 2 + 52;

        //hellp
        addRenderableWidget(new TexturedButton(
                this.width - rightSide, 28, 24, 24,
                0, 0, 24,
                new FiguraIdentifier("textures/gui/help.png"),
                72, 24,
                new FiguraText("gui.help.tooltip"),
                bx -> this.minecraft.setScreen(new ConfirmLinkScreen((bl) -> {
                    if (bl) Util.getPlatform().openUri(FiguraLinkCommand.LINK.WIKI.url);
                    this.minecraft.setScreen(this);
                }, FiguraLinkCommand.LINK.WIKI.url, true))
        ));

        //sounds
        TexturedButton sounds = new TexturedButton(this.width - rightSide + 36, 28, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/sound.png"), 72, 24, new FiguraText("gui.wardrobe.sound.tooltip"),
                button -> Minecraft.getInstance().setScreen(new SoundScreen(this))
        );
        addRenderableWidget(sounds);

        //keybinds
        TexturedButton keybinds = new TexturedButton(this.width - rightSide + 72, 28, 24, 24, 0, 0, 24, new FiguraIdentifier("textures/gui/keybind.png"), 72, 24, new FiguraText("gui.wardrobe.keybind.tooltip"),
                button -> Minecraft.getInstance().setScreen(new KeybindScreen(this))
        );
        addRenderableWidget(keybinds);

        //avatar metadata
        addRenderableOnly(avatarInfo = new AvatarInfoWidget(this.width - panels - 4, 56, panels, back.y - 60));

        //panic warning - always added last, on top
        addRenderableWidget(panic = new Label(
                new FiguraText("gui.panic", Configs.PANIC_BUTTON.keyBind.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW),
                middle, this.height - version.getHeight(), TextUtils.Alignment.CENTER, 0)
        );
        panic.y -= panic.getHeight() / 2;
        panic.setVisible(false);
    }

    @Override
    public void tick() {
        //children tick
        super.tick();
        loadingErrorWidget.tick();
        statusWidget.tick();
        avatarInfo.tick();

        //panic visible
        panic.setVisible(AvatarManager.panic);

        //backend buttons
        Avatar avatar;
        boolean backend = NetworkStuff.backendStatus == 3;
        upload.active = NetworkStuff.canUpload() && !AvatarManager.localUploaded && (avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID())) != null && avatar.nbt != null;
        delete.active = backend;
    }

    @Override
    public void removed() {
        LocalAvatarFetcher.save();
        super.removed();
    }
}
