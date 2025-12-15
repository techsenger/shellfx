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

package com.techsenger.tabshell.jfx;

import com.techsenger.tabshell.layout.dock.TabDockViewModel;
import devtoolsfx.event.NodeSelectedEvent;

/**
 *
 * @author Pavel Castornii
 */
public class JfxTabDockViewModel<T extends JfxTabDockMediator> extends TabDockViewModel<T> {

    public JfxTabDockViewModel() {

    }

    @Override
    protected void initialize() {
        super.initialize();
        getMediator().getConnector().getEventBus().subscribe(NodeSelectedEvent.class, (e) -> {
            getMediator().getConnector().getOptions().setInspectMode(false);
        });
    }

    void updateInspectMode() {
        var isInspectMode = getMediator().getConnector().getOptions().isInspectMode();
        getMediator().getConnector().getOptions().setInspectMode(!isInspectMode);
    }
}
