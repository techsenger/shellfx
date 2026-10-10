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

/**
 * The {@link FileOperation}s of a {@link FileStorage}. Each constant documents which {@link FileEntryType} the entry
 * at the reported URI has - some operations apply to exactly one, letting a listener tell files and directories
 * apart from the operation alone; others apply to either, and don't. An application extends it to declare its own
 * operations next to these ones.
 *
 * @author Pavel Castornii
 */
public class FileOperations {

    /**
     * A file's content was read (in full, or via a stream/header peek). Always {@link FileEntryType#FILE} - a
     * directory has no byte content to read this way, see {@link #LIST} for its equivalent.
     */
    public static final FileOperation READ = new DefaultFileOperation("READ");

    /**
     * A file's content was written. Always {@link FileEntryType#FILE} - a directory has no generic byte-level
     * write; its own content (its entries) only changes via {@link #CREATE} or {@link #RENAME}.
     */
    public static final FileOperation WRITE = new DefaultFileOperation("WRITE");

    /**
     * A new, empty entry appeared at the reported URI: a regular file or a directory, so the entry type isn't
     * implied by the operation alone. Writing content to a not-yet-existing file also brings a new file into
     * existence, but is reported as {@link #WRITE}, not as this constant.
     */
    public static final FileOperation CREATE = new DefaultFileOperation("CREATE");

    /**
     * A file or directory was renamed, reported for its new URI. Either {@link FileEntryType#FILE} or
     * {@link FileEntryType#DIRECTORY} - unlike the other constants here, the entry type isn't implied by the
     * operation itself and must be resolved from the entry if it's needed.
     */
    public static final FileOperation RENAME = new DefaultFileOperation("RENAME");

    /**
     * A directory's direct children were listed, reported for the listed directory itself (not its children): its
     * equivalent of {@link #READ}, since its "content" is its list of entries rather than bytes. Looking up a single
     * entry or the chain of entries leading to it is reported the same way, for the requested URI, so there it is
     * the URI of that entry - a file or a directory.
     */
    public static final FileOperation LIST = new DefaultFileOperation("LIST");

    protected FileOperations() {
        // empty
    }
}
