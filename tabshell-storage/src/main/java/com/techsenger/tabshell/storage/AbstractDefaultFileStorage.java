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

package com.techsenger.tabshell.storage;

import com.techsenger.toolkit.core.file.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDefaultFileStorage extends AbstractFileStorage {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDefaultFileStorage.class);

    public AbstractDefaultFileStorage(FileStorageType type, String displayName, URI rootUri) {
        super(type, displayName, rootUri, true);
    }

    @Override
    public List<GenericFile> getFiles(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<GenericFile>();
        var path = toPath(uri);
        checkIfExists(path);
        GenericFile.Builder builder = new GenericFile.Builder();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path filePath : stream) {
                try {
                    var createdFile = GenericFile.createFile(builder, filePath, filePath.toUri(), this);
                    result.add(createdFile);
                } catch (InvalidFileException ex) {
                    logger.error("Couldn't create GenericFile from {}", filePath, ex);
                } finally {
                    builder.reset();
                }
            }
        } catch (SecurityException | AccessDeniedException e) {
            throw new AccessDeniedException("No access to directory: " + path);
        }
        return result;
    }

    @Override
    public GenericFile getFile(URI uri) throws NoSuchFileException, AccessDeniedException, InvalidFileException,
            IOException {
        var path = toPath(uri);
        checkIfExists(path);
        var builder = new GenericFile.Builder();
        var genFile = GenericFile.createFile(builder, path, uri, this);
        return genFile;
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
