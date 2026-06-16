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

import com.techsenger.connectorfx.event.ConnectorEvent;
import java.util.Set;
import java.util.regex.Matcher;

/**
 *
 * @author Pavel Castornii
 */
public class Filter {

    private volatile boolean selected;

    private volatile boolean selectedNodeOnly;

    private volatile Matcher matcher;

    private volatile  Set<Class<? extends ConnectorEvent>> selectedEventTypes;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelectedNodeOnly() {
        return selectedNodeOnly;
    }

    public void setSelectedNodeOnly(boolean selectedNodeOnly) {
        this.selectedNodeOnly = selectedNodeOnly;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public Set<Class<? extends ConnectorEvent>> getSelectedEventTypes() {
        return selectedEventTypes;
    }

    public void setSelectedEventTypes(Set<Class<? extends ConnectorEvent>> selectedEventTypes) {
        this.selectedEventTypes = selectedEventTypes;
    }
}
