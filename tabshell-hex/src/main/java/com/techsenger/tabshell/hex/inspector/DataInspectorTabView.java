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

package com.techsenger.tabshell.hex.inspector;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.tab.AbstractTabView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.Spacer;
import java.nio.ByteOrder;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

/**
 *
 * @author Pavel Castornii
 */
public class DataInspectorTabView<T extends DataInspectorTabViewModel> extends AbstractTabView<T> {

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

    private final Button previousButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_LEFT));

    private final Button nextButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_RIGHT));

    private final ToolBar toolBar = new ToolBar(previousButton, nextButton, new Spacer());

    private final TableView<TypeItem<?>> typeTableView = new TableView<>();

    private final Label decimalLabel = new Label("Decimal");

    private final TextField decimalTextField = new TextField();

    private final Label hexadecimalLabel = new Label("Hexadecimal");

    private final TextField hexadecimalTextField = new TextField();

    private final Label octalLabel = new Label("Octal");

    private final TextField octalTextField = new TextField();

    private final Label binaryLabel = new Label("Binary");

    private final TextField binaryTextField = new TextField();

    private final GridPane baseGridPane = new GridPane();

    public DataInspectorTabView(T viewModel) {
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
        TableColumn<TypeItem<?>, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        TableColumn<TypeItem<?>, String> typeValueColumn = new TableColumn<>("Value");
        typeValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        typeValueColumn.setCellFactory(col -> new SelectableTableCell<TypeItem<?>>());
        typeValueColumn.setSortable(false);
        typeTableView.getColumns().addAll(typeColumn, typeValueColumn);
        VBox.setVgrow(typeTableView, Priority.ALWAYS);

        baseGridPane.add(decimalLabel, 0, 0);
        baseGridPane.add(decimalTextField, 1, 0);
        GridPane.setHgrow(decimalTextField, Priority.ALWAYS);
        decimalTextField.getStyleClass().add(Styles.DENSE);
        baseGridPane.add(hexadecimalLabel, 0, 1);
        baseGridPane.add(hexadecimalTextField, 1, 1);
        GridPane.setHgrow(hexadecimalTextField, Priority.ALWAYS);
        hexadecimalTextField.getStyleClass().add(Styles.DENSE);
        baseGridPane.add(octalLabel, 0, 2);
        baseGridPane.add(octalTextField, 1, 2);
        GridPane.setHgrow(octalTextField, Priority.ALWAYS);
        octalTextField.getStyleClass().add(Styles.DENSE);
        baseGridPane.add(binaryLabel, 0, 3);
        baseGridPane.add(binaryTextField, 1, 3);
        GridPane.setHgrow(binaryTextField, Priority.ALWAYS);
        binaryTextField.getStyleClass().add(Styles.DENSE);
        baseGridPane.setVgap(SizeConstants.INSET);
        baseGridPane.setHgap(SizeConstants.INSET);
        baseGridPane.setPadding(new Insets(SizeConstants.INSET));

        this.toolBar.getStyleClass().add(Styles.DENSE);
        getContentPane().getChildren().addAll(toolBar, typeTableView, baseGridPane);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        byteOrderCheckBox.valueProperty().bindBidirectional(viewModel.byteOrderWrapper());
        viewModel.selectedTypeItemWrapper().bind(typeTableView.getSelectionModel().selectedItemProperty());
        decimalTextField.textProperty().bind(viewModel.decimalProperty());
        hexadecimalTextField.textProperty().bind(viewModel.hexadecimalProperty());
        octalTextField.textProperty().bind(viewModel.octalProperty());
        binaryTextField.textProperty().bind(viewModel.binaryProperty());
    }

    protected ComboBox<ByteOrder> getByteOrderComboBox() {
        return byteOrderCheckBox;
    }

    protected ToolBar getToolBar() {
        return toolBar;
    }

    protected TableView<TypeItem<?>> getTypeTableView() {
        return typeTableView;
    }

    protected GridPane getBaseGridPane() {
        return baseGridPane;
    }

    protected ComboBox<ByteOrder> getByteOrderCheckBox() {
        return byteOrderCheckBox;
    }

    protected Button getPreviousButton() {
        return previousButton;
    }

    protected Button getNextButton() {
        return nextButton;
    }

    protected Label getDecimalLabel() {
        return decimalLabel;
    }

    protected TextField getDecimalTextField() {
        return decimalTextField;
    }

    protected Label getHexadecimalLabel() {
        return hexadecimalLabel;
    }

    protected TextField getHexadecimalTextField() {
        return hexadecimalTextField;
    }

    protected Label getOctalLabel() {
        return octalLabel;
    }

    protected TextField getOctalTextField() {
        return octalTextField;
    }

    protected Label getBinaryLabel() {
        return binaryLabel;
    }

    protected TextField getBinaryTextField() {
        return binaryTextField;
    }
}
