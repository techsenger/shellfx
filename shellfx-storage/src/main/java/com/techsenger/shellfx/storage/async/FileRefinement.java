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

/**
 * What a {@link FileRefiner} found out about a file in the second phase. The file is not changed until the client
 * calls {@link #apply} on the UI thread, and the client then decorates it.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public final class FileRefinement<F extends StorageFile> {

    private final F file;

    private final Runnable action;

    FileRefinement(F file, Runnable action) {
        this.file = file;
        this.action = action;
    }

    public F getFile() {
        return file;
    }

    /**
     * Gives the file what was found out. It must be called on the UI thread.
     */
    public void apply() {
        action.run();
    }
}
