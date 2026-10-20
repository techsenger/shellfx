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
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.StorageFileUtils;

/**
 * Gives the files the icon and the style that its resolvers determine for the type of the decoration. The files must
 * be {@code DefaultStorageFile}s, as the icon and the style are written to them.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public final class DefaultFileDecorator<F extends StorageFile> implements FileDecorator<F> {

    private final FileIconResolver<? super F> iconResolver;

    private final FileStyleResolver<? super F> styleResolver;

    /**
     * Creates a decorator.
     *
     * @param iconResolver determines the icon of every file the decorator decorates
     * @param styleResolver determines the style of every file the decorator decorates
     */
    public DefaultFileDecorator(FileIconResolver<? super F> iconResolver, FileStyleResolver<? super F> styleResolver) {
        this.iconResolver = iconResolver;
        this.styleResolver = styleResolver;
    }

    @Override
    public void decorate(F file, FileDecorationType type, Theme theme) {
        var defaultFile = StorageFileUtils.cast(file);
        defaultFile.setIcon(iconResolver.resolve(file, type));
        defaultFile.setStyle(styleResolver.resolve(file, type, theme));
    }
}
