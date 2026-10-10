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

/**
 * Determines the styles a storage is shown with. While a view has a resolver, it owns the inline styles of the
 * storage elements, so a missing style clears them.
 *
 * @author Pavel Castornii
 * @param <S> the type of storages
 */
@FunctionalInterface
public interface StorageStyleResolver<S extends FileStorage<?>> {

    /**
     * Resolves the styles of the storage.
     *
     * @param storage the storage to resolve the styles for
     * @return the styles, or {@code null} to clear the styles of the storage
     */
    @Nullable IconTextStyle resolve(S storage);
}
