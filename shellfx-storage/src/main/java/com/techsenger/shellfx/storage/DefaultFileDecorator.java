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

import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.icon.FontIcon;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.style.FileIconResolver;
import com.techsenger.shellfx.storage.style.FileStyleResolver;
import com.techsenger.shellfx.storage.style.IconTextStyle;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gives the files the icon and the style that its resolvers determine for the state they are in. The resolvers must
 * be safe to call from several threads, as the second loading phase calls them from its workers.
 *
 * <p>The results of all the files decorated by {@link #decorateAsync} before the write happens are written together
 * on the UI thread, so that a directory of thousands of files does not queue a task for each file.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public class DefaultFileDecorator<F extends StorageFile> implements FileDecorator<F> {

    private record Decorated<T extends StorageFile>(T file, FontIcon<?> icon, IconTextStyle style) {

    }

    private final FileIconResolver<? super F> iconResolver;

    private final FileStyleResolver<? super F> styleResolver;

    private final Queue<Decorated<F>> results = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean scheduled = new AtomicBoolean();

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
    public void decorate(List<F> files, Theme theme) {
        for (var file : files) {
            setIcon(file, iconResolver.resolve(file));
            setStyle(file, styleResolver.resolve(file, theme));
        }
    }

    @Override
    public void decorateAsync(F file, Theme theme) {
        results.add(new Decorated<>(file, iconResolver.resolve(file), styleResolver.resolve(file, theme)));
        if (scheduled.compareAndSet(false, true)) {
            UiExecutor.execute(this::writeResults);
        }
    }

    /**
     * Writes the icon to the file. The default works on {@link DefaultStorageFile}; a subclass whose files are of
     * another class overrides it to write the icon its own way.
     *
     * @param file the file to write to
     * @param icon the icon of the file
     * @throws IllegalArgumentException if the decorator can't write to the file
     */
    protected void setIcon(F file, FontIcon<?> icon) {
        toDefaultFile(file).setIcon(icon);
    }

    /**
     * Writes the style to the file. The default works on {@link DefaultStorageFile}; a subclass whose files are of
     * another class overrides it to write the style its own way.
     *
     * @param file the file to write to
     * @param style the style of the file
     * @throws IllegalArgumentException if the decorator can't write to the file
     */
    protected void setStyle(F file, IconTextStyle style) {
        toDefaultFile(file).setStyle(style);
    }

    private DefaultStorageFile toDefaultFile(F file) {
        if (!(file instanceof DefaultStorageFile defaultFile)) {
            throw new IllegalArgumentException("The decorator can only write to DefaultStorageFile: " + file);
        }
        return defaultFile;
    }

    private void writeResults() {
        scheduled.set(false);
        var result = results.poll();
        while (result != null) {
            setIcon(result.file(), result.icon());
            setStyle(result.file(), result.style());
            result = results.poll();
        }
    }
}
