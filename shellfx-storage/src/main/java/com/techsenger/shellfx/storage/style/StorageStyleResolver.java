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

package com.techsenger.shellfx.storage.style;

import com.techsenger.shellfx.material.theme.Theme;

/**
 * Determines the styles a storage is shown with. The storage gives its own resolver, which knows the state of the
 * storage. While a view has a style of a storage, it owns the inline styles of the storage elements, so a style without
 * values clears them.
 *
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface StorageStyleResolver {

    /**
     * Resolves the styles of the storage.
     *
     * @param theme the theme the storage is shown in
     * @return the styles, never {@code null}; a style without values clears the styles of the storage
     */
    IconTextStyle resolve(Theme theme);
}
