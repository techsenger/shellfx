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

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

/**
 * Tells a cell when the values it displays of its file change, which the second loading phase does after the file is
 * shown. A cell owns one observer and moves it to the values of the new file whenever the cell gets another item.
 *
 * @author Pavel Castornii
 */
public final class FileCellObserver {

    private final ChangeListener<Object> listener;

    private final WeakChangeListener<Object> weakListener;

    private ObservableValue<?>[] values = new ObservableValue<?>[0];

    /**
     * Creates an observer that is not observing any value yet.
     *
     * @param onChanged called on the UI thread when an observed value changes
     */
    public FileCellObserver(Runnable onChanged) {
        this.listener = (obs, oldValue, newValue) -> onChanged.run();
        this.weakListener = new WeakChangeListener<>(this.listener);
    }

    /**
     * Observes the given values instead of the previous ones. It does nothing if they are already observed.
     *
     * @param first the first value the cell displays of its file
     * @param others the other values the cell displays of its file
     */
    public void observe(ObservableValue<?> first, ObservableValue<?>... others) {
        var newValues = new ObservableValue<?>[others.length + 1];
        newValues[0] = first;
        System.arraycopy(others, 0, newValues, 1, others.length);
        if (Arrays.equals(this.values, newValues)) {
            return;
        }
        unobserve();
        this.values = newValues;
        for (var value : newValues) {
            value.addListener(weakListener);
        }
    }

    /**
     * Stops observing the values observed so far, which a cell does when it becomes empty.
     */
    public void unobserve() {
        for (var value : this.values) {
            value.removeListener(weakListener);
        }
        this.values = new ObservableValue<?>[0];
    }
}
