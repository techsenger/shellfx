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

package com.techsenger.shellfx.storage;

import java.net.URI;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFileStorage<T extends StorageFile> implements FileStorage<T> {

    private final FileStorageType type;

    private final String name;

    private final URI uri;

    private final StorageListenerSupport listenerSupport = new StorageListenerSupport();

    public AbstractFileStorage(FileStorageType type, String name, URI uri) {
        this.type = type;
        this.name = name;
        var normalized = uri.normalize();
        this.uri = normalized;
    }

    @Override
    public FileStorageType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public void addListener(StorageListener listener) {
        listenerSupport.addListener(listener);
    }

    @Override
    public void removeListener(StorageListener listener) {
        listenerSupport.removeListener(listener);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.type);
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.uri.toString());
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
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.uri.toString(), other.uri.toString())) {
            return false;
        }
        return this.type == other.type;
    }

    @Override
    public String toString() {
        return "AbstractFileStorage[" + "type=" + type + ", name=" + name + ", uri=" + uri + ']';
    }

    /**
     * Reports to the listeners of this storage that {@code operation} succeeded on the file at {@code uri}.
     *
     * @param uri the URI of the file it was performed on
     * @param operation the operation performed
     * @param operationType whether this was a directly user-initiated action or a step of bulk/internal machinery
     */
    protected void notifyListeners(URI uri, FileOperation operation, OperationType operationType) {
        listenerSupport.notifyListeners(uri, operation, operationType);
    }
}
