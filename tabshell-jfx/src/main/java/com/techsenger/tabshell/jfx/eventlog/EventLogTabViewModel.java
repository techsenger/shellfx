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
import devtoolsfx.connector.Connector;
import devtoolsfx.event.ConnectorEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class EventLogTabViewModel extends AbstractTabViewModel {

    private final ShellTabViewModel shellTab;

    private final Connector connector;

    private final List<ConnectorEvent> events = new ArrayList<>();

    private final ObservableList<LogEntry> entries = FXCollections.observableArrayList();

    private final Consumer<ConnectorEvent> eventListener = this::handleEvent;

    public EventLogTabViewModel(ShellTabViewModel shellTab, Connector connector) {
        this.shellTab = shellTab;
        this.connector = connector;
        setTitle("Event Log");
        setClosable(false);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(JfxComponentNames.EVENT_LOG_TAB);
    }

    void start() {
        this.connector.getEventBus().subscribe(ConnectorEvent.class, eventListener);
    }

    void stop() {
        this.connector.getEventBus().unsubscribe(eventListener);
    }

    ObservableList<LogEntry> getEntries() {
        return entries;
    }

    private void handleEvent(ConnectorEvent event) {
        this.events.add(event);
        this.entries.add(new LogEntry("01.01.01", event.toLogString(), event));
    }
}
