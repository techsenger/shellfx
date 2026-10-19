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
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default provider: finds the targets of the links, which may lie in another storage, and gives every file its
 * state. The links of a storage that is not a network one are resolved in the first phase, while those of a network
 * storage are refined in the second. A subclass completes more in the first phase by overriding {@link #prepare}, and
 * refines more in the second by overriding {@link #isRefinable} and {@link #refine}.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public class DefaultFileProvider<F extends StorageFile> implements FileProvider<F> {

    @FunctionalInterface
    private interface Loader<R> {

        R load() throws IOException;
    }

    private static final class Task implements FileProviderTask {

        private final AtomicBoolean finished = new AtomicBoolean();

        private volatile boolean cancelled;

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * Gives the end of the operation to the callback, unless some other call has done it already.
         */
        void finish(Consumer<FileProviderResult> onFinished, FileProviderResult result) {
            if (finished.compareAndSet(false, true)) {
                onFinished.accept(result);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DefaultFileProvider.class);

    private static final int WORKERS_PER_STORAGE = 4;

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
    public FileProviderTask getFilesAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<List<F>> onLoaded, Consumer<FileProviderResult> onFinished) {
        return loadAsync("files at " + uri, () -> {
            var files = storage.getFiles(uri, operationType);
            files.forEach(file -> assembleListed(storage, file));
            return files;
        }, onLoaded, onFinished);
    }

    @Override
    public FileProviderTask getFileAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<F> onLoaded, Consumer<FileProviderResult> onFinished) {
        return loadAsync("file at " + uri, () -> {
            var file = storage.getFile(uri, operationType);
            assembleListed(storage, file);
            return file;
        }, onLoaded, onFinished);
    }

    @Override
    public FileProviderTask getHierarchyAsync(FileStorage<F> storage, URI uri, OperationType operationType,
            Consumer<List<F>> onLoaded, Consumer<FileProviderResult> onFinished) {
        return loadAsync("hierarchy at " + uri, () -> {
            var files = storage.getHierarchy(uri, operationType);
            files.forEach(file -> assembleListed(storage, file));
            return files;
        }, onLoaded, onFinished);
    }

    @Override
    public FileProviderTask getRootAsync(FileStorage<F> storage, Consumer<F> onLoaded,
            Consumer<FileProviderResult> onFinished) {
        return loadAsync("root of " + storage.getUri(), () -> {
            var root = storage.getRoot();
            assembleListed(storage, root);
            return root;
        }, onLoaded, onFinished);
    }

    @Override
    public FileProviderTask getRootsAsync(Consumer<List<F>> onLoaded, Consumer<FileProviderResult> onFinished) {
        return loadAsync("roots", () -> {
            List<F> roots = new ArrayList<>();
            for (FileStorage<F> storage : storages) {
                var root = storage.getRoot();
                assembleListed(storage, root);
                roots.add(root);
            }
            return roots;
        }, onLoaded, onFinished);
    }

    @Override
    public FileProviderTask refineFilesAsync(List<F> files, Consumer<F> onRefined,
            Consumer<FileProviderResult> onFinished) {
        var task = new Task();
        Map<FileStorage<?>, Queue<F>> queuesByStorage = new HashMap<>();
        var total = 0;
        for (var file : files) {
            if (file.getState() == FileState.PENDING) {
                queuesByStorage.computeIfAbsent(file.getStorage(), s -> new ConcurrentLinkedQueue<>()).add(file);
                total++;
            }
        }
        var remaining = new AtomicInteger(total);
        for (var queue : queuesByStorage.values()) {
            var workers = Math.min(WORKERS_PER_STORAGE, queue.size());
            for (var i = 0; i < workers; i++) {
                Thread.startVirtualThread(() -> refineQueue(queue, task, remaining, onRefined, onFinished));
            }
        }
        return task;
    }

    /**
     * Completes a file listed in the first phase, on the calling thread, before it is asked whether it needs the
     * second phase. The default finds the target of a link of a storage that is not a network one, which is quick. A
     * subclass gives the file in the same way whatever else it can tell at once, and keeps what the superclass does.
     *
     * @param storage the storage that holds the file
     * @param file the file listed in the first phase
     * @throws IOException if the file can't be completed, which makes the file failed
     */
    protected void prepare(FileStorage<F> storage, F file) throws IOException {
        if (storage.getType() != FileStorageType.NETWORK && isLinkUnresolved(file)) {
            resolveLink(file);
        }
    }

    /**
     * Tells whether the file needs the second phase, which refines it in the background and shows it as pending until
     * it is done. The default is a link of a network storage whose target is not known yet. A subclass that refines
     * more overrides this together with {@link #refine} and keeps the answer of the superclass.
     *
     * @param storage the storage that holds the file
     * @param file the file listed in the first phase, after {@link #prepare}
     * @return {@code true} if {@link #refine} has to be called for the file
     */
    protected boolean isRefinable(FileStorage<F> storage, F file) {
        return storage.getType() == FileStorageType.NETWORK && isLinkUnresolved(file);
    }

    /**
     * Refines the file that {@link #isRefinable} accepted, on a background thread; it may be slow. The default gets
     * the target of a link.
     *
     * @param file the file to refine
     * @throws IOException if the file can't be refined, which makes the file failed
     */
    protected void refine(F file) throws IOException {
        resolveLink(file);
    }

    /**
     * Tells whether the file is a link whose target is not known yet but whose resolved URI is.
     *
     * @param file the file
     * @return {@code true} if the target of the link is to be found
     */
    protected boolean isLinkUnresolved(F file) {
        var link = file.getLink();
        return link != null && link.getTarget() == null && link.getResolvedUri() != null;
    }

    /**
     * Writes the state to the file. The default works on {@link DefaultStorageFile}; a subclass whose files are of
     * another class overrides it to write the state its own way.
     *
     * @param file the file to write to
     * @param state the state of the file
     * @throws IllegalArgumentException if the provider can't write to the file
     */
    protected void setState(F file, FileState state) {
        toDefaultFile(file).setState(state);
    }

    private DefaultStorageFile toDefaultFile(F file) {
        if (!(file instanceof DefaultStorageFile defaultFile)) {
            throw new IllegalArgumentException("The provider can only write to DefaultStorageFile: " + file);
        }
        return defaultFile;
    }

    private void assembleListed(FileStorage<F> storage, F file) {
        var state = FileState.READY;
        try {
            prepare(storage, file);
            if (isRefinable(storage, file)) {
                state = FileState.PENDING;
            }
        } catch (IOException | RuntimeException ex) {
            logger.error("Couldn't prepare the file {}", file.getUri(), ex);
            state = FileState.FAILED;
        }
        setState(file, state);
    }

    /**
     * Gets the target of a link from the storage that holds the resolved URI, which may not be the storage of the
     * link; the link stays broken if no storage holds it or nothing exists there.
     */
    private void resolveLink(F file) throws IOException {
        var link = file.getLink();
        var resolvedUri = link == null ? null : link.getResolvedUri();
        if (resolvedUri == null || !(link instanceof DefaultEntryLink defaultLink)) {
            return;
        }
        var storage = FileStorageUtils.findByUri(storages, resolvedUri);
        if (storage.isEmpty()) {
            // no storage holds the end of the chain, so the link stays broken
            return;
        }
        try {
            defaultLink.setTarget(storage.get().getFile(resolvedUri, OperationType.SECONDARY));
        } catch (NoSuchFileException ex) {
            // nothing exists at the end of the chain, so the link stays broken
        }
    }

    /**
     * Loads on a background thread and gives the result, unless the task has been cancelled by then.
     */
    private <R> FileProviderTask loadAsync(String what, Loader<R> loader, Consumer<R> onLoaded,
            Consumer<FileProviderResult> onFinished) {
        var task = new Task();
        Thread.startVirtualThread(() -> {
            if (task.isCancelled()) {
                task.finish(onFinished, FileProviderResult.CANCELLED);
                return;
            }
            R result;
            try {
                result = loader.load();
            } catch (IOException | RuntimeException ex) {
                logger.error("Error getting {}", what, ex);
                task.finish(onFinished, FileProviderResult.FAILED);
                return;
            }
            if (task.isCancelled()) {
                task.finish(onFinished, FileProviderResult.CANCELLED);
                return;
            }
            onLoaded.accept(result);
            task.finish(onFinished, FileProviderResult.SUCCESS);
        });
        return task;
    }

    private boolean tryRefine(F file) {
        try {
            refine(file);
            return true;
        } catch (IOException | RuntimeException ex) {
            logger.error("Couldn't refine the file {}", file.getUri(), ex);
            return false;
        }
    }

    private void refineQueue(Queue<F> queue, Task task, AtomicInteger remaining, Consumer<F> onRefined,
            Consumer<FileProviderResult> onFinished) {
        while (true) {
            if (task.isCancelled()) {
                task.finish(onFinished, FileProviderResult.CANCELLED);
                return;
            }
            var file = queue.poll();
            if (file == null) {
                return;
            }
            var refined = tryRefine(file);
            if (task.isCancelled()) {
                task.finish(onFinished, FileProviderResult.CANCELLED);
                return;
            }
            setState(file, refined ? FileState.READY : FileState.FAILED);
            onRefined.accept(file);
            if (remaining.decrementAndGet() == 0) {
                task.finish(onFinished, FileProviderResult.SUCCESS);
            }
        }
    }
}
