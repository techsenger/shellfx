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

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Loads the files of a directory and assembles the final result in the files themselves. The first phase lists the
 * directory and gives every file its icon, style and state at once. Only the files of a network storage are loaded in
 * two phases, so a slow storage never keeps the user waiting: the second phase refines, in the background, the files
 * that are not ready, and rewrites the icon, style and state of each as soon as it is done. A file that is not ready
 * yet has a state icon. The files of any other storage are final after the first phase. The storages themselves only
 * list: whatever needs more than a listing is done here.
 *
 * <p>A provider serves one list at a time: loading a directory cancels the refinement of the previous one.
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
     * Runs the first phase: lists the directory and gives every file its state, icon and style; a file of a network
     * storage that the second phase has to refine gets {@link FileState#PENDING}. The refinement of the previous
     * directory, if any, is cancelled.
     *
     * @param storage the storage that holds the directory
     * @param uri the URI of the directory
     * @param operationType how the storage classifies the listing it reports
     * @return the files of the directory, to be shown right away
     * @throws IOException if the directory can't be listed
     */
    List<F> getFiles(FileStorage<F> storage, URI uri, OperationType operationType) throws IOException;

    /**
     * Returns the root directory of the storage, with its icon, style and state like the files of the first phase.
     * The root is never refined.
     *
     * @param storage the storage whose root to return
     * @return the root, which belongs to the caller
     */
    F getRoot(FileStorage<F> storage);

    /**
     * Returns the directories that lead from the root of the storage to the directory at the URI, each with its
     * icon, style and state like the files of the first phase. It does not cancel the refinement.
     *
     * @param storage the storage that holds the directory
     * @param uri the URI of the directory
     * @param operationType how the storage classifies the access it reports
     * @return the directories from the root to the one at the URI, which belong to the caller
     * @throws IOException if the hierarchy can't be read
     */
    List<F> getHierarchy(FileStorage<F> storage, URI uri, OperationType operationType) throws IOException;

    /**
     * Gives the files that the client has got from the storage itself their icon, style and state, like the files of
     * the first phase, so a client that reads the storage directly can still use the second phase for them.
     *
     * @param storage the storage the files come from
     * @param files the files to assemble, which belong to the caller
     */
    void assembleFiles(FileStorage<F> storage, List<F> files);

    /**
     * Runs the second phase in the background, which only the files of a network storage need: refines the files
     * that are not ready, a few at a time for every storage, and rewrites their state on a background thread and
     * their icon and style on the UI thread, where the elements that show the files observe them. The refinement of
     * the previous call, if any, is cancelled.
     *
     * @param files the files of the first phase
     * @param onFinished called once on the UI thread when every file has been handled; it is not called if there
     *     was nothing to refine or the refinement is cancelled
     */
    void refineFilesAsync(List<F> files, Runnable onFinished);

    /**
     * Stops the refinement that is running: the files still waiting stay in the state they have, and the callback
     * of the refinement is not called any more.
     */
    void cancelRefinement();
}
