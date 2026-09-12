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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

/**
 *
 * @author Pavel Castornii
 */
public class DockHostViewModel<C extends DockHostComposer> extends AbstractAreaViewModel<C>
        implements FullDockHostPort {

    private final ReadOnlyDoubleWrapper centerWidth = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper centerHeight = new ReadOnlyDoubleWrapper();

    public DockHostViewModel(DockHostParams params) {
        super(params);
    }

    @Override
    public FullDockHostPort.ComposerAccess getComposerAccess() {
        return (FullDockHostPort.ComposerAccess) super.getComposerAccess();
    }

    @Override
    public double getCenterWidth() {
        return centerWidth.get();
    }

    @Override
    public ReadOnlyDoubleProperty centerWidthProperty() {
        return centerWidth.getReadOnlyProperty();
    }

    @Override
    public double getCenterHeight() {
        return centerHeight.get();
    }

    @Override
    public ReadOnlyDoubleProperty centerHeightProperty() {
        return centerHeight.getReadOnlyProperty();
    }

    @Override
    protected DockHostHistory getHistory() {
        return (DockHostHistory) super.getHistory();
    }

    /**
     * Returns the writable wrapper backing the center area's actual width.
     *
     * <p>Framework contract: intended to be bound from the View's center node width, exclusively by
     * {@code DockHostView}. Direct invocation by user code results in undefined behavior.
     */
    ReadOnlyDoubleWrapper centerWidthWrapper() {
        return centerWidth;
    }

    /**
     * Returns the writable wrapper backing the center area's actual height.
     *
     * <p>Framework contract: intended to be bound from the View's center node height, exclusively by
     * {@code DockHostView}. Direct invocation by user code results in undefined behavior.
     */
    ReadOnlyDoubleWrapper centerHeightWrapper() {
        return centerHeight;
    }
}
