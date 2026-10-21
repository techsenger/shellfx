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

package com.techsenger.shellfx.storage.view;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.style.FileDecorator;
import java.util.Arrays;
import java.util.Objects;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.Labeled;

/**
 * Keeps the look of a cell that shows a file: decorates the file, sets the icon and the styles of the cell whenever
 * the decoration of the file changes, and tells the cell when the other values it displays change, which the second
 * loading phase does after the file is shown. A cell owns one updater and moves it to the new file whenever the cell
 * gets another item.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public final class FileCellUpdater<F extends StorageFile> {

    private final Labeled cell;

    private final @Nullable FontIconView iconView;

    private final FileDecorator<F> decorator;

    /**
     * Asks the cell to show the file again when a value it displays, such as a size or a date, changes. The cell
     * knows how to turn the value into text, the updater does not.
     */
    private final ChangeListener<Object> valuesListener;

    private final WeakChangeListener<Object> weakValuesListener;

    /**
     * Sets the icon and the styles of the cell when the decoration of the file changes, which the updater does by
     * itself without asking the cell to show the file again.
     */
    private final ChangeListener<Object> decorationListener;

    private final WeakChangeListener<Object> weakDecorationListener;

    private @Nullable F file;

    private ObservableValue<?>[] observed = new ObservableValue<?>[0];

    /**
     * Creates an updater for a cell that has no icon. It is not showing any file yet.
     *
     * @param cell the cell whose texts to style
     * @param decorator gives the shown files their icon and style
     * @param onChanged called on the UI thread when a value passed to {@code update} changes, so the cell shows the
     *     file again
     */
    public FileCellUpdater(Labeled cell, FileDecorator<F> decorator, Runnable onChanged) {
        this(cell, null, decorator, onChanged);
    }

    /**
     * Creates an updater for a cell. It is not showing any file yet.
     *
     * @param cell the cell whose texts to style
     * @param iconView the icon of the cell, which gets the icon and the style of the file, or {@code null} if the
     *     cell has no icon
     * @param decorator gives the shown files their icon and style
     * @param onChanged called on the UI thread when a value passed to {@code update} changes, so the cell shows the
     *     file again
     */
    public FileCellUpdater(Labeled cell, @Nullable FontIconView iconView, FileDecorator<F> decorator,
            Runnable onChanged) {
        this.cell = cell;
        this.iconView = iconView;
        this.decorator = decorator;
        this.valuesListener = (obs, oldValue, newValue) -> onChanged.run();
        this.weakValuesListener = new WeakChangeListener<>(this.valuesListener);
        this.decorationListener = (obs, oldValue, newValue) -> {
            if (this.file != null) {
                refresh(this.file);
            }
        };
        this.weakDecorationListener = new WeakChangeListener<>(this.decorationListener);
    }

    /**
     * Decorates the file, sets the icon of the cell and applies the style to the cell and its icon, and keeps doing
     * so when the decoration of the file changes. The cell sets its text itself and is asked to show the file again
     * when one of the values changes.
     *
     * @param file the file shown in the cell
     * @param values the other values the cell displays of the file
     */
    public void update(F file, ObservableValue<?>... values) {
        if (this.file != file || !Arrays.equals(this.observed, values)) {
            unobserve();
            this.file = file;
            this.observed = values;
            for (var value : values) {
                value.addListener(weakValuesListener);
            }
            file.decorationProperty().addListener(weakDecorationListener);
        }
        refresh(file);
    }

    /**
     * Stops observing and clears the styles of the cell, which a cell does when it becomes empty.
     */
    public void clear() {
        unobserve();
        FileCellUtils.updateStyles(cell, iconView, null);
    }

    private void refresh(F file) {
        decorator.decorate(file);
        if (iconView != null) {
            iconView.setIcon(Objects.requireNonNull(file.getDecoration()).icon());
        }
        FileCellUtils.updateStyles(cell, iconView, file);
    }

    private void unobserve() {
        for (var value : this.observed) {
            value.removeListener(weakValuesListener);
        }
        this.observed = new ObservableValue<?>[0];
        if (this.file != null) {
            this.file.decorationProperty().removeListener(weakDecorationListener);
            this.file = null;
        }
    }
}
