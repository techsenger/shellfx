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

package com.techsenger.tabshell.material;

import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public class SearchPane extends StackPane {

    private final ComboBox<String> textComboBox = new ComboBox<>();

    private final Button clearButton = new Button();

    public SearchPane() {
        clearButton.getStyleClass().add(StyleClasses.CROSS_BUTTON);
        clearButton.setFocusTraversable(false);
        StackPane.setMargin(this.clearButton, new Insets(0, SizeConstants.INSET * 2, 0, 0));
        textComboBox.getStyleClass().add(StyleClasses.EXTRA_DENSE);
        textComboBox.setEditable(true);
        textComboBox.setMaxWidth(Double.MAX_VALUE);
        setAlignment(Pos.CENTER_RIGHT);
        getChildren().add(textComboBox);
        getStyleClass().addAll("search-pane", StyleClasses.EXTRA_DENSE);
        var styles = SearchPane.class.getResource("search-pane.css").toExternalForm();
        getStylesheets().add(styles);
        textComboBox.getEditor().textProperty().addListener((ov, oldV, newV) -> {
            if (clearButton.getParent() == null) {
                if (newV != null && !newV.isEmpty()) {
                    getChildren().add(clearButton);
                }
            } else {
                if (newV == null || newV.isEmpty()) {
                    getChildren().remove(clearButton);
                }
            }
        });
        clearButton.setOnAction(e -> textComboBox.getEditor().setText(null));
    }

    public ComboBox<String> getTextComboBox() {
        return textComboBox;
    }

    public Button getClearButton() {
        return clearButton;
    }
}
