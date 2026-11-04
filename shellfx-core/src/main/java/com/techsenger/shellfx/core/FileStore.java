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

package com.techsenger.shellfx.core;

import com.techsenger.annotations.Nullable;
import com.techsenger.toolkit.core.function.Factory;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A store that keeps the data in a file as a single serialized root object. {@link #restore()} reads the file, or
 * creates the data with the factory if there is no file, and {@link #save()} writes the data back, or deletes the file
 * if the data is empty. The file is replaced as a whole on every save so a failure never leaves a half-written one. A
 * failure to read or write the file is thrown, so the owner of the store decides what to do with it.
 *
 * @param <T> the type of the root object; it is not checked when the file is read, so the reader of a file that was
 *     not written by its own store must be ready for a {@link ClassCastException}
 * @author Pavel Castornii
 */
public final class FileStore<T extends StoreData> {

    private static final class ClassLoaderObjectInputStream extends ObjectInputStream {

        private final ClassLoader classLoader;

        ClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            return Class.forName(desc.getName(), false, classLoader);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FileStore.class);

    private final Path path;

    private final ClassLoader classLoader;

    private final Factory<T> factory;

    private @Nullable T data;

    /**
     * Creates a store.
     *
     * @param path the file
     * @param classLoader the class loader used to read the classes of the root object back
     * @param factory creates the data when there is no file
     */
    public FileStore(Path path, ClassLoader classLoader, Factory<T> factory) {
        this.path = path;
        this.classLoader = classLoader;
        this.factory = factory;
    }

    public Path getPath() {
        return path;
    }

    /**
     * Returns the data the store works with.
     *
     * @return the live object, not a copy
     * @throws IllegalStateException if the data has not been read yet
     */
    public T getData() {
        var current = data;
        if (current == null) {
            throw new IllegalStateException("The store file " + path + " has not been restored yet");
        }
        return current;
    }

    /**
     * Reads the file and makes what it holds available through {@link #getData()}; if there is no file, the data is
     * created with the factory.
     *
     * @throws IOException if the file exists but cannot be read, then the data stays unavailable
     */
    @SuppressWarnings("unchecked")
    public void restore() throws IOException {
        if (!Files.exists(path)) {
            logger.info("No store file at {}", path);
            data = factory.create();
            return;
        }
        try (var in = new ClassLoaderObjectInputStream(Files.newInputStream(path), classLoader)) {
            data = (T) in.readObject();
            logger.debug("Read from {}: {}", path, data);
        } catch (ClassNotFoundException ex) {
            throw new IOException("Couldn't read the store file " + path, ex);
        }
    }

    /**
     * Writes the data to the file, replacing what it held; empty data deletes the file.
     *
     * @throws IllegalStateException if the data has not been read yet
     * @throws IOException if the file cannot be written or deleted
     */
    public void save() throws IOException {
        var root = getData();
        if (root.isEmpty()) {
            Files.deleteIfExists(path);
            logger.debug("Deleted {}: the data is empty", path);
            return;
        }
        var parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        // Written aside and moved over the real file, so a crash in the middle of the write can't leave it
        // half-written: it is either the old file or the new one.
        var temp = path.resolveSibling(path.getFileName() + ".tmp");
        try (var out = new ObjectOutputStream(Files.newOutputStream(temp))) {
            out.writeObject(root);
        }
        try {
            Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
        logger.debug("Wrote to {}: {}", path, root);
    }
}
