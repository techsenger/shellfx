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

package com.techsenger.tabshell.core.pane;

import com.techsenger.mvvm4fx.core.AbstractChildView;
import com.techsenger.toolkit.core.ObjectUtils;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPaneView<T extends AbstractPaneViewModel> extends AbstractChildView<T>
        implements PaneView<T> {

    private final PulseListenerManager pulseListenerManager;

    public AbstractPaneView(T viewModel) {
        super(viewModel);
        this.pulseListenerManager = new PulseListenerManager(ObjectUtils.getIdentity(this),
                () -> getNode().sceneProperty());
    }

    @Override
    protected void postInitialize(T viewModel) {
        super.postInitialize(viewModel);
        var pane = getNode();
        viewModel.getWidthWrapper().bind(pane.widthProperty());
        viewModel.getHeightWrapper().bind(pane.heightProperty());
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }
}
