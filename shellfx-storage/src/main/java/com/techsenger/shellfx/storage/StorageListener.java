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

/**
 * Notified by a {@link FileStorage} whenever it performs a {@link FileOperation} on one of its files.
 *
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface StorageListener {

    /**
     * Called after {@code operation} has completed successfully on the file at {@code uri}; always called,
     * regardless of {@code operationType} - it is this listener's own choice whether/how to react differently
     * to a {@link OperationType#SECONDARY} operation.
     *
     * @param uri the URI of the file it was performed on.
     * @param operation the operation performed.
     * @param operationType whether this was a directly user-initiated action or a step of bulk/internal
     *     machinery.
     */
    void onOperation(URI uri, FileOperation operation, OperationType operationType);
}
