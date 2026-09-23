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

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.event.ConnectorEvent;
import com.techsenger.connectorfx.event.ElementEvent;
import com.techsenger.shellfx.core.UiExecutor;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.tab.AbstractTabViewModel;
import com.techsenger.shellfx.devtools.Selector;
import com.techsenger.shellfx.shared.find.FindResult;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class EventTabViewModel<C extends EventTabComposer> extends AbstractTabViewModel<C>
        implements EventToolBarAwarePort {

    private static final long ZONE_OFFSET_MILLIS = ZoneId.systemDefault().getRules().getOffset(Instant.now())
            .getTotalSeconds() * 1000L;

    private static final Logger logger = LoggerFactory.getLogger(EventTabViewModel.class);

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

    private final ObservableSource<String> appendTextSource = new SimpleObservableSource<>();

    private final ObservableSource<Void> clearTextSource = new SimpleObservableSource<>();

    private final Connector connector;

    private final Selector selector;

    private final char[] timeArray = new char[12]; // HH:mm:ss.SSS

    private final ConcurrentLinkedQueue<LogEntry> newEntries = new ConcurrentLinkedQueue();

    private final Filter filter = new Filter();

    private final Consumer<ConnectorEvent> eventListener = this::handleEvent;

    private final StringBuilder messageBuilder = new StringBuilder();

    private final StringBuilder textBuilder = new StringBuilder();

    private Thread entryProcessor;

    private int matchedEntriesCount;

    private int totalEntriesCount;

    public EventTabViewModel(EventTabParams params) {
        super(params);
        this.connector = params.getConnector();
        this.selector = params.getSelector();
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
    public void onRecord(boolean selected) {
        if (selected) {
            subscribe();
        } else {
            unsubscribe();
        }
    }

    @Override
    public void onClear() {
        UiExecutor.execute(() -> clear());
    }

    @Override
    public void onFilterSelected(boolean selected) {
        filter.setSelected(selected);
    }

    @Override
    public void onSelectedNodeOnly(boolean selected) {
        filter.setSelectedNodeOnly(selected);
    }

    @Override
    public void onEventTypesChanged() {
        filter.setSelectedEventTypes(getComposer().getToolBarPort().getSelectedEventTypes());
    }

    @Override
    public void onMatchCase(boolean selected) {
        filter.setMatcher(getComposer().getToolBarPort().createFindMatcher());
    }

    @Override
    public void onRefresh() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<FindResult> onFind() {
        filter.setMatcher(getComposer().getToolBarPort().createFindMatcher());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onFindCleared() {
        filter.setMatcher(getComposer().getToolBarPort().createFindMatcher());
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Events");
        setClosable(false);
        selectedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                createAndStartProcessor();
            } else {
                stopAndDestroyProcessor();
            }
        });
        UiExecutor.execute(() -> {
            var tb = getComposer().getToolBarPort();
            this.filter.setSelected(tb.isFilterSelected());
            this.filter.setMatcher(tb.createFindMatcher());
            this.filter.setSelectedNodeOnly(tb.isSelectedNodeOnly());
            this.filter.setSelectedEventTypes(tb.getSelectedEventTypes());
            updateStatistics();
        });
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        stopAndDestroyProcessor();
    }

    protected Connector getConnector() {
        return connector;
    }

    protected void createAndStartProcessor() {
        Runnable task = () -> {
            List<LogEntry> processedEntries = new ArrayList<>(1000);
            while (!Thread.currentThread().isInterrupted()) {
                var now = System.currentTimeMillis();
                int newEntriesCount = 0;
                while (true) {
                    LogEntry entry = newEntries.poll();
                    if (entry == null) {
                        break;
                    }
                    newEntriesCount++;
                    if (getComposer().getToolBarPort().isFilterSelected()) {
                        if (matchesFilter(filter, entry)) {
                            processedEntries.add(entry);
                        }
                    } else {
                        processedEntries.add(entry);
                    }
                    if (entry.timestamp() > now) {
                        break;
                    }
                }
                totalEntriesCount = totalEntriesCount + newEntriesCount;
                matchedEntriesCount = matchedEntriesCount + processedEntries.size();
                sendText(processedEntries);
                UiExecutor.execute(() -> updateStatistics());
                processedEntries.clear();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
        };
        this.entryProcessor = Thread.ofVirtual().start(task);
        logger.debug("{} EntryProcessor started", getDescriptor().getLogPrefix());
    }

    protected void sendText(List<LogEntry> entries) {
        if (entries.size() == 0) {
            return;
        }
        textBuilder.setLength(0);
        entries.forEach(e -> textBuilder.append(e.zonedTime()).append(" ").append(e.message()).append("\n"));
        var text = textBuilder.toString();
        UiExecutor.execute(() -> appendTextSource.next(text));
    }

    protected void stopAndDestroyProcessor() {
        if (this.entryProcessor == null) {
            return;
        }
        this.entryProcessor.interrupt();
        this.entryProcessor = null;
        logger.debug("{} EntryProcessor stopped", getDescriptor().getLogPrefix());
    }

    protected boolean matchesFilter(Filter filter, LogEntry entry) {
        if (filter.isSelectedNodeOnly()) {
            var selectedNode = selector.getSelectedNode();
            if (!(entry.event() instanceof ElementEvent elementEvent)
                    || !Objects.equals(elementEvent.getElement(), selectedNode)) {
                return false;
            }
        }
        if (!filter.getSelectedEventTypes().contains(entry.event().getClass())) {
            return false;
        }
        var matcher = filter.getMatcher();
        if (matcher != null && !matcher.reset(entry.message()).find()) {
            return false;
        }
        return true;
    }

    protected void subscribe() {
        this.connector.getEventBus().subscribe(ConnectorEvent.class, eventListener);
    }

    protected void unsubscribe() {
        this.connector.getEventBus().unsubscribe(eventListener);
    }

    protected void clear() {
        newEntries.clear();
        clearTextSource.next(null);
        // after all
        matchedEntriesCount = 0;
        totalEntriesCount = 0;
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

    ObservableSource<String> getAppendTextSource() {
        return appendTextSource;
    }

    ObservableSource<Void> getClearTextSource() {
        return clearTextSource;
    }

    private void updateStatistics() {
        var tb = getComposer().getToolBarPort();
        tb.setStatistics(matchedEntriesCount + " / " + totalEntriesCount);
    }
}
