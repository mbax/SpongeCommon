/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.common.config.SpongeConfig.Type.GLOBAL;

import com.google.inject.Injector;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactoryUtils;
import org.spongepowered.api.event.game.state.GameStateEvent;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.launch.SpongeLaunch;
import org.spongepowered.common.registry.SpongeGameRegistry;

import java.nio.file.Path;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class SpongeImpl {

    public static final String API_NAME = "SpongeAPI";
    public static final String API_ID = "spongeapi";

    public static final String ECOSYSTEM_NAME = "Sponge";
    public static final String ECOSYSTEM_ID = "sponge";

    @Nullable
    private static SpongeImpl instance;

    private static final Path gameDir = SpongeLaunch.getGameDirectory();
    private static final Path configDir = SpongeLaunch.getConfigDirectory();
    private static final Path pluginsDir = SpongeLaunch.getPluginsDirectory();
    private static final Path spongeConfigDir = configDir.resolve(ECOSYSTEM_ID);

    @Nullable private static SpongeConfig<SpongeConfig.GlobalConfig> globalConfig;

    private final Injector injector;
    private final Game game;
    private final Logger logger;
    private final org.slf4j.Logger slf4jLogger;
    private final PluginContainer plugin;
    private final PluginContainer minecraftPlugin;

    @Inject
    public SpongeImpl(Injector injector, Game game, Logger logger,
                      @Named(SpongeImpl.ECOSYSTEM_NAME) PluginContainer plugin, @Named("Minecraft") PluginContainer minecraftPlugin) {

        checkState(instance == null, "Sponge was already initialized");
        instance = this;

        this.injector = checkNotNull(injector, "injector");
        this.game = checkNotNull(game, "game");
        this.logger = checkNotNull(logger, "logger");
        this.slf4jLogger = LoggerFactory.getLogger(this.logger.getName());
        this.plugin = checkNotNull(plugin, "plugin");
        this.minecraftPlugin = checkNotNull(minecraftPlugin, "minecraftPlugin");
    }

    public static SpongeImpl getInstance() {
        checkState(instance != null, "Sponge was not initialized");
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static Injector getInjector() {
        return getInstance().injector;
    }

    public static SpongeGame getGame() {
        return (SpongeGame) getInstance().game;
    }

    public static SpongeGameRegistry getRegistry() {
        return getGame().getRegistry();
    }

    public static boolean postEvent(Event event) {
        return getGame().getEventManager().post(event);
    }

    public static Logger getLogger() {
        return getInstance().logger;
    }

    public static org.slf4j.Logger getSlf4jLogger() {
        return getInstance().slf4jLogger;
    }

    public static PluginContainer getPlugin() {
        return getInstance().plugin;
    }

    public static PluginContainer getMinecraftPlugin() {
        return getInstance().minecraftPlugin;
    }

    public static Path getGameDir() {
        return gameDir;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static Path getPluginsDir() {
        return pluginsDir;
    }

    public static Path getSpongeConfigDir() {
        return spongeConfigDir;
    }

    public static SpongeConfig<SpongeConfig.GlobalConfig> getGlobalConfig() {
        if (globalConfig == null) {
            globalConfig = new SpongeConfig<>(GLOBAL, spongeConfigDir.resolve("global.conf"), ECOSYSTEM_ID);
        }

        return globalConfig;
    }

    public static void postState(Class<? extends GameStateEvent> type, GameState state) {
        getGame().setState(state);
        ((SpongeEventManager) getGame().getEventManager()).post(SpongeEventFactoryUtils.createState(type, getGame()), true);
    }
    
    public static void postShutdownEvents() {
        postState(GameStoppingEvent.class, GameState.GAME_STOPPING);
        postState(GameStoppedEvent.class, GameState.GAME_STOPPED);
    }

}
