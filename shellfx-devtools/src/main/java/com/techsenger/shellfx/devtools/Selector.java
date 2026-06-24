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

package com.techsenger.shellfx.devtools;

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.HighlightOptions;
import com.techsenger.connectorfx.LocalElement;
import com.techsenger.connectorfx.event.NodeSelectedEvent;
import com.techsenger.connectorfx.scenegraph.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the single point to control the selected node.
 *
 * @author Pavel Castornii
 */
public class Selector {

    private static final Logger logger = LoggerFactory.getLogger(Selector.class);

    @FunctionalInterface
    public interface SelectorListener {
        void onSelect(int oldWindowUid, int newWindowUid, Element oldElement, Element newElement);
    }

    private final Connector connector;

    private final String logPrefix;

    /**
     * Listeners called before the selection is applied to the connector. Event Bus events from the connector are
     * received between pre- and post-listener notifications.
     */
    private final List<SelectorListener> preListeners = new ArrayList<>();

    /**
     * Listeners called after the selection is applied to the connector and the internal state is updated. Event Bus
     * events from the connector are received before this point.
     */
    private final List<SelectorListener> postListeners = new ArrayList<>();

    private HighlightOptions highlightOptions = new HighlightOptions(false, false, false);

    private boolean selectionVisible;

    private Integer selectedWindowUid;

    private Element selectedNode;

    Selector(Connector connector, String logPrefix) {
        this.connector = connector;
        this.logPrefix = logPrefix;
        // this event is fired only when a node is selected in inspector mode (using select button)
        connector.getEventBus().subscribe(NodeSelectedEvent.class, (e) -> {
            // When inspect mode is set to false the selection is removed because for selection
            // and inspect mode the same overlay. So, it is necessary to reselect the node.
            var selected = e.element();
            connector.getOptions().setInspectMode(false);
            if (selected != null) {
                selectNode(e.eventSource().uid(), selected);
            } else {
                updateSelectedElements(e.eventSource().uid(), null);
            }
        });
    }

    /**
     * The listeners that are called before selection.
     *
     * @param listener
     */
    public void addPreListener(SelectorListener listener) {
        this.preListeners.add(listener);
    }

    /**
     * The listeners that are called after selection.
     *
     * @param listener
     */
    public void addPostListener(SelectorListener listener) {
        this.postListeners.add(listener);
    }

    public void selectWindow(int uid) {
        notifyPreListeners(this.selectedWindowUid, uid, selectedNode, selectedNode);
        this.connector.selectWindow(uid);
        this.updateSelectedElements(uid, null);
    }

    public void selectNode(int uid, Element node) {
        notifyPreListeners(this.selectedWindowUid, uid, selectedNode, node);
        this.connector.selectNode(uid, node, highlightOptions);
        updateSelectedElements(uid, node);
    }

    public void clearSelection() {
        connector.clearSelection(selectedWindowUid);
    }

    public Integer getSelectedWindowUid() {
        return this.selectedWindowUid;
    }

    public Element getSelectedNode() {
        return this.selectedNode;
    }

    void setSelectedWindowUid(Integer selectedWindowUid) {
        this.selectedWindowUid = selectedWindowUid;
        logger.debug("{} Selected window UID: {}", logPrefix, this.selectedWindowUid);
    }

    void setSelectionVisible(boolean selectionVisible) {
        this.selectionVisible = selectionVisible;
        updateHighlightOptions(selectionVisible);
        // this method may be called by connector event handlers at any time
        if (this.selectedWindowUid != null) {
            if (this.selectedNode != null) {
                selectNode(this.selectedWindowUid, this.selectedNode);
            } else {
                selectWindow(this.selectedWindowUid);
            }
        }
    }

    /**
     * From connector we can get node properties only when this node is selected. So, if we disable selection
     * we can't update node properties when user selects another node. As a workaround we don't disable selection
     * but use {@link HighlightOptions} with all {@code false}.
     */
    private void updateHighlightOptions(boolean selection) {
        if (selection) {
            this.highlightOptions = new HighlightOptions(true, false, false);
        } else {
            this.highlightOptions = new HighlightOptions(false, false, false);
        }
    }

    private void updateSelectedElements(Integer newWindowUid, Element newNode) {
        if (!Objects.equals(this.selectedWindowUid, newWindowUid) || !Objects.equals(this.selectedNode, newNode)) {
            var oldWindowUid = this.selectedWindowUid;
            var oldNode = this.selectedNode;
            setSelectedWindowUid(newWindowUid);
            setSelectedNode(newNode);
            notifyPostListeners(oldWindowUid, newWindowUid, oldNode, newNode);
        }
    }

    private void notifyPreListeners(int oldWindowUid, int newWindowUid, Element oldElement, Element newElement) {
        if (!Objects.equals(this.selectedWindowUid, newWindowUid) || !Objects.equals(this.selectedNode, newElement)) {
            this.preListeners.stream().forEach(l -> l.onSelect(oldWindowUid, newWindowUid, oldElement, newElement));
        }
    }

    private void notifyPostListeners(int oldWindowUid, int newWindowUid, Element oldElement, Element newElement) {
        this.postListeners.stream().forEach(l -> l.onSelect(oldWindowUid, newWindowUid, oldElement, newElement));
    }

    public void setSelectedNode(Element selectedNode) {
        this.selectedNode = selectedNode;
        if (logger.isDebugEnabled()) {
            if (this.selectedNode != null && this.selectedNode.isNodeElement()) {
                logger.debug("{} Selected node: {} from window with UID: {}", logPrefix, this.selectedNode,
                         ((LocalElement) this.selectedNode).unwrap().getScene().getWindow().hashCode());
            } else {
                logger.debug("{} Selected node: {}", logPrefix, this.selectedNode);
            }
        }
    }
}
