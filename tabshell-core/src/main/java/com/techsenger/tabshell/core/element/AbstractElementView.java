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

package com.techsenger.tabshell.core.element;

import com.techsenger.mvvm4fx.core.AbstractChildView;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractElementView<T extends AbstractElementViewModel> extends AbstractChildView<T>
        implements ElementView<T> {

    private final PulseListenerManager pulseListenerManager;

    public AbstractElementView(T viewModel) {
        super(viewModel);
        this.pulseListenerManager = new PulseListenerManager(getDescriptor().getFullName(),
                () -> getNode().sceneProperty());
    }

    @Override
    public ElementComposer<?> getComposer() {
        return (ElementComposer<?>) super.getComposer();
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }

    @Override
    protected ElementComposer<?> createComposer() {
        return (ElementComposer<?>) super.createComposer();
    }
}
