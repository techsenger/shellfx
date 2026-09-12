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

import com.techsenger.patternfx.core.ComponentState;
import com.techsenger.patternfx.mvvm.AbstractChildView;
import com.techsenger.patternfx.mvvm.ViewUtils;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAreaView<VM extends AbstractAreaViewModel<?>>
        extends AbstractChildView<VM> implements AreaView<VM> {

    public class Composer extends AbstractChildView<VM>.Composer implements AreaView.Composer {

    }

    private PulseListenerManager pulseListenerManager;

    public AbstractAreaView(VM viewModel) {
        super(viewModel);
        this.pulseListenerManager = new PulseListenerManager(getDescriptor().getFullName(),
                () -> getNode().sceneProperty());
        viewModel.getDescriptor().stateProperty().addListener((ov, oldV, newV) -> {
            if (newV == ComponentState.INITIALIZED) {
                ViewUtils.setView(getNode(), this);
            }
        });
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected void bind() {
        super.bind();
        getViewModel().widthWrapper().bind(getNode().widthProperty());
        getViewModel().heightWrapper().bind(getNode().heightProperty());
    }

    @Override
    protected Composer createComposer() {
        return new AbstractAreaView<VM>.Composer();
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }
}
