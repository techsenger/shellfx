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
 * <p>A {@code FileStorage} abstracts over different storage technologies (local file system, FTP,
 * Google Drive, etc.) and exposes a uniform API for listing, reading, writing, and navigating
 * file entries. All entries produced by a storage are typed via the type parameter {@code T},
 * which allows storage-specific implementations to return richer subclasses of
 * {@link GenericFile} without requiring callers to cast.
 *
 * <p>Every storage has a single root URI returned by {@link #getRootUri()}. All URIs passed to
 * or returned by this interface must be within that root.
 *
 * @param <T> the concrete file entry type produced by this storage
 * @author Pavel Castornii
 */
public interface FileStorage<T extends GenericFile> {

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
    URI getRootUri();

    /**
     * Returns a human-readable name for this storage suitable for display in the UI.
     *
     * <p>Examples: {@code "Disk C:"} on Windows, {@code "/"} on Linux, {@code "Google Drive"}.
     *
     * @return the display name, never {@code null}
     */
    String getDisplayName();

    /**
     * Returns {@code true} if this is the default storage for the current platform or user
     * session. There should be at most one default storage per storage type.
     *
     * @return {@code true} if this storage is the default, {@code false} otherwise
     */
    boolean isDefault();

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
     *                               as a valid {@link GenericFile} in the current environment
     *                               (e.g. a {@code Thumbs.db:encryptable} path on Linux)
     * @throws IOException           if an I/O error occurs
     */
    T getFile(URI uri) throws NoSuchFileException, AccessDeniedException, InvalidFileException, IOException;

    /**
     * Returns the parent directory of the given file entry with full metadata loaded from storage.
     *
     * @param file the file whose parent to retrieve
     * @return the parent entry, never {@code null}
     * @throws NoSuchFileException   if the parent directory does not exist
     * @throws AccessDeniedException if the caller lacks read permission
     * @throws IOException           if an I/O error occurs
     */
    T getParent(T file) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Returns the root directory entry of this storage.
     *
     * <p>The root may be either a real directory (e.g. on a network drive) or a virtual
     * placeholder (e.g. a local file system root that does not physically exist as a standalone
     * directory entry).
     *
     * @return the root entry, never {@code null}
     */
    T getRoot();

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
