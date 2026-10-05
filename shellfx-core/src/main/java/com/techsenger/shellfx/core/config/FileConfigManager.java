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

import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default {@link ConfigManager}: holds the configs of a {@link ConfigFile} and writes them back when
 * {@link #save()} is called, which is left to whoever stops the module. Always used on the UI thread.
 *
 * @author Pavel Castornii
 */
public class FileConfigManager extends AbstractConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(FileConfigManager.class);

    private final ConfigFile file;

    /**
     * Creates a manager over the configs {@code file} already holds; the caller reads the file beforehand.
     *
     * @param file the file to save the configs to.
     */
    public FileConfigManager(ConfigFile file) {
        super(file.getData());
        this.file = file;
    }

    /**
     * Writes all configs to the file, first warning about every listener that is still registered on a config:
     * by then the components are gone, so such a listener is a leak.
     */
    public void save() {
        warnAboutListeners(getData().getConfigsByClass());
        warnAboutListeners(getData().getConfigsByUuid());
        try {
            file.write();
        } catch (IOException ex) {
            logger.warn("Couldn't save configs to {}", file.getPath(), ex);
        }
    }

    private <K> void warnAboutListeners(Map<K, AbstractConfig> configs) {
        configs.forEach((key, config) -> {
            for (var listener : config.getListeners()) {
                logger.warn("Config {} ({}) is saved while listener {} is still registered on it, probably a leak",
                        key, config.getClass().getName(), listener.getClass().getName());
            }
        });
    }
}
