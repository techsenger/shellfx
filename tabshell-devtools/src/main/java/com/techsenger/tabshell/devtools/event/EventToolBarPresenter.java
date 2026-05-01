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

package com.techsenger.tabshell.devtools.event;

import com.techsenger.connectorfx.event.AttributeListEvent;
import com.techsenger.connectorfx.event.AttributeUpdatedEvent;
import com.techsenger.connectorfx.event.ConnectorEvent;
import com.techsenger.connectorfx.event.ExceptionEvent;
import com.techsenger.connectorfx.event.JavaFXEvent;
import com.techsenger.connectorfx.event.MousePosEvent;
import com.techsenger.connectorfx.event.NodeAddedEvent;
import com.techsenger.connectorfx.event.NodeRemovedEvent;
import com.techsenger.connectorfx.event.NodeSelectedEvent;
import com.techsenger.connectorfx.event.NodeStyleClassEvent;
import com.techsenger.connectorfx.event.NodeVisibilityEvent;
import com.techsenger.connectorfx.event.RootChangedEvent;
import com.techsenger.connectorfx.event.WindowClosedEvent;
import com.techsenger.connectorfx.event.WindowPropertiesEvent;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.ToolBarPresenter;
import com.techsenger.tabshell.shared.find.FindFeature;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public class EventToolBarPresenter<V extends EventToolBarView> extends ToolBarPresenter<V> implements EventToolBarPort {

    private final Map<Class<? extends ConnectorEvent>, AtomicBoolean> eventTypesByClass = Map.ofEntries(
            Map.entry(AttributeListEvent.class, new AtomicBoolean(true)),
            Map.entry(AttributeUpdatedEvent.class, new AtomicBoolean(true)),
            Map.entry(ExceptionEvent.class, new AtomicBoolean(false)),
            Map.entry(JavaFXEvent.class, new AtomicBoolean(false)),
            Map.entry(MousePosEvent.class, new AtomicBoolean(false)),
            Map.entry(NodeAddedEvent.class, new AtomicBoolean(true)),
            Map.entry(NodeRemovedEvent.class, new AtomicBoolean(true)),
            Map.entry(NodeSelectedEvent.class, new AtomicBoolean(true)),
            Map.entry(NodeStyleClassEvent.class, new AtomicBoolean(true)),
            Map.entry(NodeVisibilityEvent.class, new AtomicBoolean(true)),
            Map.entry(RootChangedEvent.class, new AtomicBoolean(true)),
            Map.entry(WindowClosedEvent.class, new AtomicBoolean(true)),
            Map.entry(WindowPropertiesEvent.class, new AtomicBoolean(true)));

    private boolean filterSelected;

    private boolean selectedNodeOnly;

    private String statistics;

    private boolean recordSelected;

    public EventToolBarPresenter(V view, EventToolBarAwarePort toolBarAware) {
        super(view, toolBarAware, FindFeature.MATCH_CASE);
    }

    @Override
    public Set<Class<? extends ConnectorEvent>> getSelectedEventTypes() {
        return eventTypesByClass.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isFilterSelected() {
        return filterSelected;
    }

    public void setFilterSelected(boolean filterSelected) {
        this.filterSelected = filterSelected;
        getView().setFilterSelected(filterSelected);
    }

    public void setSelectedNodeOnly(boolean selectedNodeOnly) {
        this.selectedNodeOnly = selectedNodeOnly;
        getView().setSelectedNodeOnly(selectedNodeOnly);
    }

    @Override
    public boolean isSelectedNodeOnly() {
        return this.selectedNodeOnly;
    }

    @Override
    public void setStatistics(String text) {
        this.statistics = text;
        getView().setStatistics(text);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.EVENT_TOOL_BAR);
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        getView().setEventTypes(eventTypesByClass);
    }

    @Override
    protected EventToolBarAwarePort getToolBarAware() {
        return (EventToolBarAwarePort) super.getToolBarAware();
    }

    protected void onRecord(boolean selected) {
        this.recordSelected = selected;
        getToolBarAware().onRecord(selected);
    }

    protected void onClear() {
        getToolBarAware().onClear();
    }

    protected void onFilter(boolean selected) {
        this.filterSelected = selected;
        getToolBarAware().onFilterSelected(selected);
    }

    protected void onSelectedNodeOnly(boolean selected) {
        this.selectedNodeOnly = selected;
        getToolBarAware().onSelectedNodeOnly(selected);
    }

    protected void onSelectAllEvents() {
        this.eventTypesByClass.values().forEach(e -> e.set(true));
        getToolBarAware().onEventTypesChanged();
    }

    protected void onDeselectAllEvents() {
        this.eventTypesByClass.values().forEach(e -> e.set(false));
        getToolBarAware().onEventTypesChanged();
    }

    protected void onEventSelected(Class<? extends ConnectorEvent> clazz, boolean selected) {
        this.eventTypesByClass.get(clazz).set(selected);
        getToolBarAware().onEventTypesChanged();
    }
}
