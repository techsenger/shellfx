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
 * Decorates a storage with the icon and the style it is shown with. The decorator is given to the storage when it is
 * created, by the one who knows the kinds and the states of the storage, and it takes both from one state of the
 * storage, so they never disagree.
 *
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface StorageDecorator {

    /**
     * Decorates the storage for a theme.
     *
     * @param theme the theme the storage is shown in
     * @return the icon and the style of the storage in its current state, never {@code null}; the style is never
     *     {@code null} either, and a style without values clears the styles of the storage
     */
    IconStyleDecoration decorate(Theme theme);
}
