package org.moon.figura.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Objects;
import java.util.function.Consumer;

public final class FiguraResourceListener implements ResourceManagerReloadListener {
    private final String id;
    private final Consumer<ResourceManager> reloadConsumer;

    public FiguraResourceListener(String id, Consumer<ResourceManager> reloadConsumer) {
        this.id = id;
        this.reloadConsumer = reloadConsumer;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        reloadConsumer.accept(manager);
    }

    public String getId() {
        return id;
    }
}
