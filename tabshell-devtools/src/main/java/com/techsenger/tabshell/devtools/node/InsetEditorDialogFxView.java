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

package com.techsenger.tabshell.devtools.node;

import com.techsenger.tabshell.material.style.StyleClasses;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public class InsetEditorDialogFxView<P extends InsetEditorDialogPresenter<?>> extends AbstractEditorDialogFxView<P>
        implements InsetEditorDialogView {

    private final TextField topTextField = new TextField();

    private final TextField rightTextField = new TextField();

    private final TextField bottomTextField = new TextField();

    private final TextField leftTextField = new TextField();

    @Override
    public void requestFocus() {

    }

    @Override
    public void setTop(String value) {
        this.topTextField.setText(value);
    }

    @Override
    public void setRight(String value) {
        this.rightTextField.setText(value);
    }

    @Override
    public void setBottom(String value) {
        this.bottomTextField.setText(value);
    }

    @Override
    public void setLeft(String value) {
        this.leftTextField.setText(value);
    }

    @Override
    protected void build() {
        super.build();
        HBox.setHgrow(topTextField, Priority.ALWAYS);
        topTextField.getStyleClass().add(StyleClasses.DENSE);
        HBox.setHgrow(rightTextField, Priority.ALWAYS);
        rightTextField.getStyleClass().add(StyleClasses.DENSE);
        HBox.setHgrow(bottomTextField, Priority.ALWAYS);
        bottomTextField.getStyleClass().add(StyleClasses.DENSE);
        HBox.setHgrow(leftTextField, Priority.ALWAYS);
        leftTextField.getStyleClass().add(StyleClasses.DENSE);

        getRowBox().getChildren().addAll(topTextField, rightTextField, bottomTextField, leftTextField);
    }

    @Override
    protected void addListeners() {
        super.addListeners();

        topTextField.textProperty().addListener((ov, oldV, newV) -> getPresenter().onTopChanged(newV));
        rightTextField.textProperty().addListener((ov, oldV, newV) -> getPresenter().onRightChanged(newV));
        bottomTextField.textProperty().addListener((ov, oldV, newV) -> getPresenter().onBottomChanged(newV));
        leftTextField.textProperty().addListener((ov, oldV, newV) -> getPresenter().onLeftChanged(newV));
    }
}
