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

import com.techsenger.annotations.Nullable;
import com.techsenger.toolkit.core.file.FileUtils;
import com.techsenger.toolkit.core.function.Factory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDefaultFileStorage<T extends GenericFile> extends AbstractFileStorage<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDefaultFileStorage.class);

    private final Factory<? extends DefaultGenericFile> fileFactory;

    public AbstractDefaultFileStorage(FileStorageType type, String displayName, URI rootUri,
            Factory<? extends DefaultGenericFile> fileFactory) {
        super(type, displayName, rootUri, true);
        this.fileFactory = fileFactory;
    }

    @Override
    public List<T> getFiles(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<T>();
        var path = toPath(uri);
        checkIfExists(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path filePath : stream) {
                try {
                    T createdFile = createFile(filePath, filePath.toUri());
                    result.add(createdFile);
                } catch (InvalidFileException ex) {
                    logger.error("Couldn't create GenericFile from {}", filePath, ex);
                }
            }
        } catch (SecurityException | AccessDeniedException e) {
            throw new AccessDeniedException("No access to directory: " + path);
        }
        return result;
    }

    @Override
    public List<T> getFilesRecursively(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<T>();
        var path = toPath(uri);
        checkIfExists(path);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                private boolean root = true;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (root) {
                        root = false;
                        return FileVisitResult.CONTINUE;
                    }
                    result.add(createFile(dir, attrs, dir.toUri()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    result.add(createFile(file, attrs, file.toUri()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    logger.error("Couldn't visit file {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (SecurityException e) {
            throw new AccessDeniedException("No access to directory: " + path);
        }
        return result;
    }

    @Override
    public T getFile(URI uri) throws NoSuchFileException, AccessDeniedException, InvalidFileException, IOException {
        var path = toPath(uri);
        checkIfExists(path);
        return createFile(path, uri);
    }

    @Override
    public T getParent(T file) throws NoSuchFileException, AccessDeniedException, IOException {
        var parentUri = UriUtils.getParentUri(getRootUri(), file.getUri());
        if (parentUri == null) {
            return getRoot();
        }
        var path = toPath(parentUri);
        checkIfExists(path);
        return createFile(path, parentUri);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getRoot() {
        var file = fileFactory.create();
        file.setVirtual(true);
        file.setEntryType(FileEntryType.DIRECTORY);
        file.setUri(getRootUri());
        file.setStorage(this);
        file.setName(getDisplayName());
        return (T) file;
    }

    @Override
    public void createDirectory(URI uri) throws NoSuchFileException, FileAlreadyExistsException,
            AccessDeniedException, IOException {
        var path = toPath(uri);
        var parent = path.getParent();
        checkIfExists(parent);
        Files.createDirectory(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T createVirtual(FileEntryType entryType, String name, @Nullable URI uri) {
        var file = fileFactory.create();
        file.setEntryType(entryType);
        file.setName(name);
        file.setUri(uri);
        file.setStorage(this);
        file.setVirtual(true);
        return (T) file;
    }

    @Override
    public void renameFile(URI uri, String newName) throws NoSuchFileException, FileAlreadyExistsException,
            AccessDeniedException, IOException {
        var path = toPath(uri);
        checkIfExists(path);
        var newPath = path.resolveSibling(newName);
        if (Files.exists(newPath)) {
            throw new FileAlreadyExistsException("File already exists: " + newPath);
        }
        Files.move(path, newPath);
    }

    @Override
    public void writeFile(URI uri, String content, Charset charset) throws AccessDeniedException, IOException {
        var path = toPath(uri);
        checkWritable(path);
        FileUtils.writeFile(path, content, charset);
    }

    @Override
    public String readFile(URI uri, Charset charset) throws NoSuchFileException, AccessDeniedException, IOException {
        var path = toPath(uri);
        checkIfExists(path);
        checkReadable(path);
        return FileUtils.readFile(path, charset);
    }

    @Override
    public void writeFile(URI uri, byte[] content) throws AccessDeniedException, IOException {
        var path = toPath(uri);
        checkWritable(path);
        try (OutputStream out = Files.newOutputStream(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            out.write(content);
        }
    }

    @Override
    public byte[] readFile(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var path = toPath(uri);
        checkIfExists(path);
        checkReadable(path);
        try (InputStream in = Files.newInputStream(path)) {
            return in.readAllBytes();
        }
    }

    protected Factory<? extends DefaultGenericFile> getFileFactory() {
        return fileFactory;
    }

    protected void checkWritable(Path path) throws AccessDeniedException {
        if (Files.exists(path) && !Files.isWritable(path)) {
            throw new AccessDeniedException("No write permission for file: " + path);
        }
    }

    protected void checkReadable(Path path) throws AccessDeniedException {
        if (!Files.isReadable(path)) {
            throw new AccessDeniedException("No read permission for file: " + path);
        }
    }

    protected void checkIfExists(Path path) throws NoSuchFileException {
        if (!Files.exists(path)) {
            throw new NoSuchFileException("File not found: " + path);
        }
    }

    protected Path toPath(URI uri) throws AccessDeniedException {
        try {
            return Paths.get(uri);
        } catch (SecurityException ex) {
            throw new AccessDeniedException("No access to directory: " + uri);
        }
    }

    @SuppressWarnings("unchecked")
    private T createFile(Path path, URI uri) throws InvalidFileException {
        try {
            var attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return createFile(path, attrs, uri);
        } catch (InvalidFileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidFileException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private T createFile(Path path, BasicFileAttributes attrs, URI uri) {
        var file = fileFactory.create();
        file.setStorage(this);
        file.setName(path.getFileName().toString());
        file.setHidden(isHidden(path));
        file.setUri(uri);
        file.setLastModified(attrs.lastModifiedTime().toMillis());
        if (attrs.isDirectory()) {
            file.setEntryType(FileEntryType.DIRECTORY);
        } else {
            var entryType = FileEntryType.FILE;
            if (attrs.isSymbolicLink()) {
                entryType = FileEntryType.SYMBOLIC_LINK;
            }
            file.setEntryType(entryType);
            file.setSize(attrs.size());
        }
        file.setVirtual(false);
        return (T) file;
    }

    private boolean isHidden(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException ex) {
            return false;
        }
    }
}
