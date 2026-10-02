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
import com.techsenger.shellfx.material.icon.FontIcon;
import static com.techsenger.shellfx.storage.UriUtils.getParentUri;
import com.techsenger.shellfx.storage.style.StorageIcons;
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
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotLinkException;
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
public abstract class AbstractSystemFileStorage<T extends GenericFile> extends AbstractFileStorage<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSystemFileStorage.class);

    private final Factory<? extends DefaultGenericFile> fileFactory;

    public AbstractSystemFileStorage(FileStorageType type, String displayName, URI rootUri,
            Factory<? extends DefaultGenericFile> fileFactory) {
        super(type, displayName, rootUri);
        this.fileFactory = fileFactory;
    }

    @Override
    public FontIcon<?> getIcon() {
        return switch (getType()) {
            case BASE -> StorageIcons.BASE_DISK;
            case NETWORK -> StorageIcons.NETWORK_DISK;
            case FLOPPY -> StorageIcons.FLOPPY;
            case OPTICAL -> StorageIcons.DISC;
            default -> throw new AssertionError("Unknown type for system storage");
        };
    }

    @Override
    public List<T> getDirectories(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<T>();
        var path = toPath(uri);
        checkIfExists(path);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path childPath : stream) {
                try {
                    var attrs = Files.readAttributes(childPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                    // a link is not a directory, even if it points to one
                    if (attrs.isDirectory() && !isLink(attrs)) {
                        result.add(createFile(childPath, attrs, childPath.toUri()));
                    }
                } catch (InvalidFileException ex) {
                    logger.error("Couldn't create GenericFile from {}", childPath, ex);
                } catch (IOException ex) {
                    logger.error("Couldn't read attributes for {}", childPath, ex);
                }
            }
        } catch (SecurityException | AccessDeniedException e) {
            throw new AccessDeniedException("No access to directory: " + path);
        }
        return result;
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
                    // a directory that is a link (e.g. a Windows junction) must not be entered
                    return isLink(attrs) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
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
        if (path.getFileName() == null) {
            return getRootDirectory();
        }
        return createFile(path, uri);
    }

    @Override
    public T getParent(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var segments = UriUtils.getPathSegments(getUri(), uri);
        var parentUri = getParentUri(getUri(), uri, segments);
        if (parentUri == null) {
            return null;
        } else if (segments.size() == 1) {
            return getRootDirectory();
        } else {
            var path = toPath(parentUri);
            checkIfExists(path);
            return createFile(path, parentUri);
        }
    }

    @Override
    public @Nullable T getParent(T file) throws NoSuchFileException, AccessDeniedException, IOException {
        return getParent(file.getUri());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getRootDirectory() {
        var file = fileFactory.create();
        file.setVirtual(true);
        file.setEntryType(FileEntryType.DIRECTORY);
        file.setUri(getUri());
        file.setStorage(this);
        file.setName(getDisplayName());
        var result = (T) file;
        file.setIcon(resolveIcon(result));
        return result;
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
    public T createFile(String name, URI uri) throws NoSuchFileException, FileAlreadyExistsException,
            AccessDeniedException, IOException {
        var path = toPath(uri);
        checkIfExists(path.getParent());
        Files.createFile(path);
        return createFile(path, uri);
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
        var result = (T) file;
        file.setIcon(resolveIcon(result));
        return result;
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

    /**
     * Resolves the icon assigned to {@code file} when it is constructed, before it is exposed to callers.
     *
     * @param file the file being constructed, must not be {@code null}
     * @return the icon, never {@code null}
     */
    protected FontIcon<?> resolveIcon(T file) {
        return file.isDirectory() ? StorageIcons.FOLDER : StorageIcons.FILE;
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

    /**
     * Tells whether an entry with these attributes is itself a link. Recognizes symbolic links; a system with other
     * kinds of links (e.g. Windows junctions) overrides this to recognize them too.
     *
     * @param ownAttributes the entry's own attributes, read without following links
     * @return {@code true} if the entry is a link
     */
    boolean isLink(BasicFileAttributes ownAttributes) {
        return ownAttributes.isSymbolicLink();
    }

    /**
     * Tells whether the entry is hidden, without any further access to the file system. By convention of Unix-like
     * systems it is hidden if its name starts with a dot; a system with another notion (e.g. the hidden attribute
     * on Windows) overrides this.
     *
     * @param path the entry's path
     * @param attrs the entry's own attributes, read without following links
     * @return {@code true} if the entry is hidden
     */
    boolean isHidden(Path path, BasicFileAttributes attrs) {
        var name = path.getFileName();
        return name != null && name.toString().startsWith(".");
    }

    @SuppressWarnings("unchecked")
    private T createFile(Path path, URI uri) throws InvalidFileException {
        try {
            // the entry's own attributes, so a link is seen as a link and not as what it points to
            var attrs = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
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
        file.setHidden(isHidden(path, attrs));
        file.setUri(uri);
        file.setModifiedTime(attrs.lastModifiedTime().toMillis());
        file.setCreatedTime(attrs.creationTime().toMillis());
        var entryType = resolveEntryType(attrs);
        file.setEntryType(entryType);
        if (entryType == FileEntryType.LINK) {
            file.setSize(attrs.size());
            file.setLinkTarget(readLinkTarget(path));
        } else if (entryType == FileEntryType.FILE) {
            file.setSize(attrs.size());
        }
        file.setVirtual(false);
        var result = (T) file;
        file.setIcon(resolveIcon(result));
        return result;
    }

    /**
     * Describes what the link at {@code link} points to: the path it states (a relative target is resolved against
     * the link's directory; a Windows junction states none, so its resolved path is used instead) and the type of
     * the entry there, as it is - a link that points to another link has a target of type
     * {@link FileEntryType#LINK}. If nothing can be read at the target the link is broken and the target has no
     * type.
     *
     * @return the target, or {@code null} if the path can't be determined
     */
    private @Nullable LinkTarget readLinkTarget(Path link) {
        Path targetPath;
        try {
            try {
                targetPath = link.resolveSibling(Files.readSymbolicLink(link));
            } catch (NotLinkException | UnsupportedOperationException ex) {
                targetPath = link.toRealPath();
            }
        } catch (IOException ex) {
            logger.warn("Couldn't determine the target of the link {}", link, ex);
            return null;
        }
        var target = new DefaultLinkTarget();
        target.setUri(targetPath.toUri());
        try {
            target.setEntryType(resolveEntryType(
                    Files.readAttributes(targetPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)));
        } catch (IOException ex) {
            // the link is broken: nothing exists at the target, or it is out of reach
        }
        return target;
    }

    /**
     * Returns the type of the entry with these own attributes (read without following links).
     */
    private FileEntryType resolveEntryType(BasicFileAttributes attrs) {
        if (isLink(attrs)) {
            return FileEntryType.LINK;
        }
        if (attrs.isDirectory()) {
            return FileEntryType.DIRECTORY;
        }
        return attrs.isOther() ? FileEntryType.OTHER : FileEntryType.FILE;
    }
}
