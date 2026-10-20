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
import com.techsenger.shellfx.storage.FileStorageType;
import com.techsenger.shellfx.storage.OperationType;
import com.techsenger.shellfx.storage.StorageFile;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default provider: lists the files and completes what is quick to find out. The links of a storage that is not a
 * network one are resolved here, which may lead to another storage, while those of a network storage are left to the
 * refiner. A subclass completes more by overriding {@link #prepare}. A file that can't be completed is logged and
 * given as it is.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public class DefaultFileProvider<F extends StorageFile> implements FileProvider<F> {

    @FunctionalInterface
    private interface Loader<R> {

        R load() throws IOException;
    }

    private static final Logger logger = LoggerFactory.getLogger(DefaultFileProvider.class);

    private final List<? extends FileStorage<F>> storages;

    /**
     * Creates a provider that finds the targets of the links that end in another storage.
     *
     * @param storages the storages a link may end in; they are not tracked, so the provider uses the ones it was
     *     given
     */
    public DefaultFileProvider(List<? extends FileStorage<F>> storages) {
        this.storages = List.copyOf(storages);
    }

    @Override
    public List<? extends FileStorage<F>> getStorages() {
        return storages;
    }

    @Override
    public AsyncTask provideFilesAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<List<F>> onProvided, Consumer<AsyncResult> onFinished) {
        return provideAsync("files at " + uri, () -> {
            var files = storage.getFiles(uri, operationType);
            files.forEach(file -> assemble(storage, file));
            return files;
        }, onProvided, onFinished);
    }

    @Override
    public AsyncTask provideFileAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<F> onProvided, Consumer<AsyncResult> onFinished) {
        return provideAsync("file at " + uri, () -> {
            var file = storage.getFile(uri, operationType);
            assemble(storage, file);
            return file;
        }, onProvided, onFinished);
    }

    @Override
    public AsyncTask provideHierarchyAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<List<F>> onProvided, Consumer<AsyncResult> onFinished) {
        return provideAsync("hierarchy at " + uri, () -> {
            var files = storage.getHierarchy(uri, operationType);
            files.forEach(file -> assemble(storage, file));
            return files;
        }, onProvided, onFinished);
    }

    @Override
    public AsyncTask provideRootAsync(FileStorage<F> storage, Consumer<F> onProvided,
            Consumer<AsyncResult> onFinished) {
        return provideAsync("root of " + storage.getUri(), () -> {
            var root = storage.getRoot();
            assemble(storage, root);
            return root;
        }, onProvided, onFinished);
    }

    @Override
    public AsyncTask provideRootsAsync(Consumer<List<F>> onProvided, Consumer<AsyncResult> onFinished) {
        return provideAsync("roots", () -> {
            List<F> roots = new ArrayList<>();
            for (FileStorage<F> storage : storages) {
                var root = storage.getRoot();
                assemble(storage, root);
                roots.add(root);
            }
            return roots;
        }, onProvided, onFinished);
    }

    /**
     * Completes a file listed by a storage, on the calling thread, before it is given to the client. The default finds
     * the target of a link of a storage that is not a network one, which is quick. A subclass completes whatever else
     * it can tell at once in the same way, and keeps what the superclass does.
     *
     * @param storage the storage that holds the file
     * @param file the file listed by the storage
     * @throws IOException if the file can't be completed, which makes the file failed
     */
    protected void prepare(FileStorage<F> storage, F file) throws IOException {
        if (storage.getType() != FileStorageType.NETWORK && LinkUtils.isUnresolved(file)) {
            LinkUtils.resolve(storages, file);
        }
    }

    private void assemble(FileStorage<F> storage, F file) {
        try {
            prepare(storage, file);
        } catch (IOException | RuntimeException ex) {
            logger.error("Couldn't prepare the file {}", file.getUri(), ex);
        }
    }

    /**
     * Loads on a background thread and gives the result, unless the task has been cancelled by then.
     */
    private <R> AsyncTask provideAsync(String what, Loader<R> loader, Consumer<R> onProvided,
            Consumer<AsyncResult> onFinished) {
        var task = new DefaultAsyncTask();
        Thread.startVirtualThread(() -> {
            if (task.isCancelled()) {
                task.finish(onFinished, AsyncResult.CANCELLED);
                return;
            }
            R result;
            try {
                result = loader.load();
            } catch (IOException | RuntimeException ex) {
                logger.error("Error getting {}", what, ex);
                task.finish(onFinished, AsyncResult.FAILED);
                return;
            }
            if (task.isCancelled()) {
                task.finish(onFinished, AsyncResult.CANCELLED);
                return;
            }
            onProvided.accept(result);
            task.finish(onFinished, AsyncResult.SUCCESS);
        });
        return task;
    }
}
