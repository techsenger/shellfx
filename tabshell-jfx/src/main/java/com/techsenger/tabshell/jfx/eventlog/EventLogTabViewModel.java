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

package com.techsenger.tabshell.jfx.eventlog;

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
import com.techsenger.tabshell.jfx.style.JfxIcons;
import com.techsenger.tabshell.material.icon.GenericFontIcon;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
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
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class EventLogTabViewModel<T extends EventLogTabMediator> extends AbstractTabViewModel<T> {

    protected record Filter(
            boolean active,
            boolean selectedOnly,
            Matcher matcher,
            Set<Class<? extends ConnectorEvent>> enabledEvents) { }

    private static final long ZONE_OFFSET_MILLIS = ZoneId.systemDefault().getRules().getOffset(Instant.now())
            .getTotalSeconds() * 1000L;

    private static final Logger logger = LoggerFactory.getLogger(EventLogTabViewModel.class);

    private static String getZonedTime(long timestamp, char[] timeArray) {
        long millis = timestamp + ZONE_OFFSET_MILLIS;
        long seconds = millis / 1000;
        long ms = millis % 1000;

        int totalSeconds = (int) (seconds % 86400);
        int hour = totalSeconds / 3600;
        int minute = (totalSeconds % 3600) / 60;
        int second = totalSeconds % 60;

        timeArray[0] = (char) ('0' + hour / 10);
        timeArray[1] = (char) ('0' + hour % 10);
        timeArray[2] = ':';
        timeArray[3] = (char) ('0' + minute / 10);
        timeArray[4] = (char) ('0' + minute % 10);
        timeArray[5] = ':';
        timeArray[6] = (char) ('0' + second / 10);
        timeArray[7] = (char) ('0' + second % 10);
        timeArray[8] = '.';
        timeArray[9] = (char) ('0' + ms / 100);
        timeArray[10] = (char) ('0' + (ms / 10) % 10);
        timeArray[11] = (char) ('0' + ms % 10);

        return new String(timeArray);
    }

    private final Connector connector;

    private final char[] timeArray = new char[12]; // HH:mm:ss.SSS

    private final List<LogEntry> retainedEntries = new CopyOnWriteArrayList<>();

    private final ConcurrentLinkedQueue<LogEntry> newEntries = new ConcurrentLinkedQueue();

    private final ObservableSource<String> textSource = new SimpleObservableSource<>();

    private final BooleanProperty filterActive = new SimpleBooleanProperty(true);

    private final ObjectProperty<GenericFontIcon<?>> recordIcon = new SimpleObjectProperty<>(JfxIcons.RECORD_START);

    private final BooleanProperty selectedOnly = new SimpleBooleanProperty(true);

    private final Consumer<ConnectorEvent> eventListener = this::handleEvent;

    private final StringBuilder messageBuilder = new StringBuilder();

    private final StringBuilder textBuilder = new StringBuilder();

    private final ReadOnlyObjectWrapper<Element> selectedElement = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyBooleanWrapper subscribed = new ReadOnlyBooleanWrapper();

    private final ReadOnlyIntegerWrapper displayedEntriesCount = new ReadOnlyIntegerWrapper();

    private final ReadOnlyIntegerWrapper totalEntriesCount = new ReadOnlyIntegerWrapper();

    private final ReadOnlyStringWrapper statistics = new ReadOnlyStringWrapper();

    private final ReadOnlyBooleanWrapper filteredOutRetained = new ReadOnlyBooleanWrapper();

    private final Map<Class<? extends ConnectorEvent>, EventType> eventTypesByClass =
            Collections.unmodifiableMap(
                Stream.of(
                    createEventType(AttributeListEvent.class, true),
                    createEventType(AttributeUpdatedEvent.class, true),
                    createEventType(ExceptionEvent.class, false),
                    createEventType(JavaFXEvent.class, false),
                    createEventType(MousePosEvent.class, false),
                    createEventType(NodeAddedEvent.class, true),
                    createEventType(NodeRemovedEvent.class, true),
                    createEventType(NodeSelectedEvent.class, true),
                    createEventType(NodeStyleClassEvent.class, true),
                    createEventType(NodeVisibilityEvent.class, true),
                    createEventType(RootChangedEvent.class, true),
                    createEventType(WindowClosedEvent.class, true),
                    createEventType(WindowPropertiesEvent.class, true)
                )
                .collect(Collectors.toMap(
                    EventType::getType,
                    eventType -> eventType,
                    (v1, v2) -> v1,
                    LinkedHashMap::new
                ))
            );

    private Thread entryProcessor;

    private volatile Filter filter;

    private Filter previousFilter;

    public EventLogTabViewModel(Connector connector) {
        this.connector = connector;
        setTitle("Event Log");
        setClosable(false);
        this.connector.getEventBus().subscribe(NodeSelectedEvent.class, (e) -> {
            setSelectedElement(e.getElement());
        });
    }

    public final boolean isFilterActive() {
        return filterActive.get();
    }

    public final void setFilterActive(boolean value) {
        filterActive.set(value);
    }

    public final BooleanProperty filterActiveProperty() {
        return filterActive;
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

    public ReadOnlyObjectProperty<Element> selectedElementProperty() {
        return selectedElement.getReadOnlyProperty();
    }

    public Element getSelectedElement() {
        return selectedElement.get();
    }

    public GenericFontIcon<?> getRecordIcon() {
        return recordIcon.get();
    }

    public boolean isSubscribed() {
        return subscribed.get();
    }

    public ReadOnlyBooleanProperty subscribedProperty() {
        return subscribed.getReadOnlyProperty();
    }

    public int getDisplayedEntriesCount() {
        return displayedEntriesCount.get();
    }

    public ReadOnlyIntegerProperty displayedEntriesCountProperty() {
        return displayedEntriesCount.getReadOnlyProperty();
    }

    public int getTotalEntriesCount() {
        return totalEntriesCount.get();
    }

    public ReadOnlyIntegerProperty totalEntriesCountProperty() {
        return totalEntriesCount.getReadOnlyProperty();
    }

    public String getStatistics() {
        return statistics.get();
    }

    public ReadOnlyStringProperty statisticsProperty() {
        return statistics.getReadOnlyProperty();
    }

    public boolean isFilteredOutRetained() {
        return filteredOutRetained.get();
    }

    public ReadOnlyBooleanProperty filteredOutRetainedProperty() {
        return filteredOutRetained.getReadOnlyProperty();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.filterActive.addListener((ov, oldV, newV) -> updateFilter());
        this.selectedOnly.addListener((ov, oldV, newV) -> updateFilter());
        updateFilter();
        updateStatistics();
        selectedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                createAndStartProcessor();
            } else {
                stopAndDestroyProcessor();
            }
        });
    }

    @Override
    protected void deinitialize() {
        super.deinitialize();
        stopAndDestroyProcessor();
    }

    protected ObjectProperty<GenericFontIcon<?>> recordIconProperty() {
        return recordIcon;
    }

    protected void setRecordIcon(GenericFontIcon<?> icon) {
        recordIcon.set(icon);
    }

    protected boolean matchesFilter(Filter filter, LogEntry entry) {
        if (filter.selectedOnly()) {
            if (!(entry.event() instanceof ElementEvent elementEvent)
                    || !Objects.equals(elementEvent.getElement(), getSelectedElement())) {
                return false;
            }
        }
        if (!filter.enabledEvents.contains(entry.event().getClass())) {
            return false;
        }
        if (filter.matcher != null && !filter.matcher.reset(entry.message()).find()) {
            return false;
        }
        return true;
    }

    protected void subscribe() {
        this.setRecordIcon(JfxIcons.RECORD_STOP);
        this.connector.getEventBus().subscribe(ConnectorEvent.class, eventListener);
        setSubscribed(true);
    }

    protected void unsubscribe() {
        this.setRecordIcon(JfxIcons.RECORD_START);
        this.connector.getEventBus().unsubscribe(eventListener);
        setSubscribed(false);
    }

    protected void clear() {
        retainedEntries.clear();
        newEntries.clear();
        textSource.next(null);
        // after all
        setDisplayedEntriesCount(0);
        setTotalEntriesCount(0);
        updateStatistics();
    }

    protected void handleEvent(ConnectorEvent event) {
        messageBuilder.setLength(0);
        messageBuilder.append(String.format("%-22s", event.getClass().getSimpleName()));
        messageBuilder.append(event.toLogString());
        var timestamp = System.currentTimeMillis();
        var entry = new LogEntry(timestamp, getZonedTime(timestamp, timeArray), messageBuilder.toString(), event);
        this.newEntries.offer(entry);
    }

    protected void selectAllEvents() {
        this.eventTypesByClass.values().forEach(e -> e.setEnabled(true));
    }

    protected void deselectAllEvents() {
        this.eventTypesByClass.values().forEach(e -> e.setEnabled(false));
    }

    protected void updateFilter() {
        // JFX properties are not thread safe
        Set<Class<? extends ConnectorEvent>> events = this.eventTypesByClass.entrySet().stream()
                .filter(e -> e.getValue().isEnabled())
                .map(e -> e.getKey()).collect(Collectors.toSet());
        this.filter = new Filter(isFilterActive(), isSelectedOnly(),
                getMediator().getSearchPanel().createMatcher(), events);
    }

    protected ObservableSource<String> getTextSource() {
        return textSource;
    }

    protected void createAndStartProcessor() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (!Thread.currentThread().isInterrupted()) {
                    List<LogEntry> processedEntries = new ArrayList<>(1000);

                    final var f = filter;
                    if (f != previousFilter) {
                        previousFilter = f;
                        textSource.next(null);
                        setDisplayedEntriesCount(0);
                        if (f.active()) {
                            retainedEntries.stream().filter(e -> matchesFilter(f, e)).forEach(processedEntries::add);
                        } else {
                            processedEntries.addAll(retainedEntries);
                        }
                    }

                    var now = System.currentTimeMillis();
                    int newEntriesCount = 0;
                    while (true) {
                        LogEntry entry = newEntries.poll();
                        if (entry == null) {
                            break;
                        }
                        newEntriesCount++;
                        if (f.active()) {
                            if (matchesFilter(f, entry)) {
                                retainedEntries.add(entry);
                                processedEntries.add(entry);
                            } else if (isFilteredOutRetained()) {
                                retainedEntries.add(entry);
                            }
                        } else {
                            retainedEntries.add(entry);
                            processedEntries.add(entry);
                        }
                        if (entry.timestamp() > now) {
                            break;
                        }
                    }
                    sendText(processedEntries);
                    setTotalEntriesCount(getTotalEntriesCount() + newEntriesCount);
                    setDisplayedEntriesCount(getDisplayedEntriesCount() + processedEntries.size());
                    updateStatistics();

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }
        };
        this.entryProcessor = new Thread(task);
        this.entryProcessor.start();
        logger.debug("{} EntryProcessor started", getMediator().getLogPrefix());
    }

    protected void sendText(List<LogEntry> entries) {
        if (entries.size() == 0) {
            return;
        }
        textBuilder.setLength(0);
        entries.forEach(e -> textBuilder.append(e.zonedTime()).append(" ").append(e.message()).append("\n"));
        var text = textBuilder.toString();
        textSource.next(text);
    }

    protected void stopAndDestroyProcessor() {
        if (this.entryProcessor == null) {
            return;
        }
        this.entryProcessor.interrupt();
        this.entryProcessor = null;
        logger.debug("{} EntryProcessor stopped", getMediator().getLogPrefix());
    }

    ReadOnlyBooleanWrapper getFilteredOutRetainedWrapper() {
        return this.filteredOutRetained;
    }

    private <T extends ConnectorEvent> EventType<T> createEventType(Class<T> clazz, boolean enabled) {
        var type = new EventType(clazz);
        type.setEnabled(enabled);
        type.enabledProperty().addListener((ov, oldV, newV) -> updateFilter());
        return type;
    }

    private void setSelectedElement(Element element) {
        selectedElement.set(element);
    }

    private void setSubscribed(boolean value) {
        subscribed.set(value);
    }

    private void setDisplayedEntriesCount(int value) {
        displayedEntriesCount.set(value);
    }

    private void setTotalEntriesCount(int value) {
        totalEntriesCount.set(value);
    }

    private void setStatistics(String value) {
        statistics.set(value);
    }

    private void setFilteredOutRetained(boolean value) {
        filteredOutRetained.set(value);
    }

    private void updateStatistics() {
        setStatistics(getDisplayedEntriesCount() + " / " + retainedEntries.size() + " / " + getTotalEntriesCount());
    }
}
