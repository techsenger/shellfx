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

package com.techsenger.tabshell.material.table;

import com.techsenger.patternfx.core.Name;
import com.techsenger.toolkit.core.function.Factory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.scene.control.TableColumnBase;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTableColumnManager<N extends Name, T extends TableColumnBase<?, ?>> {

    private final Map<N, Factory<T>> columnFactoriesByName = new HashMap<>();

    private BiConsumer<N, Double> widthListener;

    private BiConsumer<N, Integer> indexListener;

    private boolean indexListenerDisabled;

    private BiConsumer<N, Integer> sortIndexListener;

    private boolean sortIndexListenerDisabled;

    public void registerColumnFactory(N name, Factory<T> factory) {
        this.columnFactoriesByName.put(name, factory);
    }

    public void unregisterColumnFactory(N name) {
        this.columnFactoriesByName.remove(name);
    }

    public BiConsumer<N, Double> getWidthListener() {
        return widthListener;
    }

    public void setWidthListener(BiConsumer<N, Double> widthListener) {
        this.widthListener = widthListener;
    }

    public BiConsumer<N, Integer> getIndexListener() {
        return indexListener;
    }

    public void setIndexListener(BiConsumer<N, Integer> indexListener) {
        this.indexListener = indexListener;
    }

    public BiConsumer<N, Integer> getSortIndexListener() {
        return sortIndexListener;
    }

    public void setSortIndexListener(BiConsumer<N, Integer> sortIndexListener) {
        this.sortIndexListener = sortIndexListener;
    }

    protected Map<N, Factory<T>> getColumnFactoriesByName() {
        return columnFactoriesByName;
    }

    boolean isIndexListenerDisabled() {
        return indexListenerDisabled;
    }

    void setIndexListenerDisabled(boolean indexListenerDisabled) {
        this.indexListenerDisabled = indexListenerDisabled;
    }

    boolean isSortIndexListenerDisabled() {
        return sortIndexListenerDisabled;
    }

    void setSortIndexListenerDisabled(boolean sortIndexListenerDisabled) {
        this.sortIndexListenerDisabled = sortIndexListenerDisabled;
    }
}
