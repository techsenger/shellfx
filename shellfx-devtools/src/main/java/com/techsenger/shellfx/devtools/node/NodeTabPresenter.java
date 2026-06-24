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

package com.techsenger.shellfx.devtools.node;

import com.techsenger.connectorfx.event.AttributeListEvent;
import com.techsenger.connectorfx.event.ConnectorEvent;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.connectorfx.scenegraph.attributes.Attribute;
import com.techsenger.connectorfx.scenegraph.attributes.AttributeCategory;
import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.AddablePresenter;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.tab.AbstractTabPresenter;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.devtools.DevToolsComponents;
import com.techsenger.shellfx.devtools.DevToolsHostType;
import com.techsenger.shellfx.devtools.DevToolsTabDockPort;
import com.techsenger.shellfx.devtools.ToolBarAwarePort;
import com.techsenger.shellfx.shared.find.FindNavigationAwarePort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class NodeTabPresenter<V extends NodeTabView> extends AbstractTabPresenter<V>
        implements AddablePresenter, NodeTabPort {

    private static final Logger logger = LoggerFactory.getLogger(NodeTabPresenter.class);

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
            refreshProperties();
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

    private final DevToolsTabDockPort tabDock;

    private boolean nodeIndexCreated = false;

    private final List<Element> foundNodes = new ArrayList<>();

    private int foundNodeIndex = 0;

    /**
     * All properties, including filtered out properties.
     */
    private final Map<AttributeCategory, List<PropertyItem>> allPropsByCategory = new LinkedHashMap<>();

    /**
     * The properties that are currently shown.
     */
    private final Map<AttributeCategory, List<PropertyItem>> shownPropsByCategory = new LinkedHashMap<>();

    private Matcher propsMatcher;

    private int foundPropertyCount = 0;

    private Consumer<String> linkOpener = (ulr) -> getView().getComposer().getShellPort().getContext()
            .getHostServices().showDocument(ulr);

    private Element rootNode;

    private Element selectedNode;

    /**
     * ObservableType in Attribute is incorrect because its value is determined by the property instance
     * rather than the property method's return type. However, we cannot fix this in the connector because
     * using reflection there would significantly slow down node event firing.
     */
    private final Map<String, Boolean> readOnlyByProperty = new HashMap<>();

    private Map<AttributeCategory, Boolean> categoryExpansion;

    /**
     * Attribute events come after node events, so we need to save them.
     */
    private List<AttributeListEvent> savedAttributeEvents = new ArrayList<>();

    private boolean selectedFromNodeTree;

    private PropertyItem selectedProperty;

    public NodeTabPresenter(V view, NodeTabParams params) {
        super(view, params);
        this.tabDock = params.getTabDock();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Element getRootNode() {
        return rootNode;
    }

    public Element getSelectedNode() {
        return selectedNode;
    }

    public Map<AttributeCategory, Boolean> getCategoryExpansion() {
        return categoryExpansion;
    }

    public void setCategoryExpansion(Map<AttributeCategory, Boolean> categoryExpansion) {
        this.categoryExpansion = categoryExpansion;
    }

    @Override
    public void setLinkOpener(Consumer<String> opener) {
        linkOpener = opener;
    }

    @Override
    public void onAdded() {
        tabDock.getSelector().addPreListener((oldWindowUid, newWindowUid, oldElement, newElement) -> {
            if (Objects.equals(oldElement, newElement) && oldWindowUid == newWindowUid) {
                return;
            }
            clearProperties();
        });

        // these events come between pre and post listeners;
        // node selected via API or select button -> AttributeListEvents -> processEvent -> filterAndAdd
        this.tabDock.getConnector().getEventBus().subscribe(ConnectorEvent.class, event -> {
            switch (event) {
                case AttributeListEvent ale -> {
                    if (Objects.equals(this.selectedNode, ale.element())) {
                        if (selectedFromNodeTree) {
                            processPropertyEvent(ale);
                            logger.debug("{} Properties {} processed", getDescriptor().getLogPrefix(), ale.category());
                        }
                    } else {
                        this.savedAttributeEvents.add(ale);
                        logger.debug("{} Properties {} saved", getDescriptor().getLogPrefix(), ale.category());
                    }
                }
                default -> { }
            }
        });

        tabDock.getSelector().addPostListener((oldWindowUid, newWindowUid, oldElement, newElement) -> {
            if (oldWindowUid != newWindowUid) {
                getView().selectWindow(newWindowUid);
                this.nodeIndexCreated = false;
                getView().selectRoot();
                refreshProperties();
                return;
            }
            if (newElement == null) {
                getView().selectRoot();
            }
            if (Objects.equals(oldElement, newElement)) {
                return;
            }
            this.selectedNode = newElement; // so the onNodeSelected method won't complete
            if (this.selectedNode != null) {
                createNodeIndex();
                getView().selectNode(this.selectedNode, true);
                for (var events : this.savedAttributeEvents) { // adding saved events
                    processPropertyEvent(events);
                }
                selectPreviousProperty();
            }
            this.savedAttributeEvents.clear();
        });
    }

    @Override
    protected void applyAppearance() {
        super.applyAppearance();
        var catExpansion = new HashMap<AttributeCategory, Boolean>();
        Arrays.stream(AttributeCategory.values()).forEach(c -> catExpansion.put(c, Boolean.FALSE));
        setCategoryExpansion(catExpansion);
    }

    protected void onNodeSelected(Element node) {
        if (Objects.equals(this.selectedNode, node)) { // equals!
            return;
        }
        this.selectedNode = node;
        if (this.selectedNode == null) {
            return;
        }
        this.selectedFromNodeTree = true;
        if (node.isWindowElement()) {
            this.tabDock.getSelector().selectWindow(tabDock.getSelector().getSelectedWindowUid());
        } else {
            this.tabDock.getSelector().selectNode(tabDock.getSelector().getSelectedWindowUid(), node);
        }
        this.selectedFromNodeTree = false;
    }

    protected void onCategoryExpanded(AttributeCategory category, boolean expanded) {
        this.categoryExpansion.put(category, expanded);
    }

    protected void onRootChanged(Element node) {
        this.rootNode = node;
    }

    protected void onPropertyRequested(PropertyItem item) {
        if (item.getType() != PropertyItemType.PROPERTY) {
            return;
        }
        var field = item.getAttribute().field();
        String declaringClassName = null;
        var node = selectedNode;
        if (field != null && node != null && node.getClassInfo().module().startsWith("javafx.")) {
            declaringClassName = this.tabDock.getConnector().getDeclaringClass(node.getClassInfo().className(), field);
        }
        WindowType windowType = WindowType.NESTED;
        if (tabDock.getHostType() == DevToolsHostType.WINDOW) {
            windowType = WindowType.TOP_LEVEL;
        }
        var params = new ViewerDialogParams(windowType, getShellContext().getSettings().getAppearance(),
                node, item, declaringClassName, linkOpener);
        var dialog = getView().getComposer().openViewerDialog(params);
        dialog.setOnClosed(() -> getView().focusProperties());
    }

    protected void onPropertySelected(PropertyItem item) {
        // when properites are clered, selected item is null
        if (item != null) {
            this.selectedProperty = item;
        }
    }

    protected void onEditProperty(EditPropertyTask<?> task) {
        WindowType windowType = WindowType.NESTED;
        if (this.tabDock.getHostType() == DevToolsHostType.WINDOW) {
            windowType = WindowType.TOP_LEVEL;
        }
        var params = new EditorDialogParams(windowType, getShellContext().getSettings().getAppearance(),
                task, this.tabDock.getHistoryManager());
        var dialog = getView().getComposer().openEditorDialog(params);
        dialog.setOnClosed(() -> {
            if (dialog.isPropertyUpdated()) {
                refreshProperties();
            }
            getView().focusProperties();
        });
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Nodes");
        setClosable(false);
        getView().setReadOnlyByProperty(readOnlyByProperty);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DevToolsComponents.NODE_TAB);
    }

    protected DevToolsTabDockPort getTabDock() {
        return tabDock;
    }

    private void clearProperties() {
        this.allPropsByCategory.clear();
        this.shownPropsByCategory.clear();
        clearFindPropertyResult();
        getView().clearProperties();
    }

    private void findNode() {
        createNodeIndex();
        clearFindNodeResult();
        var matcher = getView().getComposer().getNodeToolBarPort().createFindMatcher();
        if (matcher != null) {
            findNode(rootNode, matcher);
            if (!foundNodes.isEmpty()) {
                getView().selectNode(foundNodes.get(foundNodeIndex), false);
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
        getView().selectNode(foundNodes.get(foundNodeIndex), false);
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
        getView().selectNode(foundNodes.get(foundNodeIndex), false);
        updateFoundNodeInfo();
    }

    private void clearFindNodeResult() {
        this.foundNodes.clear();
        this.foundNodeIndex = 0;
        getView().getComposer().getNodeToolBarPort().hideFindResultInfo();
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
        getView().getComposer().getNodeToolBarPort().showFindResultInfo(current, this.foundNodes.size());
    }

    private void processPropertyEvent(AttributeListEvent event) {
        var sortedList = new ArrayList<>(event.attributes());
        sortedList.sort(Comparator.comparing(Attribute::name));
        var properties = new ArrayList<PropertyItem>();
        for (var attribute : sortedList) {
            var readOnly = this.readOnlyByProperty.get(attribute.field());
            var property = new PropertyItem(event.category(), attribute,
                    readOnly == null ? false : readOnly.booleanValue());
            properties.add(property);
        }
        // adding new items to the map
        this.allPropsByCategory.put(event.category(), properties);
        filterAndAddProperties(event.category(), properties);
    }

    private void updateProperies() {
        getView().clearProperties();
        clearFindPropertyResult();
        this.propsMatcher = getView().getComposer().getPropertyToolBarPort().createFindMatcher();
        // existing items from the map are filtered
        for (var entry : this.allPropsByCategory.entrySet()) {
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
                setShownProperties(cat, this.categoryExpansion.get(cat), filteredProps);
                this.foundPropertyCount += filteredProps.size();
            }
            getView().getComposer().getPropertyToolBarPort().showFindResultInfo(foundPropertyCount);
        } else {
            setShownProperties(cat, this.categoryExpansion.get(cat), props);
        }
    }

    private void setShownProperties(AttributeCategory category, boolean expanded, List<PropertyItem> items) {
        this.shownPropsByCategory.put(category, items);
        getView().addProperties(category, expanded, items);
    }

    private void clearFindPropertyResult() {
        getView().getComposer().getPropertyToolBarPort().hideFindResultInfo();
        this.foundPropertyCount = 0;
    }

    private void refreshProperties() {
        selectedFromNodeTree = true;
        clearProperties();
        tabDock.getConnector().reloadSelectedAttributes(tabDock.getSelector().getSelectedWindowUid(), null, null);
        selectedFromNodeTree = false;
        selectPreviousProperty();
    }

    private void selectPreviousProperty() {
        if (this.selectedProperty != null) {
            if (this.selectedProperty.getType() == PropertyItemType.CATEGORY) {
                getView().selectPropertyCategory(this.selectedProperty.getCategory());
            } else {
                var selProp = this.selectedProperty;
                var properties = this.shownPropsByCategory.get(this.selectedProperty.getCategory());
                if (properties != null) {
                    properties.stream()
                            // name check is last
                            .filter(item -> item.getAttribute().displayHint() == selProp.getAttribute().displayHint()
                                    && item.getAttribute().observableType() == selProp.getAttribute().observableType()
                                    && Objects.equals(item.getAttribute().name(), selProp.getAttribute().name()))
                            .findFirst()
                            .ifPresent(getView()::selectProperty);
                }
            }
        }
    }
}
