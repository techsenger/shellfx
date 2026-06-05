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

package com.techsenger.tabshell.demo.theme;

import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.theme.Theme;
import java.util.List;
import javafx.collections.FXCollections;
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
public class ThemeDialogFxView extends AbstractDialogFxView<ThemeDialogPresenter> implements ThemeDialogView {

    private final Label themeLabel = new Label("Theme");

    private final ComboBox<Theme> themeComboBox = new ComboBox<>();

    private final HBox hBox = new HBox(themeLabel, themeComboBox);

    private final ResultButton cancelButton = new ResultButton(ThemeDialogButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(ThemeDialogButtons.OK, "OK");

    public ThemeDialogFxView() {
        super();
    }

    @Override
    public void requestFocus() {
        hBox.requestFocus();
    }

    @Override
    public void setThemes(List<Theme> themes) {
        themeComboBox.setItems(FXCollections.observableArrayList(themes));
    }

    @Override
    public void setTheme(Theme theme) {
        themeComboBox.getSelectionModel().select(theme);
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
        HBox.setHgrow(themeComboBox, Priority.ALWAYS);
        hBox.setSpacing(Spacing.getHorizontal());
        hBox.setAlignment(Pos.CENTER_LEFT);

        okButton.setDefaultButton(true);
        registerButtons(cancelButton, okButton);
        getContentBox().getChildren().add(hBox);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        themeComboBox.valueProperty().addListener((ov, oldV, newV) -> getPresenter().onThemeSelected(newV));
    }


}
