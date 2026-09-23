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

import com.techsenger.annotations.Unmodifiable;
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
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.devtools.shared.ToolBarViewModel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class EventToolBarViewModel<C extends ChildComposer> extends ToolBarViewModel<C>
        implements FullEventToolBarPort {

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

    private final ReadOnlyBooleanWrapper filterSelected = new ReadOnlyBooleanWrapper();

    private final ReadOnlyBooleanWrapper selectedNodeOnly = new ReadOnlyBooleanWrapper();

    private final ReadOnlyBooleanWrapper recordSelected = new ReadOnlyBooleanWrapper();

    private final StringProperty statistics = new SimpleStringProperty();

    public EventToolBarViewModel(EventToolBarParams params) {
        super(params);
        this.filterSelected.addListener((ov, oldV, newV) -> getToolBarAware().onFilterSelected(newV));
        this.selectedNodeOnly.addListener((ov, oldV, newV) -> getToolBarAware().onSelectedNodeOnly(newV));
        this.recordSelected.addListener((ov, oldV, newV) -> getToolBarAware().onRecord(newV));
    }

    @Override
    public @Unmodifiable Set<Class<? extends ConnectorEvent>> getSelectedEventTypes() {
        return eventTypesByClass.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isFilterSelected() {
        return filterSelected.get();
    }

    @Override
    public ReadOnlyBooleanProperty filterSelectedProperty() {
        return filterSelected.getReadOnlyProperty();
    }

    @Override
    public boolean isSelectedNodeOnly() {
        return this.selectedNodeOnly.get();
    }

    @Override
    public ReadOnlyBooleanProperty selectedNodeOnlyProperty() {
        return selectedNodeOnly.getReadOnlyProperty();
    }

    @Override
    public boolean isRecordSelected() {
        return recordSelected.get();
    }

    @Override
    public ReadOnlyBooleanProperty recordSelectedProperty() {
        return recordSelected.getReadOnlyProperty();
    }

    @Override
    public void setStatistics(String text) {
        this.statistics.set(text);
    }

    @Override
    public StringProperty statisticsProperty() {
        return statistics;
    }

    @Override
    public String getStatistics() {
        return this.statistics.get();
    }

    @Override
    protected EventToolBarAwarePort getToolBarAware() {
        return (EventToolBarAwarePort) super.getToolBarAware();
    }

    protected void onClear() {
        getToolBarAware().onClear();
    }

    protected void selectAllEventTypes() {
        this.eventTypesByClass.values().forEach(e -> e.set(true));
        getToolBarAware().onEventTypesChanged();
    }

    protected void deselectAllEventTypes() {
        this.eventTypesByClass.values().forEach(e -> e.set(false));
        getToolBarAware().onEventTypesChanged();
    }

    protected void setEventTypeSelected(Class<? extends ConnectorEvent> clazz, boolean selected) {
        this.eventTypesByClass.get(clazz).set(selected);
        getToolBarAware().onEventTypesChanged();
    }

    Map<Class<? extends ConnectorEvent>, AtomicBoolean> getEventTypesByClass() {
        return eventTypesByClass;
    }

    ReadOnlyBooleanWrapper recordSelectedWrapper() {
        return recordSelected;
    }

    ReadOnlyBooleanWrapper filterSelectedWrapper() {
        return filterSelected;
    }

    ReadOnlyBooleanWrapper selectedNodeOnlyWrapper() {
        return selectedNodeOnly;
    }
}
