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
import com.techsenger.shellfx.material.theme.Theme;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.List;

/**
 * Represents a storage backend that provides access to a hierarchical file system.
 *
 * <p>A {@code FileStorage} abstracts over different storage technologies (local file system, FTP, Google Drive, etc.)
 * and exposes a uniform API for listing, reading, writing, and navigating file entries. All entries produced by a
 * storage are typed via the type parameter {@code T}, which allows storage-specific implementations to return richer
 * subclasses of {@link StorageFile} without requiring callers to cast.
 *
 * <p><b>Naming.</b> "File" in most of this interface's own names ({@code FileStorage}, {@link #getFiles(URI)},
 * {@link #getFile(URI)}, {@link StorageFile}) is used generically, for an entry of either structural kind - a
 * directory is a "file" in that sense too. "Directory" always means specifically the
 * {@link FileEntryType#DIRECTORY} kind, as opposed to {@link FileEntryType#FILE} (a regular, non-directory
 * entry) - so the same word "file" is narrower at the {@link FileEntryType} level than it is at most of this
 * interface's own methods. {@link #createFile}, like {@link #writeFile(URI, byte[])}/{@link #readFile(URI)},
 * is one of the exceptions: it uses "file" in the narrower, {@link FileEntryType#FILE}-only sense. One
 * consequence of the generic sense: {@link #getDirectories(URI)} exists as a narrower, explicitly-filtered
 * alternative to {@link #getFiles(URI)} for callers (e.g. a directory tree) that only need subdirectories, so
 * a storage need not fetch every regular file's metadata just to answer them.
 *
 * <p>Every storage has a single root URI returned by {@link #getUri()}. All URIs passed to
 * or returned by this interface must be within that root.
 *
 * <p><b>EAFP.</b> A storage follows the EAFP principle (Easier to Ask for Forgiveness than Permission): it performs
 * the requested operation right away and, only if the operation fails, investigates the reason and reports it with
 * the most specific exception, such as {@link NoSuchFileException} or {@link AccessDeniedException}, even when the
 * underlying backend reports it as a plain {@link IOException}. It does not check beforehand that the entry
 * exists or that the permissions suffice (LBYL, "look before you leap"). The reasons are:
 * <ul>
 * <li>A successful operation, the overwhelmingly common case, costs exactly one request to the backend. Every
 * pre-check is an extra request, which adds up in bulk operations over thousands of entries and is expensive for a
 * remote storage, where each request is a network round trip.</li>
 * <li>A pre-check can be outdated by the time the operation runs, because another process may delete or lock the
 * entry in between. The state found after a failure is the state the operation really failed on.</li>
 * <li>The caller sees one failure path: whatever went wrong is thrown as an exception, whether or not the backend
 * would have detected it by itself.</li>
 * </ul>
 * The only exception is a check that cannot be made afterwards, because the operation would not fail but do
 * something unwanted, for instance silently replacing an existing entry.
 *
 * @param <T> the concrete file entry type produced by this storage
 * @author Pavel Castornii
 */
public interface FileStorage<T extends StorageFile> {

    /**
     * Returns the type of this storage (e.g. local, FTP, cloud).
     *
     * @return the storage type, never {@code null}
     */
    FileStorageType getType();

    /**
     * Returns the root URI of this storage. All entries managed by this storage have URIs that
     * are descendants of this URI.
     *
     * @return the root URI, never {@code null}
     */
    URI getUri();

    /**
     * Returns a human-readable name of this storage suitable for display in the UI.
     *
     * <p>Examples: {@code "Disk C:"} on Windows, {@code "/"} on Linux, {@code "Google Drive"}.
     *
     * @return the name, never {@code null}
     */
    String getName();

    /**
     * Returns the icon representing this storage in the UI (e.g. a location bar or a directory tree root).
     *
     * @return the icon, never {@code null}
     */
    FontIcon<?> getIcon();

    /**
     * Creates the style of this storage for the theme, for a {@link FileStyleResolver} to use when it resolves the
     * style of the storage in a list of storages. The storage is shown with its icon and its name, so both can be
     * styled. The storage does not know where it is shown, so views never apply this style themselves.
     *
     * @param theme the theme the storage is shown in
     * @return the style, or {@code null} if the storage has no style of its own
     */
    @Nullable IconTextStyle createStyle(Theme theme);

    /**
     * Returns the root directory entry of this storage.
     *
     * <p>The root may be either a real directory (e.g. on a network drive) or a virtual placeholder (e.g. a local
     * file system root that does not physically exist as a standalone directory entry).
     *
     * @return the root entry, never {@code null}
     */
    T getRoot();

    /**
     * Creates the style of the root directory of this storage for the theme, for a {@link FileStyleResolver} to use
     * when it resolves the style of the root. The root is shown with its icon and its name, so both can be styled.
     * The storage does not know where it is shown, so views never apply this style themselves.
     *
     * @param theme the theme the root is shown in
     * @return the style, or {@code null} if the storage has no style of its own for the root
     */
    @Nullable IconTextStyle createRootStyle(Theme theme);

    /**
     * Returns the direct subdirectories of the directory identified by {@code uri}, excluding regular files.
     *
     * @param uri the URI of the directory to list
     * @return a list of direct subdirectories, never {@code null}, may be empty
     * @throws NoSuchFileException   if no directory exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission for the directory
     * @throws IOException           if an I/O error occurs
     */
    List<T> getDirectories(URI uri) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Returns the direct children of the directory identified by {@code uri}.
     *
     * @param uri the URI of the directory to list
     * @return a list of direct children, never {@code null}, may be empty
     * @throws NoSuchFileException   if no directory exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission for the directory
     * @throws IOException           if an I/O error occurs
     */
    List<T> getFiles(URI uri) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Returns all descendants of the directory identified by {@code uri}, traversing
     * subdirectories recursively.
     *
     * <p>The order is guaranteed: a directory always appears in the list before its contents.
     * The directory at {@code uri} itself is not included in the result.
     *
     * @param uri the URI of the root directory for the recursive traversal
     * @return an ordered list of all descendants, never {@code null}, may be empty
     * @throws NoSuchFileException   if no directory exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission for the directory
     * @throws IOException           if an I/O error occurs
     */
    List<T> getFilesRecursively(URI uri) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Returns the file entry at the given URI.
     *
     * @param uri the URI of the entry to retrieve
     * @return the file entry, never {@code null}
     * @throws NoSuchFileException   if no entry exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission
     * @throws InvalidFileException  if the entry exists on the storage but cannot be represented
     *                               as a valid {@link StorageFile} in the current environment
     *                               (e.g. a {@code Thumbs.db:encryptable} path on Linux)
     * @throws IOException           if an I/O error occurs
     */
    T getFile(URI uri) throws NoSuchFileException, AccessDeniedException, InvalidFileException, IOException;

    /**
     * Returns the parent directory of the given file entry with full metadata loaded from storage.
     * <p>
     * If the given file represents the storage root directory, {@code null} is returned.
     * </p>
     *
     * @param file the file whose parent directory to retrieve
     * @return the parent directory, or {@code null} if {@code file} is the storage root directory
     * @throws NoSuchFileException if the parent directory does not exist
     * @throws AccessDeniedException if the caller lacks read permission
     * @throws IOException if an I/O error occurs
     */
    @Nullable T getParent(T file) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Returns the parent directory of the file identified by the given URI with full metadata loaded from storage.
     * <p>
     * If the given URI represents the storage root directory, {@code null} is returned.
     * </p>
     *
     * @param uri the URI of the file whose parent directory to retrieve
     * @return the parent directory, or {@code null} if the given URI represents the storage root directory
     * @throws NoSuchFileException if the parent directory does not exist
     * @throws AccessDeniedException if the caller lacks read permission
     * @throws IOException if an I/O error occurs
     */
    @Nullable T getParent(URI uri) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Returns the whole chain of entries leading to the entry at {@code uri}, from the storage root down to the
     * entry itself, which is the last element.
     *
     * <p>All elements except the last are always directories. The last one is the entry at {@code uri} itself, so it
     * is a directory if {@code uri} points to a directory, and a file if it points to a file. If {@code uri} is the
     * storage URI, the chain is the root directory alone.
     *
     * <p>The operation may be very expensive, so the storage decides whether the elements are real entries or
     * {@link StorageFile#isVirtual() virtual} ones without data. A virtual element is guaranteed to have only its
     * name, URI and entry type.
     *
     * @param uri the URI of the entry the chain ends with
     * @return the chain starting with the root directory and ending with the entry at {@code uri}, never
     *     {@code null} or empty
     * @throws NoSuchFileException   if no entry exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission for one of the directories
     * @throws IOException           if an I/O error occurs
     */
    List<T> getHierarchy(URI uri) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Creates a new directory at the given URI. Only one directory level is created; the parent
     * directory must already exist.
     *
     * @param uri the URI of the directory to create
     * @throws NoSuchFileException        if the parent directory does not exist
     * @throws FileAlreadyExistsException if an entry already exists at {@code uri}
     * @throws AccessDeniedException      if the caller lacks write permission for the parent
     * @throws IOException                if an I/O error occurs
     */
    void createDirectory(URI uri) throws NoSuchFileException, FileAlreadyExistsException, AccessDeniedException,
            IOException;

    /**
     * Creates a new, empty regular file with real backing on this storage - the only way to do so, since
     * {@link #writeFile(URI, byte[])}/{@link #writeFile(URI, String, Charset)} always require content. Like
     * {@link #writeFile(URI, byte[])} and unlike {@link #createDirectory(URI)}/{@link #createVirtual}, this
     * method only ever creates a {@link FileEntryType#FILE} entry - never a directory or a symbolic link, whose
     * creation needs more than a name and a URI (a symbolic link needs a target, which this method has no
     * parameter for).
     *
     * @param name the name of the file to create
     * @param uri  the URI of the file to create
     * @return the created file, never {@code null}
     * @throws NoSuchFileException        if the parent directory does not exist
     * @throws FileAlreadyExistsException if an entry already exists at {@code uri}
     * @throws AccessDeniedException      if the caller lacks write permission for the parent
     * @throws IOException                if an I/O error occurs
     */
    T createFile(String name, URI uri) throws NoSuchFileException, FileAlreadyExistsException,
            AccessDeniedException, IOException;

    /**
     * Creates a virtual entry that has no real backing on this storage.
     *
     * <p>Virtual entries are used as lightweight placeholders — for example, a parent directory
     * inferred from a child URI, or a temporary entry pending a create operation.
     *
     * @param entryType the structural type of the virtual entry
     * @param name      the name of the virtual entry
     * @param uri       the URI of the virtual entry
     * @return the virtual entry, never {@code null}
     */
    T createVirtual(FileEntryType entryType, String name, @Nullable URI uri);

    /**
     * Renames the file or directory at {@code uri} to {@code newName}.
     *
     * <p>The entry is renamed within its current parent directory; moving to a different
     * directory is not supported by this method.
     *
     * @param uri     the URI of the entry to rename
     * @param newName the new name (not a full path, just the file name)
     * @throws NoSuchFileException        if no entry exists at {@code uri}
     * @throws FileAlreadyExistsException if an entry with {@code newName} already exists in the
     *                                    same directory
     * @throws AccessDeniedException      if the caller lacks write permission
     * @throws IOException                if an I/O error occurs
     */
    void renameFile(URI uri, String newName) throws NoSuchFileException, FileAlreadyExistsException,
            AccessDeniedException, IOException;

    /**
     * Returns {@code true} if the given URI refers to a location within this storage.
     *
     * @param uri the URI to check
     * @return {@code true} if this storage owns the given URI, {@code false} otherwise
     */
    boolean refersToStorage(URI uri);

    /**
     * Writes text content to a file at {@code uri} using the given character set. If the file
     * already exists it is overwritten; if it does not exist it is created.
     *
     * @param uri     the URI of the file to write
     * @param content the text content to write
     * @param charset the character set to use for encoding
     * @throws AccessDeniedException if the caller lacks write permission
     * @throws IOException           if an I/O error occurs
     */
    void writeFile(URI uri, String content, Charset charset) throws AccessDeniedException, IOException;

    /**
     * Reads the entire content of a text file at {@code uri} using the given character set.
     *
     * @param uri     the URI of the file to read
     * @param charset the character set to use for decoding
     * @return the file content as a string, never {@code null}
     * @throws NoSuchFileException   if no file exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission
     * @throws IOException           if an I/O error occurs
     */
    String readFile(URI uri, Charset charset) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Writes raw bytes to a file at {@code uri}. If the file already exists it is overwritten;
     * if it does not exist it is created.
     *
     * @param uri     the URI of the file to write
     * @param content the bytes to write
     * @throws AccessDeniedException if the caller lacks write permission
     * @throws IOException           if an I/O error occurs
     */
    void writeFile(URI uri, byte[] content) throws AccessDeniedException, IOException;

    /**
     * Reads the entire content of a binary file at {@code uri}.
     *
     * @param uri the URI of the file to read
     * @return the file content as a byte array, never {@code null}
     * @throws NoSuchFileException   if no file exists at {@code uri}
     * @throws AccessDeniedException if the caller lacks read permission
     * @throws IOException           if an I/O error occurs
     */
    byte[] readFile(URI uri) throws NoSuchFileException, AccessDeniedException, IOException;
}
