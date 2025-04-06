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

package com.techsenger.tabshell.text.viewer;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogView;
import com.techsenger.tabshell.material.FxUtils;
import com.techsenger.toolkit.fx.input.IntegerTextFormatter;
import com.techsenger.toolkit.fx.utils.ButtonUtils;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.converter.IntegerStringConverter;

/**
 *
 * @author Pavel Castornii
 */
class GoToLineDialogView extends AbstractSimpleDialogView<GoToLineDialogViewModel> {

    private final Label lineLabel = new Label("Line");

    private final ComboBox<Integer> lineComboBox = new ComboBox<>();

    private final Label columnLabel = new Label("Column");

    private final ComboBox<Integer> columnComboBox = new ComboBox<>();

    GoToLineDialogView(GoToLineDialogViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public GoToLineDialogViewModel getViewModel() {
        return (GoToLineDialogViewModel) super.getViewModel();
    }

    @Override
    public void requestFocus() {
        this.lineComboBox.requestFocus();
    }

    @Override
    protected void build(GoToLineDialogViewModel viewModel) {
        super.build(viewModel);
        this.getButtonBox().getChildren().addAll(getCancelButton(), getOkButton());
        ButtonUtils.makeEqualWidth(getCancelButton(), getOkButton());
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(SizeConstants.INSET, SizeConstants.INSET, 0, SizeConstants.INSET));
        gridPane.setHgap(SizeConstants.INSET);
        gridPane.setVgap(SizeConstants.INSET);
        var c0 = new ColumnConstraints();
        c0.setPercentWidth(25);
        var c1 = new ColumnConstraints();
        c1.fillWidthProperty().set(true);
        c1.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(c0, c1);

        gridPane.add(lineLabel, 0, 0);
        lineLabel.setFocusTraversable(false);
        lineLabel.setMinWidth(Region.USE_PREF_SIZE);
        gridPane.add(lineComboBox, 1, 0);
        lineComboBox.getStyleClass().add(Styles.DENSE);
        lineComboBox.setMaxWidth(Double.MAX_VALUE);
        lineComboBox.setEditable(true);
        lineComboBox.getEditor().setTextFormatter(new IntegerTextFormatter(null, false));
        lineComboBox.setConverter(new IntegerStringConverter());
        lineComboBox.setItems(viewModel.getLines());
        FxUtils.makeValueUpdateOnEnter(lineComboBox);
        gridPane.add(columnLabel, 0, 1);
        columnLabel.setFocusTraversable(false);
        columnLabel.setMinWidth(Region.USE_PREF_SIZE);
        gridPane.add(columnComboBox, 1, 1);
        columnComboBox.getStyleClass().add(Styles.DENSE);
        columnComboBox.setEditable(true);
        columnComboBox.setMaxWidth(Double.MAX_VALUE);
        columnComboBox.getEditor().setTextFormatter(new IntegerTextFormatter(null, false));
        columnComboBox.setConverter(new IntegerStringConverter());
        columnComboBox.setItems(viewModel.getColumns());
        FxUtils.makeValueUpdateOnEnter(columnComboBox);

        getContentPane().getChildren().addAll(gridPane, getButtonBox());
        getFocusTrap().activate();
    }

    @Override
    protected void bind(GoToLineDialogViewModel viewModel) {
        super.bind(viewModel);
        viewModel.lineProperty().bind(lineComboBox.valueProperty());
        viewModel.columnProperty().bind(columnComboBox.valueProperty());
    }

    @Override
    protected void addListeners(GoToLineDialogViewModel viewModel) {
        super.addListeners(viewModel);
        lineComboBox.getEditor().textProperty().addListener((ov, oldV, newV) -> viewModel.checkOkButtonState(newV));
    }
}
