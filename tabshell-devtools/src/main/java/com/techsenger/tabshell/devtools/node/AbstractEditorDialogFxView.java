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

import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.core.window.WindowType;
import com.techsenger.tabshell.dialogs.alert.AlertDialogFxView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogParams;
import com.techsenger.tabshell.dialogs.alert.AlertDialogPort;
import com.techsenger.tabshell.dialogs.alert.AlertDialogPresenter;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.style.Spacing;
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
public abstract class AbstractEditorDialogFxView<P extends AbstractEditorDialogPresenter<?>>
        extends AbstractDialogFxView<P> implements EditorDialogView {

    public class Composer extends AbstractDialogFxView<P>.Composer implements EditorDialogView.Composer {

        @Override
        public AlertDialogPort openAlertDialog(AlertDialogParams params) {
            var dialog = createAlertDialog(params);
            if (params.getWindowType() == WindowType.NESTED) {
                getContainer().getComposer().addWindow(dialog);
            } else {
                dialog.getStage().initOwner(getStage());
                dialog.getStage().show();
            }
            return dialog.getPresenter();
        }

        protected AlertDialogFxView<?> createAlertDialog(AlertDialogParams params) {
            var view = new AlertDialogFxView<>();
            var presenter = new AlertDialogPresenter<>(view, params);
            presenter.initialize();
            return view;
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

    @Override
    public void setPropertyName(String name) {
        this.propertyNameLabel.setText(name);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractEditorDialogFxView.Composer();
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
        makeEqualWidth(cancelButton, okButton);
    }

    protected Label getPropertyNameLabel() {
        return propertyNameLabel;
    }

    protected HBox getRowBox() {
        return rowBox;
    }
}
