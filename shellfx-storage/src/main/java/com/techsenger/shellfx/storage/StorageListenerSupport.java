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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link FileStorage}'s {@link StorageListener} bookkeeping, meant to be held as a field and delegated to - a
 * storage that can't extend {@link AbstractFileStorage} holds this state itself.
 *
 * @author Pavel Castornii
 */
public final class StorageListenerSupport {

    private final List<StorageListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Registers {@code listener}; it starts receiving every subsequent {@link #notifyListeners} call.
     *
     * @param listener the listener to register.
     */
    public void addListener(StorageListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters {@code listener}; a no-op if it isn't currently registered.
     *
     * @param listener the listener to unregister.
     */
    public void removeListener(StorageListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies every currently registered listener that {@code operation} was performed on the file at
     * {@code uri}.
     *
     * @param uri the URI of the file it was performed on.
     * @param operation the operation performed.
     * @param operationType whether this was a directly user-initiated action or a step of bulk/internal
     *     machinery.
     */
    public void notifyListeners(URI uri, FileOperation operation, OperationType operationType) {
        for (var listener : listeners) {
            listener.onOperation(uri, operation, operationType);
        }
    }
}
