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

import com.techsenger.connectorfx.LocalElement;
import com.techsenger.connectorfx.event.EventSource;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.devtools.ToolBarFxView;
import com.techsenger.tabshell.devtools.ToolBarPort;
import com.techsenger.tabshell.devtools.ToolBarPresenter;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.find.FindFeature;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

    public class Composer extends AbstractTabFxView<P>.Composer implements ComponentTabComposer {

        private final ComponentTabFxView<P> view = ComponentTabFxView.this;

        @Override
        public void compose() {
            super.compose();

            view.toolBar = createToolBar();
            view.toolBar.getPresenter().initialize();
            view.getModifiableChildren().add(view.toolBar);
            view.getContentBox().getChildren().add(0, view.toolBar.getNode());
        }

        protected ToolBarFxView<?> createToolBar() {
            var view = new ToolBarFxView<>("Name / UUID");
            var presenter = new ToolBarPresenter<>(view, getPresenter().new ToolBarAwarePortImpl(),
                    FindFeature.MATCH_CASE, FindFeature.FIND_NEXT, FindFeature.FIND_PREVIOUS);
            return view;
        }

        @Override
        public ToolBarPort getToolBar() {
            return view.toolBar.getPresenter().getPort();
        }
    }

    private final TreeView<ComponentItem> treeView = new TreeView<>();

    private ToolBarFxView<?> toolBar;

    @Override
    public void requestFocus() {

    }

    @Override
    public void setRootItem(ComponentItem item) {
        rebuildTree(item);
    }

    @Override
    public void selectItem(List<Integer> path) {
        TreeItem<ComponentItem> treeItem = null;
        for (var index : path) {
            if (treeItem == null) {
                treeItem = treeView.getRoot();
            } else {
                treeItem = treeItem.getChildren().get(index);
            }
            treeItem.setExpanded(true);
        }
        treeView.getSelectionModel().select(treeItem);
        treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex());
    }

    @Override
    public ComponentItem getSelectedItem() {
        var item = treeView.getSelectionModel().getSelectedItem();
        if (item != null) {
            return item.getValue();
        } else {
            return null;
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
    protected void build() {
        super.build();
        setTitle("Components");
        setClosable(false);
        treeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, StyleClasses.NO_BORDER);
        treeView.setShowRoot(true);
        treeView.setCellFactory(e -> new ComponentTreeCell());

        VBox.setVgrow(treeView, Priority.ALWAYS);
        getContentBox().getChildren().add(treeView);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        treeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
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
                getPresenter().handleComponentSelected(element);
            }
        });
    }

    protected TreeView<ComponentItem> getTreeView() {
        return treeView;
    }

    private void rebuildTree(ComponentItem rootItem) {
        var rootTreeItem = convertToTreeItem(rootItem);
        // do not use the same root multiple times, as it causes a bug with node expansion
        treeView.setRoot(rootTreeItem);
    }
}
