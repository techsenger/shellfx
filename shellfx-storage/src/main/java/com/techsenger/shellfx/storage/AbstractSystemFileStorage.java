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
import static com.techsenger.shellfx.storage.UriUtils.getParentUri;
import com.techsenger.shellfx.storage.style.StorageIconResolver;
import com.techsenger.shellfx.storage.style.StorageStyleResolver;
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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSystemFileStorage<T extends StorageFile> extends AbstractFileStorage<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSystemFileStorage.class);

    private static final int MAX_LINK_CHAIN = 40;

    private final Factory<? extends DefaultStorageFile> fileFactory;

    private final Path path;

    /**
     * Constructs a storage. The target of a link that ends in another storage is not found by the storage; it is
     * found later, in the second loading phase.
     *
     * @param type the storage type
     * @param name the display name
     * @param rootUri the root URI
     * @param id the identifier of the volume or the device the storage stands for
     * @param fileFactory the factory of the file entries
     * @param iconResolver the resolver of the icon of the storage
     * @param styleResolver the resolver of the style of the storage
     */
    public AbstractSystemFileStorage(FileStorageType type, String name, URI rootUri, String id,
            Factory<? extends DefaultStorageFile> fileFactory, StorageIconResolver iconResolver,
            StorageStyleResolver styleResolver) {
        super(type, name, rootUri, id, iconResolver, styleResolver);
        this.fileFactory = fileFactory;
        this.path = Paths.get(rootUri);
    }

    @Override
    public List<T> getDirectories(URI uri, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<T>();
        var entryPath = toPath(uri);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(entryPath)) {
            for (Path childPath : stream) {
                try {
                    var attrs = readAttributes(childPath);
                    // a link is not a directory, even if it points to one
                    if (attrs.isDirectory() && !isLink(attrs)) {
                        result.add(createFile(childPath, attrs, childPath.toUri()));
                    }
                } catch (InvalidFileException ex) {
                    logger.error("Couldn't create StorageFile from {}", childPath, ex);
                } catch (IOException ex) {
                    logger.error("Couldn't read attributes for {}", childPath, ex);
                }
            }
        } catch (DirectoryIteratorException e) {
            throw diagnoseReadFailure(entryPath, e.getCause());
        } catch (IOException e) {
            throw diagnoseReadFailure(entryPath, e);
        }
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    public List<T> getFiles(URI uri, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, IOException {
        var result = new ArrayList<T>();
        var entryPath = toPath(uri);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(entryPath)) {
            for (Path filePath : stream) {
                try {
                    T createdFile = createFile(filePath, filePath.toUri());
                    result.add(createdFile);
                } catch (InvalidFileException ex) {
                    logger.error("Couldn't create StorageFile from {}", filePath, ex);
                }
            }
        } catch (DirectoryIteratorException e) {
            throw diagnoseReadFailure(entryPath, e.getCause());
        } catch (IOException e) {
            throw diagnoseReadFailure(entryPath, e);
        }
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    public List<T> getFilesRecursively(URI uri, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, IOException {
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
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    public boolean refersToStorage(URI uri) {
        return FileStorageUtils.isWithin(getUri(), uri, false);
    }

    @Override
    public T getFile(URI uri, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, InvalidFileException, IOException {
        var entryPath = toPath(uri);
        T result;
        if (entryPath.equals(getPath())) {
            result = getRoot();
        } else {
            try {
                result = createFile(entryPath, uri);
            } catch (IOException ex) {
                throw diagnoseReadFailure(entryPath, ex);
            }
        }
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    public @Nullable T getParent(URI uri, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, IOException {
        var segments = UriUtils.getPathSegments(getUri(), uri);
        var parentUri = getParentUri(getUri(), uri, segments);
        T result;
        if (parentUri == null) {
            result = null;
        } else if (segments.size() == 1) {
            result = getRoot();
        } else {
            var entryPath = toPath(parentUri);
            try {
                result = createFile(entryPath, parentUri);
            } catch (IOException ex) {
                throw diagnoseReadFailure(entryPath, ex);
            }
        }
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    public @Nullable T getParent(T file, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, IOException {
        return getParent(file.getUri(), operationType);
    }

    @Override
    public List<T> getHierarchy(URI uri, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, IOException {
        var segments = UriUtils.getPathSegments(getUri(), uri);
        var result = new ArrayList<T>(segments.size() + 1);
        result.add(getRoot());
        var currentUri = getUri();
        for (var i = 0; i < segments.size(); i++) {
            currentUri = i + 1 == segments.size() ? uri : UriUtils.resolvePath(currentUri, segments.get(i), true);
            var entryPath = toPath(currentUri);
            try {
                result.add(createFile(entryPath, currentUri));
            } catch (IOException ex) {
                throw diagnoseReadFailure(entryPath, ex);
            }
        }
        notifyListeners(uri, FileOperations.LIST, operationType);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getRoot() {
        var file = fileFactory.create();
        file.setVirtual(true);
        file.setRoot(true);
        file.setEntryType(FileEntryType.DIRECTORY);
        file.setUri(getUri());
        file.setStorage(this);
        file.setName(getName());
        return (T) file;
    }

    @Override
    public void createDirectory(URI uri, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        try {
            Files.createDirectory(entryPath);
        } catch (IOException ex) {
            throw diagnoseCreateFailure(entryPath, ex);
        }
        notifyListeners(uri, FileOperations.CREATE, operationType);
    }

    @Override
    public T createFile(URI uri, String name, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        T result;
        try {
            Files.createFile(entryPath);
            result = createFile(entryPath, uri);
        } catch (InvalidFileException ex) {
            // the file was created, only its entry could not be built, so "already exists" would be wrong
            throw ex;
        } catch (IOException ex) {
            throw diagnoseCreateFailure(entryPath, ex);
        }
        notifyListeners(uri, FileOperations.CREATE, operationType);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T createVirtual(@Nullable URI uri, FileEntryType entryType, String name) {
        var file = fileFactory.create();
        file.setEntryType(entryType);
        file.setName(name);
        file.setUri(uri);
        file.setStorage(this);
        file.setVirtual(true);
        return (T) file;
    }

    @Override
    public void renameFile(URI uri, String newName, OperationType operationType) throws NoSuchFileException,
            FileAlreadyExistsException, AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        var newPath = entryPath.resolveSibling(newName);
        try {
            Files.move(entryPath, newPath);
        } catch (IOException ex) {
            throw diagnoseDeleteFailure(entryPath, ex);
        }
        notifyListeners(newPath.toUri(), FileOperations.RENAME, operationType);
    }

    @Override
    public void writeFile(URI uri, String content, Charset charset, OperationType operationType)
            throws AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        try {
            FileUtils.writeFile(entryPath, content, charset);
        } catch (IOException ex) {
            throw diagnoseWriteFailure(entryPath, ex);
        }
        notifyListeners(uri, FileOperations.WRITE, operationType);
    }

    @Override
    public String readFile(URI uri, Charset charset, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        String result;
        try {
            result = FileUtils.readFile(entryPath, charset);
        } catch (IOException ex) {
            throw diagnoseReadFailure(entryPath, ex);
        }
        notifyListeners(uri, FileOperations.READ, operationType);
        return result;
    }

    @Override
    public void writeFile(URI uri, byte[] content, OperationType operationType)
            throws AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        try (OutputStream out = Files.newOutputStream(entryPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            out.write(content);
        } catch (IOException ex) {
            throw diagnoseWriteFailure(entryPath, ex);
        }
        notifyListeners(uri, FileOperations.WRITE, operationType);
    }

    @Override
    public byte[] readFile(URI uri, OperationType operationType)
            throws NoSuchFileException, AccessDeniedException, IOException {
        var entryPath = toPath(uri);
        byte[] result;
        try (InputStream in = Files.newInputStream(entryPath)) {
            result = in.readAllBytes();
        } catch (IOException ex) {
            throw diagnoseReadFailure(entryPath, ex);
        }
        notifyListeners(uri, FileOperations.READ, operationType);
        return result;
    }

    protected Factory<? extends DefaultStorageFile> getFileFactory() {
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
     * Creates the link of a file.
     *
     * @param targetUri the URI of the path the link states
     * @param resolvedUri the URI of the entry at the end of the chain of links, or {@code null} if there is none
     * @param target the file at the end of the chain of links, or {@code null} if the second loading phase has to
     *     find it or the link is broken
     * @return the link, never {@code null}
     */
    protected EntryLink createLink(URI targetUri, @Nullable URI resolvedUri, @Nullable T target) {
        var link = new DefaultEntryLink();
        link.setTargetUri(targetUri);
        link.setResolvedUri(resolvedUri);
        link.setTarget(target);
        return link;
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
     * Reads the entry's own attributes without following links, in a single access to the file system. A system
     * whose subclasses need a richer kind of attributes overrides this to read them.
     *
     * @param entryPath the entry's path
     * @return the attributes
     * @throws IOException if the attributes can't be read
     */
    BasicFileAttributes readAttributes(Path entryPath) throws IOException {
        return Files.readAttributes(entryPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
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
     * Returns the type of the entry. A system whose attributes can't tell some entry from a link (e.g. a Windows
     * junction) overrides this to recognize it.
     *
     * @param entryPath the entry's path
     * @param attrs the entry's own attributes, read without following links
     * @return the type of the entry
     */
    FileEntryType resolveEntryType(Path entryPath, BasicFileAttributes attrs) {
        if (isLink(attrs)) {
            return FileEntryType.LINK;
        }
        if (attrs.isDirectory()) {
            return FileEntryType.DIRECTORY;
        }
        return attrs.isOther() ? FileEntryType.OTHER : FileEntryType.FILE;
    }

    /**
     * Lets a subclass fill in what only the entry's attributes tell, e.g. the permissions; called once for every
     * created file. Does nothing by default.
     *
     * @param file the file being created
     * @param entryPath the entry's path
     * @param attrs the entry's own attributes, read without following links; a Unix storage gives the POSIX ones
     */
    protected void populateFile(DefaultStorageFile file, Path entryPath, BasicFileAttributes attrs) {
        // empty
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
            var attrs = readAttributes(entryPath);
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
        file.setUri(uri);
        file.setModifiedTime(attrs.lastModifiedTime().toMillis());
        file.setCreatedTime(attrs.creationTime().toMillis());
        var entryType = resolveEntryType(entryPath, attrs);
        file.setEntryType(entryType);
        if (entryType == FileEntryType.LINK) {
            file.setSize(attrs.size());
            file.setLink(readLink(entryPath));
        } else if (entryType == FileEntryType.FILE) {
            file.setSize(attrs.size());
        }
        file.setVirtual(false);
        populateFile(file, entryPath, attrs);
        return (T) file;
    }

    /**
     * Describes what the link at {@code link} points to: the path it states (a relative target is resolved against
     * the link's directory and normalized, so a stated {@code ./tmp} or {@code ../x} comes out as a clean path; a
     * Windows junction states none, so its resolved path is used instead) and the path of the entry at the end of the
     * chain of links, as the file system resolves it. The file there is left to the second loading phase, except for
     * an end that exists but has no path of its own (e.g. the pipe behind {@code /dev/stderr}): the file system
     * cannot be asked about it again, so its attributes are read here and the file is given at once. If nothing
     * exists at the end, or the chain is a cycle, the link is broken.
     *
     * @return the link, or {@code null} if the path can't be determined
     */
    private @Nullable EntryLink readLink(Path link) {
        var folder = link.getParent();
        if (folder == null) {
            return null;
        }
        Path targetPath;
        try {
            // the path stated in a link is relative to the folder of the link
            targetPath = folder.resolve(Files.readSymbolicLink(link)).normalize();
        } catch (NotLinkException | UnsupportedOperationException ex) {
            try {
                // a junction states no path, so the resolved one is used
                targetPath = link.toRealPath();
            } catch (IOException ex2) {
                logger.warn("Couldn't determine the target of the link {}", link, ex2);
                return null;
            }
        } catch (IOException ex) {
            logger.warn("Couldn't determine the target of the link {}", link, ex);
            return null;
        }
        URI resolvedUri = null;
        T target = null;
        try {
            resolvedUri = link.toRealPath().toUri();
        } catch (IOException ex) {
            target = readChainEnd(link);
        }
        return createLink(targetPath.toUri(), resolvedUri, target);
    }

    /**
     * Gives the file at the end of the chain of links for the case when the file system can't resolve the real path
     * of the link, so the second loading phase could not find that file by its URI. This happens to an end that exists
     * but has no path of its own (e.g. the pipe behind {@code /dev/stderr}); its attributes are read through the
     * links, and the file is created from them with the last path of the chain.
     *
     * @return the file, or {@code null} if the link is broken: nothing exists at the end of the chain, it is a
     *     cycle, or it is out of reach
     */
    private @Nullable T readChainEnd(Path link) {
        try {
            var endPath = findChainEnd(link);
            return createFile(endPath, Files.readAttributes(link, BasicFileAttributes.class), endPath.toUri());
        } catch (IOException ex) {
            logger.debug("The link {} is broken", link, ex);
            return null;
        }
    }

    /**
     * Follows the chain of links from {@code link} and returns the path where it ends, without asking the file
     * system whether that path exists. A chain longer than {@link #MAX_LINK_CHAIN} is cut where it stands.
     */
    private Path findChainEnd(Path link) {
        var current = link;
        for (var i = 0; i < MAX_LINK_CHAIN && Files.isSymbolicLink(current); i++) {
            try {
                current = current.resolveSibling(Files.readSymbolicLink(current)).normalize();
            } catch (IOException | UnsupportedOperationException ex) {
                break;
            }
        }
        return current;
    }
}
