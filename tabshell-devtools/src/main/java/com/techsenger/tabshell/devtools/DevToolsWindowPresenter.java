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

package com.techsenger.tabshell.devtools;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.window.AbstractWindowPresenter;
import com.techsenger.tabshell.devtools.style.DevToolsIcons;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsWindowPresenter<V extends DevToolsWindowView> extends AbstractWindowPresenter<V> {

    public DevToolsWindowPresenter(V view, DevToolsWindowParams params) {
        super(view, params);
        getView().getComposer().setHistoryManager(params.getHistoryManager());
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DevToolsComponents.WINDOW);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("DevTools");
        setIcon(DevToolsIcons.TOOLS);
    }

    @Override
    protected void applyAppearance() {
        super.applyAppearance();
        setWidth(1000);
        setHeight(400);
    }
}
