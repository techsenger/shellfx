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

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvvm.AbstractChildViewModel;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.config.ConfigUtils;
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

    private final @Nullable AreaConfig config;

    public AbstractAreaViewModel(AreaParams params) {
        super(params);
        this.config = params.getConfig();
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
    protected void postInitialize() {
        super.postInitialize();
        if (config != null) {
            loadConfigToState();
            observeStateForConfig();
        }
    }

    /**
     * Applies the values stored in the config to the state of this component. Called once from
     * {@code postInitialize()} before {@link #observeStateForConfig()}; overriding methods must call {@code super}.
     */
    protected void loadConfigToState() { }

    /**
     * Registers listeners that write changes of the state of this component into the config and notify the config
     * listeners. Called once after {@link #loadConfigToState()}; overriding methods must call {@code super}.
     */
    protected void observeStateForConfig() {
        ConfigUtils.observe(this.width, config, AreaConfig::setWidth);
        ConfigUtils.observe(this.height, config, AreaConfig::setHeight);
    }

    protected @Nullable AreaConfig getConfig() {
        return config;
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
