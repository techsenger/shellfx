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
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
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
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
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

    private final Path path;

    public AbstractSystemFileStorage(FileStorageType type, String displayName, URI rootUri,
            Factory<? extends DefaultGenericFile> fileFactory) {
        super(type, displayName, rootUri);
        this.fileFactory = fileFactory;
        this.path = Paths.get(rootUri);
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
        var entryPath = toPath(uri);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(entryPath)) {
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
        } catch (DirectoryIteratorException e) {
            throw diagnoseReadFailure(entryPath, e.getCause());
        } catch (IOException e) {
            throw diagnoseReadFailure(entryPath, e);
        }
        return result;
    }

    @Override
    public List<T> getFiles(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<T>();
        var entryPath = toPath(uri);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(entryPath)) {
            for (Path filePath : stream) {
                try {
                    T createdFile = createFile(filePath, filePath.toUri());
                    result.add(createdFile);
                } catch (InvalidFileException ex) {
                    logger.error("Couldn't create GenericFile from {}", filePath, ex);
                }
            }
        } catch (DirectoryIteratorException e) {
            throw diagnoseReadFailure(entryPath, e.getCause());
        } catch (IOException e) {
            throw diagnoseReadFailure(entryPath, e);
        }
        return result;
    }

    @Override
    public List<T> getFilesRecursively(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<T>();
        var entryPath = toPath(uri);
        try {
            Files.walkFileTree(entryPath, new SimpleFileVisitor<>() {
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
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    if (file.equals(entryPath)) {
                        // the start itself can't be visited, so there is nothing to return
                        throw exc;
                    }
                    logger.error("Couldn't visit file {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw diagnoseReadFailure(entryPath, e);
        }
        return result;
    }

    @Override
    public T getFile(URI uri) throws NoSuchFileException, AccessDeniedException, InvalidFileException, IOException {
        var entryPath = toPath(uri);
        if (entryPath.equals(getPath())) {
            return getRootDirectory();
        }
        try {
            return createFile(entryPath, uri);
        } catch (IOException ex) {
            throw diagnoseReadFailure(entryPath, ex);
        }
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
            var entryPath = toPath(parentUri);
            try {
                return createFile(entryPath, parentUri);
            } catch (IOException ex) {
                throw diagnoseReadFailure(entryPath, ex);
            }
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
        var entryPath = toPath(uri);
        try {
            Files.createDirectory(entryPath);
        } catch (IOException ex) {
            throw diagnoseCreateFailure(entryPath, ex);
        }
    }

    @Override
    public T createFile(String name, URI uri) throws NoSuchFileException, FileAlreadyExistsException,
            AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        try {
            Files.createFile(entryPath);
            return createFile(entryPath, uri);
        } catch (InvalidFileException ex) {
            // the file was created, only its entry could not be built, so "already exists" would be wrong
            throw ex;
        } catch (IOException ex) {
            throw diagnoseCreateFailure(entryPath, ex);
        }
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
        var entryPath = toPath(uri);
        try {
            Files.move(entryPath, entryPath.resolveSibling(newName));
        } catch (IOException ex) {
            throw diagnoseDeleteFailure(entryPath, ex);
        }
    }

    @Override
    public void writeFile(URI uri, String content, Charset charset) throws AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        try {
            FileUtils.writeFile(entryPath, content, charset);
        } catch (IOException ex) {
            throw diagnoseWriteFailure(entryPath, ex);
        }
    }

    @Override
    public String readFile(URI uri, Charset charset) throws NoSuchFileException, AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        try {
            return FileUtils.readFile(entryPath, charset);
        } catch (IOException ex) {
            throw diagnoseReadFailure(entryPath, ex);
        }
    }

    @Override
    public void writeFile(URI uri, byte[] content) throws AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        try (OutputStream out = Files.newOutputStream(entryPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            out.write(content);
        } catch (IOException ex) {
            throw diagnoseWriteFailure(entryPath, ex);
        }
    }

    @Override
    public byte[] readFile(URI uri) throws NoSuchFileException, AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        try (InputStream in = Files.newInputStream(entryPath)) {
            return in.readAllBytes();
        } catch (IOException ex) {
            throw diagnoseReadFailure(entryPath, ex);
        }
    }

    protected Factory<? extends DefaultGenericFile> getFileFactory() {
        return fileFactory;
    }

    /**
     * Returns the path of the root of this storage, the counterpart of {@link #getUri()}.
     *
     * @return the root path, never {@code null}
     */
    protected Path getPath() {
        return path;
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

    /**
     * Explains why reading {@code entryPath} (a file, its attributes or a directory listing) failed. The methods of
     * {@code java.nio.file} document no more than a general {@link IOException} for this, so the specific
     * {@link NoSuchFileException} and {@link AccessDeniedException} are found out here, and only after the failure,
     * so a successful read costs no extra check.
     *
     * @param entryPath the path that was read.
     * @param cause the exception the read failed with.
     * @return {@code cause} itself if it is already specific (a {@link FileSystemException}) or nothing better is
     *     found, otherwise the exception that describes the real reason.
     */
    protected IOException diagnoseReadFailure(Path entryPath, IOException cause) {
        if (cause instanceof FileSystemException) {
            return cause;
        }
        if (!Files.exists(entryPath)) {
            return new NoSuchFileException(entryPath.toString());
        }
        if (!Files.isReadable(entryPath)) {
            return new AccessDeniedException(entryPath.toString());
        }
        return cause;
    }

    /**
     * Explains why writing the content of {@code entryPath} failed; same idea as
     * {@link #diagnoseReadFailure(Path, IOException)}. Writing may create the file, so what is found out depends on
     * whether it exists: an existing file must be writable itself, a new one needs a parent directory that exists
     * and is writable.
     *
     * @param entryPath the path that was written.
     * @param cause the exception the write failed with.
     * @return {@code cause} itself if it is already specific (a {@link FileSystemException}) or nothing better is
     *     found, otherwise the exception that describes the real reason.
     */
    protected IOException diagnoseWriteFailure(Path entryPath, IOException cause) {
        if (cause instanceof FileSystemException) {
            return cause;
        }
        if (Files.exists(entryPath)) {
            return Files.isWritable(entryPath) ? cause : new AccessDeniedException(entryPath.toString());
        }
        return diagnoseParentFailure(entryPath, cause);
    }

    /**
     * Explains why creating {@code entryPath} (an empty file or a directory) failed; same idea as
     * {@link #diagnoseReadFailure(Path, IOException)}. {@code java.nio.file} reports a missing parent directory
     * only as a general {@link IOException}, and an existing entry only as an optional
     * {@link FileAlreadyExistsException}.
     *
     * @param entryPath the path that was created.
     * @param cause the exception the creation failed with.
     * @return {@code cause} itself if it is already specific (a {@link FileSystemException}) or nothing better is
     *     found, otherwise the exception that describes the real reason.
     */
    protected IOException diagnoseCreateFailure(Path entryPath, IOException cause) {
        if (cause instanceof FileSystemException) {
            return cause;
        }
        var parent = entryPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            return new NoSuchFileException(parent.toString());
        }
        if (Files.exists(entryPath, LinkOption.NOFOLLOW_LINKS)) {
            return new FileAlreadyExistsException(entryPath.toString());
        }
        return diagnoseParentFailure(entryPath, cause);
    }

    /**
     * Explains why removing {@code entryPath} from its directory (deleting or renaming it) failed; same idea as
     * {@link #diagnoseReadFailure(Path, IOException)}. The entry must exist, and what it needs is the write
     * permission on its parent directory, not on itself.
     *
     * @param entryPath the path that was removed.
     * @param cause the exception the removal failed with.
     * @return {@code cause} itself if it is already specific (a {@link FileSystemException}) or nothing better is
     *     found, otherwise the exception that describes the real reason.
     */
    protected IOException diagnoseDeleteFailure(Path entryPath, IOException cause) {
        if (cause instanceof FileSystemException) {
            return cause;
        }
        if (!Files.exists(entryPath, LinkOption.NOFOLLOW_LINKS)) {
            return new NoSuchFileException(entryPath.toString());
        }
        return diagnoseParentFailure(entryPath, cause);
    }

    protected Path toPath(URI uri) {
        return Paths.get(uri);
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
     * @param entryPath the entry's path
     * @param attrs the entry's own attributes, read without following links
     * @return {@code true} if the entry is hidden
     */
    boolean isHidden(Path entryPath, BasicFileAttributes attrs) {
        var name = entryPath.getFileName();
        return name != null && name.toString().startsWith(".");
    }

    /**
     * Tells whether the entry is an executable file, without any further access to the file system. By convention of
     * Unix-like systems it is if it is a regular file with an execute permission bit set for anyone; a system with
     * another notion (e.g. the file extension on Windows) overrides this.
     *
     * @param entryPath the entry's path
     * @param attrs the entry's own attributes, read without following links
     * @return {@code true} if the entry is executable
     */
    boolean isExecutable(Path entryPath, BasicFileAttributes attrs) {
        if (!attrs.isRegularFile() || !(attrs instanceof PosixFileAttributes posixAttributes)) {
            return false;
        }
        var permissions = posixAttributes.permissions();
        return permissions.contains(PosixFilePermission.OWNER_EXECUTE)
                || permissions.contains(PosixFilePermission.GROUP_EXECUTE)
                || permissions.contains(PosixFilePermission.OTHERS_EXECUTE);
    }

    /**
     * Finds out whether the parent directory of {@code entryPath} is missing or not writable, which is what a failed
     * change of that directory usually comes down to.
     */
    private IOException diagnoseParentFailure(Path entryPath, IOException cause) {
        var parent = entryPath.getParent();
        if (parent == null) {
            return cause;
        }
        if (!Files.exists(parent)) {
            return new NoSuchFileException(parent.toString());
        }
        return Files.isWritable(parent) ? cause : new AccessDeniedException(parent.toString());
    }

    @SuppressWarnings("unchecked")
    private T createFile(Path entryPath, URI uri) throws InvalidFileException {
        try {
            // the entry's own attributes, so a link is seen as a link and not as what it points to
            var attrs = Files.readAttributes(entryPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return createFile(entryPath, attrs, uri);
        } catch (InvalidFileException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidFileException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private T createFile(Path entryPath, BasicFileAttributes attrs, URI uri) {
        var file = fileFactory.create();
        file.setStorage(this);
        file.setName(entryPath.getFileName().toString());
        file.setHidden(isHidden(entryPath, attrs));
        file.setExecutable(isExecutable(entryPath, attrs));
        file.setUri(uri);
        file.setModifiedTime(attrs.lastModifiedTime().toMillis());
        file.setCreatedTime(attrs.creationTime().toMillis());
        var entryType = resolveEntryType(attrs);
        file.setEntryType(entryType);
        if (entryType == FileEntryType.LINK) {
            file.setSize(attrs.size());
            file.setLinkTarget(readLinkTarget(entryPath));
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
     * the link's directory and normalized, so a stated {@code ./tmp} or {@code ../x} comes out as a clean path; a
     * Windows junction states none, so its resolved path is used instead) and the type of
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
                targetPath = link.resolveSibling(Files.readSymbolicLink(link)).normalize();
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
