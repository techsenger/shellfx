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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.layout.LayoutComponents;
import com.techsenger.tabshell.layout.tabhost.TabHostComposer;
import com.techsenger.tabshell.layout.tabhost.TabHostPresenter;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockPresenter<V extends TabDockView, C extends TabHostComposer>
        extends TabHostPresenter<V, C> {

    protected class Port extends TabHostPresenter<V, C>.Port implements TabDockPort {

        private final TabDockPresenter<V, C> presenter = TabDockPresenter.this;

        @Override
        public ComponentPosition getMinimizedPosition() {
            return presenter.getMinimizedPosition();
        }

        @Override
        public void setMinimizedPosition(ComponentPosition position) {
            presenter.minimizedPosition = position;
        }

    }

    private ComponentPosition minimizedPosition;

    public TabDockPresenter(V view) {
        super(view);
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
    }

    protected ComponentPosition getMinimizedPosition() {
        return minimizedPosition;
    }

    @Override
    protected Port createPort() {
        return new TabDockPresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(LayoutComponents.TAB_DOCK);
    }
}
