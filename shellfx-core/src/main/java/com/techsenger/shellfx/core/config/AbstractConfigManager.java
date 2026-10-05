/*
 * Copyright 2024-2026 Pavel Castornii.
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
import java.util.Map;
import java.util.UUID;

/**
 * Keeps the configs of a {@link ConfigData}; whether and where they are persisted is up to the subclass.
 * Always used on the UI thread.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractConfigManager implements ConfigManager {

    private final ConfigData data;

    AbstractConfigManager(ConfigData data) {
        this.data = data;
    }

    @Override
    public <T extends AbstractConfig> @Nullable T getConfig(Class<T> type) {
        return find(data.getConfigsByClass(), type);
    }

    @Override
    public <T extends AbstractConfig> T getOrCreateConfig(Class<T> type, Factory<T> factory) {
        return getOrCreate(data.getConfigsByClass(), type, factory);
    }

    @Override
    public <T extends AbstractConfig> void putConfig(Class<T> type, T config) {
        data.getConfigsByClass().put(type, config);
    }

    @Override
    public void removeConfig(Class<? extends AbstractConfig> type) {
        data.getConfigsByClass().remove(type);
    }

    @Override
    public <T extends AbstractConfig> @Nullable T getConfig(UUID uuid) {
        return find(data.getConfigsByUuid(), uuid);
    }

    @Override
    public <T extends AbstractConfig> T getOrCreateConfig(UUID uuid, Factory<T> factory) {
        return getOrCreate(data.getConfigsByUuid(), uuid, factory);
    }

    @Override
    public void putConfig(UUID uuid, AbstractConfig config) {
        data.getConfigsByUuid().put(uuid, config);
    }

    @Override
    public void removeConfig(UUID uuid) {
        data.getConfigsByUuid().remove(uuid);
    }

    final ConfigData getData() {
        return data;
    }

    @SuppressWarnings("unchecked")
    private <K, T extends AbstractConfig> @Nullable T find(Map<K, AbstractConfig> configs, K key) {
        return (T) configs.get(key);
    }

    private <K, T extends AbstractConfig> T getOrCreate(Map<K, AbstractConfig> configs, K key, Factory<T> factory) {
        @Nullable T config = find(configs, key);
        if (config == null) {
            config = factory.create();
            configs.put(key, config);
        }
        return config;
    }
}
