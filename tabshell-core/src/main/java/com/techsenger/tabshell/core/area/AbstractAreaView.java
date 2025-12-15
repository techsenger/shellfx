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

import com.techsenger.patternfx.core.AbstractChildView;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAreaView<T extends AbstractAreaViewModel<?>, S extends AbstractAreaComponent<?>>
        extends AbstractChildView<T, S> implements AreaView<T, S> {

    private PulseListenerManager pulseListenerManager;

    public AbstractAreaView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected void initialize() {
        this.pulseListenerManager = new PulseListenerManager(getComponent().getFullName(),
                () -> getNode().sceneProperty());
        super.initialize();
    }

    @Override
    protected void bind() {
        super.bind();
        var pane = getNode();
        getViewModel().getWidthWrapper().bind(pane.widthProperty());
        getViewModel().getHeightWrapper().bind(pane.heightProperty());
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }
}
