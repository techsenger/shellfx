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

import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.core.tab.AbstractTabView;
import devtoolsfx.connector.LocalElement;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.Element;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class JfxInspectorTabView<T extends JfxInspectorTabViewModel> extends AbstractTabView<T> {

    private static final class RootAttributeTreeItem extends TreeItem<AttributeInfo> {

        private RootAttributeTreeItem(AttributeInfo rootAttribute) {
            setValue(rootAttribute);
            rootAttribute.getChildren().addListener((ListChangeListener<AttributeInfo>) (e) -> {
                while (e.next()) {
                    if (e.wasAdded()) {
                        for (var added : e.getAddedSubList()) {
                            var c = new AttributeCategoryTreeItem(added);
                            getChildren().add(c);
                        }
                    }
                    if (e.wasRemoved()) {
                        for (var c: getChildren()) {
                            ((AttributeCategoryTreeItem) c).dispose();
                        }
                        getChildren().clear();
                    }
                }
            });
        }
    }

    private static final class AttributeCategoryTreeItem extends TreeItem<AttributeInfo> {

        private ListChangeListener<AttributeInfo> listener =  (e) -> {
            while (e.next()) {
                if (e.wasAdded()) {
                    for (var added : e.getAddedSubList()) {
                        var c = new TreeItem(added);
                        getChildren().add(c);
                    }
                }
                if (e.wasRemoved()) {
                    getChildren().clear();
                }
            }
        };

        private AttributeCategoryTreeItem(AttributeInfo categoryAttribute) {
            setValue(categoryAttribute);
            this.expandedProperty().bindBidirectional(categoryAttribute.expandedProperty());
            categoryAttribute.getChildren().addListener(listener);
        }

        private void dispose() {
            this.expandedProperty().unbindBidirectional(getValue().expandedProperty());
            getValue().getChildren().removeListener(listener);
        }

    }

    private final TreeView<Element> nodeTreeView = new TreeView<>();

    private final TextField nodeTextField = new TextField();

    private final VBox nodeVBox = new VBox(nodeTreeView, nodeTextField);

    private final TreeTableView<AttributeInfo> attributeTableView = new TreeTableView<>();

    private final TextField attributeTextField = new TextField();

    private final VBox attributeVBox = new VBox(attributeTableView, attributeTextField);

    private final SplitPane splitPane = new SplitPane(nodeVBox, attributeVBox);

    private final ShellView<?> shell;

    public JfxInspectorTabView(ShellView<?> shell, T viewModel) {
        super(viewModel);
        this.shell = shell;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void preInitialize(T viewModel) {
        super.preInitialize(viewModel);
        var stage = shell.getStage();
        var root = LocalElement.of(stage, new EventSource(null, stage.hashCode(), true));
        var rootItem = createNodeItem(root);
        nodeTreeView.setRoot(rootItem);
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        var styles = JfxInspectorTabView.class.getResource("inspector.css").toExternalForm();
        getContentPane().getStylesheets().add(styles);

        nodeTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Element item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(viewModel.getElementText(item));
                }
            }
        });
        nodeTreeView.getStyleClass().add(StyleClasses.EXTRA_DENSE);
        VBox.setVgrow(nodeTreeView, Priority.ALWAYS);

        TreeTableColumn<AttributeInfo, String> propertyColumn = new TreeTableColumn<>("Property");
        propertyColumn.setCellValueFactory(param -> {
            // root is not shown
            var info = param.getValue().getValue();
            return new SimpleStringProperty(info.getText());
        });
        TreeTableColumn<AttributeInfo, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> {
            // root is not shown
            var info = param.getValue().getValue();
            if (info.getCategory() != null) {
                return new SimpleStringProperty();
            } else {
                var text = "";
                if (info.getAttribute().value() != null) {
                    text = info.getAttribute().value().toString();
                }
                return new SimpleStringProperty(text);
            }
        });
        attributeTableView.getColumns().addAll(propertyColumn, valueColumn);
        attributeTableView.getStyleClass().addAll(StyleClasses.EXTRA_DENSE);
        attributeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        attributeTableView.setShowRoot(false);
        attributeTableView.setRoot(new RootAttributeTreeItem(viewModel.getRootAttribute()));
        VBox.setVgrow(attributeTableView, Priority.ALWAYS);

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
        viewModel.getAttributesUpdated().addListener((v) -> this.attributeTableView.refresh());
    }

    private TreeItem createNodeItem(Element element) {
        var item = new TreeItem<>(element);
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
        item.getChildren().add(new TreeItem(null));
        return item;
    }
}
