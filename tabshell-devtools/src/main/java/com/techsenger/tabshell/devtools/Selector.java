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
import java.util.function.BiConsumer;

/**
 * This is the single point to control the selected node.
 *
 * @author Pavel Castornii
 */
public class Selector {

    private final Connector connector;

    private final List<BiConsumer<Integer, Element>> listeners = new ArrayList<>();

    private HighlightOptions highlightOptions = new HighlightOptions(false, false, false);

    private boolean selectionVisible;

    private Integer selectedWindowUid;

    private Element selectedNode;;

    Selector(Connector connector) {
        this.connector = connector;
    }

    public void addListener(BiConsumer<Integer, Element> listener) {
        this.listeners.add(listener);
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

    public void selectWindow(int uid) {
        this.connector.selectWindow(uid);
        this.updateSelectedElements(uid, null);
    }

    public void selectNode(int uid, Element node) {
        this.connector.selectNode(uid, node, highlightOptions);
        updateSelectedElements(uid, node);
    }

    public void clearSelection(int uid) {
        connector.clearSelection(uid);
    }

    public Integer getSelectedWindowUid() {
        return this.selectedWindowUid;
    }

    public Element getSelectedNode() {
        return this.selectedNode;
    }

    void setSelectionVisible(boolean selectionVisible) {
        this.selectionVisible = selectionVisible;
        updateHighlightOptions(selectionVisible);
        // this method may be called by connector event handlers at any time
        if (this.selectedWindowUid != null) {
            if (selectionVisible) {
                if (this.selectedNode != null) {
                    selectNode(this.selectedWindowUid, this.selectedNode);
                } else {
                    selectWindow(this.selectedWindowUid);
                }
            } else {
                clearSelection(this.selectedWindowUid);
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

    private void updateSelectedElements(Integer uid, Element node) {
        if (!Objects.equals(this.selectedWindowUid, uid) || !Objects.equals(this.selectedNode, node)) {
            this.selectedWindowUid = uid;
            this.selectedNode = node;
            notifyListeners();
        }
    }

    private void notifyListeners() {
        this.listeners.stream().forEach(l -> l.accept(this.selectedWindowUid, this.selectedNode));
    }
}
