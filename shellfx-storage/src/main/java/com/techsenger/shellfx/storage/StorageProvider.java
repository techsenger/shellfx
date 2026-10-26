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

import java.util.List;

/**
 * Tells which storages there are at the moment. It creates nothing and is asked each time the storages are needed, so
 * storages that come and go, e.g. removable drives, are seen; a client that works with only some of the storages
 * supplies just those.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
@FunctionalInterface
public interface StorageProvider<F extends StorageFile> {

    /**
     * Returns the storages that are there now.
     *
     * @return the storages, never {@code null}
     */
    List<? extends FileStorage<F>> getStorages();
}
