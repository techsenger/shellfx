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

package com.techsenger.tabshell.core.element;

import com.techsenger.patternfx.mvvmx.AbstractChildView;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractElementView<T extends AbstractElementViewModel<?>, S extends AbstractElementComponent<?>>
        extends AbstractChildView<T, S> implements ElementView<T, S> {

    private PulseListenerManager pulseListenerManager;

    public AbstractElementView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected void initialize() {
        this.pulseListenerManager = new PulseListenerManager(getComponent().getFullName(),
                () -> getNode().sceneProperty());
        super.initialize();
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }
}
