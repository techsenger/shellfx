/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.core.area;

import com.techsenger.mvvm4fx.core.AbstractChildViewModel;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAreaViewModel extends AbstractChildViewModel implements AreaViewModel {

    private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();

    private final ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper();

    public AbstractAreaViewModel() {
        super();
    }

    @Override
    public ReadOnlyDoubleProperty widthProperty() {
        return this.width.getReadOnlyProperty();
    }

    @Override
    public double getWidth() {
        return this.width.get();
    }

    @Override
    public ReadOnlyDoubleProperty heightProperty() {
        return this.height.getReadOnlyProperty();
    }

    @Override
    public double getHeight() {
        return this.height.get();
    }

    @Override
    public AreaMediator getMediator() {
        return (AreaMediator) super.getMediator();
    }

    ReadOnlyDoubleWrapper getWidthWrapper() {
        return width;
    }

    ReadOnlyDoubleWrapper getHeightWrapper() {
        return height;
    }
}
