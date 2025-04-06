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

package com.techsenger.tabshell.core.file;

import java.net.URI;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFileStorage implements FileStorage {

    private final FileStorageType type;

    private final String displayName;

    private final URI rootUri;

    /**
     * For performance uri is used as a string.
     */
    private final String rootUriAsString;

    private final boolean isDefault;

    public AbstractFileStorage(FileStorageType type, String displayName, URI rootUri) {
        this(type, displayName, rootUri, false);
    }

    AbstractFileStorage(FileStorageType type, String displayName, URI rootUri, boolean isDefault) {
        this.type = type;
        this.displayName = displayName;
        var normalized = rootUri.normalize();
        this.rootUri = normalized;
        this.rootUriAsString = normalized.toString();
        this.isDefault = isDefault;
    }

    @Override
    public FileStorageType getType() {
        return type;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public final boolean isDefault() {
        return isDefault;
    }

    @Override
    public URI getRootUri() {
        return rootUri;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.type);
        hash = 79 * hash + Objects.hashCode(this.displayName);
        hash = 79 * hash + Objects.hashCode(this.rootUriAsString);
        hash = 79 * hash + (this.isDefault ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractFileStorage other = (AbstractFileStorage) obj;
        if (this.isDefault != other.isDefault) {
            return false;
        }
        if (!Objects.equals(this.displayName, other.displayName)) {
            return false;
        }
        if (!Objects.equals(this.rootUriAsString, other.rootUriAsString)) {
            return false;
        }
        return this.type == other.type;
    }

    String getRootUriAsString() {
        return rootUriAsString;
    }
}
