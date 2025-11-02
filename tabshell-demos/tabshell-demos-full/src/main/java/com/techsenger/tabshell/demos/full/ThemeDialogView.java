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

package com.techsenger.tabshell.demos.full;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.theme.ShellTheme;
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogView;
import com.techsenger.toolkit.fx.utils.ButtonUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

/**
 *
 * @author Pavel Castornii
 */
public class ThemeDialogView extends AbstractSimpleDialogView<ThemeDialogViewModel> {

    private final Label themeLabel = new Label("Theme");

    private final ComboBox<ShellTheme> themeComboBox = new ComboBox<>();

    private final HBox hBox = new HBox(themeLabel, themeComboBox);

    public ThemeDialogView(ThemeDialogViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        hBox.requestFocus();
    }

    @Override
    protected void build(ThemeDialogViewModel viewModel) {
        super.build(viewModel);
        themeLabel.setMinWidth(Region.USE_PREF_SIZE);
        themeComboBox.setItems(viewModel.getThemes());
        themeComboBox.setMaxWidth(Double.MAX_VALUE);
        themeComboBox.setConverter(new StringConverter<ShellTheme>() {
            @Override
            public String toString(ShellTheme t) {
                return t.getName();
            }

            @Override
            public ShellTheme fromString(String string) {
                return null;
            }
        });
        themeComboBox.getStyleClass().add(Styles.DENSE);
        HBox.setHgrow(themeComboBox, Priority.ALWAYS);
        hBox.setPadding(new Insets(SizeConstants.INSET, SizeConstants.INSET, 0, SizeConstants.INSET));
        hBox.setSpacing(SizeConstants.INSET);
        hBox.setAlignment(Pos.CENTER_LEFT);

        getButtonBox().getChildren().addAll(getCancelButton(), getOkButton());
        getContentPane().getChildren().addAll(hBox, getButtonBox());
    }

    @Override
    protected void bind(ThemeDialogViewModel viewModel) {
        super.bind(viewModel);
        themeComboBox.valueProperty().bindBidirectional(viewModel.themeProperty());
    }

    @Override
    protected void makeEqualButtons() {
        if (getViewModel().isCancelVisible()) {
            ButtonUtils.makeEqualWidthBySize(getCancelButton(), getOkButton());
        }
    }


}
