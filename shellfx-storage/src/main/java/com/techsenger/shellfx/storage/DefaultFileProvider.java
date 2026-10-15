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

import com.techsenger.shellfx.material.icon.FontIcon;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.style.FileIconResolver;
import com.techsenger.shellfx.storage.style.FileStyleResolver;
import com.techsenger.shellfx.storage.style.IconTextStyle;
import java.io.IOException;
import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default provider: refines the links whose target lies in another storage, and gives every file the icon and the
 * style that its resolvers determine. The files of a network storage are refined in the second phase; the
 * files of any other storage are refined at once, in the first. A subclass refines more by overriding
 * {@link #isRefinable} and {@link #refine}.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public class DefaultFileProvider<F extends StorageFile> implements FileProvider<F> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFileProvider.class);

    private static final int WORKERS_PER_STORAGE = 4;

    private final List<? extends FileStorage<F>> storages;

    private final FileIconResolver<? super F> iconResolver;

    private final FileStyleResolver<? super F> styleResolver;

    private final Supplier<Theme> themeSupplier;

    private final AtomicLong generation = new AtomicLong();

    /**
     * Creates a provider that finds the targets of the links that end in another storage.
     *
     * @param storages the storages a link may end in; they are not tracked, so the provider uses the ones it was
     *     given
     * @param iconResolver determines the icon of every file the provider returns
     * @param styleResolver determines the style of every file the provider returns
     * @param themeSupplier gives the current theme the files are styled for
     */
    public DefaultFileProvider(List<? extends FileStorage<F>> storages, FileIconResolver<? super F> iconResolver,
            FileStyleResolver<? super F> styleResolver, Supplier<Theme> themeSupplier) {
        this.storages = List.copyOf(storages);
        this.iconResolver = iconResolver;
        this.styleResolver = styleResolver;
        this.themeSupplier = themeSupplier;
    }

    @Override
    public List<? extends FileStorage<F>> getStorages() {
        return storages;
    }

    @Override
    public List<F> getFiles(FileStorage<F> storage, URI uri, OperationType operationType) throws IOException {
        cancelRefinement();
        var files = storage.getFiles(uri, operationType);
        assembleFiles(storage, files);
        return files;
    }

    @Override
    public F getRoot(FileStorage<F> storage) {
        var root = storage.getRoot();
        assembleFiles(storage, List.of(root));
        return root;
    }

    @Override
    public List<F> getHierarchy(FileStorage<F> storage, URI uri, OperationType operationType)
            throws IOException {
        var files = storage.getHierarchy(uri, operationType);
        assembleFiles(storage, files);
        return files;
    }

    @Override
    public void assembleFiles(FileStorage<F> storage, List<F> files) {
        files.forEach(file -> assembleListed(storage, file));
    }

    @Override
    public void refineFilesAsync(List<F> files, Runnable onFinished) {
        var token = generation.incrementAndGet();
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
                Thread.startVirtualThread(() -> refineQueue(queue, token, remaining, onFinished));
            }
        }
    }

    @Override
    public void cancelRefinement() {
        generation.incrementAndGet();
    }

    /**
     * Tells whether the second phase has to refine the file, which is then shown as pending until it is done. The
     * default is a link whose target is not known yet but whose resolved URI is. A subclass that refines more
     * overrides this together with {@link #refine} and keeps the answer of the superclass.
     *
     * @param file the file listed in the first phase
     * @return {@code true} if {@link #refine} has to be called for the file
     */
    protected boolean isRefinable(F file) {
        var link = file.getLink();
        return link != null && link.getTarget() == null && link.getResolvedUri() != null;
    }

    /**
     * Refines the file that {@link #isRefinable} accepted, on a background thread; it may be slow. The default gets
     * the target of a link from the storage that holds the resolved URI, which may not be the storage of the link;
     * the link stays broken if no storage holds it or nothing exists there.
     *
     * @param file the file to refine
     * @throws IOException if the file can't be refined, which makes the file failed
     */
    protected void refine(F file) throws IOException {
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
     * Gives the file its state, and the icon and style that the resolvers determine for it in that state. It is
     * for the files that nobody observes yet, so everything is written on the calling thread.
     *
     * @param file the file to assemble
     * @param state the state the file is in now
     */
    protected void assemble(F file, FileState state) {
        assemble(file, state, Runnable::run);
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

    /**
     * Writes the icon to the file. The default works on {@link DefaultStorageFile}; a subclass whose files are of
     * another class overrides it to write the icon its own way.
     *
     * @param file the file to write to
     * @param icon the icon of the file
     * @throws IllegalArgumentException if the provider can't write to the file
     */
    protected void setIcon(F file, FontIcon<?> icon) {
        toDefaultFile(file).setIcon(icon);
    }

    /**
     * Writes the style to the file. The default works on {@link DefaultStorageFile}; a subclass whose files are of
     * another class overrides it to write the style its own way.
     *
     * @param file the file to write to
     * @param style the style of the file
     * @throws IllegalArgumentException if the provider can't write to the file
     */
    protected void setStyle(F file, IconTextStyle style) {
        toDefaultFile(file).setStyle(style);
    }

    private DefaultStorageFile toDefaultFile(F file) {
        if (!(file instanceof DefaultStorageFile defaultFile)) {
            throw new IllegalArgumentException("The provider can only write to DefaultStorageFile: " + file);
        }
        return defaultFile;
    }

    private void assembleListed(FileStorage<F> storage, F file) {
        if (!isRefinable(file)) {
            assemble(file, FileState.READY);
        } else if (storage.getType() == FileStorageType.NETWORK) {
            assemble(file, FileState.PENDING);
        } else {
            assemble(file, tryRefine(file) ? FileState.READY : FileState.FAILED);
        }
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

    /**
     * Writes the state at once and resolves the icon and style for it on the calling thread, then lets the publisher
     * write them, so that the publisher decides where the observable properties of the file are changed.
     */
    private void assemble(F file, FileState state, Executor publisher) {
        setState(file, state);
        var icon = iconResolver.resolve(file);
        var style = styleResolver.resolve(file, themeSupplier.get());
        publisher.execute(() -> {
            setIcon(file, icon);
            setStyle(file, style);
        });
    }

    private void refineQueue(Queue<F> queue, long token, AtomicInteger remaining, Runnable onFinished) {
        while (generation.get() == token) {
            var file = queue.poll();
            if (file == null) {
                return;
            }
            var refined = tryRefine(file);
            if (generation.get() != token) {
                return;
            }
            // the file is shown by now, so its icon and style are written on the UI thread, where their observers are
            assemble(file, refined ? FileState.READY : FileState.FAILED, task -> UiExecutor.execute(() -> {
                if (generation.get() != token) {
                    return;
                }
                task.run();
                if (remaining.decrementAndGet() == 0) {
                    onFinished.run();
                }
            }));
        }
    }
}
