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
 * The structural type of a file system entry.
 *
 * @author Pavel Castornii
 */
public enum FileEntryType {

    /**
     * A directory.
     */
    DIRECTORY,

    /**
     * A regular file whose content can be safely read, written, or opened.
     */
    FILE,

    /**
     * A symbolic link.
     */
    SYMBOLIC_LINK,

    /**
     * An entry that is none of the above — a Unix domain socket, named pipe (FIFO), device file, or similar
     * special file.
     *
     * <p>Such entries must never be opened for reading or writing: opening a FIFO for reading blocks
     * indefinitely unless a writer is simultaneously connected on the other end, and other special files can
     * have similarly unsafe or undefined open/read semantics. Callers that read file content, generate
     * previews, or inspect file headers (e.g. for icon or type detection) must check for this type first and
     * skip any such attempt.
     */
    OTHER
}
