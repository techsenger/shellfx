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

package com.techsenger.shellfx.demo.dialogs;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.storage.DefaultStorageFile;
import com.techsenger.shellfx.storage.FileEntryType;
import com.techsenger.shellfx.storage.FileStorage;
import java.net.URI;

/**
 * A file of the demo storage. A pending one is listed without its size and modified time, which the second loading
 * phase fills in.
 *
 * @author Pavel Castornii
 */
final class DemoFile extends DefaultStorageFile {

    private final @Nullable Long finalSize;

    private final long finalModifiedTime;

    private volatile boolean pending;

    DemoFile(FileStorage<?> storage, URI uri, String name, boolean directory, @Nullable Long size,
            long modifiedTime, boolean pending) {
        setStorage(storage);
        setUri(uri);
        setName(name);
        setEntryType(directory ? FileEntryType.DIRECTORY : FileEntryType.FILE);
        this.finalSize = size;
        this.finalModifiedTime = modifiedTime;
        this.pending = pending;
        if (!pending) {
            setSize(size);
            setModifiedTime(modifiedTime);
        }
    }

    @Override
    protected void setRoot(boolean root) {
        super.setRoot(root);
    }

    boolean isPending() {
        return pending;
    }

    /**
     * Fills in the size and the modified time, as the second phase does for a file that was listed without them.
     */
    void complete() {
        setSize(finalSize);
        setModifiedTime(finalModifiedTime);
        this.pending = false;
    }
}
