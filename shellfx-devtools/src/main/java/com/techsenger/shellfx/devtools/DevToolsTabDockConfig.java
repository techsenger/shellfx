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

package com.techsenger.shellfx.devtools;

import com.techsenger.shellfx.core.area.AreaConfig;
import com.techsenger.shellfx.devtools.component.ComponentTabConfig;
import com.techsenger.shellfx.devtools.environment.EnvironmentTabConfig;
import com.techsenger.shellfx.devtools.event.EventTabConfig;
import com.techsenger.shellfx.devtools.node.NodeTabConfig;
import com.techsenger.shellfx.devtools.stylesheet.StylesheetTabConfig;
import java.io.Serial;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsTabDockConfig extends AreaConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean selectionSelected;

    private ComponentTabConfig componentTab = new ComponentTabConfig();

    private NodeTabConfig nodeTab = new NodeTabConfig();

    private EventTabConfig eventTab = new EventTabConfig();

    private StylesheetTabConfig stylesheetTab = new StylesheetTabConfig();

    private EnvironmentTabConfig environmentTab = new EnvironmentTabConfig();

    public boolean isSelectionSelected() {
        return selectionSelected;
    }

    public void setSelectionSelected(boolean selectionSelected) {
        this.selectionSelected = selectionSelected;
    }

    public ComponentTabConfig getComponentTab() {
        return componentTab;
    }

    public void setComponentTab(ComponentTabConfig componentTab) {
        this.componentTab = componentTab;
    }

    public NodeTabConfig getNodeTab() {
        return nodeTab;
    }

    public void setNodeTab(NodeTabConfig nodeTab) {
        this.nodeTab = nodeTab;
    }

    public EventTabConfig getEventTab() {
        return eventTab;
    }

    public void setEventTab(EventTabConfig eventTab) {
        this.eventTab = eventTab;
    }

    public StylesheetTabConfig getStylesheetTab() {
        return stylesheetTab;
    }

    public void setStylesheetTab(StylesheetTabConfig stylesheetTab) {
        this.stylesheetTab = stylesheetTab;
    }

    public EnvironmentTabConfig getEnvironmentTab() {
        return environmentTab;
    }

    public void setEnvironmentTab(EnvironmentTabConfig environmentTab) {
        this.environmentTab = environmentTab;
    }
}
