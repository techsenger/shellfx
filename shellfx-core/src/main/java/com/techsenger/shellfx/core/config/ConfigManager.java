/*
 * Copyright 2026 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.shellfx.core.config;

import com.techsenger.annotations.Nullable;
import com.techsenger.toolkit.core.function.Factory;
import java.util.UUID;

/**
 * Owns the configs of the application. A config is either shared by class (one per application, e.g. view modes
 * of a panel) or belongs to one component instance, identified by its UUID.
 *
 * @author Pavel Castornii
 */
public interface ConfigManager {

    /**
     * Returns the shared config of {@code type}, or {@code null} if there is none.
     *
     * @param type the config class.
     * @param <T> the config type.
     */
    <T extends AbstractConfig> @Nullable T getConfig(Class<T> type);

    /**
     * Returns the shared config of {@code type}, creating it with {@code factory} if there is none yet.
     *
     * @param type the config class.
     * @param factory creates the config with its default values.
     * @param <T> the config type.
     */
    <T extends AbstractConfig> T getOrCreateConfig(Class<T> type, Factory<T> factory);

    /**
     * Stores {@code config} as the shared config of {@code type}, replacing the previous one.
     *
     * @param type the config class.
     * @param config the config to store.
     * @param <T> the config type.
     */
    <T extends AbstractConfig> void putConfig(Class<T> type, T config);

    /**
     * Removes the shared config of {@code type}; does nothing if there is none.
     *
     * @param type the config class.
     */
    void removeConfig(Class<? extends AbstractConfig> type);

    /**
     * Returns the config of the component instance {@code uuid}, or {@code null} if there is none.
     *
     * @param uuid the identifier of the component instance.
     * @param <T> the config type.
     */
    <T extends AbstractConfig> @Nullable T getConfig(UUID uuid);

    /**
     * Returns the config of the component instance {@code uuid}, creating it with {@code factory} if there is
     * none yet.
     *
     * @param uuid the identifier of the component instance.
     * @param factory creates the config with its default values.
     * @param <T> the config type.
     */
    <T extends AbstractConfig> T getOrCreateConfig(UUID uuid, Factory<T> factory);

    /**
     * Stores {@code config} as the config of the component instance {@code uuid}, replacing the previous one.
     *
     * @param uuid the identifier of the component instance.
     * @param config the config to store.
     */
    void putConfig(UUID uuid, AbstractConfig config);

    /**
     * Removes the config of the component instance {@code uuid}, e.g. once its tab is closed for good; does
     * nothing if there is none.
     *
     * @param uuid the identifier of the component instance.
     */
    void removeConfig(UUID uuid);
}
