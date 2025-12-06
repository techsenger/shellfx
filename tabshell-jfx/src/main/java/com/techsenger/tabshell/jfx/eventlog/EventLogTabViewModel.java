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
import java.time.LocalTime;
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

    private static String getTime() {
        LocalTime now = LocalTime.now();
        char[] buffer = new char[12]; // HH:mm:ss.SSS

        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        int millis = now.getNano() / 1_000_000;

        buffer[0] = (char) ('0' + hour / 10);
        buffer[1] = (char) ('0' + hour % 10);
        buffer[2] = ':';
        buffer[3] = (char) ('0' + minute / 10);
        buffer[4] = (char) ('0' + minute % 10);
        buffer[5] = ':';
        buffer[6] = (char) ('0' + second / 10);
        buffer[7] = (char) ('0' + second % 10);
        buffer[8] = '.';
        buffer[9] = (char) ('0' + millis / 100);
        buffer[10] = (char) ('0' + (millis / 10) % 10);
        buffer[11] = (char) ('0' + millis % 10);

        return new String(buffer);
    }

    private final ShellTabViewModel shellTab;

    private final Connector connector;

    private final List<LogEntry> allEntries = new ArrayList<>();

    private final ObservableList<LogEntry> filteredEntries = FXCollections.observableArrayList();

    private final Consumer<ConnectorEvent> eventListener = this::handleEvent;

    private final StringBuilder messageBuilder = new StringBuilder();

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

    ObservableList<LogEntry> getFilteredEntries() {
        return filteredEntries;
    }

    private void handleEvent(ConnectorEvent event) {
        messageBuilder.setLength(0);
        messageBuilder.append(String.format("%-22s", event.getClass().getSimpleName()));
        messageBuilder.append(event.toLogString());
        var entry = new LogEntry(getTime(), messageBuilder.toString(), event);
        this.allEntries.add(entry);
        this.filteredEntries.add(entry);
    }
}
