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

import com.techsenger.shellfx.storage.StorageFile;
import java.util.List;
import java.util.function.Consumer;

/**
 * The second phase of loading a directory, which only some files need. What is slow to find out is left to it, so a
 * slow storage never keeps the user waiting: the client shows the files of the {@link FileProvider} at once, marks the
 * files that {@link #isRefinable} accepts as temporarily decorated, and lets the refiner refine them in the
 * background. Every file is given to the client as soon as it is refined, not when all of them are, as a
 * {@link FileRefinement} that the client applies on the UI thread and then decorates for good. The refiner itself never
 * changes a file.
 *
 * <p>The refiner keeps no state of the operations, so one refiner serves any number of lists at once, and the client
 * cancels the tasks it no longer needs. The callbacks are called on a background thread, and the end callback is
 * called once, in the same way as the one of the provider. A client that needs no second phase uses the provider
 * alone.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public interface FileRefiner<F extends StorageFile> {

    /**
     * Tells whether the file listed by the provider needs the second phase.
     *
     * @param file the file listed in the first phase
     * @return {@code true} if the file has to be marked as pending and refined
     */
    boolean isRefinable(F file);

    /**
     * Refines the files in the background, a few at a time for every storage.
     *
     * @param files the files that {@link #isRefinable} accepted; the list is read in the background, so it must not
     *     be changed after the call
     * @param onRefined called with the refinement of a file as soon as the file is handled; a file that could not be
     *     refined, which is logged, is given a refinement that changes nothing
     * @param onFinished called once, with {@code SUCCESS} after the last file, or at once if there are no files
     * @return the task of the operation
     */
    AsyncTask refineFilesAsync(List<F> files, Consumer<FileRefinement<F>> onRefined, Consumer<AsyncResult> onFinished);
}
