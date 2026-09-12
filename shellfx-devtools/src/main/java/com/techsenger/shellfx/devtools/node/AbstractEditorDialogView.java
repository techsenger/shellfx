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

package com.techsenger.shellfx.devtools.node;

import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.window.AbstractWindowView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import com.techsenger.shellfx.dialogs.alert.FullAlertDialogPort;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractEditorDialogView<VM extends AbstractEditorDialogViewModel<?>>
        extends AbstractDialogView<VM> {

    public class Composer extends AbstractWindowView<VM>.Composer implements EditorDialogComposer {

        @Override
        public FullAlertDialogPort openAlertDialog(AlertDialogParams params) {
            var dialog = createAlertDialog(params);
            if (params.getWindowType() == WindowType.NESTED) {
                getParent().getComposer().addDialog(dialog);
            } else {
                dialog.getStage().initOwner(getStage());
                dialog.getStage().show();
            }
            return dialog.getViewModel();
        }

        protected AlertDialogView<?> createAlertDialog(AlertDialogParams params) {
            var viewModel = new AlertDialogViewModel<>(params);
            var dialogView = new AlertDialogView<>(viewModel);
            dialogView.initialize();
            return dialogView;
        }
    }

    protected static TextField createTextField() {
        var tf = new TextField();
        HBox.setHgrow(tf, Priority.ALWAYS);
        return tf;
    }

    private final Label propertyNameLabel = new Label();

    private final HBox rowBox = new HBox(propertyNameLabel);

    private final ResultButton cancelButton = new ResultButton(EditorDialogButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(EditorDialogButtons.OK, "OK");

    public AbstractEditorDialogView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractEditorDialogView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        propertyNameLabel.setMinWidth(Label.USE_PREF_SIZE);

        rowBox.setSpacing(Spacing.getHorizontal());
        rowBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(rowBox, Priority.ALWAYS);
        getContentBox().getChildren().add(rowBox);

        registerButtons(cancelButton, okButton);
        getButtonWidthGroup().add(cancelButton, okButton);
    }

    @Override
    protected void bind() {
        super.bind();
        propertyNameLabel.textProperty().bind(getViewModel().propertyNameProperty());
    }

    protected Label getPropertyNameLabel() {
        return propertyNameLabel;
    }

    protected HBox getRowBox() {
        return rowBox;
    }
}
