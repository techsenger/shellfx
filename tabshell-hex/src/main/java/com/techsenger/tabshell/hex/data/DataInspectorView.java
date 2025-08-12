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

package com.techsenger.tabshell.hex.data;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.core.tab.AbstractTabView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.Spacer;
import java.nio.ByteOrder;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

/**
 *
 * @author Pavel Castornii
 */
public class DataInspectorView<T extends DataInspectorViewModel> extends AbstractTabView<T> {

    private static class SelectableTableCell<T> extends TextFieldTableCell<T, String> {

        SelectableTableCell() {
            super(new DefaultStringConverter());
        }

        @Override
        public void startEdit() {
            if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                return;
            }
            super.startEdit();
            if (isEditing()) {
                TextField textField = (TextField) getGraphic();
                textField.setEditable(false);
            }
        }
    }

    private final ComboBox<ByteOrder> byteOrderCheckBox = new ComboBox<>();

    private final Button previousButton = new Button(null, new FontIconView(CoreIcons.CHEVRON_LEFT));

    private final Button nextButton = new Button(null, new FontIconView(CoreIcons.CHEVRON_RIGHT));

    private final ToolBar toolBar = new ToolBar(previousButton, nextButton, new Spacer());

    private final TableView<TypeItem<?>> typeTableView = new TableView<>();

    private final TableView<BaseItem> baseTableView = new TableView<>();

    private final SplitPane splitPane = new SplitPane(typeTableView, baseTableView);

    public DataInspectorView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);

        byteOrderCheckBox.getStyleClass().add(Styles.DENSE);
        byteOrderCheckBox.setItems(viewModel.getByteOrders());

        this.previousButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);
        this.nextButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICONED_BUTTON);

        typeTableView.setItems(viewModel.getTypeItems());
        typeTableView.getStyleClass().add(StyleClasses.EXTRA_DENSE);
        typeTableView.setEditable(true);
        typeTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(typeTableView, Priority.ALWAYS);
        TableColumn<TypeItem<?>, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        TableColumn<TypeItem<?>, String> typeValueColumn = new TableColumn<>("Value");
        typeValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        typeValueColumn.setCellFactory(col -> new SelectableTableCell<TypeItem<?>>());
        typeValueColumn.setSortable(false);
        typeTableView.getColumns().addAll(typeColumn, typeValueColumn);

        baseTableView.setItems(viewModel.getBaseItems());
        baseTableView.getStyleClass().add(StyleClasses.EXTRA_DENSE);
        baseTableView.setEditable(true);
        baseTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(baseTableView, Priority.ALWAYS);
        TableColumn<BaseItem, String> baseColumn = new TableColumn<>("Base");
        baseColumn.setCellValueFactory(cellData -> cellData.getValue().baseProperty());
        TableColumn<BaseItem, String> baseValueColumn = new TableColumn<>("Value");
        baseValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        baseValueColumn.setCellFactory(col -> new SelectableTableCell<BaseItem>());
        baseValueColumn.setSortable(false);
        baseTableView.getColumns().addAll(baseColumn, baseValueColumn);

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        splitPane.setOrientation(Orientation.VERTICAL);

        this.toolBar.getStyleClass().add(Styles.DENSE);
        getContentPane().getChildren().addAll(toolBar, splitPane);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        byteOrderCheckBox.valueProperty().bindBidirectional(viewModel.byteOrderWrapper());
        viewModel.selectedTypeItemWrapper().bind(typeTableView.getSelectionModel().selectedItemProperty());
        viewModel.selectedBaseItemWrapper().bind(baseTableView.getSelectionModel().selectedItemProperty());
    }

    protected ComboBox<ByteOrder> getByteOrderComboBox() {
        return byteOrderCheckBox;
    }

    protected ToolBar getToolBar() {
        return toolBar;
    }

    protected TableView<BaseItem> getBaseTableView() {
        return baseTableView;
    }

    protected SplitPane getSplitPane() {
        return splitPane;
    }


}
