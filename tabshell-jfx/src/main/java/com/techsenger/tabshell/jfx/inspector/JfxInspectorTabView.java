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

package com.techsenger.tabshell.jfx.inspector;

import com.techsenger.tabshell.material.style.SizeConstants;
import devtoolsfx.connector.LocalElement;
import java.text.DecimalFormat;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class JfxInspectorTabView<T extends JfxInspectorTabViewModel<?>, S extends JfxInspectorTabComponent<?>>
        extends AbstractTabView<T, S> {

    private static PropertyValue createValue(PropertyItem item) {
        Attribute<?> attr = item.getAttribute();
        switch (attr.displayHint()) {
            case NUMERIC -> {
                boolean isDefault = false;
                if (attr.valueState() == ValueState.DEFAULT) {
                    isDefault = true;
                } else if (attr.value() instanceof Number number) {
                    isDefault = isZero(number.doubleValue());
                }
                if (attr.value() instanceof Double num) {
                    if (num == Region.USE_PREF_SIZE) {
                        return new PropertyValue("USE_PREF_SIZE", true);
                    }
                    if (num == Region.USE_COMPUTED_SIZE) {
                        return new PropertyValue("USE_COMPUTED_SIZE", true);
                    }
                    if (num == Double.MIN_VALUE) {
                        return new PropertyValue("MIN_VALUE", true);
                    }
                    if (num == Double.MAX_VALUE) {
                        return new PropertyValue("MAX_VALUE", true);
                    }
                    return new PropertyValue(FORMAT.format(num), isDefault);
                }
                return new PropertyValue(String.valueOf(attr.value()), isDefault);
            }
            case INSETS -> {
                if (attr.value() instanceof Insets insets) {
                    if (isZero(insets.getTop()) && isZero(insets.getRight())
                            && isZero(insets.getBottom()) && isZero(insets.getLeft())) {
                        return new PropertyValue("Insets.EMPTY", true);
                    }
                }
                return new PropertyValue(String.valueOf(attr.value()), attr.valueState() == ValueState.DEFAULT);
            }
            default -> {
                if (attr.value() instanceof List<?> list && list.isEmpty()) {
                    return new PropertyValue("[]", true);
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
                if (attr.valueState() == ValueState.DEFAULT || strValue.equals("null") || strValue.isEmpty())  {
                    isDefault = true;
                }
                return new PropertyValue(strValue, isDefault);
            }
        }
    }

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

    private static TreeItem<Element> createNodeItem(Element element) {
        var item = new TreeItem<>(element);
        // element.hasChildren() has a bug
        if (element.getChildren().size() > 0) {
            item.getChildren().add(new TreeItem(null));
            item.expandedProperty().addListener((ov, oldV, newV) -> {
                var children = item.getChildren();
                if (newV && children.size() == 1 && children.get(0).getValue() == null) {
                    item.getChildren().clear();
                    for (var child : element.getChildren()) {
                        var childItem = createNodeItem(child);
                        item.getChildren().add(childItem);
                    }
                }
            });
        }
        return item;
    }

    private static final class RootTreeItem extends TreeItem<PropertyItem> {

        private RootTreeItem(PropertyItem rootAttribute) {
            setValue(rootAttribute);
            rootAttribute.getChildren().addListener((ListChangeListener<PropertyItem>) (e) -> {
                while (e.next()) {
                    if (e.wasAdded()) {
                        for (var added : e.getAddedSubList()) {
                            var c = new CategoryTreeItem(added);
                            getChildren().add(c);
                        }
                    }
                    if (e.wasRemoved()) {
                        for (var c: getChildren()) {
                            ((CategoryTreeItem) c).dispose();
                        }
                        getChildren().clear();
                    }
                }
            });
        }
    }

    private static final class CategoryTreeItem extends TreeItem<PropertyItem> {

        private ListChangeListener<PropertyItem> listener =  (e) -> {
            while (e.next()) {
                if (e.wasAdded()) {
                    for (var added : e.getAddedSubList()) {
                        added.setValue(createValue(added));
                        var c = new TreeItem<PropertyItem>(added);
                        getChildren().add(c);
                    }
                }
                if (e.wasRemoved()) {
                    getChildren().clear();
                }
            }
        };

        private CategoryTreeItem(PropertyItem categoryAttribute) {
            setValue(categoryAttribute);
            this.expandedProperty().bindBidirectional(categoryAttribute.expandedProperty());
            categoryAttribute.getChildren().addListener(listener);
        }

        private void dispose() {
            this.expandedProperty().unbindBidirectional(getValue().expandedProperty());
            getValue().getChildren().removeListener(listener);
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
                if (!propertyItem.isCategory()) {
                    Label mainLabel = new Label(item);
                    HBox box = new HBox(SizeConstants.THIRD_INSET, mainLabel);
                    var attr = propertyItem.getAttribute();
                    if (attr != null) {
                        if (propertyItem.getValue().isDefault()) {
                            mainLabel.getStyleClass().add("default-value");
                        }
                        if (propertyItem.getAttribute().cssProperty() != null) {
                            Label cssHint = new Label("CSS");
                            cssHint.getStyleClass().add("css-hint");
                            if (propertyItem.getValue().isDefault()) {
                                cssHint.getStyleClass().add("default-value");
                            }
                            var cssContainer = new HBox(cssHint);
                            cssContainer.getStyleClass().add("css-container");
                            cssContainer.setAlignment(Pos.TOP_LEFT);
                            box.getChildren().add(cssContainer);
                        }
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
                if (attr != null && propertyItem.getValue().isDefault()) {
                    mainLabel.getStyleClass().add("default-value");
                }
                setGraphic(mainLabel);
                setText(null);
            }
        }
    }

    private static final DecimalFormat FORMAT = new DecimalFormat("#.###");

    private static boolean isZero(double v) {
        return Math.abs(v) < 1e-10;
    }

    private final TreeView<Element> nodeTreeView = new TreeView<>();

    private final VBox nodeBox = new VBox();

    private final TreeTableView<PropertyItem> propertyTableView = new TreeTableView<>();

    private final VBox propertyBox = new VBox();

    private final SplitPane splitPane = new SplitPane(nodeBox, propertyBox);

    public JfxInspectorTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        var stage = getComponent().getShellTab().getShell().getView().getStage();
        var root = LocalElement.of(stage, new EventSource(null, stage.hashCode(), true));
        var rootItem = createNodeItem(root);
        nodeTreeView.setRoot(rootItem);

        var styles = JfxInspectorTabView.class.getResource("inspector-tab.css").toExternalForm();
        getContentBox().getStylesheets().add(styles);

        nodeTreeView.setCellFactory(tv -> new NodeCell());
        nodeTreeView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, StyleClasses.NO_BORDER);
        VBox.setVgrow(nodeTreeView, Priority.ALWAYS);
        var nodeSearchPanel = getComponent().getNodeSearchPanel().getView();
        nodeSearchPanel.getNode().getItems().addAll(nodeSearchPanel.getSearchField(),
                nodeSearchPanel.getMatchCaseButton(), nodeSearchPanel.getRefreshButton());
        nodeBox.getChildren().addAll(nodeSearchPanel.getNode(), nodeTreeView);

        TreeTableColumn<PropertyItem, String> propertyColumn = new TreeTableColumn<>("Property");
        propertyColumn.setCellValueFactory(param -> {
            // root is not shown
            var item = param.getValue().getValue();
            String text;
            if (item.isCategory()) {
                text = JfxInspectorTabViewModel.getCategoryText(item.getCategory());
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
            if (item.isCategory()) {
                return new SimpleStringProperty();
            } else {
                return new SimpleStringProperty(item.getValue().text());
            }
        });
        valueColumn.setCellFactory(col -> new ValueTableCell());
        propertyTableView.getColumns().addAll(propertyColumn, valueColumn);
        propertyTableView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE, "no-header");
        propertyTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        propertyTableView.setShowRoot(false);
        propertyTableView.setRoot(new RootTreeItem(getViewModel().getRootInfo()));
        propertyTableView.setPlaceholder(new Label(""));
        VBox.setVgrow(propertyTableView, Priority.ALWAYS);
        var propertySearchPanel = getComponent().getPropertySearchPanel().getView();
        propertySearchPanel.getNode().getItems().addAll(propertySearchPanel.getSearchField(),
                propertySearchPanel.getMatchCaseButton(), propertySearchPanel.getRefreshButton());
        propertyBox.getChildren().addAll(propertySearchPanel.getNode(),
                propertyTableView);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        getContentBox().getChildren().add(splitPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        nodeTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV == null) {
                viewModel.setSelectedElement(null);
            } else {
                viewModel.setSelectedElement(newV.getValue());
            }
        });
        propertyTableView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV == null) {
                viewModel.setSelectedInfo(null);
            } else {
                viewModel.setSelectedInfo(newV.getValue());
            }
        });
        viewModel.getAttributesUpdated().addListener((v) -> this.propertyTableView.refresh());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        propertyTableView.setRowFactory(ttv -> {
            TreeTableRow<PropertyItem> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    getViewModel().handlePropertyClick();
                }
            });
            return row;
        });
    }

    protected TreeView<Element> getNodeTreeView() {
        return nodeTreeView;
    }

    protected VBox getNodeBox() {
        return nodeBox;
    }

    protected TreeTableView<PropertyItem> getInfoTableView() {
        return propertyTableView;
    }

    protected VBox getInfoBox() {
        return propertyBox;
    }

    protected SplitPane getSplitPane() {
        return splitPane;
    }
}
