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
import java.net.URI;

/**
 * Gives the file at the end of a chain of links, which may lie in any storage, so a storage can describe a link
 * without knowing the other storages.
 *
 * @param <T> the type of the files
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface LinkTargetResolver<T extends StorageFile> {

    /**
     * Resolves the file at the URI.
     *
     * @param uri the URI of the file at the end of the chain of links
     * @return the file, or {@code null} if there is none or it can't be reached, so the link is broken
     */
    @Nullable T resolve(URI uri);
}
