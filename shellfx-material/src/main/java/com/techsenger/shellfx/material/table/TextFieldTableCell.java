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

package com.techsenger.shellfx.material.table;

import com.techsenger.annotations.Nullable;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

/**
 *
 * @author Pavel Castornii
 */
public class TextFieldTableCell<S, T> extends TableCell<S, T> {

    private static final PseudoClass EDITING = PseudoClass.getPseudoClass("editing");

    private final StringConverter<T> converter;

    private TextField textField;

    public TextFieldTableCell(StringConverter<T> converter) {
        this.converter = converter;
        getStyleClass().add("text-field-table-cell");
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (getItem() == null) {
            return;
        }
        if (textField == null) {
            textField = new TextField();
            textField.setOnAction(e -> commitEdit(converter.fromString(textField.getText())));
            textField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }
        textField.setText(converter.toString(getItem()));
        HBox.setHgrow(textField, Priority.ALWAYS);
        setGraphic(buildEditGraphic());
        setText(null);
        textField.selectAll();
        textField.requestFocus();
        pseudoClassStateChanged(EDITING, true);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        pseudoClassStateChanged(EDITING, false);
        this.textField = null;
        updateDisplay();
    }

    @Override
    public void commitEdit(T t) {
        super.commitEdit(t);
        pseudoClassStateChanged(EDITING, false);
        this.textField = null;
        updateDisplay();
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            updateDisplay();
        }
    }

    protected @Nullable TextField getTextField() {
        return textField;
    }

    protected StringConverter<T> getConverter() {
        return converter;
    }

    /**
     * Returns the graphic to display during editing. The text field is
     * included by default. Override to add icons or other nodes alongside it.
     */
    protected Node buildEditGraphic() {
        return textField;
    }

    /**
     * Updates text and graphic in non-editing state. Override to customize display.
     */
    protected void updateDisplay() {
        setText(converter.toString(getItem()));
        setGraphic(null);
    }
}
