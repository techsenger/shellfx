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

package com.techsenger.shellfx.demo.settings;

import com.techsenger.shellfx.core.dialog.AbstractDialogFxView;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.theme.AtlantaFxTheme;
import com.techsenger.shellfx.material.theme.Theme;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 *
 * @author Pavel Castornii
 */
public class SettingsDialogFxView extends AbstractDialogFxView<SettingsDialogPresenter> implements SettingsDialogView {

    private final Label themeLabel = new Label("Theme");

    private final ComboBox<Theme> themeComboBox =
            new ComboBox<>(FXCollections.observableArrayList(AtlantaFxTheme.values()));

    private final Label densityLabel = new Label("Density");

    private final ComboBox<Density> densityComboBox = new ComboBox<>();

    private final GridPane gridPane = new GridPane();

    private final ResultButton cancelButton = new ResultButton(SettingsDialogButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(SettingsDialogButtons.OK, "OK");

    public SettingsDialogFxView() {
        super();
    }

    @Override
    public void requestFocus() {
        themeComboBox.requestFocus();
    }

    @Override
    public void setSelectedTheme(Theme theme) {
        themeComboBox.getSelectionModel().select(theme);
    }

    @Override
    public void setSelectedDensity(Density density) {
        densityComboBox.getSelectionModel().select(density);
    }

    @Override
    protected void build() {
        super.build();
        themeLabel.setMinWidth(Region.USE_PREF_SIZE);
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
        GridPane.setHgrow(themeComboBox, Priority.ALWAYS);
        gridPane.addRow(gridPane.getRowCount(), themeLabel, themeComboBox);

        densityLabel.setMinWidth(Region.USE_PREF_SIZE);
        densityComboBox.getItems().add(null);
        densityComboBox.getItems().addAll(Density.values());
        densityComboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(densityComboBox, Priority.ALWAYS);
        gridPane.addRow(gridPane.getRowCount(), densityLabel, densityComboBox);

        gridPane.setVgap(Spacing.getVertical());
        gridPane.setHgap(Spacing.getHorizontal());

        okButton.setDefaultButton(true);
        registerButtons(cancelButton, okButton);
        getButtonWidthGroup().add(cancelButton, okButton);
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        getContentBox().getChildren().add(gridPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        themeComboBox.valueProperty().addListener((ov, oldV, newV) -> getPresenter().onThemeSelected(newV));
        densityComboBox.valueProperty().addListener((ov, oldV, newV) -> getPresenter().onDensitySelected(newV));
    }
}
