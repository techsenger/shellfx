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
import com.techsenger.shellfx.storage.DefaultStorageFile;
import com.techsenger.shellfx.storage.StorageFile;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Decorates files with an icon and a style lazily, when a file is about to be shown and has none, so only the
 * files that are shown are decorated. The files must be {@code DefaultStorageFile}s, as the decoration is written
 * to them. All methods must be called on the UI thread.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public final class FileDecorator<F extends StorageFile> {

    private final FileIconResolver<? super F> iconResolver;

    private final FileStyleResolver<? super F> styleResolver;

    private final Function<? super F, FileDecorationType> typeResolver;

    private final Supplier<Theme> themeSupplier;

    /**
     * Creates a decorator.
     *
     * @param iconResolver determines the icon of a file
     * @param styleResolver determines the style of a file
     * @param typeResolver tells whether the decoration of a file is provisional or final at the moment it is decorated
     * @param themeSupplier supplies the theme the files are styled for
     */
    public FileDecorator(FileIconResolver<? super F> iconResolver, FileStyleResolver<? super F> styleResolver,
            Function<? super F, FileDecorationType> typeResolver, Supplier<Theme> themeSupplier) {
        this.iconResolver = iconResolver;
        this.styleResolver = styleResolver;
        this.typeResolver = typeResolver;
        this.themeSupplier = themeSupplier;
    }

    /**
     * Gives the file the decoration it has none of, which a cell calls before it shows the file. A file that only
     * lacks the style keeps its icon. Afterwards the decoration of the file is complete.
     *
     * @param file the file about to be shown
     */
    public void decorate(F file) {
        var decoration = file.getDecoration();
        if (decoration != null && decoration.style() != null) {
            return;
        }
        var type = typeResolver.apply(file);
        var icon = decoration != null ? decoration.icon() : iconResolver.resolve(file, type);
        var style = styleResolver.resolve(file, type, themeSupplier.get());
        DefaultStorageFile.cast(file).setDecoration(new IconStyleDecoration(icon, style));
    }

    /**
     * Makes the decoration of the files outdated, which the cells that show them decorate again at once, and the
     * other files are decorated when they are shown.
     *
     * @param files the files whose icon and style are outdated
     */
    public void reset(Collection<? extends F> files) {
        for (var file : files) {
            DefaultStorageFile.cast(file).setDecoration(null);
        }
    }

    /**
     * Makes the style of the files outdated but keeps their icons, as a theme or a mark changes only the style.
     *
     * @param files the files whose style is outdated
     */
    public void resetStyle(Collection<? extends F> files) {
        for (var file : files) {
            var decoration = file.getDecoration();
            if (decoration != null) {
                DefaultStorageFile.cast(file).setDecoration(new IconStyleDecoration(decoration.icon(), null));
            }
        }
    }
}
