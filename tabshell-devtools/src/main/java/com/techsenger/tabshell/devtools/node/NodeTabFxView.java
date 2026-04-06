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

import atlantafx.base.theme.Tweaks;
import com.techsenger.connectorfx.ConnectorOptions;
import com.techsenger.connectorfx.LocalElement;
import com.techsenger.connectorfx.event.EventSource;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.connectorfx.scenegraph.attributes.Attribute;
import static com.techsenger.connectorfx.scenegraph.attributes.Attribute.DisplayHint.INSETS;
import static com.techsenger.connectorfx.scenegraph.attributes.Attribute.DisplayHint.NUMERIC;
import com.techsenger.connectorfx.scenegraph.attributes.AttributeCategory;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogContainerFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.devtools.ElementUtils;
import com.techsenger.tabshell.devtools.ToolBarFxView;
import com.techsenger.tabshell.devtools.ToolBarPort;
import com.techsenger.tabshell.devtools.ToolBarPresenter;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.find.FindFeature;
import com.techsenger.toolkit.fx.utils.TreeViewUtils;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class NodeTabFxView<P extends NodeTabPresenter<?, ?>> extends AbstractTabFxView<P> implements NodeTabView {

    private static final class NodeCell extends TreeCell<Element> {

        @Override
        protected void updateItem(Element item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(ElementUtils.getTitle(item));
            }
        }
    }

    private static class NodeTreeItem extends TreeItem<Element> {

        private boolean childrenCreated = false;

        NodeTreeItem(Element t) {
            super(t);
            expandedProperty().addListener((ov, oldV, newV) -> {
                if (newV) {
                    createChildren();
                }
            });
        }

        private void createChildren() {
            if (childrenCreated) {
                return;
            }
            // remove the fake child item
            getChildren().clear();
            for (var child : getValue().getChildren()) {
                var childItem = createNodeItem(child);
                if (childItem != null) {
                    getChildren().add(childItem);
                }
            }
            childrenCreated = true;
        }
    }

    private static final class CategoryTreeItem extends TreeItem<PropertyItem> {

        private CategoryTreeItem(PropertyItem item) {
            setValue(item);
        }

        public void setNotifier(BiConsumer<AttributeCategory, Boolean> notifier) {
            expandedProperty().addListener((ov, oldV, newV) -> notifier
                    .accept(getValue().getCategory(), newV));
        }
    }

    private static final class PropertyTableCell extends TreeTableCell<PropertyItem, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                var propertyItem = getTableRow().getItem();
                if (propertyItem.getType() == PropertyItemType.PROPERTY) {
                    var valueData = propertyItem.getValueData();
                    Label mainLabel = new Label(item);
                    HBox box = new HBox(Spacing.HORIZONTAL_THIRD, mainLabel);
                    if (valueData.isDefault()) {
                        mainLabel.getStyleClass().add("default-value");
                    }
                    if (propertyItem.getAttribute().cssProperty() != null) {
                        Label cssHint = new Label("CSS");
                        cssHint.getStyleClass().add("css-hint");
                        if (valueData.isDefault()) {
                            cssHint.getStyleClass().add("default-value");
                        }
                        var cssContainer = new HBox(cssHint);
                        cssContainer.getStyleClass().add("css-container");
                        cssContainer.setAlignment(Pos.TOP_LEFT);
                        box.getChildren().add(cssContainer);
                    }

                    setText(null);
                    setGraphic(box);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
        }
    }

    private static final class ValueTableCell extends TreeTableCell<PropertyItem, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                var propertyItem = getTableRow().getItem();
                var attr = propertyItem.getAttribute();
                Label mainLabel = new Label(item);
                if (propertyItem.getType() == PropertyItemType.PROPERTY) {
                    var valueData = propertyItem.getValueData();
                    if (valueData.isDefault()) {
                        mainLabel.getStyleClass().add("default-value");
                    }
                }
                setGraphic(mainLabel);
                setText(null);
            }
        }
    }

    private static final DecimalFormat FORMAT = new DecimalFormat("#.###");

    private static NodeTreeItem createNodeItem(Element element) {
        // auxiliary nodes created by connectorfx should be added
        if (element.isNodeElement() && element.getNodeProperties().id() != null
                && element.getNodeProperties().id().startsWith(ConnectorOptions.AUX_NODE_ID_PREFIX)) {
            return null;
        }
        var item = new NodeTreeItem(element);
        // element.hasChildren() has a bug
        if (element.getChildren().size() > 0) {
            // add a fake child item
            item.getChildren().add(new TreeItem(null));
        }
        return item;
    }

    private static void refreshNodeIndex(NodeTreeItem item, Map<Element, TreeItem<Element>> treeItemsByNode) {
        treeItemsByNode.put(item.getValue(), item);
        item.createChildren();
        // Recursively traverse all children
        for (var child : item.getChildren()) {
            refreshNodeIndex((NodeTreeItem) child, treeItemsByNode);
        }
    }

    protected static String getCategoryText(AttributeCategory category) {
        return Utils.toPascalCase(category.name()) + " Properties";
    }

    private static PropertyItem.PropertyValueData createValueData(PropertyItem item) {
        Attribute<?> attr = item.getAttribute();
        switch (attr.displayHint()) {
            case NUMERIC -> {
                boolean isDefault = false;
                if (attr.valueState() == Attribute.ValueState.DEFAULT) {
                    isDefault = true;
                } else if (attr.value() instanceof Number number) {
                    isDefault = isZero(number.doubleValue());
                }
                if (attr.value() instanceof Double num) {
                    if (num == Region.USE_PREF_SIZE) {
                        return new PropertyItem.PropertyValueData("USE_PREF_SIZE", true);
                    }
                    if (num == Region.USE_COMPUTED_SIZE) {
                        return new PropertyItem.PropertyValueData("USE_COMPUTED_SIZE", true);
                    }
                    if (num == Double.MIN_VALUE) {
                        return new PropertyItem.PropertyValueData("MIN_VALUE", true);
                    }
                    if (num == Double.MAX_VALUE) {
                        return new PropertyItem.PropertyValueData("MAX_VALUE", true);
                    }
                    return new PropertyItem.PropertyValueData(FORMAT.format(num), isDefault);
                }
                return new PropertyItem.PropertyValueData(String.valueOf(attr.value()), isDefault);
            }
            case INSETS -> {
                if (attr.value() instanceof Insets insets) {
                    if (isZero(insets.getTop()) && isZero(insets.getRight())
                            && isZero(insets.getBottom()) && isZero(insets.getLeft())) {
                        return new PropertyItem.PropertyValueData("Insets.EMPTY", true);
                    }
                }
                return new PropertyItem.PropertyValueData(String
                        .valueOf(attr.value()), attr.valueState() == Attribute.ValueState.DEFAULT);
            }
            default -> {
                if (attr.value() instanceof List<?> list && list.isEmpty()) {
                    return new PropertyItem.PropertyValueData("[]", true);
                }
                boolean isDefault = false;
                var strValue = String.valueOf(attr.value());
                if (item.getAttribute().displayHint() == Attribute.DisplayHint.TRANSFORMS
                        || (item.getCategory() == AttributeCategory.REFLECTIVE
                        && (attr.name().equals("localToSceneTransform")
                        || attr.name().equals("localToParentTransform")))) {
                    strValue = strValue.replaceAll("\\R", "")
                            .replaceAll("\\[\\s+", "[")
                            .replaceAll("[\t]| {3,}", "  ");
                }
                if (attr.valueState() == Attribute.ValueState.DEFAULT
                        || strValue.equals("null") || strValue.isEmpty())  {
                    isDefault = true;
                }
                return new PropertyItem.PropertyValueData(strValue, isDefault);
            }
        }
    }

    private static boolean isZero(double v) {
        return Math.abs(v) < 1e-10;
    }

    public class Composer extends AbstractTabFxView<P>.Composer implements NodeTabComposer {

        private final NodeTabFxView<P> view = NodeTabFxView.this;

        private ToolBarFxView<?> nodeToolBar;

        private ToolBarFxView<?> propertyToolBar;

        @Override
        public void compose() {
            super.compose();

            nodeToolBar = createNodeToolBar();
            nodeToolBar.getPresenter().initialize();
            getModifiableChildren().add(nodeToolBar);
            nodeBox.getChildren().add(0, nodeToolBar.getNode());

            propertyToolBar = createPropertyToolBar();
            propertyToolBar.getPresenter().initialize();
            getModifiableChildren().add(propertyToolBar);
            propertyBox.getChildren().add(0, propertyToolBar.getNode());
        }

        public ToolBarFxView<?> getNodeToolBar() {
            return nodeToolBar;
        }

        public ToolBarFxView<?> getPropertyToolBar() {
            return propertyToolBar;
        }

        @Override
        public ToolBarPort getNodeToolBarPort() {
            return nodeToolBar == null ? null : nodeToolBar.getPresenter();
        }

        @Override
        public ToolBarPort getPropertyToolBarPort() {
            return propertyToolBar == null ? null : propertyToolBar.getPresenter();
        }

        @Override
        public void addPropertyDialog(Element element, PropertyItem item, String declaringClassName,
                Consumer<String> linkOpener) {
            var dialog = createPropertyDialog(element, item, declaringClassName, linkOpener);
            dialog.getPresenter().initialize();
            dialog.getPresenter().setResizable(true);
            dialogContainer.addDialog(dialog);
        }

        protected ToolBarFxView<?> createNodeToolBar() {
            var view = new ToolBarFxView<>("NodeClass / StyleClass / ID");
            var presenter = new ToolBarPresenter<>(view, getPresenter().new NodeToolBarAwarePort(),
                    FindFeature.FIND_NEXT, FindFeature.FIND_PREVIOUS, FindFeature.MATCH_CASE);
            return view;
        }

        protected ToolBarFxView<?> createPropertyToolBar() {
            var view = new ToolBarFxView<>("Property");
            var presenter = new ToolBarPresenter<>(view, getPresenter().new PropertyToolBarAwarePort(),
                    FindFeature.MATCH_CASE);
            return view;
        }

        protected PropertyDialogFxView<?> createPropertyDialog(Element element, PropertyItem item,
                String declaringClassName, Consumer<String> linkOpener) {
            var view = new PropertyDialogFxView<>(getShell());
            var presenter = new PropertyDialogPresenter<>(view, element, item, declaringClassName,
                    linkOpener);
            return view;
        }
    }

    private final DialogContainerFxView.Composer dialogContainer;

    private final TreeView<Element> nodeTreeView = new TreeView<>();

    private final Map<Element, TreeItem<Element>> treeItemsByNode = new HashMap<>();

    private final VBox nodeBox = new VBox(nodeTreeView);

    private final TreeTableView<PropertyItem> propertyTableView = new TreeTableView<>();

    private final VBox propertyBox = new VBox(propertyTableView);

    private final SplitPane splitPane = new SplitPane(nodeBox, propertyBox);

    public NodeTabFxView(ShellFxView<?> shell, DialogContainerFxView.Composer dialogContainer) {
        super(shell);
        this.dialogContainer = dialogContainer;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void selectNode(Element node) {
        var treeItem = this.treeItemsByNode.get(node);
        if (treeItem != null) {
            nodeTreeView.getSelectionModel().select(treeItem);
            TreeViewUtils.scrollToIfNeeded(nodeTreeView, nodeTreeView.getSelectionModel().getSelectedIndex());
        }
    }

    @Override
    public void selectRoot() {
        nodeTreeView.getSelectionModel().select(0);
        TreeViewUtils.scrollToIfNeeded(nodeTreeView, nodeTreeView.getSelectionModel().getSelectedIndex());
    }

    @Override
    public void refreshNodes() {
        var selectedItem = this.nodeTreeView.getSelectionModel().getSelectedItem();
        Element selectedNode = null;
        if (selectedItem != null) {
            selectedNode = selectedItem.getValue();
        }
        updateNodeRoot();
        refreshNodeIndex();
        selectNode(selectedNode);
    }

    @Override
    public void refreshNodeIndex() {
        refreshNodeIndex((NodeTreeItem) nodeTreeView.getRoot(), treeItemsByNode);
    }

    @Override
    public void clearProperties() {
        this.propertyTableView.setRoot(null);
    }

    @Override
    public void addProperties(AttributeCategory category, boolean expanded, List<PropertyItem> props) {
        var rootTreeItem = this.propertyTableView.getRoot();
        if (rootTreeItem == null) {
            rootTreeItem = new TreeItem<>(new PropertyItem());
            this.propertyTableView.setRoot(rootTreeItem);
        }
        var cat = new CategoryTreeItem(new PropertyItem(category));
        cat.setExpanded(expanded);
        // and only now
        cat.setNotifier(getPresenter()::onCategoryExpanded);
        rootTreeItem.getChildren().add(cat);
        for (var prop : props) {
            prop.setValueData(createValueData(prop));
            var treeItem = new TreeItem<>(prop);
            cat.getChildren().add(treeItem);
        }
    }

    @Override
    protected Composer createComposer() {
        return new NodeTabFxView.Composer();
    }

    @Override
    protected void initialize() {
        super.initialize();
        Platform.runLater(() -> getPresenter().onAdded());
    }

    @Override
    protected void build() {
        super.build();
        var styles = NodeTabFxView.class.getResource("node-tab.css").toExternalForm();
        getContentBox().getStylesheets().add(styles);

        updateNodeRoot();
        nodeTreeView.setCellFactory(tv -> new NodeCell());
        nodeTreeView.getStyleClass().addAll(StyleClasses.COMPACT, StyleClasses.NO_BORDER);
        VBox.setVgrow(nodeTreeView, Priority.ALWAYS);

        TreeTableColumn<PropertyItem, String> propertyColumn = new TreeTableColumn<>("Property");
        propertyColumn.setCellValueFactory(param -> {
            // root is not shown
            var item = param.getValue().getValue();
            String text;
            if (item.getType() == PropertyItemType.CATEGORY) {
                text = getCategoryText(item.getCategory());
            } else {
                text = item.getAttribute().name();
            }
            return new SimpleStringProperty(text);
        });
        propertyColumn.setCellFactory(col -> new PropertyTableCell());

        TreeTableColumn<PropertyItem, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> {
            // root is not shown
            var item = param.getValue().getValue();
            if (item.getType() == PropertyItemType.CATEGORY) {
                return new SimpleStringProperty();
            } else {
                var valueData = item.getValueData();
                return new SimpleStringProperty(valueData.text());
            }
        });
        valueColumn.setCellFactory(col -> new ValueTableCell());
        propertyTableView.getColumns().addAll(propertyColumn, valueColumn);
        propertyTableView.getStyleClass().addAll(StyleClasses.COMPACT, Tweaks.NO_HEADER);
        propertyTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        propertyTableView.setShowRoot(false);
        propertyTableView.setPlaceholder(new Label(""));
        propertyTableView.setRowFactory(ttv -> {
            TreeTableRow<PropertyItem> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    getPresenter().onPropertyRequested(row.getItem());
                }
            });
            return row;
        });

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        getContentBox().getChildren().add(splitPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        nodeTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) ->
            getPresenter().onNodeSelected(newV == null ? null : newV.getValue()));
    }

    protected TreeTableView<PropertyItem> getPropertyTableView() {
        return propertyTableView;
    }

    protected TreeView<Element> getNodeTreeView() {
        return nodeTreeView;
    }

    protected VBox getNodeBox() {
        return nodeBox;
    }

    protected VBox getPropertyBox() {
        return propertyBox;
    }

    protected SplitPane getSplitPane() {
        return splitPane;
    }

    private void updateNodeRoot() {
        var stage = getComposer().getShell().getStage();
        var root = LocalElement.of(stage, new EventSource(null, stage.hashCode(), true));
        var rootItem = createNodeItem(root);
        nodeTreeView.setRoot(rootItem);
        getPresenter().onRootChanged(root);
    }
}
