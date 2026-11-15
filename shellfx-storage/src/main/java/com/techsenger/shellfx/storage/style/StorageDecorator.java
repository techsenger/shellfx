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
import com.techsenger.shellfx.storage.FileStorage;
import java.util.function.Supplier;

/**
 * Decorates a storage with the icon and the style it is shown with. A component that shows a storage itself, such as
 * a root of a tree or a segment of a location bar, owns one decorator. It is independent of the file decorator: it is
 * the component that decides whether a root is shown as a storage or as a file. All methods must be called on the UI
 * thread.
 *
 * @author Pavel Castornii
 */
public final class StorageDecorator {

    private final StorageIconResolver iconResolver;

    private final StorageStyleResolver styleResolver;

    private final Supplier<Theme> themeSupplier;

    /**
     * Creates a decorator.
     *
     * @param iconResolver determines the icon of a storage
     * @param styleResolver determines the style of a storage
     * @param themeSupplier supplies the theme the storages are styled for
     */
    public StorageDecorator(StorageIconResolver iconResolver, StorageStyleResolver styleResolver,
            Supplier<Theme> themeSupplier) {
        this.iconResolver = iconResolver;
        this.styleResolver = styleResolver;
        this.themeSupplier = themeSupplier;
    }

    /**
     * Decorates the storage in its current state for the current theme.
     *
     * @param storage the storage to decorate
     * @return the icon and the style of the storage, never {@code null}
     */
    public IconStyleDecoration decorate(FileStorage<?> storage) {
        return new IconStyleDecoration(iconResolver.resolve(storage),
                styleResolver.resolve(storage, themeSupplier.get()));
    }
}
