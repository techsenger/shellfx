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

package com.techsenger.shellfx.devtools.event;

import com.techsenger.connectorfx.event.AttributeListEvent;
import com.techsenger.connectorfx.event.AttributeUpdatedEvent;
import com.techsenger.connectorfx.event.NodeAddedEvent;
import com.techsenger.connectorfx.event.NodeRemovedEvent;
import com.techsenger.connectorfx.event.NodeSelectedEvent;
import com.techsenger.connectorfx.event.NodeStyleClassEvent;
import com.techsenger.connectorfx.event.NodeVisibilityEvent;
import com.techsenger.connectorfx.event.RootChangedEvent;
import com.techsenger.connectorfx.event.WindowClosedEvent;
import com.techsenger.connectorfx.event.WindowPropertiesEvent;
import com.techsenger.shellfx.devtools.shared.ToolBarConfig;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Pavel Castornii
 */
public class EventToolBarConfig extends ToolBarConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean filterSelected;

    private boolean selectedNodeOnly;

    private Set<String> selectedEventTypes = new HashSet<>();

    public EventToolBarConfig() {
        selectedEventTypes.add(AttributeListEvent.class.getName());
        selectedEventTypes.add(AttributeUpdatedEvent.class.getName());
        selectedEventTypes.add(NodeAddedEvent.class.getName());
        selectedEventTypes.add(NodeRemovedEvent.class.getName());
        selectedEventTypes.add(NodeSelectedEvent.class.getName());
        selectedEventTypes.add(NodeStyleClassEvent.class.getName());
        selectedEventTypes.add(NodeVisibilityEvent.class.getName());
        selectedEventTypes.add(RootChangedEvent.class.getName());
        selectedEventTypes.add(WindowClosedEvent.class.getName());
        selectedEventTypes.add(WindowPropertiesEvent.class.getName());
    }

    public boolean isFilterSelected() {
        return filterSelected;
    }

    public void setFilterSelected(boolean filterSelected) {
        this.filterSelected = filterSelected;
    }

    public boolean isSelectedNodeOnly() {
        return selectedNodeOnly;
    }

    public void setSelectedNodeOnly(boolean selectedNodeOnly) {
        this.selectedNodeOnly = selectedNodeOnly;
    }

    public Set<String> getSelectedEventTypes() {
        return selectedEventTypes;
    }

    public void setSelectedEventTypes(Set<String> selectedEventTypes) {
        this.selectedEventTypes = selectedEventTypes;
    }
}
