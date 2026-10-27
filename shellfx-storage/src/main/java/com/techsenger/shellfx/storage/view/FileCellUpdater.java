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
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

/**
 * Keeps a cell that shows a file up to date: runs the update of the cell for the file now, and again whenever one of
 * the properties the cell is bound to changes, such as the decoration of the file or a size. The updater knows nothing
 * about what the update does and never calls the cell otherwise. A cell owns one updater and gives it the file and
 * the properties it is bound to whenever it gets another item.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public final class FileCellUpdater<F> {

    private final Consumer<? super F> update;

    private final ChangeListener<Object> listener;

    private final WeakChangeListener<Object> weakListener;

    private @Nullable F file;

    private ObservableValue<?>[] observed = new ObservableValue<?>[0];

    /**
     * Creates an updater. It is not observing anything yet.
     *
     * @param update updates the cell for the file, called on the UI thread
     */
    public FileCellUpdater(Consumer<? super F> update) {
        this.update = update;
        this.listener = (obs, oldValue, newValue) -> {
            if (this.file != null) {
                update.accept(this.file);
            }
        };
        this.weakListener = new WeakChangeListener<>(this.listener);
    }

    /**
     * Observes the properties of the file and updates the cell for it now, and again whenever one of the properties
     * changes. It stops observing the file shown before; nothing is subscribed again for the same file.
     *
     * @param file the file shown in the cell
     * @param properties give the properties the cell is bound to out of the file, such as its decoration or size
     */
    @SafeVarargs
    public final void observe(F file, Function<? super F, ? extends ObservableValue<?>>... properties) {
        if (this.file != file) {
            unobserve();
            this.observed = new ObservableValue<?>[properties.length];
            for (var i = 0; i < properties.length; i++) {
                this.observed[i] = properties[i].apply(file);
                this.observed[i].addListener(weakListener);
            }
            this.file = file;
        }
        update.accept(file);
    }

    /**
     * Stops observing the properties, which a cell does when it becomes empty.
     */
    public void unobserve() {
        for (var property : this.observed) {
            property.removeListener(weakListener);
        }
        this.observed = new ObservableValue<?>[0];
        this.file = null;
    }
}
