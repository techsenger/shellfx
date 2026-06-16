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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.area.AbstractAreaPresenter;
import com.techsenger.shellfx.layout.LayoutComponents;

/**
 *
 * @author Pavel Castornii
 */
public class DockHostPresenter<V extends DockHostView> extends AbstractAreaPresenter<V> implements DockHostPort {

    public DockHostPresenter(V view, DockHostParams params) {
        super(view, params);
    }

    @Override
    protected DockHostHistory getHistory() {
        return (DockHostHistory) super.getHistory();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponents.DOCK_HOST);
    }
}
