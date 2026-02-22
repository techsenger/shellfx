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

package com.techsenger.tabshell.devtools.node;

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.event.AttributeListEvent;
import com.techsenger.connectorfx.event.ConnectorEvent;
import com.techsenger.connectorfx.event.NodeSelectedEvent;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.connectorfx.scenegraph.attributes.Attribute;
import com.techsenger.connectorfx.scenegraph.attributes.AttributeCategory;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.DevToolsTabDockPort;
import com.techsenger.tabshell.devtools.ToolBarAwarePort;
import com.techsenger.tabshell.shared.find.FindNavigationAwarePort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 *
 * @author Pavel Castornii
 */
public class NodeTabPresenter<V extends NodeTabView, C extends NodeTabComposer> extends AbstractTabPresenter<V, C> {

    protected class Port extends AbstractTabPresenter<V, C>.Port implements NodeTabPort {

        @Override
        public Element getSelectedNode() {
            return getView().getSelectedNode();
        }

        @Override
        public void selectNode(Element node) {
            createNodeIndex();
            getView().selectNode(node);
        }

        @Override
        public void selectRoot() {
            getView().selectRoot();
        }
    }

    protected class NodeToolBarAwarePort implements ToolBarAwarePort, FindNavigationAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            findNode();
        }

        @Override
        public void onRefresh() {
            getView().refreshNodes();
        }

        @Override
        public void onFind() {
            findNode();
        }

        @Override
        public void onFindCleared() {
            clearFindNodeResult();
        }

        @Override
        public void onFindNext() {
            findNextNode();
        }

        @Override
        public void onFindPrevious() {
            findPreviousNode();
        }
    }

    /**
     * UpdateProperites -> filterAndAdd.
     */
    protected class PropertyToolBarAwarePort implements ToolBarAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            updateProperies();
        }

        @Override
        public void onRefresh() {

        }

        @Override
        public void onFind() {
            updateProperies();
        }

        @Override
        public void onFindCleared() {
            updateProperies();
        }
    }

    private final Connector connector;

    private final DevToolsTabDockPort tabDock;

    private boolean nodeIndexCreated = false;

    private final List<Element> foundNodes = new ArrayList<>();

    private int foundNodeIndex = 0;

    /**
     * All properties, including filtered out properties.
     */
    private final Map<AttributeCategory, List<PropertyItem>> propsByCategory = new HashMap<>();

    private Matcher propsMatcher;

    private int foundPropertyCount = 0;

    public NodeTabPresenter(V view, Connector connector, DevToolsTabDockPort tabDock) {
        super(view);
        this.connector = connector;
        this.tabDock = tabDock;
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
    public Port getPort() {
        return (Port) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new NodeTabPresenter.Port();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        this.tabDock.setOnSelection((selected) -> handleNodeSelected(getView().getSelectedNode()));
        // this event is fired only when a node is selected in inspector mode (using select button)
        // at the same time after that is is necessary to select the node using selectNode(..) method
        connector.getEventBus().subscribe(NodeSelectedEvent.class, (e) -> {
            createNodeIndex();
            getView().selectNode(e.element()); // -> handleNodeSelected(..)
        });

        var catExpansion = new HashMap<AttributeCategory, Boolean>();
        Arrays.stream(AttributeCategory.values()).forEach(c -> catExpansion.put(c, Boolean.FALSE));
        getView().setCategoryExpansion(catExpansion);
        // node selected -> AttributeListEvents -> processEvent -> filterAndAdd
        connector.getEventBus().subscribe(ConnectorEvent.class, event -> {
            switch (event) {
                case AttributeListEvent ale -> {
                    processPropertyEvent(ale);
                }
                default -> { }
            }
        });
    }

    protected void handleNodeSelected(Element node) {
        if (node == null) {
            return;
        }
        this.propsByCategory.clear();
        clearFindPropertyResult();
        getView().clearProperties();
        if (node.isWindowElement()) {
            this.connector.selectWindow(tabDock.getWindowUid());
        } else {
            this.connector.selectNode(tabDock.getWindowUid(), node, tabDock.getHighlightOptions());
        }
    }

    protected void handlePropertyRequested(PropertyItem item) {
        if (item.getType() != PropertyItemType.PROPERTY) {
            return;
        }
        var field = item.getAttribute().field();
        String declaringClassName = null;
        var node = getView().getSelectedNode();
        if (field != null && node != null && node.getClassInfo().module().startsWith("javafx.")) {
            declaringClassName = this.connector.getDeclaringClass(node.getClassInfo().className(), field);
        }
        getComposer().addPropertyDialog(node, item, declaringClassName);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.NODE_TAB);
    }

    protected Connector getConnector() {
        return connector;
    }

    protected DevToolsTabDockPort getTabDock() {
        return tabDock;
    }

    private void findNode() {
        createNodeIndex();
        clearFindNodeResult();
        var matcher = getComposer().getNodeToolBar().createFindMatcher();
        if (matcher != null) {
            var root = getView().getRootNode();
            findNode(root, matcher);
            if (!foundNodes.isEmpty()) {
                getView().selectNode(foundNodes.get(foundNodeIndex));
            }
            updateFoundNodeInfo();
        }
    }

    private void findNode(Element node, Matcher matcher) {
        if (node.isNodeElement()) {
            var id = node.getNodeProperties().id();
            var styleClasses = node.getNodeProperties().styleClass();
            if (matcher.reset(node.getClassInfo().simpleClassName()).find()
                    || (id != null && matcher.reset(id).find())
                    || (styleClasses != null && styleClasses.stream()
                            .filter(s -> matcher.reset(s).find()).anyMatch(e -> true))) {
                foundNodes.add(node);
            }
        }
        for (var child : node.getChildren()) {
            findNode(child, matcher);
        }
    }

    private void findNextNode() {
        if (this.foundNodes.isEmpty()) {
            return;
        }
        this.foundNodeIndex++;
        if (this.foundNodeIndex >= this.foundNodes.size()) {
            this.foundNodeIndex = 0;
        }
        getView().selectNode(foundNodes.get(foundNodeIndex));
        updateFoundNodeInfo();
    }

    private void findPreviousNode() {
        if (this.foundNodes.isEmpty()) {
            return;
        }
        this.foundNodeIndex--;
        if (this.foundNodeIndex < 0) {
            this.foundNodeIndex = this.foundNodes.size() - 1;
        }
        getView().selectNode(foundNodes.get(foundNodeIndex));
        updateFoundNodeInfo();
    }

    private void clearFindNodeResult() {
        this.foundNodes.clear();
        this.foundNodeIndex = 0;
        getComposer().getNodeToolBar().hideFindResultInfo();
    }

    private void createNodeIndex() {
        if (this.nodeIndexCreated) {
            return;
        }
        getView().refreshNodeIndex();
        this.nodeIndexCreated = true;
    }

    private void updateFoundNodeInfo() {
        var current = this.foundNodeIndex;
        if (!this.foundNodes.isEmpty()) {
            current = this.foundNodeIndex + 1;
        }
        getComposer().getNodeToolBar().showFindResultInfo(current, this.foundNodes.size());
    }

    private void processPropertyEvent(AttributeListEvent event) {
        var sortedList = new ArrayList<>(event.attributes());
        sortedList.sort(Comparator.comparing(Attribute::name));
        var properties = new ArrayList<PropertyItem>();
        for (var a : sortedList) {
            var property = new PropertyItem(event.category(), a);
            properties.add(property);
        }
        // adding new items to the map
        this.propsByCategory.put(event.category(), properties);
        filterAndAddProperties(event.category(), properties);
    }

    private void updateProperies() {
        getView().clearProperties();
        clearFindPropertyResult();
        this.propsMatcher = getComposer().getPropertyToolBar().createFindMatcher();
        // existing items from the map are filtered
        for (var entry : this.propsByCategory.entrySet()) {
            filterAndAddProperties(entry.getKey(), entry.getValue());
        }
    }

    private void filterAndAddProperties(AttributeCategory cat, List<PropertyItem> props) {
        if (this.propsMatcher != null) {
            List<PropertyItem> filteredProps = new ArrayList<>();
            for (var prop : props) {
                if (propsMatcher.reset(prop.getAttribute().name()).find()) {
                    filteredProps.add(prop);
                }
            }
            if (!filteredProps.isEmpty()) {
                getView().addProperties(cat, filteredProps);
                this.foundPropertyCount += filteredProps.size();
            }
            getComposer().getPropertyToolBar().showFindResultInfo(foundPropertyCount);
        } else {
            getView().addProperties(cat, props);
        }
    }

    private void clearFindPropertyResult() {
        getComposer().getPropertyToolBar().hideFindResultInfo();
        this.foundPropertyCount = 0;
    }
}
