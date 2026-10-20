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
import com.techsenger.shellfx.storage.StorageFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default refiner: finds the targets of the links of a network storage, which are not resolved in the first phase.
 * A subclass refines more by overriding {@link #isRefinable} and {@link #refine}. It does not change a file in the
 * background: what it finds out is written by the refinement, on the UI thread.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public class DefaultFileRefiner<F extends StorageFile> implements FileRefiner<F> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFileRefiner.class);

    private static final int WORKERS_PER_STORAGE = 4;

    private final List<? extends FileStorage<F>> storages;

    /**
     * Creates a refiner.
     *
     * @param storages the storages the targets of the links and the recorded files may lie in; they are not tracked,
     *     so the refiner uses the ones it was given
     */
    public DefaultFileRefiner(List<? extends FileStorage<F>> storages) {
        this.storages = List.copyOf(storages);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default accepts a link of a network storage whose target is not known yet. A subclass that refines more
     * overrides this together with {@link #refine} and keeps the answer of the superclass.
     */
    @Override
    public boolean isRefinable(F file) {
        return LinkUtils.isDeferred(file);
    }

    @Override
    public AsyncTask refineFilesAsync(List<F> files, Consumer<FileRefinement<F>> onRefined,
            Consumer<AsyncResult> onFinished) {
        var task = new DefaultAsyncTask();
        Thread.startVirtualThread(() -> {
            if (task.isCancelled()) {
                task.finish(onFinished, AsyncResult.CANCELLED);
                return;
            }
            if (files.isEmpty()) {
                task.finish(onFinished, AsyncResult.SUCCESS);
                return;
            }
            Map<FileStorage<?>, Queue<F>> queuesByStorage = new HashMap<>();
            for (var file : files) {
                queuesByStorage.computeIfAbsent(file.getStorage(), s -> new ConcurrentLinkedQueue<>()).add(file);
            }
            var remaining = new AtomicInteger(files.size());
            for (var queue : queuesByStorage.values()) {
                var workers = Math.min(WORKERS_PER_STORAGE, queue.size());
                for (var i = 0; i < workers; i++) {
                    Thread.startVirtualThread(() -> refineQueue(queue, task, remaining, onRefined, onFinished));
                }
            }
        });
        return task;
    }

    /**
     * Refines the file that {@link #isRefinable} accepted, on a background thread; it may be slow. It must not change
     * the file, which is already shown: what it finds out is written by the action it returns, which is called on the
     * UI thread. The default gets the target of a link.
     *
     * @param file the file to refine
     * @return the action that writes the found values to the file
     * @throws IOException if the file can't be refined, which makes the file failed
     */
    protected Runnable refine(F file) throws IOException {
        var target = LinkUtils.findTarget(storages, file);
        return () -> LinkUtils.setTarget(file, target);
    }

    /**
     * Returns the storages this refiner was created with.
     *
     * @return the storages, never {@code null}
     */
    protected List<? extends FileStorage<F>> getStorages() {
        return storages;
    }

    private FileRefinement<F> refineFile(F file) {
        try {
            return new FileRefinement<>(file, refine(file));
        } catch (IOException | RuntimeException ex) {
            logger.error("Couldn't refine the file {}", file.getUri(), ex);
            return new FileRefinement<>(file, () -> { });
        }
    }

    private void refineQueue(Queue<F> queue, DefaultAsyncTask task, AtomicInteger remaining,
            Consumer<FileRefinement<F>> onRefined, Consumer<AsyncResult> onFinished) {
        while (true) {
            if (task.isCancelled()) {
                task.finish(onFinished, AsyncResult.CANCELLED);
                return;
            }
            var file = queue.poll();
            if (file == null) {
                return;
            }
            var refinement = refineFile(file);
            if (task.isCancelled()) {
                task.finish(onFinished, AsyncResult.CANCELLED);
                return;
            }
            onRefined.accept(refinement);
            if (remaining.decrementAndGet() == 0) {
                task.finish(onFinished, AsyncResult.SUCCESS);
            }
        }
    }
}
