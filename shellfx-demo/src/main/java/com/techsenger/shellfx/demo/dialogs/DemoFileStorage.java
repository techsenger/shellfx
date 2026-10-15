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
import com.techsenger.shellfx.storage.AbstractFileStorage;
import com.techsenger.shellfx.storage.FileEntryType;
import com.techsenger.shellfx.storage.FileOperations;
import com.techsenger.shellfx.storage.FileStorageType;
import com.techsenger.shellfx.storage.FileStorageUtils;
import com.techsenger.shellfx.storage.OperationType;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.style.DefaultStorageStyleResolver;
import com.techsenger.shellfx.storage.style.StorageIcons;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A read-only storage of the demo with one flat directory of ten entries. Five of them are listed ready, the other
 * five without their size and modified time, which the second loading phase fills in after
 * {@link #release()} is called.
 *
 * @author Pavel Castornii
 */
final class DemoFileStorage extends AbstractFileStorage<StorageFile> {

    private record Entry(String name, boolean directory, @Nullable Long size, int days, boolean pending) {
    }

    private static final long BASE_TIME = 1_735_689_600_000L;

    private static final long DAY = 86_400_000L;

    private static final List<Entry> ENTRIES = List.of(
            new Entry("alpha", true, null, 10, false),
            new Entry("gamma", true, null, 40, false),
            new Entry("readme.txt", false, 1_200L, 20, false),
            new Entry("photo.png", false, 450_000L, 5, false),
            new Entry("notes.md", false, 80L, 60, false),
            new Entry("beta", true, null, 70, true),
            new Entry("zeta", true, null, 2, true),
            new Entry("archive.zip", false, 9_800_000L, 90, true),
            new Entry("report.pdf", false, 220_000L, 30, true),
            new Entry("tiny.log", false, 12L, 15, true));

    // completed by the button of the dialog; renewed by every listing of the directory
    private volatile CompletableFuture<Void> gate = new CompletableFuture<>();

    DemoFileStorage() {
        super(FileStorageType.NETWORK, "Demo", URI.create("demo:///"), () -> StorageIcons.BASE_DISK,
                new DefaultStorageStyleResolver());
    }

    /**
     * Lets the second loading phase go on with the files that wait.
     */
    void release() {
        gate.complete(null);
    }

    /**
     * Blocks until {@link #release()} is called for the last listing of the directory.
     */
    void awaitRelease() {
        gate.join();
    }

    @Override
    public StorageFile getRoot() {
        var root = new DemoFile(this, getUri(), getName(), true, null, BASE_TIME, false);
        root.setRoot(true);
        return root;
    }

    @Override
    public List<StorageFile> getDirectories(URI uri, OperationType operationType) {
        var result = new ArrayList<StorageFile>();
        for (var file : listFiles(uri)) {
            if (file.isDirectory()) {
                result.add(file);
            }
        }
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    public List<StorageFile> getFiles(URI uri, OperationType operationType) {
        // a new listing starts a new first phase: the files of the previous one stop waiting, the new ones wait
        var previous = gate;
        gate = new CompletableFuture<>();
        previous.complete(null);
        var result = listFiles(uri);
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    public List<StorageFile> getFilesRecursively(URI uri, OperationType operationType) {
        throw new UnsupportedOperationException("The demo storage is read-only");
    }

    @Override
    public StorageFile getFile(URI uri, OperationType operationType) throws NoSuchFileException {
        StorageFile result = null;
        if (uri.equals(getUri())) {
            result = getRoot();
        } else {
            for (var file : listFiles(getUri())) {
                if (file.getUri().equals(uri)) {
                    result = file;
                }
            }
        }
        if (result == null) {
            throw new NoSuchFileException(uri.toString());
        }
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    public @Nullable StorageFile getParent(StorageFile file, OperationType operationType) {
        return getParent(file.getUri(), operationType);
    }

    @Override
    public @Nullable StorageFile getParent(URI uri, OperationType operationType) {
        return uri.equals(getUri()) ? null : getRoot();
    }

    @Override
    public List<StorageFile> getHierarchy(URI uri, OperationType operationType) throws NoSuchFileException {
        var result = new ArrayList<StorageFile>();
        result.add(getRoot());
        if (!uri.equals(getUri())) {
            result.add(getFile(uri, operationType));
        }
        return result;
    }

    @Override
    public void createDirectory(URI uri, OperationType operationType) {
        throw new UnsupportedOperationException("The demo storage is read-only");
    }

    @Override
    public StorageFile createFile(URI uri, String name, OperationType operationType) {
        throw new UnsupportedOperationException("The demo storage is read-only");
    }

    @Override
    public StorageFile createVirtual(@Nullable URI uri, FileEntryType entryType, String name) {
        var virtualUri = uri != null ? uri : URI.create(getUri() + name);
        return new DemoFile(this, virtualUri, name, entryType == FileEntryType.DIRECTORY, null, BASE_TIME, false);
    }

    @Override
    public void renameFile(URI uri, String newName, OperationType operationType) {
        throw new UnsupportedOperationException("The demo storage is read-only");
    }

    @Override
    public boolean refersToStorage(URI uri) {
        return FileStorageUtils.isWithin(getUri(), uri, false);
    }

    @Override
    public void writeFile(URI uri, String content, Charset charset, OperationType operationType) {
        throw new UnsupportedOperationException("The demo storage is read-only");
    }

    @Override
    public String readFile(URI uri, Charset charset, OperationType operationType) {
        throw new UnsupportedOperationException("The demo storage is read-only");
    }

    @Override
    public void writeFile(URI uri, byte[] content, OperationType operationType) {
        throw new UnsupportedOperationException("The demo storage is read-only");
    }

    @Override
    public byte[] readFile(URI uri, OperationType operationType) {
        throw new UnsupportedOperationException("The demo storage is read-only");
    }

    /**
     * Creates the files of the directory from scratch, so every listing gets its own files in their first-phase
     * state. Only the root has entries.
     */
    private List<StorageFile> listFiles(URI uri) {
        var result = new ArrayList<StorageFile>();
        if (!uri.equals(getUri())) {
            return result;
        }
        for (var entry : ENTRIES) {
            var fileUri = URI.create(getUri() + entry.name() + (entry.directory() ? "/" : ""));
            result.add(new DemoFile(this, fileUri, entry.name(), entry.directory(), entry.size(),
                    BASE_TIME + entry.days() * DAY, entry.pending()));
        }
        return result;
    }
}
