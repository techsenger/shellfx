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

package com.techsenger.shellfx.devtools.component;

import atlantafx.base.theme.Tweaks;
import com.techsenger.connectorfx.LocalElement;
import com.techsenger.connectorfx.event.EventSource;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.patternfx.mvvm.ViewUtils;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.area.AreaView;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.core.tab.TabView;
import com.techsenger.shellfx.core.window.WindowContainerView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.core.window.WindowView;
import com.techsenger.shellfx.devtools.shared.NavigableToolBarParams;
import com.techsenger.shellfx.devtools.shared.NavigableToolBarPort;
import com.techsenger.shellfx.devtools.shared.NavigableToolBarView;
import com.techsenger.shellfx.devtools.shared.NavigableToolBarViewModel;
import com.techsenger.shellfx.devtools.shared.ToolBarParams;
import com.techsenger.shellfx.devtools.shared.ToolBarPort;
import com.techsenger.shellfx.devtools.shared.ToolBarView;
import com.techsenger.shellfx.devtools.shared.ToolBarViewModel;
import com.techsenger.shellfx.dialogs.namevalue.FullNameValueDialogPort;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogView;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogViewModel;
import com.techsenger.shellfx.material.layout.LabelHContainer;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.utils.ScrollPosition;
import com.techsenger.toolkit.fx.utils.TreeViewUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class ComponentTabView<VM extends ComponentTabViewModel<?>> extends AbstractTabView<VM> {

    private static final Logger logger = LoggerFactory.getLogger(ComponentTabView.class);

    private static final class ComponentTreeCell extends TreeCell<ComponentItem> {

        @Override
        protected void updateItem(ComponentItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getText());
            }
        }
    }

    /**
     * Converts a ComponentItem tree to JavaFX TreeItem with UUID-based cycle detection.
     *
     * @param rootItem The root component to convert
     * @return TreeItem representing the component structure
     */
    private static TreeItem<ComponentItem> convertToTreeItem(ComponentItem rootItem,
            Map<UUID, TreeItem<ComponentItem>> treeItemsByUuid) {
        if (rootItem == null) {
            return null;
        }
        Set<UUID> visitedUuids = new HashSet<>();
        visitedUuids.add(rootItem.getUuid());

        TreeItem<ComponentItem> treeRoot = new TreeItem<>(rootItem);
        putToMap(treeItemsByUuid, treeRoot);
        addChildren(treeRoot, rootItem.getChildren(), visitedUuids, treeItemsByUuid);
        return treeRoot;
    }

    /**
     * Recursively adds children to the tree with UUID-based cycle detection.
     *
     * @param parentTreeItem The parent TreeItem
     * @param children List of child components
     * @param visitedUuids Set of already processed UUIDs
     */
    private static void addChildren(TreeItem<ComponentItem> parentTreeItem, List<ComponentItem> children,
            Set<UUID> visitedUuids, Map<UUID, TreeItem<ComponentItem>> treeItemsByUuid) {
        if (children == null || children.isEmpty()) {
            return;
        }

        for (ComponentItem child : children) {
            UUID childUuid = child.getUuid();

            // Check if we've already processed this UUID (cycle detected)
            if (visitedUuids.contains(childUuid)) {
                var parentComponent = parentTreeItem.getValue();
                logger.warn("Cycle detected! Component {} with UUID {} has a child {} with UUID {} "
                        + "that was already processed", parentComponent.getName(), parentComponent.getUuid(),
                        child.getName(), child.getUuid());

                // Create a marker node to indicate the cycle
                TreeItem<ComponentItem> cycleMarker = new TreeItem<>(child);
                parentTreeItem.getChildren().add(cycleMarker);
                continue;
            }

            // Add UUID to visited before processing children
            visitedUuids.add(childUuid);
            TreeItem<ComponentItem> childTreeItem = new TreeItem<>(child);
            putToMap(treeItemsByUuid, childTreeItem);
            parentTreeItem.getChildren().add(childTreeItem);

            // Recursively process children
            addChildren(childTreeItem, child.getChildren(), visitedUuids, treeItemsByUuid);

            // Remove UUID from visited when backtracking (to correctly process other branches)
            visitedUuids.remove(childUuid);
        }
    }

    private static void putToMap(Map<UUID, TreeItem<ComponentItem>> treeItemsByUuid,
            TreeItem<ComponentItem> treeItem) {
        treeItemsByUuid.put(treeItem.getValue().getUuid(), treeItem);
    }

    private static TreeItem<InspectorItem> createRootItem(List<InspectorItem> items, ComponentTabViewModel<?> vm,
            Map<InspectorCategory, Boolean> expandedByCat) {
        var root = new TreeItem<InspectorItem>();
        TreeItem<InspectorItem> category = null;
        for (var item : items) {
            if (item.category() != null) {
                category = createCategoryItem(item, vm, expandedByCat);
                root.getChildren().add(category);
            } else {
                category.getChildren().add(new TreeItem<>(item));
            }
        }
        return root;
    }

    private static TreeItem<InspectorItem> createCategoryItem(InspectorItem item, ComponentTabViewModel<?> vm,
            Map<InspectorCategory, Boolean> expandedByCategory) {
        var treeItem = new TreeItem<>(item);
        if (item.category() != null) {
            treeItem.setExpanded(expandedByCategory.get(item.category()));
            treeItem.expandedProperty().addListener((ov, oldV, newV) -> {
                vm.onCategoryExpanded(item.category(), newV);
            });
        }
        return treeItem;
    }

    public class Composer extends AbstractTabView<VM>.Composer implements ComponentTabComposer {

        private final ComponentTabView<VM> view = ComponentTabView.this;

        private NavigableToolBarView<?> componentToolBar;

        private ToolBarView<?> inspectorToolBar;

        @Override
        public void compose() {
            super.compose();

            this.componentToolBar = createComponentToolBar();
            getModifiableChildren().add(this.componentToolBar);
            view.componentBox.getChildren().add(0, this.componentToolBar.getNode());

            this.inspectorToolBar = createInspectorToolBar();
            getModifiableChildren().add(inspectorToolBar);
            view.inspectorBox.getChildren().add(0, inspectorToolBar.getNode());
        }

        @Override
        public NavigableToolBarPort getComponentToolBarPort() {
            return this.componentToolBar == null ? null : this.componentToolBar.getViewModel();
        }

        @Override
        public ToolBarPort getInspectorToolBarPort() {
            return this.inspectorToolBar == null ? null : this.inspectorToolBar.getViewModel();
        }

        @Override
        public FullNameValueDialogPort addNameValueDialog(String nameCaption, String valueCaption,
                DialogParams params) {
            var dialog = createNameValueDialog(nameCaption, valueCaption, params);
            if (params.getWindowType() == WindowType.NESTED) {
                view.windowContainer.addDialog(dialog);
            } else {
                dialog.getStage().initOwner(getNode().getContent().getScene().getWindow());
                dialog.getStage().show();
            }
            return dialog.getViewModel();
        }

        protected NavigableToolBarView<?> getComponentToolBar() {
            return componentToolBar;
        }

        protected ToolBarView<?> getInspectorToolBar() {
            return inspectorToolBar;
        }

        protected NavigableToolBarView<?> createComponentToolBar() {
            var awarePort = getViewModel().new ComponentToolBarAwarePort();
            var params =  new NavigableToolBarParams(awarePort, "Name / UUID");
            var viewModel = new NavigableToolBarViewModel<ChildComposer>(params);
            var toolBarView = new NavigableToolBarView<>(viewModel);
            toolBarView.initialize();
            return toolBarView;
        }

        protected ToolBarView<?> createInspectorToolBar() {
            var awarePort = getViewModel().new InspectorToolBarAwarePort();
            var params = new ToolBarParams(awarePort, "Property / Class / Interface");
            var viewModel = new ToolBarViewModel<ChildComposer>(params);
            var toolBarView = new ToolBarView<>(viewModel);
            toolBarView.initialize();
            return toolBarView;
        }

        protected NameValueDialogView<?> createNameValueDialog(String nameCaption, String valueCaption,
                DialogParams params) {
            var viewModel = new NameValueDialogViewModel<>(params);
            var dialogView = new NameValueDialogView<>(viewModel, nameCaption, valueCaption);
            dialogView.initialize();
            return dialogView;
        }
    }

    private final TreeView<ComponentItem> componentTreeView = new TreeView<>();

    private final VBox componentBox = new VBox(componentTreeView);

    private final TreeTableView<InspectorItem> inspectorTableView = new TreeTableView();

    private final VBox inspectorBox = new VBox(inspectorTableView);

    private final SplitPane splitPane = new SplitPane(componentBox, inspectorBox);

    private final Map<UUID, TreeItem<ComponentItem>> treeItemsByUuid = new HashMap<>();

    private final WindowContainerView.Composer windowContainer;

    public ComponentTabView(VM viewModel, ShellView<?> shell, WindowContainerView.Composer windowContainer) {
        super(viewModel, shell);
        this.windowContainer = windowContainer;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new ComponentTabView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        componentTreeView.getStyleClass().add(StyleClasses.NO_BORDER);
        componentTreeView.setShowRoot(true);
        componentTreeView.setCellFactory(e -> new ComponentTreeCell());
        VBox.setVgrow(componentTreeView, Priority.ALWAYS);

        TreeTableColumn<InspectorItem, InspectorItem> propertyColumn = new TreeTableColumn<>("Property");
        propertyColumn.setCellValueFactory(param -> {
            // root is not shown
            var item = param.getValue().getValue();
            return new SimpleObjectProperty<>(item);
        });
        propertyColumn.setCellFactory(col -> new TreeTableCell<InspectorItem, InspectorItem>() {
            @Override
            protected void updateItem(InspectorItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (item.category() != null) {
                        setGraphic(null);
                        setText(item.name());
                    } else {
                        var label = new Label(item.name());
                        if (item.nameTooltip() != null) {
                            label.setTooltip(new Tooltip(item.nameTooltip()));
                        }
                        HBox hbox = new HBox(label);
                        setGraphic(hbox);
                        setText(null);
                    }
                }
            }
        });

        TreeTableColumn<InspectorItem, InspectorItem> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> {
            // root is not shown
            var item = param.getValue().getValue();
            return new SimpleObjectProperty<>(item);
        });
        valueColumn.setCellFactory(col -> new TreeTableCell<InspectorItem, InspectorItem>() {
            @Override
            protected void updateItem(InspectorItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (item.category() != null) {
                        setGraphic(null);
                        setText(item.values().get(0));
                    } else {

                        LabelHContainer labelContainer = new LabelHContainer();
                        if (item.values() != null) {
                            for (var i = 0; i < item.values().size(); i++) {
                                var text = item.values().get(i);
                                if (i + 1 != item.values().size()) {
                                    text += ", ";
                                }
                                var label = new Label(text);
                                Tooltip tooltip = null;
                                if (item.valueTooltips() != null) {
                                    tooltip = new Tooltip(item.valueTooltips().get(i));
                                    label.setTooltip(tooltip);
                                }
                                labelContainer.getLabels().add(label);
                            }
                        }

                        setGraphic(labelContainer);
                        setText(null);
                    }
                }
            }
        });

        inspectorTableView.getColumns().addAll(propertyColumn, valueColumn);
        inspectorTableView.getStyleClass().add(Tweaks.NO_HEADER);
        inspectorTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        inspectorTableView.setShowRoot(false);
        inspectorTableView.setPlaceholder(new Label(""));
        inspectorTableView.setRowFactory(ttv -> {
            TreeTableRow<InspectorItem> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    InspectorItem parent = null;
                    var rowIndex = row.getIndex();
                    if (rowIndex != 0) {
                        TreeItem<InspectorItem> prevItem = inspectorTableView.getTreeItem(rowIndex - 1);
                        parent = prevItem.getValue();
                    }
                    getViewModel().onInspectorItemRequested(parent, row.getItem());
                }
            });
            return row;
        });
        VBox.setVgrow(inspectorTableView, Priority.ALWAYS);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        getContentBox().getChildren().add(splitPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(getViewModel().rootComponentProperty(), (ov, oldV, newV) -> {
            rebuildTree(newV);
        });
        getViewModel().selectComponentByUuidSource().addListener(this::updateSelectComponent);
        getViewModel().selectComponentByElementSource().addListener(this::updateSelectComponent);
        getViewModel().selectRootComponentSource().addListener((v) -> updateSelectRootComponent());
        getViewModel().refreshInspectorSource().addListener(
                (data) -> updateRefreshInspector(data.items(), data.expandedByCategory()));
        componentTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                var component = newV.getValue();
                var jfxComponent = (JfxComponentItem) component;
                var fxView = jfxComponent.getView();
                Element element;
                if (fxView instanceof WindowView<?> window) {
                    if (window.getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
                        var stage = window.getStage();
                        element = LocalElement.of(stage, new EventSource(null, stage.hashCode(), true));
                    } else {
                        element = LocalElement.of(window.getNode());
                    }
                } else if (fxView instanceof TabView<?> tab) {
                    element = LocalElement.of(tab.getNode().getContent());
                } else if (fxView instanceof AreaView<?> area) {
                    element = LocalElement.of(area.getNode());
                } else {
                    throw new AssertionError("Unknown type of the component");
                }
                Class<? extends ParentView.Composer> fxComposerClass = fxView.getComposer().getClass();
                getViewModel().onComponentSelected(component, fxView.getClass(), fxComposerClass,
                        fxView.getViewModel(), element);
            } else {
                getViewModel().onComponentSelected(null, null, null, null, null);
            }
        });
    }

    protected TreeView<ComponentItem> getComponentTreeView() {
        return componentTreeView;
    }

    protected VBox getComponentBox() {
        return componentBox;
    }

    protected TreeTableView<InspectorItem> getInspectorTableView() {
        return inspectorTableView;
    }

    protected VBox getInspectorBox() {
        return inspectorBox;
    }

    protected SplitPane getSplitPane() {
        return splitPane;
    }

    private void updateRefreshInspector(List<InspectorItem> items, Map<InspectorCategory, Boolean> expandedByCategory) {
        if (!items.isEmpty()) {
            var root = createRootItem(items, getViewModel(), expandedByCategory);
            inspectorTableView.setRoot(root);
        } else {
            inspectorTableView.setRoot(null);
        }
    }

    private void updateSelectComponent(UUID uuid) {
        var treeItem = this.treeItemsByUuid.get(uuid);
        if (treeItem != null) {
            for (var item = treeItem; item != null; item = item.getParent()) {
                item.setExpanded(true);
            }
            componentTreeView.getSelectionModel().select(treeItem);
            TreeViewUtils.scrollToIfNeeded(componentTreeView,
                    componentTreeView.getSelectionModel().getSelectedIndex(), ScrollPosition.CENTER);
        }
    }

    private void updateSelectComponent(Element n) {
        var node = ((LocalElement) n).unwrap();
        var component = ViewUtils.findView(node, ParentView.class);
        if (component != null) {
            updateSelectComponent(component.getViewModel().getDescriptor().getUuid());
        }
    }

    private void updateSelectRootComponent() {
        componentTreeView.getSelectionModel().select(0);
        TreeViewUtils.scrollToIfNeeded(componentTreeView,
                componentTreeView.getSelectionModel().getSelectedIndex(), ScrollPosition.CENTER);
    }

    private void rebuildTree(ComponentItem rootItem) {
        this.treeItemsByUuid.clear();
        var rootTreeItem = convertToTreeItem(rootItem, this.treeItemsByUuid);
        // do not use the same root multiple times, as it causes a bug with node expansion
        componentTreeView.setRoot(rootTreeItem);
    }
}
