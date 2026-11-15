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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.StorageFile;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * Decorates files with an icon and a style lazily, when a file is about to be shown and has none, so only the
 * files that are shown are decorated. The decorations belong to the decorator, not to the files, so a component that
 * shows files owns one decorator and drops the decorations of the files it stops showing. A root stands for its
 * storage and so gets the decoration of the storage. Files are matched by instance, not by {@code equals}, so a
 * method must be given the very files that are shown. All methods must be called on the UI thread.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public final class FileDecorator<F extends StorageFile> {

    private final FileIconResolver<? super F> iconResolver;

    private final FileStyleResolver<? super F> styleResolver;

    private final Function<? super F, FileDecorationType> typeResolver;

    private final Supplier<Theme> themeSupplier;

    private final ObservableMap<F, IconStyleDecoration> decorations =
            FXCollections.observableMap(new IdentityHashMap<>());

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
     * Returns the decoration of the file.
     *
     * @param file the file
     * @return the decoration, or {@code null} if the file has not been decorated yet or has to be decorated again
     */
    public @Nullable IconStyleDecoration getDecoration(F file) {
        return decorations.get(file);
    }

    /**
     * Returns the observable decoration of the file, which changes when the file is decorated, or when a reset or the
     * second loading phase makes the decoration outdated, so the elements that show the file can observe it.
     *
     * @param file the file
     * @return the value holding the decoration, or {@code null} while the file has none
     */
    public ObservableValue<IconStyleDecoration> decorationValue(F file) {
        return Bindings.valueAt(decorations, file);
    }

    /**
     * Gives the file the decoration it has none of, which a cell calls before it shows the file. A file that only
     * lacks the style keeps its icon. Afterwards the decoration of the file is complete.
     *
     * @param file the file about to be shown
     */
    public void decorate(F file) {
        var decoration = decorations.get(file);
        if (decoration != null && decoration.style() != null) {
            return;
        }
        if (file.isRoot()) {
            decorations.put(file, file.getStorage().getDecorator().decorate(themeSupplier.get()));
            return;
        }
        var type = typeResolver.apply(file);
        var icon = decoration != null ? decoration.icon() : iconResolver.resolve(file, type);
        var style = styleResolver.resolve(file, type, themeSupplier.get());
        decorations.put(file, new IconStyleDecoration(icon, style));
    }

    /**
     * Drops the decoration of the files, which the cells that show them decorate again at once, and the other files
     * are decorated when they are shown. A component also calls it for the files it stops showing, so they are not
     * held any longer.
     *
     * @param files the shown instances whose icon and style are outdated; an equal but other instance is not matched
     */
    public void reset(Collection<? extends F> files) {
        for (var file : files) {
            decorations.remove(file);
        }
    }

    /**
     * Makes the style of the files outdated but keeps their icons, as a theme or a mark changes only the style.
     *
     * @param files the files whose style is outdated
     */
    public void resetStyle(Collection<? extends F> files) {
        for (var file : files) {
            var decoration = decorations.get(file);
            if (decoration != null) {
                decorations.put(file, new IconStyleDecoration(decoration.icon(), null));
            }
        }
    }

    /**
     * Drops the decorations of all files, which a component does when it shows other files from scratch.
     */
    public void clear() {
        decorations.clear();
    }
}
