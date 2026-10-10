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

/**
 * An operation a {@link FileStorage} performed on one of its files, reported to its {@link StorageListener}s. It is
 * a marker: the operations of the storage itself are the constants of {@link FileOperations}, and an application
 * that extends the storage with its own operations declares them as constants of its own class.
 *
 * @author Pavel Castornii
 */
public interface FileOperation {
    // marker
}
