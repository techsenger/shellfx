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

package com.techsenger.tabshell.devtools;

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.HighlightOptions;
import com.techsenger.connectorfx.event.NodeSelectedEvent;
import com.techsenger.connectorfx.scenegraph.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is the single point to control the selected node.
 *
 * @author Pavel Castornii
 */
public class Selector {

    @FunctionalInterface
    public interface SelectorListener {
        void onSelected(int oldWindowUid, int newWindowUid, Element oldElement, Element newElement);
    }

    private final Connector connector;

    private final List<SelectorListener> listeners = new ArrayList<>();

    private HighlightOptions highlightOptions = new HighlightOptions(false, false, false);

    private boolean selectionVisible;

    private Integer selectedWindowUid;

    private Element selectedNode;

    Selector(Connector connector) {
        this.connector = connector;
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

    public void addListener(SelectorListener listener) {
        this.listeners.add(listener);
    }

    public void selectWindow(int uid) {
        this.connector.selectWindow(uid);
        this.updateSelectedElements(uid, null);
    }

    public void selectNode(int uid, Element node) {
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
            this.selectedWindowUid = newWindowUid;
            this.selectedNode = newNode;
            notifyListeners(oldWindowUid, newWindowUid, oldNode, newNode);
        }
    }

    private void notifyListeners(int oldWindowUid, int newWindowUid, Element oldElement, Element newElement) {
        this.listeners.stream().forEach(l -> l.onSelected(oldWindowUid, newWindowUid, oldElement, newElement));
    }
}
