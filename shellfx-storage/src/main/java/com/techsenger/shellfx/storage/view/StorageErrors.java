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

package com.techsenger.shellfx.storage.view;

import com.techsenger.annotations.Nullable;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;

/**
 * Tells the user why an operation of a storage failed, in the same words for every client of the storages.
 *
 * @author Pavel Castornii
 */
public final class StorageErrors {

    private StorageErrors() {
        // empty
    }

    /**
     * Describes the reason of a failure by the type of the exception. The text goes after the action that failed,
     * e.g. {@code "Couldn't open 'x': " + describe(cause)}.
     *
     * @param cause the exception the operation failed with, or {@code null} if it is not known
     * @return the reason as a short phrase without the final period
     */
    public static String describe(@Nullable Exception cause) {
        if (cause == null) {
            return "unknown error";
        }
        return switch (cause) {
            case NoSuchFileException e -> "it doesn't exist, or its storage is not available";
            case AccessDeniedException e -> "access is denied";
            case NotDirectoryException e -> "it is not a directory";
            case FileSystemLoopException e -> "its links form a loop";
            default -> cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
        };
    }
}
