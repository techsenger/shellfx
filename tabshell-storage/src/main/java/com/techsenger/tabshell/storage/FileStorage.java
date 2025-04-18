/*
 * Copyright 2024-2025 Pavel Castornii.
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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface FileStorage {

    /**
     * Returns the type of the storage.
     * @return
     */
    FileStorageType getType();

    /**
     * Returns the root uri of this storage.
     *
     * @return
     */
    URI getRootUri();

    /**
     * For example, for Windows it can be Disk C:.
     *
     * @return
     */
    String getDisplayName();

    /**
     * Returns true if it is a default file storage and false otherwise.
     *
     * @return
     */
    boolean isDefault();

    /**
     * Returns list of files.
     *
     * @param uri
     * @return
     * @throws NoSuchFileException
     * @throws AccessDeniedException
     * @throws IOException
     */
    List<GenericFile> getFiles(URI uri) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Returns concrete file.
     *
     * @param uri
     * @return
     * @throws NoSuchFileException
     * @throws AccessDeniedException
     * @throws InvalidFileException if file is invalid for this filesystem (e.g., Thumbs.db:encryptable file from Linux
     * storage in Windows)
     * @throws IOException
     */
    GenericFile getFile(URI uri) throws NoSuchFileException, AccessDeniedException, InvalidFileException, IOException;

    /**
     * Creates one directory.
     *
     * @param uri
     * @throws NoSuchFileException
     * @throws FileAlreadyExistsException
     * @throws AccessDeniedException
     * @throws IOException
     */
    void createDirectory(URI uri) throws NoSuchFileException, FileAlreadyExistsException, AccessDeniedException,
            IOException;

    /**
     * Renames one file/directory.
     *
     * @param uri
     * @param newName
     * @throws NoSuchFileException
     * @throws FileAlreadyExistsException
     * @throws AccessDeniedException
     * @throws IOException
     */
    void renameFile(URI uri, String newName) throws NoSuchFileException, FileAlreadyExistsException,
            AccessDeniedException, IOException;

    /**
     * Checks if the given URI refers to this {@link FileStorage}.
     *
     * @param uri the URI to check.
     * @return true if the URI refers to this FileStorage, false otherwise.
     */
    boolean refersToStorage(URI uri);

    /**
     * Writes content to a file. If the file already exists, it is overwritten. If the file does not exist,
     * it is created.
     *
     * @param uri
     * @param content
     * @param charset
     * @throws NoSuchFileException
     * @throws AccessDeniedException
     * @throws IOException
     */
    void writeFile(URI uri, String content, Charset charset) throws AccessDeniedException, IOException;

    /**
     * Reads one file.
     *
     * @param uri
     * @param charset
     * @return
     * @throws NoSuchFileException
     * @throws AccessDeniedException
     * @throws IOException
     */
    String readFile(URI uri, Charset charset) throws NoSuchFileException, AccessDeniedException, IOException;

    /**
     * Writes content to a file. If the file already exists, it is overwritten. If the file does not exist,
     * it is created.
     *
     * @param uri
     * @param content
     * @throws NoSuchFileException
     * @throws AccessDeniedException
     * @throws IOException
     */
    void writeFile(URI uri, byte[] content) throws AccessDeniedException, IOException;

    /**
     * Reads one file.
     *
     * @param uri
     * @param charset
     * @return
     * @throws NoSuchFileException
     * @throws AccessDeniedException
     * @throws IOException
     */
    byte[] readFile(URI uri) throws NoSuchFileException, AccessDeniedException, IOException;

}
