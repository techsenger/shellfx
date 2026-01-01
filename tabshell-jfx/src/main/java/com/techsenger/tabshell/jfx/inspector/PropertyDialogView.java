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

import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogView;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class PropertyDialogView<T extends PropertyDialogViewModel, S extends PropertyDialogComponent<?>>
        extends AbstractSimpleDialogView<T, S> {

    private final GridPane gridPane = new GridPane();

    private final VBox wrapper = new VBox(gridPane);

    private final Hyperlink nameHyperlink = new Hyperlink();

    private final TextArea valueTextArea = new TextArea();

    private final Hyperlink cssHyperlink = new Hyperlink("Css Property");

    private final TextField cssTextField = new TextField();

    private final Label stateLabel = new Label("State");

    private final TextField stateTextField = new TextField();

    public PropertyDialogView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected void makeEqualButtons() {

    }

    @Override
    public void requestFocus() {
        wrapper.requestFocus();
    }

    @Override
    protected void build() {
        super.build();
        var viewModel = getViewModel();
        var info = viewModel.getInfo();

        var propUrl = viewModel.getPropertyUrl();
        nameHyperlink.setText(info.getAttribute().name());
        if (propUrl != null) {
            nameHyperlink.setTooltip(new Tooltip(propUrl));
            nameHyperlink.setOnAction(e -> {
                viewModel.openUrl(propUrl);
            });
        } else {
            nameHyperlink.getStyleClass().add(StyleClasses.NO_URL);
        }
        nameHyperlink.setMinWidth(Hyperlink.USE_PREF_SIZE);
        gridPane.add(nameHyperlink, 0, 0);
        GridPane.setValignment(nameHyperlink, VPos.TOP);
        valueTextArea.setText(info.getValue().text());
        valueTextArea.setEditable(false);
        valueTextArea.setWrapText(true);
        gridPane.add(valueTextArea, 1, 0);
        GridPane.setVgrow(valueTextArea, Priority.ALWAYS);

        cssHyperlink.setMinWidth(Hyperlink.USE_PREF_SIZE);
        gridPane.add(cssHyperlink, 0, 1);
        if (info.getAttribute().cssProperty() != null) {
            cssTextField.setText(info.getAttribute().cssProperty());
            var cssPropUrl = viewModel.getCssPropertyUrl();
            if (cssPropUrl != null) {
                cssHyperlink.setTooltip(new Tooltip(cssPropUrl));
                cssHyperlink.setOnAction(e -> {
                    viewModel.openUrl(cssPropUrl);
                });
            } else {
                cssHyperlink.getStyleClass().add(StyleClasses.NO_URL);
            }
        } else {
            cssTextField.setText("-");
            cssHyperlink.getStyleClass().add(StyleClasses.NO_URL);
        }
        cssTextField.setEditable(false);
        gridPane.add(cssTextField, 1, 1);

        stateLabel.setMinWidth(Hyperlink.USE_PREF_SIZE);
        gridPane.add(stateLabel, 0, 2);
        stateTextField.setText(info.getAttribute().valueState().name());
        stateTextField.setEditable(false);
        gridPane.add(stateTextField, 1, 2);

        gridPane.setVgap(SizeConstants.INSET);
        gridPane.setHgap(SizeConstants.INSET);
        VBox.setVgrow(gridPane, Priority.ALWAYS);

        VBox.setVgrow(wrapper, Priority.ALWAYS);
        wrapper.setPadding(new Insets(SizeConstants.INSET, SizeConstants.INSET, 0, SizeConstants.INSET));

        getButtonBox().getChildren().add(getOkButton());
        getContentPane().getChildren().addAll(wrapper, getButtonBox());
    }
}

