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

package com.techsenger.tabshell.material.list;

import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

/**
 * When using this class, keep in mind that if the {@link TextFieldColumnListCell#updateItem(java.lang.Object, boolean)}
 * method is overridden, then {@code super.updateItem(Object, boolean)} must be called AFTER your custom code.
 *
 * <p> Using {@link StringConverter} is optional. It it is not used, then item list is not modified.
 *
 * @author Pavel Castornii
 */
public class TextFieldColumnListCell<T> extends ColumnListCell<T> {

    private HBox box;

    private TextField textField;

    private final StringConverter<T> converter;

    public TextFieldColumnListCell() {
        this(null);
    }

    public TextFieldColumnListCell(StringConverter<T> converter) {
        this.converter = converter;
        setEditable(true);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        createBox();
        updateBox();
    }

    @Override
    public void commitEdit(T newValue) {
        super.commitEdit(newValue);
        if (this.converter != null) {
            var item = converter.fromString(this.textField.getText());
            getListView().getItems().set(getIndex(), item);
            requestLayout();
        }
        removeBox();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        removeBox();
    }

    public TextField getTextField() {
        return textField;
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (isEmpty()) {
            setGraphic(null);
            setText(null);
        } else {
            if (isEditing()) {
                if (this.box == null) {
                    createBox();
                }
                updateBox();
            }
        }
    }

    private void createBox() {
        this.box = new HBox();
        this.box.getStyleClass().add("box");
        //box must have the same width
        this.box.setPrefWidth(getWidth());
        this.box.setMinWidth(getWidth());
        this.box.setMaxWidth(getWidth());
        this.textField = new TextField();
        HBox.setHgrow(textField, Priority.ALWAYS);
        this.textField.setMaxHeight(Double.MAX_VALUE);
        if (this.converter != null) {
            textField.setText(this.converter.toString(getItem()));
        } else {
            textField.setText(getText());
        }
        textField.selectAll();
        NodeUtils.requestFocus(textField);
        textField.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitEdit(this.converter.fromString(textField.getText()));
                event.consume();
                NodeUtils.requestFocus(this);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                event.consume();
                NodeUtils.requestFocus(this);
            }
        });
        textField.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV && oldV && isEditing()) {
                cancelEdit();
            }
        });
    }

    private void updateBox() {
        setText(null);
        this.box.getChildren().clear();
        var graphic = getGraphic();
        setGraphic(null); //otherwise it is not possible to reuse graphic
        if (graphic != null) {
            this.box.getChildren().add(graphic);
        }
        this.box.getChildren().add(this.textField);
        setGraphic(this.box);
    }

    private void removeBox() {
        this.box = null;
        this.textField = null;
        updateSelected(false);
        updateItem(getItem(), isEmpty());
    }
}
