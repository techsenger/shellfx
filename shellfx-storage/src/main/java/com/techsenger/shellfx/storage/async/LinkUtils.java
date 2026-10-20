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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.storage.DefaultEntryLink;
import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.FileStorageType;
import com.techsenger.shellfx.storage.FileStorageUtils;
import com.techsenger.shellfx.storage.OperationType;
import com.techsenger.shellfx.storage.StorageFile;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.List;

/**
 * Finds the targets of the links, which may lie in another storage than the link itself.
 *
 * @author Pavel Castornii
 */
public final class LinkUtils {

    /**
     * Tells whether the file is a link whose target is not known yet but whose resolved URI is.
     *
     * @param file the file
     * @return {@code true} if the target of the link is to be found
     */
    public static boolean isUnresolved(StorageFile file) {
        var link = file.getLink();
        return link != null && link.getTarget() == null && link.getResolvedUri() != null;
    }

    /**
     * Tells whether the file is a link of a network storage whose target is not known yet. Such a link is left to the
     * refiner, as finding its target may be slow.
     *
     * @param file the file
     * @return {@code true} if the target of the link is to be found in the second phase
     */
    public static boolean isDeferred(StorageFile file) {
        return file.getStorage().getType() == FileStorageType.NETWORK && isUnresolved(file);
    }

    /**
     * Gets the target of a link from the storage that holds the resolved URI.
     *
     * @param <F> the type of files
     * @param storages the storages the target may lie in
     * @param file the file that is a link
     * @return the target, or {@code null}, and the link is broken, if no storage holds it or nothing exists there
     * @throws IOException if the storage can't be read
     */
    public static <F extends StorageFile> @Nullable F findTarget(List<? extends FileStorage<F>> storages,
            StorageFile file) throws IOException {
        var link = file.getLink();
        var resolvedUri = link == null ? null : link.getResolvedUri();
        if (resolvedUri == null) {
            return null;
        }
        var storage = FileStorageUtils.findByUri(storages, resolvedUri);
        if (storage.isEmpty()) {
            // no storage holds the end of the chain
            return null;
        }
        try {
            return storage.get().getFile(resolvedUri, OperationType.SECONDARY);
        } catch (NoSuchFileException ex) {
            // nothing exists at the end of the chain
            return null;
        }
    }

    /**
     * Finds the target of a link and sets it, which must be done on a thread that owns the file, as it is not
     * observable.
     *
     * @param <F> the type of files
     * @param storages the storages the target may lie in
     * @param file the file that is a link
     * @throws IOException if the storage can't be read
     */
    public static <F extends StorageFile> void resolve(List<? extends FileStorage<F>> storages, StorageFile file)
            throws IOException {
        setTarget(file, findTarget(storages, file));
    }

    /**
     * Sets the target of a link, which must be done on a thread that owns the file, as it is not observable.
     *
     * @param file the file that is a link
     * @param target the target, or {@code null} to leave the link as it is
     */
    public static void setTarget(StorageFile file, @Nullable StorageFile target) {
        if (target != null && file.getLink() instanceof DefaultEntryLink defaultLink) {
            defaultLink.setTarget(target);
        }
    }

    private LinkUtils() {
        // empty
    }
}
