/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.core.settings.xml;

import java.nio.file.Path;

/**
 * Manager, for reading/writing settings from/to file.
 *
 * @author Pavel Castornii
 */
public final class SettingsFile<T extends XmlSettings> {

    private final XmlFileHandler<T> fileHandler;

    public SettingsFile(Class<T> settingsClass, Path path) {
        this.fileHandler = new XmlFileHandler<>(settingsClass, path);
    }

    public T getSettings() {
        return fileHandler.getObject();
    }

    public void read() {
        this.fileHandler.read();
    }

    public void write() {
        this.fileHandler.write();
    }
}
