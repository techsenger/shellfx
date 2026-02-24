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

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.event.ConnectorEvent;
import com.techsenger.connectorfx.event.ElementEvent;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.node.NodeTabPort;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class EventTabPresenter<V extends EventTabView, C extends EventTabComposer> extends AbstractTabPresenter<V, C> {

    private static final long ZONE_OFFSET_MILLIS = ZoneId.systemDefault().getRules().getOffset(Instant.now())
            .getTotalSeconds() * 1000L;

    private static final Logger logger = LoggerFactory.getLogger(EventTabPresenter.class);

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

    protected class ToolBarAwarePortImpl implements EventToolBarAwarePort {

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
            clear();
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
            filter.setSelectedEventTypes(getComposer().getToolBar().getSelectedEventTypes());
        }

        @Override
        public void onMatchCase(boolean selected) {
            filter.setMatcher(getComposer().getToolBar().createFindMatcher());
        }

        @Override
        public void onRefresh() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void onFind() {
            filter.setMatcher(getComposer().getToolBar().createFindMatcher());
        }

        @Override
        public void onFindCleared() {
            filter.setMatcher(getComposer().getToolBar().createFindMatcher());
        }
    }

    private final Connector connector;

    private final NodeTabPort nodeTab;

    private final char[] timeArray = new char[12]; // HH:mm:ss.SSS

    private final ConcurrentLinkedQueue<LogEntry> newEntries = new ConcurrentLinkedQueue();

    private Thread entryProcessor;

    private final Filter filter = new Filter();

    private final Consumer<ConnectorEvent> eventListener = this::handleEvent;

    private final StringBuilder messageBuilder = new StringBuilder();

    private final StringBuilder textBuilder = new StringBuilder();

    private int matchedEntriesCount;

    private int totalEntriesCount;

    public EventTabPresenter(V view, Connector connector, NodeTabPort nodeTab) {
        super(view);
        this.connector = connector;
        this.nodeTab = nodeTab;
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
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.EVENT_TAB);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var tb = getComposer().getToolBar();
        this.filter.setSelected(tb.isFilterSelected());
        this.filter.setMatcher(tb.createFindMatcher());
        this.filter.setSelectedNodeOnly(tb.isSelectedNodeOnly());
        this.filter.setSelectedEventTypes(tb.getSelectedEventTypes());
        updateStatistics();
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        stopAndDestroyProcessor();
    }

    @Override
    public void onSelected(boolean selected) {
        super.onSelected(selected);
        if (selected) {
            createAndStartProcessor();
        } else {
            stopAndDestroyProcessor();
        }
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
                    if (getComposer().getToolBar().isFilterSelected()) {
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
                updateStatistics();
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
        getView().appendText(text);
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
            var selectedNode = nodeTab.getSelectedNode();
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
        getView().clearText();
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

    private void updateStatistics() {
        var tb = getComposer().getToolBar();
        tb.setStatistics(matchedEntriesCount + " / " + totalEntriesCount);
    }
}
