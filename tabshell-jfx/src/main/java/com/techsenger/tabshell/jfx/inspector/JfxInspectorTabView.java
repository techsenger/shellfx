/*
 * Copyright 2024-2025 Pavel Castornii.
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

import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.core.tab.AbstractTabView;
import com.techsenger.tabshell.core.tab.ShellTabView;
import com.techsenger.tabshell.jfx.inspector.PropertyInfo.ValueInfo;
import devtoolsfx.connector.LocalElement;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.attributes.Attribute;
import static devtoolsfx.scenegraph.attributes.Attribute.DisplayHint.INSETS;
import static devtoolsfx.scenegraph.attributes.Attribute.DisplayHint.NUMERIC;
import devtoolsfx.scenegraph.attributes.Attribute.ValueState;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import java.text.DecimalFormat;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
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
public class JfxInspectorTabView<T extends JfxInspectorTabViewModel> extends AbstractTabView<T> {

    private static ValueInfo createValue(PropertyInfo info) {
        Attribute<?> attr = info.getAttribute();
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
                        return new ValueInfo("USE_PREF_SIZE", true);
                    }
                    if (num == Region.USE_COMPUTED_SIZE) {
                        return new ValueInfo("USE_COMPUTED_SIZE", true);
                    }
                    if (num == Double.MIN_VALUE) {
                        return new ValueInfo("MIN_VALUE", true);
                    }
                    if (num == Double.MAX_VALUE) {
                        return new ValueInfo("MAX_VALUE", true);
                    }
                    return new ValueInfo(FORMAT.format(num), isDefault);
                }
                return new ValueInfo(String.valueOf(attr.value()), isDefault);
            }
            case INSETS -> {
                if (attr.value() instanceof Insets insets) {
                    if (isZero(insets.getTop()) && isZero(insets.getRight())
                            && isZero(insets.getBottom()) && isZero(insets.getLeft())) {
                        return new ValueInfo("Insets.EMPTY", true);
                    }
                }
                return new ValueInfo(String.valueOf(attr.value()), attr.valueState() == ValueState.DEFAULT);
            }
            default -> {
                if (attr.value() instanceof List<?> list && list.isEmpty()) {
                    return new ValueInfo("[]", true);
                }
                boolean isDefault = false;
                var strValue = String.valueOf(attr.value());
                if (info.getAttribute().displayHint() == Attribute.DisplayHint.TRANSFORMS
                        || (info.getCategory() == AttributeCategory.REFLECTIVE
                        && (attr.name().equals("localToSceneTransform")
                        || attr.name().equals("localToParentTransform")))) {
                    strValue = strValue.replaceAll("\\R", "")
                            .replaceAll("\\[\\s+", "[")
                            .replaceAll("[\t]| {3,}", "  ");
                }
                if (attr.valueState() == ValueState.DEFAULT || strValue.equals("null") || strValue.isEmpty())  {
                    isDefault = true;
                }
                return new ValueInfo(strValue, isDefault);
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
                setText(JfxInspectorTabViewModel.getNodeText(item));
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

    private static final class RootTreeItem extends TreeItem<PropertyInfo> {

        private RootTreeItem(PropertyInfo rootAttribute) {
            setValue(rootAttribute);
            rootAttribute.getChildren().addListener((ListChangeListener<PropertyInfo>) (e) -> {
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

    private static final class CategoryTreeItem extends TreeItem<PropertyInfo> {

        private ListChangeListener<PropertyInfo> listener =  (e) -> {
            while (e.next()) {
                if (e.wasAdded()) {
                    for (var added : e.getAddedSubList()) {
                        added.setValue(createValue(added));
                        var c = new TreeItem<PropertyInfo>(added);
                        getChildren().add(c);
                    }
                }
                if (e.wasRemoved()) {
                    getChildren().clear();
                }
            }
        };

        private CategoryTreeItem(PropertyInfo categoryAttribute) {
            setValue(categoryAttribute);
            this.expandedProperty().bindBidirectional(categoryAttribute.expandedProperty());
            categoryAttribute.getChildren().addListener(listener);
        }

        private void dispose() {
            this.expandedProperty().unbindBidirectional(getValue().expandedProperty());
            getValue().getChildren().removeListener(listener);
        }
    }

    private static final class PropertyTableCell extends TreeTableCell<PropertyInfo, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                var info = getTableRow().getItem();
                if (!info.isCategory()) {
                    Label mainLabel = new Label(item);
                    HBox box = new HBox(SizeConstants.THIRD_INSET, mainLabel);
                    var attr = info.getAttribute();
                    if (attr != null) {
                        if (info.getValue().isDefault()) {
                            mainLabel.getStyleClass().add("default-value");
                        }
                        if (info.getAttribute().cssProperty() != null) {
                            Label cssHint = new Label("CSS");
                            cssHint.getStyleClass().add("css-hint");
                            if (info.getValue().isDefault()) {
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

    private static final class ValueTableCell extends TreeTableCell<PropertyInfo, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                var info = getTableRow().getItem();
                var attr = info.getAttribute();
                Label mainLabel = new Label(item);
                if (attr != null && info.getValue().isDefault()) {
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

    private final TextField nodeTextField = new TextField();

    private final VBox nodeBox = new VBox(nodeTreeView, nodeTextField);

    private final TreeTableView<PropertyInfo> infoTableView = new TreeTableView<>();

    private final TextField infoTextField = new TextField();

    private final VBox infoBox = new VBox(infoTableView, infoTextField);

    private final SplitPane splitPane = new SplitPane(nodeBox, infoBox);

    private final ShellTabView<?> shellTab;

    public JfxInspectorTabView(ShellTabView<?> shellTab, T viewModel) {
        super(viewModel);
        this.shellTab = shellTab;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void preInitialize(T viewModel) {
        super.preInitialize(viewModel);
        var stage = shellTab.getShell().getStage();
        var root = LocalElement.of(stage, new EventSource(null, stage.hashCode(), true));
        var rootItem = createNodeItem(root);
        nodeTreeView.setRoot(rootItem);
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        var styles = JfxInspectorTabView.class.getResource("inspector-tab.css").toExternalForm();
        getContentPane().getStylesheets().add(styles);

        nodeTreeView.setCellFactory(tv -> new NodeCell());
        nodeTreeView.getStyleClass().add(StyleClasses.EXTRA_DENSE);
        VBox.setVgrow(nodeTreeView, Priority.ALWAYS);
        nodeTextField.getStyleClass().add(StyleClasses.EXTRA_DENSE);

        TreeTableColumn<PropertyInfo, String> propertyColumn = new TreeTableColumn<>("Property");
        propertyColumn.setCellValueFactory(param -> {
            // root is not shown
            var info = param.getValue().getValue();
            String text;
            if (info.isCategory()) {
                text = JfxInspectorTabViewModel.getCategoryText(info.getCategory());
            } else {
                text = info.getAttribute().name();
            }
            return new SimpleStringProperty(text);
        });
        propertyColumn.setCellFactory(col -> new PropertyTableCell());

        TreeTableColumn<PropertyInfo, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> {
            // root is not shown
            var info = param.getValue().getValue();
            if (info.isCategory()) {
                return new SimpleStringProperty();
            } else {
                return new SimpleStringProperty(info.getValue().text());
            }
        });
        valueColumn.setCellFactory(col -> new ValueTableCell());
        infoTableView.getColumns().addAll(propertyColumn, valueColumn);
        infoTableView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE);
        infoTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        infoTableView.setShowRoot(false);
        infoTableView.setRoot(new RootTreeItem(viewModel.getRootInfo()));
        infoTableView.setPlaceholder(new Label(""));
        VBox.setVgrow(infoTableView, Priority.ALWAYS);
        infoTextField.getStyleClass().add(StyleClasses.EXTRA_DENSE);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        getContentPane().getChildren().add(splitPane);
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        nodeTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV == null) {
                viewModel.setSelectedElement(null);
            } else {
                viewModel.setSelectedElement(newV.getValue());
            }
        });
        infoTableView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV == null) {
                viewModel.setSelectedInfo(null);
            } else {
                viewModel.setSelectedInfo(newV.getValue());
            }
        });
        viewModel.getAttributesUpdated().addListener((v) -> this.infoTableView.refresh());
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        infoTableView.setRowFactory(ttv -> {
            TreeTableRow<PropertyInfo> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewModel.handleInfoClick();
                }
            });
            return row;
        });
    }

    @Override
    protected JfxInspectorComposer<?> createComposer() {
        return new JfxInspectorComposer<>(shellTab, this);
    }

    protected ShellTabView getShellTab() {
        return shellTab;
    }

    protected TreeView<Element> getNodeTreeView() {
        return nodeTreeView;
    }

    protected TextField getNodeTextField() {
        return nodeTextField;
    }

    protected VBox getNodeBox() {
        return nodeBox;
    }

    protected TreeTableView<PropertyInfo> getInfoTableView() {
        return infoTableView;
    }

    protected TextField getInfoTextField() {
        return infoTextField;
    }

    protected VBox getInfoBox() {
        return infoBox;
    }

    protected SplitPane getSplitPane() {
        return splitPane;
    }
}
