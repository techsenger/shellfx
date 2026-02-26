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

package com.techsenger.tabshell.core.area;

import com.techsenger.patternfx.mvp.AbstractChildFxView;
import com.techsenger.tabshell.core.FxViewUtils;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractAreaFxView<P extends AreaPresenter<?, ?>>
        extends AbstractChildFxView<P> implements AreaFxView<P> {

    public class Composer extends AbstractChildFxView<P>.Composer implements AreaFxView.Composer {

    }

    private PulseListenerManager pulseListenerManager;

    public AbstractAreaFxView() {
        super();
    }

    @Override
    public double getWidth() {
        return getNode().getWidth();
    }

    @Override
    public double getHeight() {
        return getNode().getHeight();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected void initialize() {
        this.pulseListenerManager = new PulseListenerManager(getDescriptor().getFullName(),
                () -> getNode().sceneProperty());
        FxViewUtils.setComponent(getNode(), this);
        super.initialize();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractAreaFxView.Composer();
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }
}
