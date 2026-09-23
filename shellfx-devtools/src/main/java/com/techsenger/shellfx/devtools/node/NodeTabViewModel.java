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
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.tab.AbstractTabViewModel;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.devtools.DevToolsHostType;
import com.techsenger.shellfx.devtools.DevToolsTabDockPort;
import com.techsenger.shellfx.devtools.shared.IndexedFindResult;
import com.techsenger.shellfx.devtools.shared.NavigableToolBarAwarePort;
import com.techsenger.shellfx.devtools.shared.ToolBarAwarePort;
import com.techsenger.shellfx.devtools.shared.TotalFindResult;
import com.techsenger.shellfx.shared.find.FindResult;
import com.techsenger.shellfx.shared.find.NavigableFindResult;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class NodeTabViewModel<C extends NodeTabComposer> extends AbstractTabViewModel<C> implements FullNodeTabPort {

    private static final Logger logger = LoggerFactory.getLogger(NodeTabViewModel.class);

    record SelectNodeCommand(Element node, boolean afterDataUpdate) { }

    record AddPropertiesCommand(AttributeCategory category, boolean expanded, List<PropertyItem> items) { }

    protected class NodeToolBarAwarePort implements NavigableToolBarAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            // find result is refreshed by the runFind() triggered right after this returns
        }

        @Override
        public void onRefresh() {
            refreshNodesSource.next(null);
        }

        @Override
        public CompletableFuture<NavigableFindResult> onFind() {
            return CompletableFuture.completedFuture(findNode());
        }

        @Override
        public void onFindCleared() {
            clearFindNodeResult();
        }
    }

    /**
     * UpdateProperites -> filterAndAdd.
     */
    protected class PropertyToolBarAwarePort implements ToolBarAwarePort {

        @Override
        public void onMatchCase(boolean selected) {
            // find result is refreshed by the runFind() triggered right after this returns
        }

        @Override
        public void onRefresh() {
            refreshProperties();
        }

        @Override
        public CompletableFuture<FindResult> onFind() {
            return CompletableFuture.completedFuture(updateProperies());
        }

        @Override
        public void onFindCleared() {
            updateProperies();
        }
    }

    private final ObservableSource<Integer> selectWindowSource = new SimpleObservableSource<>();

    private final ObservableSource<Void> selectRootSource = new SimpleObservableSource<>();

    private final ObservableSource<Void> refreshNodesSource = new SimpleObservableSource<>();

    private final ObservableSource<SelectNodeCommand> selectNodeSource = new SimpleObservableSource<>();

    private final ObservableSource<Void> refreshNodeIndexSource = new SimpleObservableSource<>();

    private final ObservableSource<Void> focusPropertiesSource = new SimpleObservableSource<>();

    private final ObservableSource<Void> clearPropertiesSource = new SimpleObservableSource<>();

    private final ObservableSource<AddPropertiesCommand> addPropertiesSource = new SimpleObservableSource<>();

    private final ObservableSource<AttributeCategory> selectPropertyCategorySource = new SimpleObservableSource<>();

    private final ObservableSource<PropertyItem> selectPropertySource = new SimpleObservableSource<>();

    private final ReadOnlyObjectWrapper<Element> rootNode = new ReadOnlyObjectWrapper<>();

    private final ObjectProperty<Element> nodeSelection = new SimpleObjectProperty<>();

    private final ObjectProperty<PropertyItem> propertySelection = new SimpleObjectProperty<>();

    /**
     * ObservableType in Attribute is incorrect because its value is determined by the property instance
     * rather than the property method's return type. However, we cannot fix this in the connector because
     * using reflection there would significantly slow down node event firing.
     */
    private final Map<String, Boolean> readOnlyByProperty = new HashMap<>();

    private final DevToolsTabDockPort tabDock;

    private final List<Element> foundNodes = new ArrayList<>();

    /**
     * All properties, including filtered out properties.
     */
    private final Map<AttributeCategory, List<PropertyItem>> allPropsByCategory = new LinkedHashMap<>();

    /**
     * The properties that are currently shown.
     */
    private final Map<AttributeCategory, List<PropertyItem>> shownPropsByCategory = new LinkedHashMap<>();

    /**
     * Attribute events come after node events, so we need to save them.
     */
    private List<AttributeListEvent> savedAttributeEvents = new ArrayList<>();

    private boolean nodeIndexCreated = false;

    private int foundNodeIndex = 0;

    private Matcher propsMatcher;

    private int foundPropertyCount = 0;

    private Consumer<String> linkOpener = (url) -> getComposer().getShellPort().getContext()
            .getHostServices().showDocument(url);

    private Element selectedNode;

    private Map<AttributeCategory, Boolean> categoryExpansion;

    private boolean selectedFromNodeTree;

    private PropertyItem selectedProperty;

    public NodeTabViewModel(NodeTabParams params) {
        super(params);
        this.tabDock = params.getTabDock();
        this.nodeSelection.addListener((ov, oldV, newV) -> onNodeSelected(newV));
        // when properties are cleared, the selection is null and should be ignored
        this.propertySelection.addListener((ov, oldV, newV) -> {
            if (newV != null) {
                this.selectedProperty = newV;
            }
        });
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
        return rootNode.get();
    }

    @Override
    public ReadOnlyObjectProperty<Element> rootNodeProperty() {
        return rootNode.getReadOnlyProperty();
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
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Nodes");
        setClosable(false);
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
                selectWindowSource.next(newWindowUid);
                this.nodeIndexCreated = false;
                selectRootSource.next(null);
                refreshProperties();
                return;
            }
            if (newElement == null) {
                selectRootSource.next(null);
            }
            if (Objects.equals(oldElement, newElement)) {
                return;
            }
            this.selectedNode = newElement; // so the onNodeSelected method won't complete
            if (this.selectedNode != null) {
                createNodeIndex();
                if (!this.selectedFromNodeTree) {
                    selectNodeSource.next(new SelectNodeCommand(this.selectedNode, true));
                }
                for (var events : this.savedAttributeEvents) { // adding saved events
                    processPropertyEvent(events);
                }
                selectPreviousProperty();
            }
            this.savedAttributeEvents.clear();
        });
    }

    @Override
    protected void applyPersistentState() {
        super.applyPersistentState();
        var catExpansion = new HashMap<AttributeCategory, Boolean>();
        Arrays.stream(AttributeCategory.values()).forEach(c -> catExpansion.put(c, Boolean.FALSE));
        setCategoryExpansion(catExpansion);
    }

    protected void onCategoryExpanded(AttributeCategory category, boolean expanded) {
        this.categoryExpansion.put(category, expanded);
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
        var dialog = getComposer().openViewerDialog(params);
        dialog.setOnClosed(() -> focusPropertiesSource.next(null));
    }

    protected void onEditProperty(EditPropertyTask<?> task) {
        WindowType windowType = WindowType.NESTED;
        if (this.tabDock.getHostType() == DevToolsHostType.WINDOW) {
            windowType = WindowType.TOP_LEVEL;
        }
        var params = new EditorDialogParams(windowType, getShellContext().getSettings().getAppearance(),
                task, this.tabDock.getHistoryManager());
        var dialog = getComposer().openEditorDialog(params);
        dialog.setOnClosed(() -> {
            if (dialog.isPropertyUpdated()) {
                refreshProperties();
            }
            focusPropertiesSource.next(null);
        });
    }

    protected DevToolsTabDockPort getTabDock() {
        return tabDock;
    }

    ObservableSource<Integer> getSelectWindowSource() {
        return selectWindowSource;
    }

    ObservableSource<Void> getSelectRootSource() {
        return selectRootSource;
    }

    ObservableSource<Void> getRefreshNodesSource() {
        return refreshNodesSource;
    }

    ObservableSource<SelectNodeCommand> getSelectNodeSource() {
        return selectNodeSource;
    }

    ObservableSource<Void> getRefreshNodeIndexSource() {
        return refreshNodeIndexSource;
    }

    ObservableSource<Void> getFocusPropertiesSource() {
        return focusPropertiesSource;
    }

    ObservableSource<Void> getClearPropertiesSource() {
        return clearPropertiesSource;
    }

    ObservableSource<AddPropertiesCommand> getAddPropertiesSource() {
        return addPropertiesSource;
    }

    ObservableSource<AttributeCategory> getSelectPropertyCategorySource() {
        return selectPropertyCategorySource;
    }

    ObservableSource<PropertyItem> getSelectPropertySource() {
        return selectPropertySource;
    }

    Map<String, Boolean> getReadOnlyByProperty() {
        return readOnlyByProperty;
    }

    /**
     * Framework contract: written directly by the View to report the currently connected root element, intended
     * exclusively for {@code NodeTabFxView}. Direct invocation by user code results in undefined behavior.
     */
    ReadOnlyObjectWrapper<Element> rootNodeWrapper() {
        return rootNode;
    }

    /**
     * Framework contract: written directly by the View to report the node tree's current selection, intended
     * exclusively for {@code NodeTabFxView}. Direct invocation by user code results in undefined behavior.
     */
    ObjectProperty<Element> nodeSelectionProperty() {
        return nodeSelection;
    }

    /**
     * Framework contract: written directly by the View to report the property table's current selection, intended
     * exclusively for {@code NodeTabFxView}. Direct invocation by user code results in undefined behavior.
     */
    ObjectProperty<PropertyItem> propertySelectionProperty() {
        return propertySelection;
    }

    private void onNodeSelected(Element node) {
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

    private void clearProperties() {
        this.allPropsByCategory.clear();
        this.shownPropsByCategory.clear();
        clearFindPropertyResult();
        clearPropertiesSource.next(null);
    }

    private NavigableFindResult findNode() {
        createNodeIndex();
        clearFindNodeResult();
        var matcher = getComposer().getNodeToolBarPort().createFindMatcher();
        if (matcher != null) {
            findNode(getRootNode(), matcher);
            if (!foundNodes.isEmpty()) {
                selectNodeSource.next(new SelectNodeCommand(foundNodes.get(foundNodeIndex), false));
            }
        }
        return buildFoundNodeInfo();
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
        selectNodeSource.next(new SelectNodeCommand(foundNodes.get(foundNodeIndex), false));
    }

    private void findPreviousNode() {
        if (this.foundNodes.isEmpty()) {
            return;
        }
        this.foundNodeIndex--;
        if (this.foundNodeIndex < 0) {
            this.foundNodeIndex = this.foundNodes.size() - 1;
        }
        selectNodeSource.next(new SelectNodeCommand(foundNodes.get(foundNodeIndex), false));
    }

    private void clearFindNodeResult() {
        this.foundNodes.clear();
        this.foundNodeIndex = 0;
    }

    private void createNodeIndex() {
        if (this.nodeIndexCreated) {
            return;
        }
        refreshNodeIndexSource.next(null);
        this.nodeIndexCreated = true;
    }

    private NavigableFindResult buildFoundNodeInfo() {
        return new IndexedFindResult(() -> this.foundNodes.size(),
                () -> this.foundNodes.isEmpty() ? -1 : this.foundNodeIndex, this::findNextNode, this::findPreviousNode);
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

    private FindResult updateProperies() {
        clearPropertiesSource.next(null);
        clearFindPropertyResult();
        this.propsMatcher = getComposer().getPropertyToolBarPort().createFindMatcher();
        // existing items from the map are filtered
        for (var entry : this.allPropsByCategory.entrySet()) {
            filterAndAddProperties(entry.getKey(), entry.getValue());
        }
        return this.propsMatcher != null ? new TotalFindResult(foundPropertyCount) : null;
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
        } else {
            setShownProperties(cat, this.categoryExpansion.get(cat), props);
        }
    }

    private void setShownProperties(AttributeCategory category, boolean expanded, List<PropertyItem> items) {
        this.shownPropsByCategory.put(category, items);
        addPropertiesSource.next(new AddPropertiesCommand(category, expanded, items));
    }

    private void clearFindPropertyResult() {
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
        var selProp = this.selectedProperty;
        if (selProp != null) {
            if (selProp.getType() == PropertyItemType.CATEGORY) {
                selectPropertyCategorySource.next(selProp.getCategory());
            } else {
                var properties = this.shownPropsByCategory.get(selProp.getCategory());
                if (properties != null) {
                    properties.stream()
                            // name check is last
                            .filter(item -> item.getAttribute().displayHint() == selProp.getAttribute().displayHint()
                                    && item.getAttribute().observableType() == selProp.getAttribute().observableType()
                                    && Objects.equals(item.getAttribute().name(), selProp.getAttribute().name()))
                            .findFirst()
                            .ifPresent(selectPropertySource::next);
                }
            }
        }
    }
}
