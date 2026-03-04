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

package com.techsenger.tabshell.devtools.component;

import atlantafx.base.theme.Tweaks;
import com.techsenger.connectorfx.LocalElement;
import com.techsenger.connectorfx.event.EventSource;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.patternfx.mvp.ComposeParameters;
import com.techsenger.patternfx.mvp.ParentComposer;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.devtools.ToolBarFxView;
import com.techsenger.tabshell.devtools.ToolBarPort;
import com.techsenger.tabshell.devtools.ToolBarPresenter;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.find.FindFeature;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class ComponentTabFxView<P extends ComponentTabPresenter<?, ?>> extends AbstractTabFxView<P>
        implements ComponentTabView {

    private static final Logger logger = LoggerFactory.getLogger(ComponentTabFxView.class);

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
    private static TreeItem<ComponentItem> convertToTreeItem(ComponentItem rootItem) {
        if (rootItem == null) {
            return null;
        }
        Set<UUID> visitedUuids = new HashSet<>();
        visitedUuids.add(rootItem.getUuid());

        TreeItem<ComponentItem> treeRoot = new TreeItem<>(rootItem);
        addChildren(treeRoot, rootItem.getChildren(), visitedUuids);
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
            Set<UUID> visitedUuids) {
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
            parentTreeItem.getChildren().add(childTreeItem);

            // Recursively process children
            addChildren(childTreeItem, child.getChildren(), visitedUuids);

            // Remove UUID from visited when backtracking (to correctly process other branches)
            visitedUuids.remove(childUuid);
        }
    }

    private static TreeItem<InspectorItem> createRootItem(List<InspectorItem> items, ComponentTabPresenter<?, ?> p,
            Map<InspectorCategory, Boolean> expandedByCat) {
        var root = new TreeItem<InspectorItem>();
        TreeItem<InspectorItem> category = null;
        for (var item : items) {
            if (item.category() != null) {
                category = createCategoryItem(item, p, expandedByCat);
                root.getChildren().add(category);
            } else {
                category.getChildren().add(new TreeItem<>(item));
            }
        }
        return root;
    }

    private static TreeItem<InspectorItem> createCategoryItem(InspectorItem item, ComponentTabPresenter<?, ?> p,
            Map<InspectorCategory, Boolean> expandedByCategory) {
        var treeItem = new TreeItem<>(item);
        if (item.category() != null) {
            treeItem.setExpanded(expandedByCategory.get(item.category()));
            treeItem.expandedProperty().addListener((ov, oldV, newV) -> {
                p.onCategoryExpanded(item.category(), newV);
            });
        }
        return treeItem;
    }

    public class Composer extends AbstractTabFxView<P>.Composer implements ComponentTabComposer {

        private final ComponentTabFxView<P> view = ComponentTabFxView.this;

        @Override
        public void compose(ComposeParameters params) {
            super.compose(params);

            view.componentToolBar = createComponentToolBar();
            view.componentToolBar.getPresenter().initialize();
            view.getModifiableChildren().add(view.componentToolBar);
            view.componentBox.getChildren().add(0, view.componentToolBar.getNode());

            view.inspectorToolBar = createInspectorToolBar();
            view.inspectorToolBar.getPresenter().initialize();
            view.getModifiableChildren().add(view.inspectorToolBar);
            view.inspectorBox.getChildren().add(0, view.inspectorToolBar.getNode());
        }

        @Override
        public ToolBarPort getComponentToolBar() {
            return view.componentToolBar.getPresenter();
        }

        @Override
        public ToolBarPort getInspectorToolBar() {
            return view.inspectorToolBar.getPresenter();
        }

        protected ToolBarFxView<?> createComponentToolBar() {
            var view = new ToolBarFxView<>("Name / UUID");
            var presenter = new ToolBarPresenter<>(view, getPresenter().new ComponentToolBarAwarePort(),
                    FindFeature.MATCH_CASE, FindFeature.FIND_NEXT, FindFeature.FIND_PREVIOUS);
            return view;
        }

        protected ToolBarFxView<?> createInspectorToolBar() {
            var view = new ToolBarFxView<>("Property / Class / Interface");
            var presenter = new ToolBarPresenter<>(view, getPresenter().new InspectorToolBarAwarePort(),
                    FindFeature.MATCH_CASE);
            return view;
        }
    }

    private final TreeView<ComponentItem> componentTreeView = new TreeView<>();

    private final VBox componentBox = new VBox(componentTreeView);

    private final TreeTableView<InspectorItem> inspectorTableView = new TreeTableView();

    private final VBox inspectorBox = new VBox(inspectorTableView);

    private final SplitPane splitPane = new SplitPane(componentBox, inspectorBox);

    private ToolBarFxView<?> componentToolBar;

    private ToolBarFxView<?> inspectorToolBar;

    public ComponentTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void setRootComponent(ComponentItem item) {
        rebuildTree(item);
    }

    @Override
    public void selectComponent(List<Integer> path) {
        TreeItem<ComponentItem> treeItem = null;
        for (var index : path) {
            if (treeItem == null) {
                treeItem = componentTreeView.getRoot();
            } else {
                treeItem = treeItem.getChildren().get(index);
            }
            treeItem.setExpanded(true);
        }
        componentTreeView.getSelectionModel().select(treeItem);
        componentTreeView.scrollTo(componentTreeView.getSelectionModel().getSelectedIndex());
    }

    @Override
    public void updateInspector(List<InspectorItem> items, Map<InspectorCategory, Boolean> expandedByCategory) {
        if (!items.isEmpty()) {
            var root = createRootItem(items, getPresenter(), expandedByCategory);
            inspectorTableView.setRoot(root);
        } else {
            inspectorTableView.setRoot(null);
        }
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new ComponentTabFxView.Composer();
    }

    @Override
    protected void initialize() {
        super.initialize();
        Platform.runLater(() -> getPresenter().onAdded());
    }

    @Override
    protected void build() {
        super.build();
        componentTreeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, StyleClasses.NO_BORDER);
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
                        HBox hbox = new HBox();
                        hbox.setSpacing(SizeConstants.HALF_INSET);
                        if (item.values() != null) {
                            for (var i = 0; i < item.values().size(); i++) {
                                var value = item.values().get(i);
                                Node node;
                                if (i + 1 < item.values().size()) {
                                    node = new Text(value + ",");
                                    if (item.valueTooltips() != null) {
                                        Tooltip.install(node, new Tooltip(item.valueTooltips().get(i)));
                                    }
                                } else {
                                    node = new Label(value);
                                    if (item.valueTooltips() != null) {
                                        ((Label) node).setTooltip(new Tooltip(item.valueTooltips().get(i)));
                                    }
                                }
                                hbox.getChildren().add(node);
                            }
                        }
                        setGraphic(hbox);
                        setText(null);
                    }
                }
            }
        });

        inspectorTableView.getColumns().addAll(propertyColumn, valueColumn);
        inspectorTableView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, Tweaks.NO_HEADER);
        inspectorTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        inspectorTableView.setShowRoot(false);
        inspectorTableView.setPlaceholder(new Label(""));
        VBox.setVgrow(inspectorTableView, Priority.ALWAYS);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        getContentBox().getChildren().add(splitPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        componentTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                var component = newV.getValue();
                var jfxComponent = (JfxComponentItem) component;
                var fxView = jfxComponent.getView();
                Element element;
                if (fxView instanceof ShellFxView<?> shell) {
                    var stage = shell.getStage();
                    element = LocalElement.of(stage, new EventSource(null, stage.hashCode(), true));
                } else if (fxView instanceof TabFxView<?> tab) {
                    element = LocalElement.of(tab.getNode().getContent());
                } else if (fxView instanceof AreaFxView<?> area) {
                    element = LocalElement.of(area.getNode());
                } else {
                    throw new AssertionError("Unknown type of the component");
                }
                Class<? extends ParentComposer> fxComposerClass = null;
                if (fxView instanceof ParentFxView<?> pfxv) {
                    fxComposerClass = pfxv.getComposer().getClass();
                }
                getPresenter().onComponentSelected(component, fxView.getClass(), fxComposerClass, fxView.getPresenter(),
                        element);
            } else {
                getPresenter().onComponentSelected(null, null, null, null, null);
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

    protected ToolBarFxView<?> getComponentToolBar() {
        return componentToolBar;
    }

    protected ToolBarFxView<?> getInspectorToolBar() {
        return inspectorToolBar;
    }

    private void rebuildTree(ComponentItem rootItem) {
        var rootTreeItem = convertToTreeItem(rootItem);
        // do not use the same root multiple times, as it causes a bug with node expansion
        componentTreeView.setRoot(rootTreeItem);
    }
}
