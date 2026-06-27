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
public abstract class AbstractDefaultFileStorage extends AbstractFileStorage {


    static GenericFile createFile(Factory<DefaultGenericFile> fileFactory, Path path, URI uri, FileStorage storage)
            throws InvalidFileException {
        try {
            var attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return createFile(fileFactory, path, attrs, uri, storage);
        } catch (Exception ex) {
            throw new InvalidFileException(ex);
        }
    }

    static GenericFile createFile(Factory<DefaultGenericFile> fileFactory, Path path, BasicFileAttributes attrs,
            URI uri, FileStorage storage) {
        var file = fileFactory.create();
        file.setStorage(storage);
        file.setName(path.getFileName().toString());
        file.setUri(uri);
        file.setLastModified(attrs.lastModifiedTime().toMillis());
        if (attrs.isDirectory()) {
            file.setEntryType(FileEntryType.DIRECTORY);
        } else {
            var fileType = FileEntryType.FILE;
            if (attrs.isSymbolicLink()) {
                fileType = FileEntryType.SYMBOLIC_LINK;
            }
            file.setEntryType(fileType);
            file.setSize(attrs.size());
        }
        file.setVirtual(false);
        return file;
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractDefaultFileStorage.class);

    private final Factory<DefaultGenericFile> fileFactory;

    public AbstractDefaultFileStorage(FileStorageType type, String displayName, URI rootUri,
            Factory<DefaultGenericFile> fileFactory) {
        super(type, displayName, rootUri, true);
        this.fileFactory = fileFactory;
    }

    @Override
    public List<GenericFile> getFiles(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<GenericFile>();
        var path = toPath(uri);
        checkIfExists(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path filePath : stream) {
                try {
                    var createdFile = createFile(fileFactory, filePath, filePath.toUri(), this);
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
    public List<GenericFile> getFilesRecursively(URI uri) throws NoSuchFileException, AccessDeniedException,
            IOException {
        var result = new ArrayList<GenericFile>();
        var path = toPath(uri);
        checkIfExists(path);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {

                // Root directory is not included in the result list
                private boolean root = true;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (root) {
                        root = false;
                        return FileVisitResult.CONTINUE;
                    }
                    var createdFile = createFile(fileFactory, dir, attrs, dir.toUri(), AbstractDefaultFileStorage.this);
                    result.add(createdFile);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    var createdFile = createFile(fileFactory, file, attrs, file.toUri(),
                            AbstractDefaultFileStorage.this);
                    result.add(createdFile);
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
    public GenericFile getFile(URI uri) throws NoSuchFileException, AccessDeniedException, InvalidFileException,
            IOException {
        var path = toPath(uri);
        checkIfExists(path);
        var genFile = createFile(fileFactory, path, uri, this);
        return genFile;
    }

    @Override
    public GenericFile getRoot() {
        var file = fileFactory.create();
        file.setVirtual(true);
        file.setEntryType(FileEntryType.DIRECTORY);
        file.setUri(getRootUri());
        return file;
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
    public GenericFile createVirtual(FileEntryType entryType, String name, URI uri) {
        var file = fileFactory.create();
        file.setEntryType(entryType);
        file.setName(name);
        file.setUri(uri);
        file.setStorage(this);
        file.setVirtual(true);
        return file;
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

    protected Factory<DefaultGenericFile> getFileFactory() {
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
            var path = Paths.get(uri);
            return path;
        } catch (SecurityException ex) {
            throw new AccessDeniedException("No access to directory: " + uri);
        }
    }
}
