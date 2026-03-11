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

package com.techsenger.tabshell.demo.styles;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.devtools.stylesheet.StylesheetTabPresenter;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public class StylesTabFxView extends AbstractTabFxView<StylesheetTabPresenter<?, ?>> implements StylesTabView {

    private final GridPane gridPane = new GridPane();

    private final StackPane wrapper = new StackPane(gridPane);

    public StylesTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        getContentBox().getChildren().add(wrapper);
        wrapper.setPadding(new Insets(Spacing.VERTICAL, Spacing.HORIZONTAL, Spacing.VERTICAL, Spacing.HORIZONTAL));
        gridPane.setMaxWidth(Region.USE_PREF_SIZE);
        gridPane.setMaxHeight(Region.USE_PREF_SIZE);
        gridPane.setHgap(Spacing.HORIZONTAL);
        gridPane.setVgap(Spacing.VERTICAL);
        buildIconedButtons();
    }

    protected void buildIconedButtons() {
        var b1 = new Button(null, new FontIconView(SharedIcons.DIRECTORY));
        b1.getStyleClass().add(StyleClasses.ICON_BUTTON);
        var b2 = new Button(null, new FontIconView(SharedIcons.DIRECTORY));
        b2.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.DENSE);
        var b3 = new Button(null, new FontIconView(SharedIcons.DIRECTORY));
        b3.getStyleClass().addAll(StyleClasses.ICON_BUTTON, StyleClasses.COMPACT);
        var buttons = new HBox(b1, b2, b3);
        buttons.setSpacing(Spacing.HORIZONTAL);
        gridPane.addRow(gridPane.getRowCount(), new Label("Icon buttons"), buttons);
    }
}
