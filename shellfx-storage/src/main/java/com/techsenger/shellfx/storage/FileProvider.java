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
import java.util.function.Consumer;

/**
 * Loads the files of a directory and gives them their state. The first phase lists the directory and gives every file
 * its state at once. What is slow to find out is left to the second phase, so a slow storage never keeps the user
 * waiting: it refines, in the background, the files that are not ready, and gives each its final state as soon as it
 * is done. The files that need nothing slow are final after the first phase. The storages themselves only list:
 * whatever needs more than a listing is done here. The provider does not decorate the files, that is up to the
 * client, see {@link FileDecorator}.
 *
 * <p>Both phases run in the background, and their callbacks are called on a background thread. Every asynchronous
 * method takes a callback for the data and one for the end, and returns the {@link FileProviderTask} of the
 * operation. {@code onFinished} is called once, with {@link FileProviderResult#SUCCESS} when all the data has been
 * given, {@link FileProviderResult#FAILED} if the operation failed, which the provider has logged, or
 * {@link FileProviderResult#CANCELLED} if the task was cancelled before it ended; the data is not given after the
 * cancellation. The only exception is {@link #refineFilesAsync}, which ends with nothing if there was nothing to
 * refine. The provider keeps no state of the operations, so one provider serves any number of lists at once, and the
 * client cancels the tasks it no longer needs.
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
     * Runs the first phase for a directory: lists it and gives every file its state; a file that the second phase has
     * to refine gets {@link FileState#PENDING}.
     *
     * @param storage the storage that holds the directory
     * @param uri the URI of the directory
     * @param operationType how the storage classifies the listing it reports
     * @param onLoaded called with the files of the directory, to be shown right away
     * @param onFinished called with {@code SUCCESS} after the files, or with {@code FAILED} if the directory can't be
     *     listed
     * @return the task of the operation
     */
    FileProviderTask getFilesAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<List<F>> onLoaded, Consumer<FileProviderResult> onFinished);

    /**
     * Runs the first phase for the file at the URI, giving it its state.
     *
     * @param storage the storage that holds the file
     * @param uri the URI of the file
     * @param operationType how the storage classifies the access it reports
     * @param onLoaded called with the file, which belongs to the caller
     * @param onFinished called with {@code SUCCESS} after the file, or with {@code FAILED} if it can't be read
     * @return the task of the operation
     */
    FileProviderTask getFileAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<F> onLoaded, Consumer<FileProviderResult> onFinished);

    /**
     * Runs the first phase for the directories that lead from the root of the storage to the directory at the URI,
     * giving each its state.
     *
     * @param storage the storage that holds the directory
     * @param uri the URI of the directory
     * @param operationType how the storage classifies the access it reports
     * @param onLoaded called with the directories from the root to the one at the URI, which belong to the caller
     * @param onFinished called with {@code SUCCESS} after the directories, or with {@code FAILED} if the hierarchy
     *     can't be read
     * @return the task of the operation
     */
    FileProviderTask getHierarchyAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<List<F>> onLoaded, Consumer<FileProviderResult> onFinished);

    /**
     * Runs the first phase for the root directory of the storage, giving it its state. The root may be a real
     * directory, so it is read in the background, and it is never refined.
     *
     * @param storage the storage whose root to get
     * @param onLoaded called with the root, which belongs to the caller
     * @param onFinished called with {@code SUCCESS} after the root, or with {@code FAILED} if it can't be read
     * @return the task of the operation
     */
    FileProviderTask getRootAsync(FileStorage<F> storage, Consumer<F> onLoaded,
            Consumer<FileProviderResult> onFinished);

    /**
     * Runs the first phase for the root directories of all the storages of this provider, giving each its state.
     *
     * @param onLoaded called with the roots in the order of {@link #getStorages()}, which belong to the caller
     * @param onFinished called with {@code SUCCESS} after the roots, or with {@code FAILED} if any of them can't be
     *     read
     * @return the task of the operation
     */
    FileProviderTask getRootsAsync(Consumer<List<F>> onLoaded, Consumer<FileProviderResult> onFinished);

    /**
     * Runs the second phase in the background, which only some files need: refines the files that are not ready, a
     * few at a time for every storage, and gives each its final state.
     *
     * @param files the files of the first phase
     * @param onRefined called for every file as soon as it is ready or has failed, with the final state already in
     *     the file
     * @param onFinished called with {@code SUCCESS} once every file has been handled; it is not called if there was
     *     nothing to refine
     * @return the task of the operation
     */
    FileProviderTask refineFilesAsync(List<F> files, Consumer<F> onRefined, Consumer<FileProviderResult> onFinished);
}
