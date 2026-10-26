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

package com.techsenger.shellfx.storage.async;

import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.OperationType;
import com.techsenger.shellfx.storage.StorageFile;
import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides the files of a directory, a root and so on, completing what is quick to find out. What is slow to find out
 * is left to the second phase, which is up to the {@link FileRefiner}, and giving the files their icon and style is up
 * to the client and the resolvers. The storages themselves only list: whatever needs more than a listing is done
 * here.
 *
 * <p>The provider works in the background, and its callbacks are called on a background thread. Every asynchronous
 * method takes a callback for the data and one for the end, and returns the {@link AsyncTask} of the operation.
 * {@code onFinished} is called once, with {@link AsyncStatus#SUCCESS} when all the data has been given,
 * {@link AsyncStatus#FAILED} if the operation failed, which the provider has logged, or {@link AsyncStatus#CANCELLED}
 * if the task was cancelled before it ended; the data is not given after the cancellation. A failed result carries
 * the exception that failed the operation, so the client can tell the user why. The provider keeps no state of the
 * operations, so one provider serves any number of lists at once, and the client cancels the tasks it no longer
 * needs.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public interface FileProvider<F extends StorageFile> {

    /**
     * Returns the storages the files of this provider come from, and where the target of a link may lie.
     *
     * @return the storages, never {@code null}
     */
    List<? extends FileStorage<F>> getStorages();

    /**
     * Provides a directory: lists it. The files that need the second phase are marked by the client, see
     * {@link FileRefiner}.
     *
     * @param storage the storage that holds the directory
     * @param uri the URI of the directory
     * @param operationType how the storage classifies the listing it reports
     * @param onProvided called with the files of the directory, to be shown right away
     * @param onFinished called with {@code SUCCESS} after the files, or with {@code FAILED} if the directory can't be
     *     listed
     * @return the task of the operation
     */
    AsyncTask provideFilesAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<List<F>> onProvided, Consumer<AsyncResult> onFinished);

    /**
     * Provides the file at the URI.
     *
     * @param storage the storage that holds the file
     * @param uri the URI of the file
     * @param operationType how the storage classifies the access it reports
     * @param onProvided called with the file, which belongs to the caller
     * @param onFinished called with {@code SUCCESS} after the file, or with {@code FAILED} if it can't be read
     * @return the task of the operation
     */
    AsyncTask provideFileAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<F> onProvided, Consumer<AsyncResult> onFinished);

    /**
     * Provides the directories that lead from the root of the storage to the directory at the URI.
     *
     * @param storage the storage that holds the directory
     * @param uri the URI of the directory
     * @param operationType how the storage classifies the access it reports
     * @param onProvided called with the directories from the root to the one at the URI, which belong to the caller
     * @param onFinished called with {@code SUCCESS} after the directories, or with {@code FAILED} if the hierarchy
     *     can't be read
     * @return the task of the operation
     */
    AsyncTask provideHierarchyAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<List<F>> onProvided, Consumer<AsyncResult> onFinished);

    /**
     * Provides the root directory of the storage. The root may be a real directory, so it is read in the background.
     *
     * @param storage the storage whose root to get
     * @param onProvided called with the root, which belongs to the caller
     * @param onFinished called with {@code SUCCESS} after the root, or with {@code FAILED} if it can't be read
     * @return the task of the operation
     */
    AsyncTask provideRootAsync(FileStorage<F> storage, Consumer<F> onProvided,
            Consumer<AsyncResult> onFinished);

    /**
     * Provides the root directories of all the storages of this provider.
     *
     * @param onProvided called with the roots in the order of {@link #getStorages()}, which belong to the caller
     * @param onFinished called with {@code SUCCESS} after the roots, or with {@code FAILED} if any of them can't be
     *     read
     * @return the task of the operation
     */
    AsyncTask provideRootsAsync(Consumer<List<F>> onProvided, Consumer<AsyncResult> onFinished);
}
