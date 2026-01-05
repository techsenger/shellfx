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

package com.techsenger.tabshell.demos.full.theme;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogView;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.theme.Theme;
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
public class ThemeDialogView extends AbstractSimpleDialogView<ThemeDialogViewModel, ThemeDialogComponent> {

    private final Label themeLabel = new Label("Theme");

    private final ComboBox<Theme> themeComboBox = new ComboBox<>();

    private final HBox hBox = new HBox(themeLabel, themeComboBox);

    public ThemeDialogView(ThemeDialogViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        hBox.requestFocus();
    }

    @Override
    protected void build() {
        super.build();
        themeLabel.setMinWidth(Region.USE_PREF_SIZE);
        themeComboBox.setItems(getViewModel().getThemes());
        themeComboBox.setMaxWidth(Double.MAX_VALUE);
        themeComboBox.setConverter(new StringConverter<Theme>() {
            @Override
            public String toString(Theme t) {
                return t.getName();
            }

            @Override
            public Theme fromString(String string) {
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
    protected void bind() {
        super.bind();
        themeComboBox.valueProperty().bindBidirectional(getViewModel().themeProperty());
    }

    @Override
    protected void makeEqualButtons() {
        if (getViewModel().getCancel().isVisible()) {
            ButtonUtils.makeEqualWidthBySize(getCancelButton(), getOkButton());
        }
    }
}
