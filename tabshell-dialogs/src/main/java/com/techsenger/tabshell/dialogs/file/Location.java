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

package com.techsenger.tabshell.dialogs.file;

import com.techsenger.tabshell.material.icon.StyleFontIcon;
import com.techsenger.tabshell.storage.FileStorage;
import java.net.URI;

/**
 *
 * @author Pavel Castornii
 */
class Location {

    private final StyleFontIcon icon;

    private final String name;

    private final int level;

    private final FileStorage storage;

    private final URI uri;

    Location(StyleFontIcon icon, String name, int level, FileStorage storage, URI uri) {
        this.icon = icon;
        this.name = name;
        this.level = level;
        this.storage = storage;
        this.uri = uri;
    }

    public StyleFontIcon getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public FileStorage getStorage() {
        return storage;
    }

    public URI getUri() {
        return uri;
    }
}
