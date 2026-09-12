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

package com.techsenger.shellfx.core.area;

import com.techsenger.patternfx.mvvm.AbstractChildViewModel;
import com.techsenger.patternfx.mvvm.ChildComposer;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAreaViewModel<C extends ChildComposer> extends AbstractChildViewModel<C>
        implements AreaViewModel<C> {

    private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper();

    public AbstractAreaViewModel(AreaParams params) {
        super(params);
    }

    @Override
    public double getWidth() {
        return width.get();
    }

    @Override
    public ReadOnlyDoubleProperty widthProperty() {
        return width.getReadOnlyProperty();
    }

    @Override
    public double getHeight() {
        return height.get();
    }

    @Override
    public ReadOnlyDoubleProperty heightProperty() {
        return height.getReadOnlyProperty();
    }

    @Override
    protected AreaHistory getHistory() {
        return (AreaHistory) super.getHistory();
    }

    /**
     * Returns the writable wrapper backing {@link #widthProperty()}.
     *
     * <p>Framework contract: intended to be bound from the View's node width, exclusively by
     * {@link AbstractAreaView#bind()}. Direct invocation by user code results in undefined behavior.
     */
    ReadOnlyDoubleWrapper widthWrapper() {
        return width;
    }

    /**
     * Returns the writable wrapper backing {@link #heightProperty()}.
     *
     * <p>Framework contract: intended to be bound from the View's node height, exclusively by
     * {@link AbstractAreaView#bind()}. Direct invocation by user code results in undefined behavior.
     */
    ReadOnlyDoubleWrapper heightWrapper() {
        return height;
    }
}
