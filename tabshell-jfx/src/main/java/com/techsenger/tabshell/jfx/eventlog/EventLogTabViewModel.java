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

package com.techsenger.tabshell.jfx.eventlog;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
import com.techsenger.tabshell.core.tab.ShellTabViewModel;
import com.techsenger.tabshell.jfx.JfxComponentNames;
import com.techsenger.tabshell.jfx.style.JfxIcons;
import com.techsenger.tabshell.material.icon.GenericFontIcon;
import devtoolsfx.connector.Connector;
import devtoolsfx.event.AttributeListEvent;
import devtoolsfx.event.AttributeUpdatedEvent;
import devtoolsfx.event.ConnectorEvent;
import devtoolsfx.event.ElementEvent;
import devtoolsfx.event.ExceptionEvent;
import devtoolsfx.event.JavaFXEvent;
import devtoolsfx.event.MousePosEvent;
import devtoolsfx.event.NodeAddedEvent;
import devtoolsfx.event.NodeRemovedEvent;
import devtoolsfx.event.NodeSelectedEvent;
import devtoolsfx.event.NodeStyleClassEvent;
import devtoolsfx.event.NodeVisibilityEvent;
import devtoolsfx.event.RootChangedEvent;
import devtoolsfx.event.WindowClosedEvent;
import devtoolsfx.event.WindowPropertiesEvent;
import devtoolsfx.scenegraph.Element;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class EventLogTabViewModel extends AbstractTabViewModel {

    private static final long ZONE_OFFSET_MILLIS = ZoneId.systemDefault().getRules().getOffset(Instant.now())
            .getTotalSeconds() * 1000L;

    private static final char[] TIME_ARRAY = new char[12]; // HH:mm:ss.SSS

    private static String getTime() {
        long millis = System.currentTimeMillis() + ZONE_OFFSET_MILLIS;
        long seconds = millis / 1000;
        long ms = millis % 1000;

        int totalSeconds = (int) (seconds % 86400);
        int hour = totalSeconds / 3600;
        int minute = (totalSeconds % 3600) / 60;
        int second = totalSeconds % 60;

        TIME_ARRAY[0] = (char) ('0' + hour / 10);
        TIME_ARRAY[1] = (char) ('0' + hour % 10);
        TIME_ARRAY[2] = ':';
        TIME_ARRAY[3] = (char) ('0' + minute / 10);
        TIME_ARRAY[4] = (char) ('0' + minute % 10);
        TIME_ARRAY[5] = ':';
        TIME_ARRAY[6] = (char) ('0' + second / 10);
        TIME_ARRAY[7] = (char) ('0' + second % 10);
        TIME_ARRAY[8] = '.';
        TIME_ARRAY[9] = (char) ('0' + ms / 100);
        TIME_ARRAY[10] = (char) ('0' + (ms / 10) % 10);
        TIME_ARRAY[11] = (char) ('0' + ms % 10);

        return new String(TIME_ARRAY);
    }

    private final ShellTabViewModel shellTab;

    private final Connector connector;

    private final List<LogEntry> allEntries = new ArrayList<>();

    private final BooleanProperty filterEnabled = new SimpleBooleanProperty(true);

    private final ObjectProperty<GenericFontIcon<?>> recordIcon = new SimpleObjectProperty<>(JfxIcons.RECORD_START);

    private final BooleanProperty selectedOnly = new SimpleBooleanProperty(true);

    private final ObservableList<LogEntry> filteredEntries = FXCollections.observableArrayList();

    private final Consumer<ConnectorEvent> eventListener = this::handleEvent;

    private final StringBuilder messageBuilder = new StringBuilder();

    private final StringProperty searchText = new SimpleStringProperty();

    private final ReadOnlyObjectWrapper<Element> selectedElement = new ReadOnlyObjectWrapper<>();

    private final Map<Class<? extends ConnectorEvent>, EventType> eventTypesByClass =
            Collections.unmodifiableMap(
                Stream.of(
                    createEventType(AttributeListEvent.class, true),
                    createEventType(AttributeUpdatedEvent.class, true),
                    createEventType(ExceptionEvent.class, false),
                    createEventType(JavaFXEvent.class, false),
                    createEventType(MousePosEvent.class, false),
                    createEventType(NodeAddedEvent.class, false),
                    createEventType(NodeRemovedEvent.class, false),
                    createEventType(NodeSelectedEvent.class, false),
                    createEventType(NodeStyleClassEvent.class, false),
                    createEventType(NodeVisibilityEvent.class, false),
                    createEventType(RootChangedEvent.class, false),
                    createEventType(WindowClosedEvent.class, false),
                    createEventType(WindowPropertiesEvent.class, false)
                )
                .collect(Collectors.toMap(
                    EventType::getType,
                    eventType -> eventType,
                    (v1, v2) -> v1,
                    LinkedHashMap::new
                ))
            );

    public EventLogTabViewModel(ShellTabViewModel shellTab, Connector connector) {
        this.shellTab = shellTab;
        this.connector = connector;
        setTitle("Event Log");
        setClosable(false);
        this.connector.getEventBus().subscribe(NodeSelectedEvent.class, (e) -> {
            setSelectedElement(e.getElement());
        });
    }

    public final boolean isFilterEnabled() {
        return filterEnabled.get();
    }

    public final void setFilterEnabled(boolean value) {
        filterEnabled.set(value);
    }

    public final BooleanProperty filterEnabledProperty() {
        return filterEnabled;
    }

    public boolean isSelectedOnly() {
        return selectedOnly.get();
    }

    public void setSelectedOnly(boolean value) {
        selectedOnly.set(value);
    }

    public BooleanProperty selectedOnlyProperty() {
        return selectedOnly;
    }

    public Map<Class<? extends ConnectorEvent>, EventType> getEventTypesByClass() {
        return eventTypesByClass;
    }

    public Connector getConnector() {
        return connector;
    }

    public String getSearchText() {
        return searchText.get();
    }

    public void setSearchText(String value) {
        searchText.set(value);
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public ReadOnlyObjectProperty<Element> selectedElementProperty() {
        return selectedElement.getReadOnlyProperty();
    }

    public Element getSelectedElement() {
        return selectedElement.get();
    }

    public GenericFontIcon<?> getRecordIcon() {
        return recordIcon.get();
    }

    protected ObjectProperty<GenericFontIcon<?>> recordIconProperty() {
        return recordIcon;
    }

    protected void setRecordIcon(GenericFontIcon<?> icon) {
        recordIcon.set(icon);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(JfxComponentNames.EVENT_LOG_TAB);
    }

    protected boolean matchesFilter(LogEntry entry) {
        if (entry.event().getClass() == JavaFXEvent.class
                || entry.event().getClass() == WindowPropertiesEvent.class
                || entry.event().getClass() == MousePosEvent.class) {
            return false;
        }
        if (isSelectedOnly()) {
            if (!(entry.event() instanceof ElementEvent elementEvent)
                    || !Objects.equals(elementEvent.getElement(), getSelectedElement())) {
                return false;
            }
        }
        var type = this.eventTypesByClass.get(entry.event().getClass());
        if (type != null && !type.isEnabled()) {
            return false;
        }
        var text = getSearchText();
        if (text != null && !text.isEmpty() && !entry.message().contains(text)) {
            return false;
        }
        return true;
    }

    protected void start() {
        this.setRecordIcon(JfxIcons.RECORD_STOP);
        this.connector.getEventBus().subscribe(ConnectorEvent.class, eventListener);
    }

    protected void stop() {
        this.setRecordIcon(JfxIcons.RECORD_START);
        this.connector.getEventBus().unsubscribe(eventListener);
    }

    protected void clear() {
        allEntries.clear();
        filteredEntries.clear();
    }

    protected ObservableList<LogEntry> getFilteredEntries() {
        return filteredEntries;
    }

    protected void handleEvent(ConnectorEvent event) {
        messageBuilder.setLength(0);
        messageBuilder.append(String.format("%-22s", event.getClass().getSimpleName()));
        messageBuilder.append(event.toLogString());
        var entry = new LogEntry(getTime(), messageBuilder.toString(), event);
        this.allEntries.add(entry);
        if (isFilterEnabled()) {
            if (matchesFilter(entry)) {
                this.filteredEntries.add(entry);
            }
        } else {
            this.filteredEntries.add(entry);
        }
    }

    private <T extends ConnectorEvent> EventType<T> createEventType(Class<T> clazz, boolean enabled) {
        var type = new EventType(clazz);
        type.setEnabled(enabled);
        return type;
    }

    private void setSelectedElement(Element element) {
        selectedElement.set(element);
    }
}
