package org.moon.figura.avatar.local;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.cards.CardBackground;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.utils.FileTexture;
import org.moon.figura.utils.IOUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Navigates through the file system, finding all folders
 * containing avatar.json as well as all .moon files.
 */
public class LocalAvatarFetcher {

    /**
     * After calling load(), this is an AvatarFolder that contains
     * the whole filesystem of avatars.
     */
    public static final List<AvatarPath> ALL_AVATARS = new ArrayList<>();
    private static final Map<String, Properties> SAVED_DATA = new HashMap<>();

    /**
     * Clears out the root AvatarFolder, and regenerates it from the
     * file system.
     */
    public static void load() {
        //clear loaded avatars
        ALL_AVATARS.clear();

        //load avatars
        FolderPath root = new FolderPath(getLocalAvatarDirectory());
        root.fetch();

        //add new avatars
        ALL_AVATARS.addAll(root.getChildren());
    }

    /**
     * Loads the folder data from the disk
     * the folder data contains information about the avatar folders
     */
    public static void init() {
        IOUtils.readCacheFile("avatars", nbt -> {
            //loading
            ListTag list = nbt.getList("properties", Tag.TAG_COMPOUND);
            for (Tag tag : list) {
                CompoundTag compound = (CompoundTag) tag;

                String path = compound.getString("path");
                Properties properties = new Properties();
                properties.expanded = compound.getBoolean("expanded");
                properties.favourite = compound.getBoolean("favourite");

                SAVED_DATA.put(path, properties);
            }
        });
    }

    /**
     * Saves the folder data to disk
     */
    public static void save() {
        IOUtils.saveCacheFile("avatars", nbt -> {
            ListTag properties = new ListTag();

            for (Map.Entry<String, Properties> entry : SAVED_DATA.entrySet()) {
                CompoundTag compound = new CompoundTag();

                Properties prop = entry.getValue();
                if (!prop.expanded)
                    compound.putBoolean("expanded", false);
                if (prop.favourite)
                    compound.putBoolean("favourite", true);

                if (!compound.isEmpty()) {
                    compound.putString("path", entry.getKey());
                    properties.add(compound);
                }
            }

            nbt.put("properties", properties);
        });
    }

    public static void clearCache() {
        IOUtils.deleteCacheFile("avatars");
    }

    /**
     * Returns the directory where all local avatars are stored.
     * The directory is always under main directory.
     */
    public static Path getLocalAvatarDirectory() {
        return IOUtils.getOrCreateDir(FiguraMod.getFiguraDirectory(), "avatars");
    }

    /**
     * Represents a path which contains an avatar.
     */
    public static class AvatarPath {

        protected final Path path;
        protected final String name, description;
        protected final CardBackground background;
        protected final FileTexture iconTexture;

        protected Properties properties;

        public AvatarPath(Path path) {
            this.path = path;

            Properties properties = SAVED_DATA.get(this.path.toFile().getAbsolutePath());
            if (properties != null) {
                this.properties = properties;
            } else {
                this.properties = new Properties();
                saveProperties();
            }

            String filename = path.getFileName().toString();

            String name = filename;
            String description = "";
            CardBackground bg = CardBackground.DEFAULT;
            FileTexture iconTexture = null;

            if (!path.toString().toLowerCase().endsWith(".moon") && !(this instanceof FolderPath)) {
                //metadata
                try {
                    String str = IOUtils.readFile(path.resolve("avatar.json").toFile());
                    AvatarMetadataParser.Metadata metadata = AvatarMetadataParser.read(str);

                    name = Configs.WARDROBE_FILE_NAMES.value || metadata.name == null || metadata.name.isBlank() ? filename : metadata.name;
                    description = metadata.description == null ? "" : metadata.description;
                    bg = CardBackground.parse(metadata.background);
                } catch (Exception ignored) {}

                //icon
                try {
                    Path p = path.resolve("avatar.png");
                    if (p.toFile().exists())
                        iconTexture = FileTexture.of(p);
                } catch (Exception ignored) {}
            }

            this.name = name;
            this.description = description;
            this.background = bg;
            this.iconTexture = iconTexture;
        }

        public boolean search(String query) {
            String q = query.toLowerCase();
            return this.getName().toLowerCase().contains(q) || path.getFileName().toString().contains(q);
        }

        public Path getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public CardBackground getBackground() {
            return background;
        }

        public FileTexture getIcon() {
            return iconTexture;
        }

        public boolean isExpanded() {
            return properties.expanded;
        }

        public void setExpanded(boolean expanded) {
            properties.expanded = expanded;
            saveProperties();
        }

        public boolean isFavourite() {
            return properties.favourite;
        }

        public void setFavourite(boolean favourite) {
            properties.favourite = favourite;
            saveProperties();
        }

        private void saveProperties() {
            String key = this.path.toFile().getAbsolutePath();
            SAVED_DATA.put(key, properties);
        }
    }

    /**
     * Represents a path were its sub paths contains an avatar.
     */
    public static class FolderPath extends AvatarPath {

        protected final List<AvatarPath> children = new ArrayList<>();

        public FolderPath(Path path) {
            super(path);
        }

        /**
         * Recursively traverses the filesystem looking for avatars under this folder.
         * @return Whether we found an avatar in our recursive searching.
         * Either in this folder or in one of its sub folders.
         * If we didn't, then this folder can get ignored and not added as a child in another folder.
         * We only want our FolderPath to contain sub-folders that actually have avatars.
         */
        public boolean fetch() {
            File[] files = path.toFile().listFiles();
            if (files == null)
                return false;

            boolean found = false;

            //iterate over all files on this path
            //but skip non-folders and non-moon
            for (File file : files) {
                Path path = file.toPath();
                boolean moon = FiguraMod.DEBUG_MODE && path.toString().toLowerCase().endsWith(".moon");

                if (!Files.isDirectory(path) && !moon)
                    continue;

                Path metadata = path.resolve("avatar.json");
                if (moon || (Files.exists(metadata) && !Files.isDirectory(metadata))) {
                    children.add(new AvatarPath(path));
                    found = true;
                } else {
                    FolderPath folder = new FolderPath(file.toPath());
                    if (folder.fetch()) {
                        children.add(folder);
                        found = true;
                    }
                }
            }

            return found;
        }

        @Override
        public boolean search(String query) {
            boolean result = super.search(query);

            for (AvatarPath child : children) {
                if (result) break;
                result = child.search(query);
            }

            return result;
        }

        public List<AvatarPath> getChildren() {
            return children;
        }
    }

    private static class Properties {
        public boolean expanded = true;
        public boolean favourite;
    }
}
